package com.example.post.order.feignclients;

import com.example.post.order.dto.OrderCreateDto;
import com.example.post.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

//name부분은 eureka에 등록된 application name을 의미 //유레카에 듣록된걸 쓰고 있다
//url부분은 k8s의 서비스명 (local에서만 유레카 불러오게 하기). 있으면 쓰고 없으면 공백으로 채운다(:)
@FeignClient(name = "product-service", url ="${product.service.url:}")
public interface ProductFeignClient {

    @GetMapping("/product/detail/{id}") //get으로 보내겠다
    ProductDto getProductById(@PathVariable("id")Long id);


    @PutMapping("/product/updatestock")
    void updateStockQuantity(@RequestBody OrderCreateDto dto);
}
