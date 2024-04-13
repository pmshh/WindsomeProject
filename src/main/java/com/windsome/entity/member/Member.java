package com.windsome.entity.member;

import com.windsome.constant.Role;
import com.windsome.dto.order.OrderRequestDTO;
import com.windsome.entity.auditing.BaseTimeEntity;
import com.windsome.entity.board.Board;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = {"reviews", "addresses"})
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

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    private int availablePoints; // 사용 가능한 포인트

    private int totalEarnedPoints; // 총 적립 포인트

    private int totalUsedPoints; // 총 사용 포인트

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    private boolean isDeleted; // 회원 삭제 여부

    private String oauth;

    /**
     * Constructors, Getters, Setters, etc.
     */
    public void addPoint(OrderRequestDTO orderRequestDTO) {
        int curPoint = this.availablePoints;
        this.availablePoints = (int) (curPoint + (Math.floor(orderRequestDTO.getTotalOrderPrice() * 0.03)));
    }
}
