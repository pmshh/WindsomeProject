## :necktie: WindsomeProject 
윈섬은 다양한 의류 상품을 판매하는 반응형 웹 쇼핑몰 사이트입니다.<br>
사용자는 편리하게 의류를 탐색하고 구매할 수 있으며, 다양한 결제 옵션과 사용자 친화적인 인터페이스를 제공합니다.<br>

>([윈섬 사이트 바로 가기](http://windsome.shop)) - test 계정: admin/admin)<br>

![반응형](https://github.com/user-attachments/assets/812857f6-70fa-4c6a-988e-8786092a382e)

<br>

## 목차
1. [프로젝트 개요](#1-프로젝트-개요)
2. [MVC 패턴 적용](#2-mvc-패턴-적용)
3. [데이터베이스 설계 및 RDS 사용](#3-데이터베이스-설계-및-rds-사용)
4. [API 설계](#4-api-설계)
5. [주요 로직 및 코드 설명](#5-주요-로직-및-코드-설명)
6. [트러블 슈팅](#6-트러블-슈팅)
7. [기술적인 고민과 해결과정](#7-기술적인-고민과-해결과정)
8. [향후 계획 또는 개선 사항](#8-향후-계획-또는-개선-사항)

<br>

## 1. 프로젝트 개요
- **개발 기간**: 2023.12. ~ 2024.04. (14주)
- **인원**: 1명 (개인 프로젝트)
- **기술 스택**:
  - **Back-end**: Java, Spring Boot 2.7.6, JPA, Spring Security(OAuth2), QueryDSL
  - **Front-end**: HTML/CSS, JavaScript, jQuery, Bootstrap
  - **DB**: MySQL (AWS RDS)
  - **Deploy**: AWS EC2
- **주요 기능**:
  - 사용자 인증 및 관리 (일반 로그인, 소셜 로그인, 회원가입, 아이디/비밀번호 찾기)
  - 상품 목록 조회 및 필터링 (페이징, 검색, 정렬)
  - 상품 주문 (장바구니, 주문서 작성, 주문 조회)
  - 게시판 기능 (공지사항, Q&A, 리뷰 게시판, 댓글, 답글)
  - 관리자 페이지 (대시보드, 회원 관리, 상품 관리, 주문 관리, 게시판 관리)

<br>

## 2. MVC 패턴 적용
프로젝트는 MVC(Model-View-Controller) 아키텍처를 따라 설계되었습니다.<br>
이러한 아키텍처를 선택한 이유는 각각의 역할을 분리하여 개발을 효율적으로 진행하기 위함입니다.
- **Model**: 애플리케이션의 상태와 비즈니스 로직을 담당하며, 데이터의 관리와 조작을 담당합니다.<br>
- **View**: 사용자에게 보여지는 화면을 담당합니다.<br>
- **Controller**: 사용자의 입력을 처리하고 적절한 Model과 View를 연결하는 역할을 합니다.

<br>이렇게 각 구성 요소를 분리함으로써 코드의 재사용성과 유지보수성이 향상되었으며, 변경이 필요한 부분이 발생했을 때 한 부분만 수정하여 전체 시스템에 영향을 최소화할 수 있었습니다.<br>
이를 통해 프로젝트의 개발과 유지보수가 훨씬 효율적으로 이루어질 수 있었습니다.<br><br>

![그림1](https://github.com/user-attachments/assets/ae7187f6-0c07-461c-8230-5c5d15e35b8f)

</details>

<br>

## 3. 데이터베이스 설계 및 RDS 사용
프로젝트의 데이터베이스로는 MySQL을 선택했습니다. MySQL은 오픈 소스이며 안정성과 성능 면에서 우수한 평판을 가지고 있습니다. 특히 프로젝트의 규모와 요구 사항에 적합하며, 쉬운 배포와 관리를 위해 선택되었습니다. 또한 MySQL은 널리 사용되는 RDBMS 중 하나인 만큼, 다양한 문서와 커뮤니티 지원을 받을 수 있어서 문제 발생 시 도움을 받기가 쉽습니다.

### RDS 사용
안정적이고 확장 가능한 데이터베이스 환경을 쉽게 구축하고 관리하기위해 데이터베이스는 AWS RDS를 사용하여 배포했습니다.

### 데이터베이스 설계 과정
데이터베이스 설계 과정은 다음과 같은 단계를 포함했습니다.

1. **요구 사항 분석**: 프로젝트의 요구 사항을 분석하고 데이터 모델링에 반영했습니다. 각 기능과 모듈에 필요한 데이터를 식별하고 관련 테이블을 정의했습니다.
2. **개체-관계 다이어그램(ERD) 작성**: 분석한 요구 사항을 기반으로 개체-관계 다이어그램을 작성했습니다. 이를 통해 데이터베이스의 구조와 테이블 간의 관계를 명확하게 이해할 수 있었습니다.
3. **정규화**: 데이터베이스 설계에서는 정규화를 적용하여 데이터 중복을 최소화했습니다. 각 테이블을 적절하게 정규화하여 데이터의 일관성과 무결성을 유지했습니다.
4. **테이블 설계**: ERD를 기반으로 각 테이블의 속성과 관계를 정의하고 테이블을 설계했습니다. 테이블 간의 관계를 명확히 정의하여 데이터베이스의 구조를 최적화했습니다.

![Windsome Project ERD](https://github.com/user-attachments/assets/85b44602-a436-482d-b3f6-5958f38b812a)
[ERD 바로가기](https://www.erdcloud.com/d/wAXsKCv4LW35Cnvs6)

이렇게 데이터베이스를 설계하고 AWS RDS를 사용함으로써, 프로젝트의 데이터 관리와 배포를 효율적으로 수행할 수 있었습니다.

<br>

## 4. API 설계

프로젝트의 주요 API는 다음과 같이 설계되었습니다:

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

<br>

## 5. 주요 로직 및 코드 설명

### 1) 로그인/회원가입 기능
로그인 및 회원가입 기능은 Spring Security를 기반으로 구현되었습니다.

- **로그인**: 
  - **Form login 방식**: Spring Security의 Form login 방식을 사용하여 사용자가 아이디와 비밀번호를 입력하여 로그인할 수 있도록 구현했습니다. 이를 통해 인증된 사용자만 애플리케이션에 접근할 수 있습니다.
  - **CustomAuthenticationFailureHandler**: 로그인 실패 시 사용자에게 적절한 오류 메시지를 제공하기 위해 CustomAuthenticationFailureHandler를 구현했습니다. 예를 들어, 잘못된 비밀번호나 존재하지 않는 아이디로 로그인을 시도할 경우 사용자에게 안내 메시지를 표시합니다.
  - **세션 관리**: 사용자의 로그인 상태를 유지하기 위해 세션 관리를 설정했습니다. 이를 통해 사용자가 애플리케이션을 이용하는 동안 인증 상태를 유지할 수 있습니다.

- **회원가입**: 
  - **회원가입 폼**: 사용자가 이메일, 비밀번호, 아이디 등의 필수 정보를 입력할 수 있는 회원가입 폼을 제공했습니다. 각 입력 필드는 유효성 검사를 거쳐 올바른 형식으로 입력되었는지 확인합니다.
  - **비밀번호 암호화**: 사용자의 비밀번호는 Spring Security의 PasswordEncoder를 사용하여 암호화한 후 데이터베이스에 저장됩니다. 이를 통해 사용자 비밀번호의 보안성을 강화했습니다.
  - **자동 로그인**: 회원가입이 성공적으로 완료되면 사용자를 자동으로 로그인시켜 사용자 경험을 개선했습니다.

- **OAuth2 소셜 로그인**:
  - **OAuth2 프로토콜**: 카카오와 네이버와 같은 소셜 로그인 기능을 OAuth2 프로토콜을 이용하여 구현했습니다. 사용자는 소셜 계정을 통해 간편하게 로그인할 수 있습니다.
  - **OAuth2UserService**: 소셜 로그인 정보를 처리하기 위해 OAuth2UserService를 구현했습니다. 소셜 로그인 정보는 데이터베이스에서 조회되며, 필요한 경우 새로운 회원으로 등록됩니다.
  - **OAuth2SuccessHandler**: 소셜 로그인 성공 시 사용자를 메인 화면으로 리다이렉트하는 OAuth2SuccessHandler를 구현했습니다. 이를 통해 소셜 로그인 후 사용자가 원하는 페이지로 빠르게 이동할 수 있습니다.

<br>

### 2) 상품 주문 기능
상품 주문 기능은 사용자가 상품을 선택하고 주문할 수 있도록 하는 기능입니다.

- **주문 프로세스**: 
  - 상품을 장바구니에 추가하거나 바로 주문
  - 주문할 상품의 수량과 옵션(색상, 사이즈) 선택
  - 주문 정보 입력(배송지, 포인트 사용 등)
  - 주문 요약 확인 후 주문 완료
  - 주문 내역 DB 저장 및 주문 조회 기능 제공

- **MVC 아키텍처 적용**:
  - **Controller**: 사용자의 주문 요청을 처리하고, Service 계층으로 전달합니다. 입력된 데이터의 유효성을 검사하고, 비즈니스 로직을 실행합니다. 예를 들어, OrderController는 사용자가 주문 폼을 작성하고 제출할 때 발생하는 요청을 처리합니다.
  - **Service**: 주문 처리에 필요한 비즈니스 로직을 구현하고, 실제 주문을 생성하여 데이터베이스에 저장합니다. OrderService는 주문 정보를 처리하고, 결제 정보와 회원의 포인트 정보를 업데이트합니다. 또한, 주문 생성 후 장바구니에서 해당 상품을 제거합니다.
  - **View**: 주문 페이지 UI를 제공하여 사용자가 주문 정보를 입력하고 주문을 완료할 수 있도록 합니다. 예를 들어, 주문 정보 입력 폼과 결제 버튼을 포함한 HTML 페이지가 제공됩니다.
  
  이렇게 구성된 MVC 아키텍처는 코드를 모듈화하고 각 계층 간의 의존성을 최소화하여 애플리케이션의 유연성을 높였습니다. 따라서 새로운 기능을 추가하거나 기존 기능을 변경할 때 시스템 전체에 미치는 영향을 최소화하면서 개발을 진행할 수 있습니다.

<br>

### 3) 게시판 및 댓글 기능
사용자들이 정보를 교류하고 상호 작용할 수 있도록 게시판 및 댓글 기능을 제공합니다.

- **주요 기능**
  - 게시글 CRUD 기능
  - 게시글 검색 및 페이징 기능
  - 이전 글/다음 글 탐색 기능
  - 계층형 답글 기능
  - 댓글 CRUD 기능

- **도메인 설계**
  - Board와 Comment 엔티티는 Member 엔티티와 양방향 N:1 연관 관계를 맺고 있습니다. 즉, 한 명의 회원은 여러 개의 게시글과 댓글을 작성할 수 있습니다.
  - 1개의 게시글에는 여러 댓글이 작성될 수 있기 때문에, Board 엔티티와 Comment 엔티티는 단방향 1:N 연관 관계를 맺고 있습니다. 즉, 하나의 게시글은 여러 개의 댓글을 가질 수 있습니다.

- **페이징 및 검색 기능**
  
  - 게시글 목록 조회 시 페이징 기능과 사용자가 입력한 검색 조건에 따라 동적으로 쿼리를 생성하기 위해 QueryDSL을 활용했습니다. 또한, 필요한 컬럼값만 효율적으로 가져오기 위해 DTO(Data Transfer Object)를 사용하여 데이터를 조회했습니다.
```java
@RequiredArgsConstructor
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBoard board = QBoard.board;
    private final QProductImage productImage = QProductImage.productImage;

    @Override
    public Page<NoticeListDTO> getNoticeList(SearchDTO searchDTO, Pageable pageable) {
        List<NoticeListDTO> content = queryFactory
                .select(
                        new QNoticeListDTO(
                                board.id,
                                board.title,
                                board.content,
                                board.member.name,
                                board.regTime,
                                board.hasNotice
                        )
                )
                .from(board)
                .where(like(searchDTO))
                .where(board.member.isDeleted.eq(false))
                .where(board.boardType.eq("Notice"))
                .orderBy(board.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(board.count())
                .from(board)
                .where(like(searchDTO))
                .where(board.boardType.eq("Notice"));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(SearchDTO searchDTO) {
        if (StringUtils.equals(searchDTO.getSearchDateType(), "title")) {
            return board.title.like("%" + searchDTO.getSearchQuery() + "%");
        } else if (StringUtils.equals(searchDTO.getSearchDateType(), "content")) {
            return board.content.like("%" + searchDTO.getSearchQuery() + "%");
        } else if (StringUtils.equals(searchDTO.getSearchDateType(), "name")) {
            return board.member.name.like("%" + searchDTO.getSearchQuery() + "%");
        } else if (StringUtils.equals(searchDTO.getSearchDateType(), "")) {
            return board.title.like("%" + searchDTO.getSearchQuery() + "%")
                    .or(board.content.like("%" + searchDTO.getSearchQuery() + "%"))
                    .or(board.member.name.like("%" + searchDTO.getSearchQuery() + "%"));
        }
        return null;
    }
}
```

- **이전글/다음글 탐색 기능 및 NativeQuery 활용**
  
  - 이전글/다음글 탐색 기능은 사용자가 현재 보고 있는 게시글의 이전 글과 다음 글을 쉽게 탐색할 수 있도록 합니다. 이 기능을 구현하기 위해 쿼리 작성 시 UNION을 사용해야 했지만, JPQL에서는 UNION을 지원하지 않으므로 NativeQuery를 활용했습니다.
  - 쿼리 결과를 매핑할 때 데이터 전송량을 줄이고 복잡한 조인 결과를 단순화하기 위해 인터페이스를 활용했습니다. 이를 통해 필요한 필드만 선택적으로 가져오고, 타입 안정성을 유지하여 컴파일 타임에 발생할 수 있는 오류를 방지했습니다.
  - 이후 Service 계층에서 매핑된 쿼리 결과를 DTO 객체로 변환하여 반환했습니다.

```java
@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, QuerydslPredicateExecutor<Board>, BoardRepositoryCustom {
    @Query(value = "select b.board_id as boardId, b.title, b.content, b.has_notice as hasNotice, b.member_id as memberId, b.reg_time as regTime from board b where b.board_id = :boardId" +
                " union all (select b.board_id as boardId, b.title, b.content, b.has_notice as hasNotice, b.member_id as memberId, b.reg_time as regTime from board b where b.board_id < :boardId and b.board_type = 'Notice' order by b.board_id desc limit 1)" +
                " union all (select b.board_id as boardId, b.title, b.content, b.has_notice as hasNotice, b.member_id as memberId, b.reg_time as regTime from board b where b.board_id > :boardId and b.board_type = 'Notice' order by b.board_id asc limit 1)", nativeQuery = true)
        List<NoticeDtlDtoInterface> getNoticeDtl(@Param("boardId") Long boardId);
}
```
```java
public interface NoticeDtlDtoInterface {
    Long getBoardId();
    String getTitle();
    String getContent();
    boolean getHasNotice();
    Long getMemberId();
    LocalDateTime getRegTime();
}
```
```java
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    // 생성자 주입 생략

    public List<NoticeDtlDTO> getNoticeDtlList(Long noticeId) {
        List<NoticeDtlDTO> noticeDtlDTOList = new ArrayList<>();
        for (NoticeDtlDtoInterface notice : boardRepository.getNoticeDtl(noticeId)) {
            NoticeDtlDTO noticeDtlDto = new NoticeDtlDTO(notice, memberService.getMemberByMemberId(notice.getMemberId()).getName());
            noticeDtlDTOList.add(noticeDtlDto);
        }
        return noticeDtlDTOList;
    }
}
```

<br>

### 4. 기타 다양한 기술 및 사항
- **DTO를 활용한 클라이언트 값 반환**: 엔티티를 직접 노출하지 않고 DTO로 변환하여 반환함으로써 시스템의 보안성을 높이고, 필요한 데이터만 전송하여 성능을 최적화하였습니다.
- **데이터베이스 성능 최적화**: Lazy 로딩을 사용하여 필요한 경우에만 연관된 엔티티를 로딩하도록 하였습니다.
- **예외 처리 전략**: 일관된 예외 처리 전략을 적용하여 안정성을 향상시켰습니다. 모든 예외는 `try-catch` 블록으로 처리되며, 상황에 맞는 적절한 오류 메시지를 반환하기 위해 커스텀 예외를 정의하였습니다. 예를 들어, 회원 삭제 시 회원이 존재하지 않는 경우 `EntityNotFoundException`을 발생시키고, 관리자 권한을 가진 회원을 삭제하려 할 때는 `AdminDeletionException`을 발생시킵니다. 이를 통해 코드의 가독성과 유지 보수성을 높였습니다.

![예외 처리 전략](https://github.com/user-attachments/assets/bfc9c1d1-bafa-4968-825f-a57cec53057f)

<br>

## 6. 트러블 슈팅

### 1) 스프링 순환 참조

#### [문제 배경]
프로젝트 진행 중, AccountService와 SecurityConfig 간에 스프링 순환 참조 문제가 발생했습니다. 이 문제는 스프링 빈을 생성하는 과정에서 발생했으며, 두 클래스 간의 의존성 주입이 상호 참조를 일으킨 것이 원인이었습니다.

#### [해결 방법]
문제를 해결하기 위해 스프링의 의존성 주입과 빈 생성 메커니즘을 다시 검토했습니다.<br>
AccountService에서 UserDetailsService를 구현하는 대신, 별도의 CustomUserDetailsService 클래스를 만들어 UserDetailsService를 구현하게 했습니다.<br>
이를 통해 SecurityConfig는 더 이상 AccountService를 참조하지 않게 되어 순환 참조 문제가 해결되었습니다.

#### [이전 코드와 비교]
- 이전 코드: AccountService와 SecurityConfig가 서로를 참조.<br>
- 변경 후 코드: AccountService와 SecurityConfig 간의 참조를 제거하고, CustomUserDetailsService를 사용.<br><br>
![스프링 순환 참조](https://github.com/user-attachments/assets/14c73d4e-ee15-4fb7-b794-b1a0e35bb1e4)

#### [해당 경험을 통해 알게 된 점]
이 경험을 통해 스프링의 빈 라이프사이클과 의존성 주입 메커니즘에 대한 이해를 높일 수 있었습니다. 또한, 의존성을 명확히 분리하여 순환 참조 문제를 방지하는 방법을 익혔습니다.

### 2) LazyInitializationException 발생 및 해결 과정

#### [문제 배경]
특정 메소드에서 LazyInitializationException이 발생했습니다. 이는 ProductOption 엔티티가 Product 엔티티와 N:1 관계를 가지고 있으며, ProductOption의 Product 속성이 FetchType.LAZY로 설정되어 있었기 때문입니다. 트랜잭션이 종료되면서 영속성 컨텍스트가 닫히고, 이후에 LAZY 로딩이 발생해 예외가 발생했습니다.

#### [해결 방법]
서비스 계층에서 조회한 데이터를 DTO로 변환하여 반환함으로써 문제를 해결했습니다. 이를 통해 영속성 컨텍스트 종료 후에도 데이터를 사용할 수 있게 되었고, LAZY 로딩 문제를 피할 수 있었습니다.

#### [이전 코드와 비교]
- 이전 코드: 서비스 계층에서 엔티티를 그대로 반환, 컨트롤러에서 사용.<br>
- 변경 후 코드: 서비스 계층에서 DTO로 변환 후 반환, 컨트롤러에서 DTO 사용.

#### [해당 경험을 통해 알게 된 점]
LAZY 로딩에 대한 이해를 높일 수 있었고, 영속성 컨텍스트와 관련된 예외를 방지하기 위해 DTO를 사용하는 것이 중요한 패턴임을 깨달았습니다.

### 3) orphanRemoval 관련 오류 및 해결 과정

#### [문제 배경]
상품 수정 시 Product의 productOptions 필드를 업데이트하는 과정에서 orphanRemoval 관련 오류가 발생했습니다. 이는 엔티티의 연관 관계 설정과 관련된 문제로, 특정 옵션을 삭제하려 할 때 발생했습니다.

#### [해결 방법]
기존 컬렉션을 clear() 메소드로 비우고, 새로운 요소를 addAll() 메소드로 추가하여 문제를 해결했습니다. 이를 통해 엔티티의 연관 관계에서 발생하는 문제를 안전하게 해결할 수 있었습니다.

#### [이전 코드와 비교]
- 이전 코드: 직접 컬렉션의 요소를 제거하거나 추가.
- 변경 후 코드: 전체 컬렉션을 비운 후 새로운 요소를 추가.

#### [해당 경험을 통해 알게 된 점]
엔티티의 연관 관계를 다룰 때, 특히 orphanRemoval 속성 사용 시 주의해야 할 점을 배웠습니다. 올바른 컬렉션 관리 방식을 통해 데이터 일관성을 유지하는 것이 중요함을 깨달았습니다.

<br>

## 7. 기술적인 고민과 해결과정

### 데이터베이스 접근 전략
프로젝트에서 데이터베이스 접근에는 JPA를 활용하였으며, 최적의 쿼리 작성 방식을 선택하기 위해 다양한 접근 방식을 비교하고 고민했습니다.

### 쿼리 작성 방식의 비교
1. **기본 JPA 쿼리 메서드**: 간편한 쿼리 작성이 가능하지만, 복잡한 조건이나 Join 연산을 다루기 어렵다는 한계가 있었습니다.
2. **직접 JPQL 작성**: 자유로운 쿼리 작성이 가능하나, 오타나 문법 오류 발생 가능성이 높고, JPA 엔티티와의 매핑 문제도 고려해야 했습니다.
3. **NativeQuery 사용**: SQL 문법을 직접 작성하여 성능 최적화나 특정 데이터베이스 기능 활용에 유리하지만, 데이터베이스 종속성이 증가하고 보안 문제에 취약할 수 있습니다.
4. **QueryDSL 도입**: 타입 안정성을 보장하고 쿼리 작성을 편리하게 하며, 컴파일 시점에 오류를 확인할 수 있어 개발 효율성을 높이는 장점이 있습니다.

### 최종 선택 및 적용
프로젝트의 요구 사항과 데이터베이스 구조를 종합적으로 고려하여, 다음과 같은 접근 방식을 채택하였습니다.
- **JPQL**: JPQL을 주로 사용하여 복잡한 쿼리를 구현했습니다. JPQL은 자유로운 쿼리 작성이 가능하며, 프로젝트 대부분의 요구 사항을 충족했습니다.
- **QueryDSL**: 동적 쿼리 작성 시 활용하여 타입 안정성과 쿼리 작성의 편리성을 확보했습니다.
- **NativeQuery**: JPQL에서 지원하지 않는 UNION과 같은 복잡한 쿼리가 필요할 때 선택적으로 사용하였습니다.

### 결과
이러한 전략을 통해 데이터베이스 접근의 유연성을 높였으며, 유지보수성과 성능 최적화를 동시에 도모할 수 있었습니다.

<br>

## 8. 향후 계획 또는 개선 사항

### 테스트 커버리지 확대를 통한 품질 개선
현재는 주로 기본적인 기능에 대한 테스트 코드만 작성되어 있으나, 실제 사용자가 경험할 수 있는 다양한 상황을 반영한 시나리오를 테스트하는 과정이 필요하다고 생각합니다.

향후에는 다양한 사용자 시나리오를 고려한 테스트 코드를 추가하여 서비스의 신뢰성과 안정성을 높이고, 사용자들에게 더 나은 서비스를 제공할 수 있도록 노력할 것입니다. 이뿐만 아니라 테스트 주도 개발(TDD) 방법론을 도입하여 테스트 코드를 먼저 작성하고 이를 통과하는 코드를 작성함으로써 품질을 보장하고자 합니다. 이를 통해 사용자들이 다양한 환경에서도 편리하게 서비스를 이용할 수 있도록 보장할 수 있습니다.

### HTTPS 적용을 통한 보안 강화
현재는 HTTP를 통해 통신이 이루어지고 있으나, 사용자 데이터와 통신의 안전성을 보장하기 위해 HTTPS를 적용할 계획입니다.

HTTPS를 사용하면 데이터 전송 시 암호화를 통해 중간에서 데이터가 도청되거나 변조되는 것을 방지할 수 있으며, 사용자에게 보다 안전한 환경을 제공할 수 있습니다. 이를 통해 프로젝트의 보안성을 강화하고, 사용자들의 신뢰도를 높일 수 있을 것으로 기대됩니다.

### 액세스 토큰과 리프레시 토큰을 활용한 인증 방식 개선
현재는 세션 방식을 통해 사용자 인증과 세션 관리를 진행하고 있습니다. 그러나 세션 방식은 서버 측에서 상태를 유지하고 관리하기 때문에 확장성이 제한되고, 여러 서버 간의 세션 공유에도 어려움이 있습니다.

향후에는 JWT(Json Web Token)를 활용한 액세스 토큰과 리프레시 토큰 방식을 도입하여 인증 방식을 개선할 계획입니다. JWT를 사용하면 서버 측에서 상태를 저장하지 않고도 클라이언트 측에서 인증 정보를 안전하게 저장하고 전송할 수 있어, 서버의 부하를 줄이고 분산 환경에서의 관리도 용이해집니다.

이를 통해 프로젝트의 보안성과 확장성을 향상시키고, 사용자들에게 더욱 신뢰할 만한 서비스를 제공할 수 있을 것으로 기대됩니다.
