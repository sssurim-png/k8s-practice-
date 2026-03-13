package com.example.post.product.service;

import com.example.post.product.domain.Product;
import com.example.post.product.dto.*;
import com.example.post.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final RedisTemplate<String,String>redisTemplate;

    @Value("${aws.s3.bucket1}")
    private String bucket;

    public ProductService(ProductRepository productRepository, S3Client s3Client, @Qualifier("stockInventory")RedisTemplate<String,String> redisTemplate) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }


    //    1. 상품등록
    public Long save(CreateDto dto, String email) {

       Product product = productRepository.save(dto.toEntity(email));
        System.out.println(dto);
//        파일업로드
        if (dto.getProductImage() != null) {
            String fileName = "product-" + product.getId() + "-productImage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();

            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateProfileImageUrl(imgUrl);//뒤늦게 변경

//            동시성문제 해결을 위해 상품등록시 redis재고 세팅




        }
        redisTemplate.opsForValue().set(String.valueOf(product.getId()),String.valueOf(product.getStockQuantity()));
        return product.getId();
    }

//    2. 상품상세조회
    public DetailDto findproduct(Long id) {
        Optional<Product> opt_products = productRepository.findById(id);
        Product product = opt_products.orElseThrow(()->new NoSuchElementException("상품아이디가 없습니다"));
        DetailDto dto = DetailDto.fromEntity(product);
        return dto;
    }

//    3. 상품목록조회
    public Page<DetailDto> findByAll(Pageable pageable, SearchDto searchDto) {//검색도 하기
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (searchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getProductName() + "%"));
                }
                if (searchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
                }
            };

            Page<Product> postList = productRepository.findAll(specification, pageable);
        return postList.map(p->DetailDto.fromEntity(p));
        }



        /// s3이미지 변경
    public void update(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("product cannot be found"));
        product.updateProduct(dto);

        if (dto.getProductImage() != null) {
//            이미지를 수정하거나 추가하고자 하는 경우:삭제 후 추가
//            기존이미지를 파일명으로 삭제
            if (dto.getProductImage() != null) {
                String imgUrl = product.getImage_path();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
///            신규이미지를 등록
            String newfileName = "product-" + product.getId() + "-productImage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newfileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();

            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newfileName)).toExternalForm();
            product.updateProfileImageUrl(newImgUrl);//뒤늦게 변경

        } else if (product.getImage_path() != null){
//            이미지를 삭제하고자 하는 경우
                String imgUrl = product.getImage_path();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));

            product.updateProfileImageUrl(null);
            }
        }

//        4. 재고 수량 관리
    public void updateStock(ProductStockUpdateDto stockUpdateDto){
        Product product = productRepository.findById(stockUpdateDto.getProductId()).orElseThrow(()-> new IllegalArgumentException("재고가 없습니다"));
        if(product.getStockQuantity()<stockUpdateDto.getProductCount()){
            throw new IllegalArgumentException("상품재고 부족");
        }
        product.updateStockQuantity(stockUpdateDto.getProductCount());
    }
    }







