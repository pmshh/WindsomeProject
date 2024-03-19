package com.windsome.service.product;

import com.windsome.dto.product.*;
import com.windsome.entity.*;
import com.windsome.entity.product.*;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.repository.category.CategoryRepository;
import com.windsome.repository.product.*;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.service.FileService;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final InventoryRepository inventoryRepository;

    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final ProductImageService productImageService;
    private final ProductSizeService productSizeService;
    private final ProductColorService productColorService;

    @Value("${productImgLocation}")
    private String productImgLocation;

    /**
     * 상품 등록
     */
    public Long createProduct(ProductFormDTO productFormDto, List<MultipartFile> productImageFileList) throws Exception {
        // 상품 생성 및 저장
        Category category = categoryRepository.findById(productFormDto.getCategoryId()).orElseThrow();
        Product product = modelMapper.map(productFormDto, Product.class);
        product.setCategory(category);
        product.setAverageRating(BigDecimal.valueOf(0));
        productRepository.save(product);

        // 상품 색상, 사이즈, 재고 저장
        saveProductOptions(productFormDto, product);

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
        // 상품 수정
        Category category = categoryRepository.findById(productFormDto.getCategoryId()).orElseThrow(EntityNotFoundException::new);
        Product product = productRepository.findById(productFormDto.getId()).orElseThrow(EntityNotFoundException::new);
        product.setCategory(category);
        product.updateProduct(productFormDto);

        // 기존 상품 색상, 사이즈, 재고 삭제
        productColorRepository.deleteAllByProductId(product.getId());
        productSizeRepository.deleteAllByProductId(product.getId());
        inventoryRepository.deleteAllByProductId(product.getId());

        // 상품 색상, 사이즈, 재고 저장
        saveProductOptions(productFormDto, product);

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
        List<ProductImage> productImages = productImageRepository.findByProductIdOrderByIdAsc(productId);

        List<ProductImageDTO> productImageDTOList = new ArrayList<>();
        for (ProductImage productImage : productImages) {
            ProductImageDTO productImageDto = ProductImageDTO.toDto(productImage);
            productImageDTOList.add(productImageDto);
        }

        Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        ProductFormDTO productFormDto = ProductFormDTO.toDto(product);
        productFormDto.setCategoryId(product.getCategory().getId());
        productFormDto.setProductImageDTOList(productImageDTOList);
        return productFormDto;
    }

    /**
     * 메인 화면 상품 리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<MainPageProductDTO> getMainPageProducts(ProductSearchDTO productSearchDto, Pageable pageable) {
        Page<MainPageProductDTO> mainPageProducts = productRepository.getMainPageProducts(productSearchDto, pageable);
        for (MainPageProductDTO mainPageProductDTO : mainPageProducts) {
            Long productId = mainPageProductDTO.getId();
            List<ProductColor> productColors = productColorRepository.findAllByProductId(productId);
            List<ProductColorCodeDTO> productColorCodeDTOList = new ArrayList<>();
            for (ProductColor color : productColors) {
                ProductColorCodeDTO productColorCodeDTO = new ProductColorCodeDTO();
                productColorCodeDTO.setColorCode(color.getColor().getCode());
                productColorCodeDTOList.add(productColorCodeDTO);
            }
            mainPageProductDTO.setColorCodeDTOList(productColorCodeDTOList);
        }
        return mainPageProducts;
    }

    /**
     * 상품 삭제
     */
    public void deleteProduct(Long[] productIds) throws Exception {
        // 상품 이미지, 상품 조회
        for (Long productId : productIds) {
            List<ProductImage> productImageList = productImageRepository.findByProductId(productId).orElseThrow(EntityNotFoundException::new);
            Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

            // 상품 이미지, 상품 삭제
            productRepository.delete(product);

            // 서버 이미지 파일 삭제
            for (ProductImage productImage : productImageList) {
                // 기존 이미지 삭제
                if (!StringUtils.isEmpty(productImage.getServerImageName())) {
                    fileService.deleteFile(productImgLocation + "/" + productImage.getServerImageName());
                }
            }
        }
    }

    /**
     * 상품 이미지 삭제
     */
    public void deleteProductImage(Long productImageId) {
        ProductImage productImage = productImageRepository.findById(productImageId).orElseThrow(EntityNotFoundException::new);
        if (!productImage.isRepresentativeImage()) {
            productImage.setServerImageName("");
            productImage.setImageUrl("");
            productImage.setOriginalImageName("");
            productImageRepository.save(productImage);
        } else {
            throw new ProductImageDeletionException("첫 번째 상품 이미지는 삭제할 수 없습니다.");
        }
    }

    /**
     * 상품 사이즈 조회
     */
    public List<ProductSizeResponseDTO> getProductSizesByProductId(Long productId) {
        return productSizeService.getProductSizesByProductId(productId);
    }

    /**
     * 상품 색상 조회
     */
    public List<ProductColorResponseDTO> getProductColorsByProductId(Long productId) {
        return productColorService.getProductColorsByProductId(productId);
    }

    private void saveProductOptions(ProductFormDTO productFormDto, Product product) {
        for (OptionDTO optionDTO : productFormDto.getOptionDTOList()) {
            Color color = colorRepository.findById(optionDTO.getColorId()).orElseThrow(EntityNotFoundException::new);
            Size size = sizeRepository.findById(optionDTO.getSizeId()).orElseThrow(EntityNotFoundException::new);

            if (!productColorRepository.existsByProductIdAndColorId(product.getId(), optionDTO.getColorId())) {
                ProductColor productColor = new ProductColor();
                productColor.setProduct(product);
                productColor.setColor(color);
                productColorRepository.save(productColor);
            }

            if (!productSizeRepository.existsByProductIdAndSizeId(product.getId(), optionDTO.getSizeId())) {
                ProductSize productSize = new ProductSize();
                productSize.setProduct(product);
                productSize.setSize(size);
                productSizeRepository.save(productSize);
            }

            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setSize(size);
            inventory.setColor(color);
            inventory.setQuantity(optionDTO.getQuantity());
            inventoryRepository.save(inventory);
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
}
