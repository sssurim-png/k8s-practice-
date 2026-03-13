package com.example.post.common.auth;


import com.example.post.member.domain.Member;
import com.example.post.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//토큰에서 정보를 빼오거나 받아오지는 않지만 여전희 member에서 토큰을 만듦
@Component
public class JwtTokenProvider {//토큰 스펙
    private final MemberRepository memberRepository;


    @Value("${jwt.secretKey}")
    private String st_secret_key;

    @Value("${jwt.expiration}")
    private int expiration;


    // Rt생성
    @Value("${jwt.secretKeyRt}")
    private String st_secret_key_rt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    private Key secret_key_rt;
    private Key secret_key;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }


    @PostConstruct
    public void init() {
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key_rt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(Member member) {
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                .signWith(secret_key)
                .compact();
        return token;
    }

    public String createRtToken(Member member) {
//        유효기간이 긴 토큰 생성
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(secret_key_rt)
                .compact();

//        rt토큰을 redis에 저장
//        opsForValue: 일반 스트링 자료구조. opsForSet(또는 Zset 또는 List 등) 존재
//        redisTemplate.opsForValue().set(member.getEmail(), token);
        redisTemplate.opsForValue().set(member.getEmail(), token,expirationRt, TimeUnit.MINUTES); //유효기간
        return token;

    }

    public Member validateRt(String refreshToken){
        Claims claims = null;
///        rt토큰 그 자체를 검증
        System.out.println(refreshToken);
        try {
            claims = Jwts.parserBuilder()//filter에 있던거 그대로
                    .setSigningKey(st_secret_key_rt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
//에러 터질게 많다 handler안보내고 Exception으로 한번에 잡아서 어떤 에러 분류인지 (ex.일괄 400번대-프론트에서만 더 추가적으로 잘 짜면된다)
        }catch (Exception e){
            throw new IllegalArgumentException("잘못된 토큰입니다");

        }

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Entity is not found"));
        //        redis rt와 비교 검증
        String redisRt = redisTemplate.opsForValue().get(email);
        if(!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("잘못된 토큰입니다");
        }


        return  member;
    }
}