package com.example.post.product.dto;

import com.example.post.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DetailDto {
    private Long id;
    private String name;
    private int price;
    private String category;
    private int stockQuantity;
    private String image_path;

    public static DetailDto fromEntity(Product product){
        return DetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .image_path(product.getImage_path())
                .build();
    }

//    public DetailDto(
//            Long id,
//            String name,
//            String category,
//            int price,
//            int stockQuantity,
//            String image_path
//    ) {
//        this.id = id;
//        this.name = name;
//        this.category = category;
//        this.price = price;
//        this.stockQuantity = stockQuantity;
//        this.image_path = image_path;
//    }
}


