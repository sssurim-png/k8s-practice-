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
public class SearchDto { //페이징 처리, 사이즈?
    private String productName;
    private String category;

    public static SearchDto fromEntity(Product product){
        return SearchDto.builder()
                .productName(product.getName())
                .category(product.getCategory())
                .build();
    }

}
