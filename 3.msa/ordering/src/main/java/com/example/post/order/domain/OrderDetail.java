package com.example.post.order.domain;

import com.example.post.common.domain.DateTime;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Entity
public class OrderDetail extends DateTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT),nullable = false)
    private Ordering ordering;

    //    msa환경에서는 빈번한 http요청에 의한 성능저하를 막기위해, 반정규화 설계도 가능
    private String productName;
    private Long productId;


}
