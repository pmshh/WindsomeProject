package com.windsome.entity;

import com.windsome.constant.ProductSellStatus;
import com.windsome.constant.Role;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.product.Product;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.orderProduct.OrderProductRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class OrderTest {

    @Autowired OrderRepository orderRepository;
    @Autowired ProductRepository productRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired OrderProductRepository orderProductRepository;
    @PersistenceContext EntityManager em;

    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest() {
        Order order = this.createOrder();
        order.getOrderProducts().remove(0);
        em.flush();
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {
        Order order = new Order();

        for (int i = 0; i < 3; i++) {
            Product product = this.createProduct();
            productRepository.save(product);
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(product);
            orderProduct.setOrderQuantity(10);
            orderProduct.setPrice(1000);
            orderProduct.setOrder(order);
            order.getOrderProducts().add(orderProduct);
        }

        orderRepository.saveAndFlush(order);
        em.clear();

        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderProducts().size());
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest() {
        Order order = this.createOrder();
        Long orderItemId = order.getOrderProducts().get(0).getId();
        em.flush();
        em.clear();

        OrderProduct orderProduct = orderProductRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);
        System.out.println("Order Class : " + orderProduct.getOrder().getClass());
        System.out.println(" ================================ ");
        orderProduct.getOrder().getOrderDate();
        System.out.println(" ================================ ");
    }

    public Product createProduct() {
        Product product = new Product();
        product.setName("테스트 상품");
        product.setPrice(10000);
        product.setProductDetail("상세 설명");
        product.setProductSellStatus(ProductSellStatus.AVAILABLE);
//        product.setStockNumber(100);
        return product;
    }

    public Order createOrder() {
        Order order = new Order();

        for (int i = 0; i < 3; i++) {
            Product product = createProduct();
            productRepository.save(product);
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(product);
            orderProduct.setOrderQuantity(10);
            orderProduct.setPrice(1000);
            orderProduct.setOrder(order);
            order.getOrderProducts().add(orderProduct);
        }

        Member member = saveMember();

        order.setMember(member);
        orderRepository.save(order);
        return order;
    }

    public Member saveMember() {
        Member member = Member.builder()
                .userIdentifier("test1234")
                .password("test1234")
                .name("test")
                .email("test1234@naver.com")
                .zipcode("test")
                .addr("test")
                .addrDetail("test")
                .role(Role.USER)
                .availablePoints(0)
                .totalUsedPoints(0)
                .totalEarnedPoints(0)
                .build();
        return memberRepository.save(member);
    }
}