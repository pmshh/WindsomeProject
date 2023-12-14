package com.windsome.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String userId;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private String Address1;

    private String Address2;

    private String Address3;

    @Enumerated(EnumType.STRING)
    private Role state;

    private LocalDateTime joinedAt;

    private int money;

    private int point;

    private boolean emailVerified;

    private String emailCheckToken;

}
