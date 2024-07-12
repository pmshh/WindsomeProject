# ShopProject :necktie:
>사이트 : http://windsome.shop (test 계정: admin/admin)
><br>다양한 의류 상품을 판매하는 웹 쇼핑몰 사이트입니다. ([레퍼런스 사이트](https://lmis.co.kr))
>
<br>![메인 화면](https://github.com/pmshh/ShopProject/assets/98300570/bfff6eba-a9b3-4264-8d09-362d30679762)
<br>
<br>![123](https://github.com/user-attachments/assets/8ee20655-162e-498c-ad30-7caea66b124a)
## 1. 개발 기간 & 참여 인원
- 2023년 12월 14일 ~ 2024년 03월 27일
- 1인 프로젝트

## 2. 사용 기술
`Back-end`
- Java 17
- Spring Boot 2.7.6
- JPA(Spring Data JPA)
- Spring Security
- QueryDSL
- QAuth2
- Junit 5
- Gradle 8.6
- MySQL 8.0

<br>`Front-end`
- HTML/CSS
- JavaScript
- jQuery 3.7.1
- Bootstrap 5.3.2

<br>`Deploy`
- AWS EC2
- AWS RDS

## 3. 프로젝트 구조
<details>
<summary>프로젝트 구조 - 접기/펼치기</summary>
  
<br>[계층형 아키텍처 구조]<br>
```java
└─ src
   ├─ main
   │  ├─ generated
   │  ├─ java
   │  │  └─ com
   │  │     └─ windsome
   │  │        ├─ config
   │  │        │  ├─ AppConfig.java
   │  │        │  ├─ AuditorAwareImpl.java
   │  │        │  ├─ p6spy
   │  │        │  │  ├─ P6SpyConfig.java
   │  │        │  │  ├─ P6SpyEventListener.java
   │  │        │  │  └─ P6SpyFormatter.java
   │  │        │  ├─ QuerydslConfig.java
   │  │        │  ├─ security
   │  │        │  │  ├─ CurrentMember.java
   │  │        │  │  ├─ CustomAuthFailureHandler.java
   │  │        │  │  ├─ CustomUserDetailsService.java
   │  │        │  │  ├─ memberAccount.java
   │  │        │  │  ├─ oauth
   │  │        │  │  │  ├─ CustomOAuth2User.java
   │  │        │  │  │  ├─ OAuth2SuccessHandler.java
   │  │        │  │  │  └─ OAuth2UserDetailsService.java
   │  │        │  │  └─ SecurityConfig.java
   │  │        │  └─ WebMvcConfig.java
   │  │        ├─ constant
   │  │        │  ├─ OrderProductStatus.java
   │  │        │  ├─ OrderStatus.java
   │  │        │  ├─ PaymentStatus.java
   │  │        │  ├─ ProductSellStatus.java
   │  │        │  └─ Role.java
   │  │        ├─ controller
   │  │        │  ├─ admin
   │  │        │  │  ├─ AdminBoardController.java
   │  │        │  │  ├─ AdminDashboardController.java
   │  │        │  │  ├─ AdminMemberController.java
   │  │        │  │  ├─ AdminOrderController.java
   │  │        │  │  └─ AdminProductController.java
   │  │        │  ├─ advice
   │  │        │  │  └─ MemberControllerAdvice.java
   │  │        │  ├─ board
   │  │        │  │  ├─ BoardController.java
   │  │        │  │  └─ CommentController.java
   │  │        │  ├─ cart
   │  │        │  │  └─ CartController.java
   │  │        │  ├─ main
   │  │        │  │  └─ MainController.java
   │  │        │  ├─ member
   │  │        │  │  ├─ AddressController.java
   │  │        │  │  └─ MemberController.java
   │  │        │  ├─ order
   │  │        │  │  ├─ OrderController.java
   │  │        │  │  └─ PaymentController.java
   │  │        │  └─ product
   │  │        │     └─ ProductController.java
   │  │        ├─ dto
   │  │        │  ├─ admin
   │  │        │  │  ├─ CategorySalesDTO.java
   │  │        │  │  ├─ CategorySalesResult.java
   │  │        │  │  ├─ DashboardInfoDTO.java
   │  │        │  │  ├─ OrderManagementDTO.java
   │  │        │  │  └─ PageDTO.java
   │  │        │  ├─ board
   │  │        │  │  ├─ BoardDTO.java
   │  │        │  │  ├─ notice
   │  │        │  │  │  ├─ NoticeDtlDTO.java
   │  │        │  │  │  ├─ NoticeDtlDtoInterface.java
   │  │        │  │  │  ├─ NoticeDTO.java
   │  │        │  │  │  ├─ NoticeListDTO.java
   │  │        │  │  │  └─ NoticeUpdateDTO.java
   │  │        │  │  ├─ qa
   │  │        │  │  │  ├─ CommentDeleteDTO.java
   │  │        │  │  │  ├─ CommentDTO.java
   │  │        │  │  │  ├─ CommentEnrollDTO.java
   │  │        │  │  │  ├─ CommentUpdateDTO.java
   │  │        │  │  │  ├─ QaDtlDTO.java
   │  │        │  │  │  ├─ QaDtlDtoInterface.java
   │  │        │  │  │  ├─ QaEnrollDTO.java
   │  │        │  │  │  ├─ QaListDTO.java
   │  │        │  │  │  └─ QaUpdateDTO.java
   │  │        │  │  ├─ review
   │  │        │  │  │  ├─ ProductDTO.java
   │  │        │  │  │  ├─ ProductListDTO.java
   │  │        │  │  │  ├─ ProductReviewDTO.java
   │  │        │  │  │  ├─ ProductSearchDTO.java
   │  │        │  │  │  ├─ ReviewDtlPageReviewDTO.java
   │  │        │  │  │  ├─ ReviewEnrollDTO.java
   │  │        │  │  │  ├─ ReviewListDTO.java
   │  │        │  │  │  └─ ReviewUpdateDTO.java
   │  │        │  │  └─ SearchDTO.java
   │  │        │  ├─ cart
   │  │        │  │  ├─ CartDetailDTO.java
   │  │        │  │  ├─ CartOrderDTO.java
   │  │        │  │  ├─ CartProductDTO.java
   │  │        │  │  └─ CartProductListDTO.java
   │  │        │  ├─ category
   │  │        │  │  ├─ CategoryDTO.java
   │  │        │  │  └─ MainPageCategoryDTO.java
   │  │        │  ├─ member
   │  │        │  │  ├─ AdminMemberDetailDTO.java
   │  │        │  │  ├─ AdminMemberFormDTO.java
   │  │        │  │  ├─ MemberDetailDTO.java
   │  │        │  │  ├─ MemberFormDTO.java
   │  │        │  │  ├─ MemberListResponseDTO.java
   │  │        │  │  ├─ MemberListSearchDTO.java
   │  │        │  │  ├─ SignUpRequestDTO.java
   │  │        │  │  ├─ UpdatePasswordDTO.java
   │  │        │  │  └─ UserSummaryDTO.java
   │  │        │  ├─ order
   │  │        │  │  ├─ AdminPageOrderDTO.java
   │  │        │  │  ├─ AdminPageOrderProductDTO.java
   │  │        │  │  ├─ OrderDetailDTO.java
   │  │        │  │  ├─ OrderDetailProductDTO.java
   │  │        │  │  ├─ OrderHistProductResponseDTO.java
   │  │        │  │  ├─ OrderHistResponseDTO.java
   │  │        │  │  ├─ OrderPageProductResponseDTO.java
   │  │        │  │  ├─ OrderProductDTO.java
   │  │        │  │  ├─ OrderProductListDTO.java
   │  │        │  │  ├─ OrderProductRequestDTO.java
   │  │        │  │  ├─ OrderProductResponseDTO.java
   │  │        │  │  └─ OrderRequestDTO.java
   │  │        │  ├─ product
   │  │        │  │  ├─ MainPageProductDTO.java
   │  │        │  │  ├─ OptionDTO.java
   │  │        │  │  ├─ ProductFormDTO.java
   │  │        │  │  ├─ ProductImageDTO.java
   │  │        │  │  ├─ ProductInfoResponseDTO.java
   │  │        │  │  ├─ ProductOptionColorDTO.java
   │  │        │  │  ├─ ProductOptionDTO.java
   │  │        │  │  └─ ProductSearchDTO.java
   │  │        │  └─ validator
   │  │        │     ├─ ProfileFormDtoValidator.java
   │  │        │     └─ SignUpDtoValidator.java
   │  │        ├─ entity
   │  │        │  ├─ auditing
   │  │        │  │  ├─ BaseEntity.java
   │  │        │  │  └─ BaseTimeEntity.java
   │  │        │  ├─ board
   │  │        │  │  ├─ Board.java
   │  │        │  │  └─ Comment.java
   │  │        │  ├─ cart
   │  │        │  │  ├─ Cart.java
   │  │        │  │  └─ CartProduct.java
   │  │        │  ├─ member
   │  │        │  │  ├─ Address.java
   │  │        │  │  └─ Member.java
   │  │        │  ├─ order
   │  │        │  │  ├─ Order.java
   │  │        │  │  ├─ OrderProduct.java
   │  │        │  │  └─ Payment.java
   │  │        │  ├─ PersistentLogins.java
   │  │        │  └─ product
   │  │        │     ├─ Category.java
   │  │        │     ├─ Product.java
   │  │        │     ├─ ProductImage.java
   │  │        │     └─ ProductOption.java
   │  │        ├─ exception
   │  │        │  ├─ AdminDeletionException.java
   │  │        │  └─ ProductImageDeletionException.java
   │  │        ├─ repository
   │  │        │  ├─ board
   │  │        │  │  ├─ BoardRepository.java
   │  │        │  │  ├─ BoardRepositoryCustom.java
   │  │        │  │  ├─ BoardRepositoryCustomImpl.java
   │  │        │  │  └─ CommentRepository.java
   │  │        │  ├─ cart
   │  │        │  │  └─ CartRepository.java
   │  │        │  ├─ cartProduct
   │  │        │  │  └─ CartProductRepository.java
   │  │        │  ├─ category
   │  │        │  │  └─ CategoryRepository.java
   │  │        │  ├─ member
   │  │        │  │  ├─ AddressRepository.java
   │  │        │  │  ├─ MemberRepository.java
   │  │        │  │  ├─ MemberRepositoryCustom.java
   │  │        │  │  └─ MemberRepositoryCustomImpl.java
   │  │        │  ├─ order
   │  │        │  │  └─ OrderRepository.java
   │  │        │  ├─ orderProduct
   │  │        │  │  └─ OrderProductRepository.java
   │  │        │  ├─ payment
   │  │        │  │  └─ PaymentRepository.java
   │  │        │  ├─ product
   │  │        │  │  ├─ ProductOptionRepository.java
   │  │        │  │  ├─ ProductRepository.java
   │  │        │  │  ├─ ProductRepositoryCustom.java
   │  │        │  │  └─ ProductRepositoryCustomImpl.java
   │  │        │  └─ productImage
   │  │        │     └─ ProductImageRepository.java
   │  │        ├─ service
   │  │        │  ├─ admin
   │  │        │  │  └─ AdminService.java
   │  │        │  ├─ board
   │  │        │  │  ├─ BoardService.java
   │  │        │  │  └─ CommentService.java
   │  │        │  ├─ cart
   │  │        │  │  ├─ CartProductService.java
   │  │        │  │  └─ CartService.java
   │  │        │  ├─ file
   │  │        │  │  └─ FileService.java
   │  │        │  ├─ mail
   │  │        │  │  ├─ ConsoleEmailService.java
   │  │        │  │  ├─ EmailMessageDto.java
   │  │        │  │  ├─ EmailService.java
   │  │        │  │  └─ HtmlMailSender.java
   │  │        │  ├─ main
   │  │        │  │  └─ MainService.java
   │  │        │  ├─ member
   │  │        │  │  ├─ AddressService.java
   │  │        │  │  └─ MemberService.java
   │  │        │  ├─ order
   │  │        │  │  ├─ OrderProductService.java
   │  │        │  │  ├─ OrderService.java
   │  │        │  │  └─ PaymentService.java
   │  │        │  └─ product
   │  │        │     ├─ CategoryService.java
   │  │        │     ├─ ProductImageService.java
   │  │        │     ├─ ProductOptionService.java
   │  │        │     └─ ProductService.java
   │  │        └─ ShopApplication.java
```
</details>

## 4. API 설계
<details>
<summary>메인 화면 API - 접기/펼치기</summary>
  
![MainController](https://github.com/pmshh/ShopProject/assets/98300570/24316c95-d450-45b6-9886-0966eb3155e9)
</details>

<details>
<summary>회원 관련 API - 접기/펼치기</summary>
  
![MemberController](https://github.com/pmshh/ShopProject/assets/98300570/39930324-99cf-4cdc-9af1-9134f4fbe74b)
![AddressController](https://github.com/pmshh/ShopProject/assets/98300570/2e9e86d5-4f0b-40bb-89cc-4f60cd3fe781)
</details>

<details>
<summary>상품 관련 API - 접기/펼치기</summary>
  
![ProductController](https://github.com/pmshh/ShopProject/assets/98300570/8226ab89-8b8e-4c77-aa7d-ea639fadff16)
</details>

<details>
<summary>장바구니 관련 API - 접기/펼치기</summary>
  
![CartController](https://github.com/pmshh/ShopProject/assets/98300570/6b8b54d2-8d30-4e04-a653-3c25cfcbe398)
</details>

<details>
<summary>주문 관련 API - 접기/펼치기</summary>
  
![OrderController](https://github.com/pmshh/ShopProject/assets/98300570/f4e97bc0-6995-4c0f-a457-22f6daf9161f)
![PaymentController](https://github.com/pmshh/ShopProject/assets/98300570/f7c2e4ec-b7d7-45d7-9880-4d51cd45a5e4)
</details>

<details>
<summary>게시판 관련 API - 접기/펼치기</summary>
  
![BoardController](https://github.com/pmshh/ShopProject/assets/98300570/b0617c24-27b0-4077-bfc7-81ac62d1de91)
![CommentController](https://github.com/pmshh/ShopProject/assets/98300570/1194619b-4bed-4d7b-bd99-7b422d0d358e)
</details>

<details>
<summary>어드민 페이지 관련 API - 접기/펼치기</summary>

![AdminBoardController](https://github.com/pmshh/ShopProject/assets/98300570/c17eae1a-c4e5-4bfb-b4ea-764c03baae12)
![AdminDashboardControler](https://github.com/pmshh/ShopProject/assets/98300570/b505104f-a979-452c-8565-3625f7b2f8c0)
![AdminMemberController](https://github.com/pmshh/ShopProject/assets/98300570/b88514fd-8afa-4052-b12a-66536f2e524f)
![AdminOrderController](https://github.com/pmshh/ShopProject/assets/98300570/9abc2fa8-402b-456e-8ee7-2bb4f4da79a4)
![AdminProductController](https://github.com/pmshh/ShopProject/assets/98300570/dcfe5433-e4da-4a6d-ae82-6df92c2d0a76)
</details>

## 5. ERD
![ShopProjectERD (1)](https://github.com/pmshh/ShopProject/assets/98300570/3a3ab2ad-32a4-44df-a331-012e431c544c)

## 6. 주요 기능
`사용자 인증 및 권한 관리`
- 스프링 시큐리티를 통해 로그인, 회원가입, 아이디/비밀번호 찾기와 같은 사용자 인증 기능을 구현하였습니다.
- Form Login과 OAuth2 login 방식을 지원하여 다양한 인증 방식을 제공합니다.
  - Form Login
    - CustomAuthFailureHandler를 구현하여 인증 실패 시 사용자 정의 에러 처리 로직을 추가하였습니다.
    - CustomUserDetailsService를 구현하여 사용자 정보를 데이터베이스에서 동적으로 가져오는 기능을 구현하였습니다.
  - OAuth2 Login
    - OAuth2UserDetailsService를 구현하여 OAuth2로 인증된 사용자의 정보를 가져오는 기능을 구현하였습니다.
- 사용자의 권한에 따라 페이지 접근을 제어하는 등의 권한 관리 기능을 구현하였습니다.

<br>

`페이징`
- QueryDSL을 이용하여 페이징 기능과 복잡한 검색 조건을 가진 검색 기능을 구현하였습니다.

<br>

`상품 주문`
- 사용자가 상품을 주문할 때 상품의 색상과 사이즈를 선택할 수 있도록 상품 옵션 기능을 구현하였습니다.
- 주문서 작성 시 사용자는 자신이 저장한 배송지 목록 중 원하는 배송지를 선택하여 상품을 주문할 수 있습니다.
- 결제 시스템:
  - Iamport API를 이용하여 카카오 페이, 네이버 페이, 신용 카드 결제 등 다양한 결제 수단을 제공합니다.
  - 자바스크립트를 활용했기 때문에 결제금액, 상태에 대해 변조가 이루어지기 쉽기 때문에 결제 검증 로직을 추가했습니다. (실제 결제 금액과 주문 금액이 서로 일치하는지를 확인)
- 주문한 상품과 관련된 상세 내역을 조회할 수 있는 주문 조회 기능을 구현했습니다.

<br>

`장바구니`
- 장바구니 화면에서 주문하고자 하는 상품만 선택하여 주문할 수 있습니다.
- 장바구니에 담긴 상품들을 수정하거나 삭제할 수 있습니다.
- 현재 로그인한 사용자의 장바구니에 담긴 상품 개수를 모든 뷰에서 확인할 수 있도록, @ControllerAdvice를 활용하여 모델에 추가하였습니다.

<br>

`게시판`
- 공지사항 게시판:
  - 특정 게시글을 강조하기 위해 상단 고정 기능을 제공합니다. 
- Q&A 게시판:
  - 비밀글: Q&A 게시판의 모든 게시글은 비밀글 작성이 원칙이며, 게시글 작성 시 입력한 비밀번호를 통해서 게시글을 관리할 수 있습니다.
  - 댓글: 댓글 관련 CRUD 기능을 구현하였습니다. 댓글 작성 시 비밀글로 설정할 수 있으며, 비밀글로 설정한 경우 작성자 본인 혹은 관리자만이 해당 댓글을 확인할 수 있습니다.
  - 계층형 답글: 계층형 답글 기능을 추가하여 사용자들이 게시글에 대한 답변을 계층적으로 작성하고 응답할 수 있도록 했습니다.
- 리뷰 게시판:
  - 상품 리뷰 작성 시 상품에 대한 평점을 부가할 수 있으며, 해당 평점은 상품의 평균 평점 필드에 반영됩니다. (평균 평점 값은 메인 페이지의 상품 정렬에 사용됨)

<br>

`관리자 페이지`
- 관리자 페이지는 SpringSecurity 설정을 통해 관리자 권한을 가진 사용자만이 접근할 수 있도록 페이지 접근을 제어하였습니다.
- 대시보드: 관리자가 시스템 전반의 요약 정보를 한눈에 확인할 수 있는 기능을 제공합니다.
- 회원 관리: 회원 관련 CRUD 기능을 구현하였습니다. 삭제 기능은 실제 데이터를 삭제하는 대신 is_delete 컬럼의 값을 true로 설정하여 회원을 비활성화합니다.
- 상품 관리: 상품 관련 CRUD 기능을 구현하였습니다.
- 주문 관리: 주문된 상품들의 목록을 조회하고, 주문 상태를 변경하거나 주문 내역을 상세하게 조회할 수 있는 기능을 제공합니다. 관리자는 주문 상태를 업데이트하고, 주문 취소 및 환불 등의 처리를 할 수 있습니다.
- 게시판 관리: 관리자는 등록된 게시글을 조회하고 필요에 따라 삭제할 수 있는 기능을 제공합니다.

## 7. 실행 화면
<details>
<summary>동적 정렬, 페이징, 검색 기능 - 접기/펼치기</summary>

<br>`동적 정렬, 페이징`<br><br>
![메인 페이지 동적 정렬 및 페이징](https://github.com/pmshh/ShopProject/assets/98300570/763d7416-87d8-493e-9926-6dcb4ccb562f)
<br><br>`상품 검색`<br><br>
![메인 페이지 동적 쿼리 검색 기능](https://github.com/pmshh/ShopProject/assets/98300570/2061cc96-1397-4946-a098-0f2c7fb3cdc6)<br><br>

</details>

<details>
<summary>회원가입 - 접기/펼치기</summary>

<br>`유효성 검사`<br><br>
![유효성 검사](https://github.com/pmshh/ShopProject/assets/98300570/a2879029-7d26-45d7-be6d-691855023afc)
<br><br>`이메일 인증(구글 SMTP 서버 사용)`<br><br>
![구글 SMTP 서버](https://github.com/pmshh/ShopProject/assets/98300570/ff295216-f88a-4910-accc-1e80501af8a4)
<br><br>`주소 검색(다음 주소 API 사용)`<br><br>
![다음 주소 API](https://github.com/pmshh/ShopProject/assets/98300570/2cd53402-4a2f-475c-b0b5-679cd2059428)

</details>

<details>
<summary>로그인 - 접기/펼치기</summary>

<br>`Form Login`<br><br>
![로그인(form login)](https://github.com/pmshh/ShopProject/assets/98300570/d08700b1-3e2c-4438-8aa2-8eff5bc59ffa)
<br><br>`OAuth2 Login`<br><br>
![로그인(OAuth2)](https://github.com/pmshh/ShopProject/assets/98300570/9c89c9d1-b771-4563-810b-17ae80a31eed)<br><br>

</details>

<details>
<summary>아이디/비밀번호 찾기 - 접기/펼치기</summary>

<br>`아이디 찾기`<br><br>
![아이디 찾기](https://github.com/pmshh/ShopProject/assets/98300570/d03eab25-bdb9-47ca-b4da-e94c2cf33d8c)
<br><br>`비밀번호 찾기`<br><br>
![비밀번호 찾기](https://github.com/pmshh/ShopProject/assets/98300570/049690e7-82f2-43c6-88b2-f7398f0eff02)<br><br>

</details>

<details>
<summary>장바구니 - 접기/펼치기</summary>

<br>![장바구니](https://github.com/pmshh/ShopProject/assets/98300570/b32cb24f-671f-4039-8da2-b593e6e5638a)<br><br>

</details>

<details>
<summary>상품 주문 - 접기/펼치기</summary>

<br>`배송지 관리`<br><br>
![주문 과정 1](https://github.com/pmshh/ShopProject/assets/98300570/39363ba1-52ee-4788-801f-be8f4380def4)
<br><br>`포인트 사용`<br><br>
![주문 과정 2](https://github.com/pmshh/ShopProject/assets/98300570/17dca627-da69-4d88-84c0-40764ddca21c)
<br><br>`주문 결제`<br><br>
![주문 과정 3](https://github.com/pmshh/ShopProject/assets/98300570/84731d75-cd53-4e5e-b277-c503da41727b)
<br><br>`주문 조회`<br><br>
![주문 조회](https://github.com/pmshh/ShopProject/assets/98300570/75200ec0-75fa-4373-8180-32e9ebb98284)<br><br>

</details>

<details>
<summary>게시판 - 접기/펼치기</summary>

<br>`이전글/다음글 및 검색 기능`<br><br>
![공지사항 게시판](https://github.com/pmshh/ShopProject/assets/98300570/dfad2cf0-530e-43f0-ba03-5f24b5b31265)
<br><br>`댓글 기능`<br><br>
![Q A 게시판 댓글](https://github.com/pmshh/ShopProject/assets/98300570/23bbab9d-59f1-427d-8701-05b72e545b08)
<br><br>`계층형 답글 기능`<br><br>
![Q A 게시판 답글](https://github.com/pmshh/ShopProject/assets/98300570/a746e1db-164e-4e0b-bbda-793958c963ab)
<br><br>`리뷰 게시판`<br><br>
![리뷰 게시판](https://github.com/pmshh/ShopProject/assets/98300570/fb21f1f3-d502-4f17-91ad-fa51190c3382)

</details>

<details>
<summary>관리자 페이지 - 접기/펼치기</summary>

<br>![관리자 페이지](https://github.com/pmshh/ShopProject/assets/98300570/80807b4b-ca67-4376-a4f6-ac25716df7cb)

</details>

## 8. 프로젝트 회고
프로젝트 회고를 블로그에 정리해두었습니다.
<br> https://velog.io/@pms000723/ShopProject-ShopProject-%ED%9A%8C%EA%B3%A0
