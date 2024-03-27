package com.windsome.service.product;

import com.windsome.dto.board.review.ProductListDTO;
import com.windsome.dto.board.review.ProductReviewDTO;
import com.windsome.dto.product.*;
import com.windsome.entity.product.*;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.repository.product.*;
import com.windsome.service.file.FileService;
import com.windsome.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    @Value("${productImgLocation}")
    private String productImgLocation;

    private final ProductRepository productRepository;

    private final ProductImageService productImageService;
    private final CategoryService categoryService;
    private final ProductOptionService productOptionService;
    private final FileService fileService;
    private final ModelMapper modelMapper;

    /**
     * 상품 등록
     */
    public Long createProduct(ProductFormDTO productFormDto, List<MultipartFile> productImageFileList) throws Exception {
        // 상품 생성 및 저장
        Category category = categoryService.getCategoryById(productFormDto.getCategoryId());
        Product product = modelMapper.map(productFormDto, Product.class);
        product.setCategory(category);
        product.setAverageRating(BigDecimal.valueOf(0));

        // 상품에 저장할 상품 옵션 List 생성
        List<ProductOption> productOptionList = createProductOptionList(productFormDto, product);

        // 상품 옵션을 상품에 저장
        product.setProductOptions(productOptionList);
        productRepository.save(product);

        // 상품 이미지 생성 및 저장
        for (int i = 0; i < productImageFileList.size(); i++) {
            ProductImage productImage = new ProductImage(product, i == 0);
            productImageService.saveProductImage(productImage, productImageFileList.get(i));
        }

        return product.getId();
    }

    /**
     * 상품 수정
     */
    public Long updateProduct(ProductFormDTO productFormDto, List<MultipartFile> productImageFileList) throws Exception {
        // 상품, 카테고리 조회
        Category category = categoryService.getCategoryById(productFormDto.getCategoryId());
        Product product = productRepository.findById(productFormDto.getId()).orElseThrow(EntityNotFoundException::new);

        // 상품에 저장할 상품 옵션 List 생성
        List<ProductOption> productOptionList = createProductOptionList(productFormDto, product);

        // 상품 수정
        product.updateProduct(productFormDto, category, productOptionList);
        productRepository.save(product);

        // 상품 이미지 수정
        List<Long> productImageIds = productFormDto.getProductImageIds();
        for (int i = 0; i < productImageFileList.size(); i++) {
            productImageService.updateProductImage(productImageIds.get(i), productImageFileList.get(i));
        }

        return product.getId();
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductFormDTO getProductFormDto(Long productId) {
        // 상품, 상품 옵션, 상품 이미지 조회
        Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        List<ProductOption> productOptionList = productOptionService.getProductOptionsByProductId(productId);
        List<ProductImage> productImages = productImageService.getProductImagesByProductId(productId);

        // 상품 이미지 담긴 List 생성
        List<ProductImageDTO> productImageDTOList = new ArrayList<>();
        for (ProductImage productImage : productImages) {
            ProductImageDTO productImageDto = ProductImageDTO.toDto(productImage);
            productImageDTOList.add(productImageDto);
        }

        // Entity -> DTO 변환
        ProductFormDTO productFormDto = ProductFormDTO.toDto(product);
        productFormDto.setCategoryId(product.getCategory().getId());
        productFormDto.setProductImageDTOList(productImageDTOList);
        List<OptionDTO> optionDTOList = productOptionList.stream()
                .map(option -> new OptionDTO(option.getColor(), option.getSize(), option.getQuantity()))
                .collect(Collectors.toList());
        productFormDto.setOptionDTOList(optionDTOList);

        return productFormDto;
    }

    /**
     * 메인 화면 상품 리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<MainPageProductDTO> getMainPageProducts(ProductSearchDTO productSearchDto, Pageable pageable) {
        Page<MainPageProductDTO> mainPageProducts = productRepository.getMainPageProducts(productSearchDto, pageable);
        mainPageProducts.forEach(mainPageProductDTO -> {
            List<ProductOption> productOptions = productOptionService.getProductOptionsByProductId(mainPageProductDTO.getId());
            List<ProductOptionColorDTO> productOptionColorDTOList = productOptions.stream()
                    .map(productOption -> new ProductOptionColorDTO(productOption.getColor()))
                    .collect(Collectors.toList());
            mainPageProductDTO.setProductOptionColors(productOptionColorDTOList);
        });
        return mainPageProducts;
    }

    /**
     * 상품 삭제
     */
    public void deleteProducts(Long[] productIds) throws Exception {
        // 상품 이미지, 상품 조회
        for (Long productId : productIds) {
            // 상품, 상품 이미지 조회
            Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
            List<ProductImage> productImageList = productImageService.getProductImagesByProductId(productId);

            // 서버 이미지 파일 삭제
            for (ProductImage productImage : productImageList) {
                // 기존 이미지 삭제
                if (!StringUtils.isEmpty(productImage.getServerImageName())) {
                    fileService.deleteFile(productImgLocation + "/" + productImage.getServerImageName());
                }
            }

            // 상품, 상품 이미지 삭제
            productRepository.delete(product);
        }
    }

    /**
     * 상품 이미지 삭제
     */
    public void deleteProductImage(Long productImageId) {
        ProductImage productImage = productImageService.getProductImageByProductImageId(productImageId);
        if (!productImage.isRepresentativeImage()) {
            productImage.setServerImageName("");
            productImage.setImageUrl("");
            productImage.setOriginalImageName("");
            productImageService.save(productImage);
        } else {
            throw new ProductImageDeletionException("첫 번째 상품 이미지는 삭제할 수 없습니다.");
        }
    }

    /**
     * OrderService - 상품 정보 조회
     * @return 상품 정보를 담은 ProductInfoResponseDTO 객체
     */
    public ProductInfoResponseDTO getProductInfoByProductId(Long productId) {
        return productRepository.getProductInfoByProductId(productId);
    }

    /**
     * OrderService - 상품 정보 조회
     * @return 상품 정보를 담은 Product 객체
     */
    public Product getProductByProductId(Long productId) {
        return productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 리뷰 등록 화면 - 상품 검색(상품 리스트 조회)
     */
    public List<ProductListDTO> getReviewPageItemList(String searchQuery, Pageable pageable) {
        return productRepository.getReviewPageItemList(searchQuery, pageable);
    }

    /**
     * 리뷰 등록 화면 - 상품 검색(상품 리스트 조회) 카운트 쿼리
     */
    public Long getReviewPageItemListCount(String searchQuery) {
        return productRepository.getReviewPageItemListCount(searchQuery);
    }

    /**
     * 총 상품 개수 조회
     */
    public long getTotalProducts() {
        return productRepository.count();
    }

    /**
     * 관리자 페이지 - 상품 전체 조회
     */
    public Page<Product> getProducts(ProductSearchDTO productSearchDto, Pageable pageable) {
        return productRepository.findProducts(productSearchDto, pageable);
    }

    private List<ProductOption> createProductOptionList(ProductFormDTO productFormDto, Product product) {
        List<ProductOption> productOptionList = new ArrayList<>();
        for (OptionDTO optionDTO : productFormDto.getOptionDTOList()) {
            ProductOption productOption = ProductOption.builder()
                    .product(product)
                    .color(optionDTO.getColor())
                    .size(optionDTO.getSize())
                    .quantity(optionDTO.getQuantity())
                    .build();
            productOptionList.add(productOption);
        }
        return productOptionList;
    }
}
