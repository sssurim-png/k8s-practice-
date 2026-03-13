package com.example.post.product.domain;

import com.example.post.common.domain.DateTime;
import com.example.post.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@ToString
@Builder
public class Product extends DateTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private  String name;
    private int price;
    @Column
    private String category;
    private int stockQuantity;
    private String image_path;

    private String memberEmail;

    @Builder
    public Product(String memberEmail,
                   String name,
                   int price,
                   String category,
                   int stockQuantity,
                   String image_path) {

        this.memberEmail = memberEmail;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.image_path = image_path;
    }

    public void updateStockQuantity(int orderQuantity){
        this.stockQuantity = this.stockQuantity-orderQuantity;

    }

    public void updateProfileImageUrl(String productImage){
        this.image_path = productImage;
    }

    public void updateProduct(ProductUpdateDto dto){
        this.name=dto.getName();
        this.category=dto.getCategory();
        this.stockQuantity =dto.getStockQuantity();
        this.price =dto.getPrice();
    }

}
