package com.example.post.order.domain;

import com.example.post.common.domain.DateTime;

import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Entity
public class Ordering extends DateTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

//  msa모듈간의 관계성 제거
    private String memberEmail;

    @OneToMany(mappedBy = "ordering",fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    List<OrderDetail> orderDetailList = new ArrayList<>();//ArrayList필수


}
