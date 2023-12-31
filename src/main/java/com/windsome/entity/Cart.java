package com.windsome.entity;

import com.windsome.entity.Auditing.BaseEntity;
import com.windsome.entity.Auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public static Cart createCart(Account account) {
        Cart cart = new Cart();
        cart.setAccount(account);
        return cart;
    }
}
