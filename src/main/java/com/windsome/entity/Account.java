package com.windsome.entity;

import com.windsome.constant.Role;
import com.windsome.dto.order.OrderDto;
import com.windsome.entity.Auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Account extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
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
    private Role state;

    private int money;

    private int point;

    private int totalPoint;

    private int totalUsePoint;

    private int totalOrderPrice;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public static Account addPoint(Account account, OrderDto orderDto) {
        int curPoint = account.getPoint();
        account.setPoint((int) (curPoint + (Math.floor(orderDto.getOrderSalePrice() * 0.05))));

        return account;
    }
}
