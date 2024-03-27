package com.windsome.entity.member;

import com.windsome.entity.auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Address extends BaseTimeEntity {

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

    /**
     * Constructors, Getters, Setters, etc.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(name, address.name) && Objects.equals(zipcode, address.zipcode) && Objects.equals(addr, address.addr) && Objects.equals(addrDetail, address.addrDetail) && Objects.equals(tel, address.tel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, zipcode, addr, addrDetail, tel);
    }
}
