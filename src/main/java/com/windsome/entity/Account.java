package com.windsome.entity;

import com.windsome.constant.Role;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = true)
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account extends BaseTimeEntity {

    @Id @GeneratedValue
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

}
