package com.example.post.product.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductUpdateDto {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
//    이미지 수정은 일반적으로 별도의 api로 처리한다
    private MultipartFile productImage; //s3와 통신해야되서 따로 떼는 경우가 있다


}
