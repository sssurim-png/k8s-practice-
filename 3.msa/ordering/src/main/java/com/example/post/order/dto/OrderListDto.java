package com.example.post.order.dto;

import com.example.post.order.domain.OrderDetail;
import com.example.post.order.domain.OrderStatus;
import com.example.post.order.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderListDto { //member+order+detail
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailDto> orderDetails;

    public static OrderListDto fromEntity(Ordering ordering){
        List<OrderDetailDto> orderDetailDtos =new ArrayList<>();
        for(OrderDetail orderDetail : ordering.getOrderDetailList()){ //“이 주문(Ordering)에 속한 주문상세들만” DTO로 변환. 주문들이 섞이지 않게, 주문상세가 주문에 종속되도록
            orderDetailDtos.add(OrderDetailDto.fromEntity(orderDetail));
        }

       return OrderListDto.builder()
               .id(ordering.getId())
               .memberEmail(ordering.getMemberEmail())
               .orderStatus(ordering.getOrderStatus())
               .orderDetails(orderDetailDtos)
               .build();
    }
}
