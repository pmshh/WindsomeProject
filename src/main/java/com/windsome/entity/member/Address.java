package com.windsome.entity.member;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;  // 받는분 성함

    private String zipcode; // 우편 번호

    private String addr; // 받는분 주소

    private String addrDetail; // 받는분 상세 주소

    private String tel; // 받는분 전화 번호

    private String req; // 배송 메시지

    private boolean isDefault; // 기본 배송지 여부
}
