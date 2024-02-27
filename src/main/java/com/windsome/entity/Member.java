package com.windsome.entity;

import com.windsome.constant.Role;
import com.windsome.dto.order.OrderDto;
import com.windsome.entity.auditing.BaseTimeEntity;
import com.windsome.entity.board.Review;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = "reviews")
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String userIdentifier;

    @Column(unique = true)
    private String email;

    private String name;

    private String password;

    private String address1;

    private String address2;

    private String address3;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role state;

    private int point; // 현재 보유중인 포인트 잔액

    private int totalPoint; // 총 적립 포인트

    private int totalUsePoint; // 총 사용 포인트

    private int totalOrderPrice; // 총 주문 금액

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void addPoint(OrderDto orderDto) {
        int curPoint = this.point;
        this.point = (int) (curPoint + (Math.floor(orderDto.getOrderSalePrice() * 0.05)));
    }
}
