package com.example.post.order.service;

import com.example.post.common.service.SseAlarmService;
import com.example.post.order.domain.OrderDetail;
import com.example.post.order.domain.Ordering;
import com.example.post.order.dto.OrderCreateDto;
import com.example.post.order.dto.OrderListDto;
import com.example.post.order.dto.ProductDto;
import com.example.post.order.feignclients.ProductFeignClient;
import com.example.post.order.repository.OrderingDetailRepository;
import com.example.post.order.repository.OrderingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService {
    private OrderingRepository orderingRepository;
    private final SseAlarmService sseAlarmService;
    private final OrderingDetailRepository orderingDetailRepository;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Autowired
    public OrderingService(OrderingRepository orderingRepository1, SseAlarmService sseAlarmService, OrderingDetailRepository orderingDetailRepository, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate<String, Object> kafkaTemplate) {

        this.orderingRepository = orderingRepository1;
        this.sseAlarmService = sseAlarmService;
        this.orderingDetailRepository = orderingDetailRepository;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Long save(List<OrderCreateDto> orderCreateDtoList,String email) {
        Ordering ordering =Ordering.builder()
                .memberEmail(email)
                .build();
        orderingRepository.save(ordering);
        for(OrderCreateDto dto: orderCreateDtoList) {
///           1. 재고조회(동기요청-http요청) //게이트웨이 인증때문에 안가고 유레카로 집접가서 내부통신
//        http://localhost:8080/product-service : apigateway을 통한 호출
//        http://product-service : eureka에게 질의 후 product-service직접 호출 //@LoadBalance필요


            String endpoint1 = "http://product-service/product/detail/" +dto.getProductId(); // 유레카 질의 // localhost:8080=gatewat주소// 로드 밸런스 붙여서 가져와서 저거 없이 내부통신으로 가능

            HttpHeaders headers =new HttpHeaders(); //get이니까 헤더에 넣을게 없다 (평소 ex contentType)
///            HttpEntity = header+body
//          get은 body세팅 x 나머지느 세팅해야한다
            HttpEntity<String> httpEntity =new HttpEntity<>(headers);

           ResponseEntity<ProductDto> responseEntity= restTemplate.exchange(endpoint1, HttpMethod.GET,httpEntity,ProductDto.class); //주입받은 restTemplate //유레카라서 토큰 필요 x////헤더에 아무것도 없어도 세팅하고 보낸다 헤더에 뭔가를 넣어줄수도 있어서 // responseEntity로 감쌈 //http로 가서 http로 받음
            ProductDto product =responseEntity.getBody();



           if(product.getStockQuantity()<dto.getProductCount()){
               throw new IllegalArgumentException("재고가 부족합니다");
           }
///           2.주문발생
            OrderDetail orderDetail = OrderDetail.builder() //2. 주문상세
                    .ordering(ordering)
                    .productId(dto.getProductId())
                    .productName(product.getName())  //위에 http요청으로
                    .quantity(dto.getProductCount())
                    .build();
            orderingDetailRepository.save(orderDetail);


///            3. 재고감소 요청(동기-http/비동기-이벤트기반(실무에서 잘 안함) 모두 가능) -비동기시 롤백처리가 안되서 잘 안쓰임(성능은 좋다)
            String endpoint2 = "http://product-service/product/updatestock" ;

            HttpHeaders headers2 =new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderCreateDto> httpEntity2 =new HttpEntity<>(dto,headers2);

           restTemplate.exchange(endpoint2, HttpMethod.PUT,httpEntity2,Void.class);
            ProductDto product2 =responseEntity.getBody();
        }

    return ordering.getId();
    }

    public List<OrderListDto>findByAll(){
        List<OrderListDto> listDtos =new ArrayList<>();
        List<Ordering> orderings = orderingRepository.findAll();
        for(Ordering ordering : orderings){
            listDtos.add(OrderListDto.fromEntity(ordering));
        }
        return listDtos;

    }

    public List<OrderListDto>myorders(String email){

        List<OrderListDto> listDtos = new ArrayList<>();
        List<Ordering> orderings = orderingRepository.findByMemberEmail(email);
        for(Ordering ordering: orderings){
            listDtos.add(OrderListDto.fromEntity(ordering));
        }

        return listDtos;

    }



/// ///Feignq버전 -interface (rest안쓰고)

public Long createFeign( List<OrderCreateDto> orderCreateDtoList, String email){
// 재고조회
    Ordering ordering = Ordering.builder()
            .memberEmail(email)
            .build();
    orderingRepository.save(ordering);

//2주문발생
    for (OrderCreateDto dto : orderCreateDtoList){
        ProductDto product = productFeignClient.getProductById(dto.getProductId());
        if(product.getStockQuantity() < dto.getProductCount()){
            throw new IllegalArgumentException("재고가 부족합니다");
        }
        OrderDetail orderDetail = OrderDetail.builder()
                .ordering(ordering)
                .productName(product.getName())
                .productId(dto.getProductId())
                .quantity(dto.getProductCount())
                .build();
        orderingDetailRepository.save(orderDetail);

//        재고감소 요청??
//        feign을 사용한 동기적 재고감소 요청
//        productFeignClient.updateStockQuantity(dto);
//        kafka를 활용한 비동기적 재고감소 요청
        kafkaTemplate.send("stock-update-topic",dto);
    }

    return ordering.getId();
}





}
