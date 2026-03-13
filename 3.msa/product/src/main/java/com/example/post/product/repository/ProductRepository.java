package com.example.post.product.repository;

import com.example.post.product.domain.Product;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findByName(String name);
    //검색처리하려면 findAll 설정다시하기

///    select for update 2.동시성 문제
    @Lock(LockModeType.PESSIMISTIC_WRITE)
   @Query("select p from Product p where  p.id =:id")
    Optional<Product> findByIdForUpdate(@Param("id:")Long id);



Page<Product>  findAll(Specification<Product> specification, Pageable pageable);
}
