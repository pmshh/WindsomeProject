package com.windsome.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "cateCode")
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Category {

    @Id @GeneratedValue
    private Long cateCode;

    private String name;

    private int tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> child = new ArrayList<>();
}
