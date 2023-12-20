package com.windsome.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String userIdentifier;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String name;

    private String password;

    private String address1;

    private String address2;

    private String address3;

    @Enumerated(EnumType.STRING)
    private Role state;

    private LocalDateTime joinedAt;

    private int money;

    private int point;

    public void completeSignUp() {
        this.joinedAt = LocalDateTime.now();
    }
}
