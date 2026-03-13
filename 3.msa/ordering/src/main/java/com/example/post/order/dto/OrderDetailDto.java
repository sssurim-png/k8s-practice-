package com.example.post.order.dto;

import com.example.post.order.domain.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderDetailDto { //detail쪽
    private Long detailId;
    private String productName;
    private int productCount;

    public static OrderDetailDto fromEntity(OrderDetail orderDetail){
        return OrderDetailDto.builder()
                .detailId(orderDetail.getId())
                .productName (orderDetail.getProductName())
                .productCount(orderDetail.getQuantity())
                .build();


    }
}
