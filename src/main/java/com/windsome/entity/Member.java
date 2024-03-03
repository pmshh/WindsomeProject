package com.windsome.entity;

import com.windsome.constant.Role;
import com.windsome.dto.order.OrderRequestDTO;
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

    private String name;

    private String password;

    @Column(unique = true)
    private String email;

    private String tel;

    private String zipcode;

    private String addr;

    private String addrDetail;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    private int availablePoints; // 사용 가능한 포인트

    private int totalEarnedPoints; // 총 적립 포인트

    private int totalUsedPoints; // 총 사용 포인트

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void addPoint(OrderRequestDTO orderRequestDTO) {
        int curPoint = this.availablePoints;
        this.availablePoints = (int) (curPoint + (Math.floor(orderRequestDTO.getTotalPaymentPrice() * 0.05)));
    }
}
