package com.windsome.entity;

import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.item.ItemFormDto;
import com.windsome.entity.Auditing.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Item extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String itemNm;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockNumber;

    @Lob
    @Column(nullable = false)
    private String itemDetail;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cate_id")
    private Category category;

    private double discount;

    @Column(precision =3, scale = 2)
    private BigDecimal ratingAvg;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void updateItem(ItemFormDto itemFormDto) {
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
        this.discount = itemFormDto.getDiscount();
    }

    public void removeStock(int stockNumber) {
        int restStock = this.stockNumber - stockNumber;
        if (restStock <= 0) {
            this.itemSellStatus = ItemSellStatus.SOLD_OUT;
        }
        this.stockNumber = restStock;
    }

    public void addStock(int stockNumber) {
        this.stockNumber += stockNumber;
    }
}
