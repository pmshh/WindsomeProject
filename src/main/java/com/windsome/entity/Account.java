package com.windsome.entity;

import com.windsome.constant.Role;
import com.windsome.entity.Auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
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

    public static Account addPoint(Account account, List<OrderItem> orderItemList) {
        int totalOrderPrice = 0;
        for (OrderItem orderItem : orderItemList) {
            totalOrderPrice += orderItem.getPrice() * orderItem.getCount();
        }
        int curPoint = account.getPoint();
        account.setPoint((int) (curPoint + (Math.floor((totalOrderPrice * 0.05)))));

        return account;
    }
}
