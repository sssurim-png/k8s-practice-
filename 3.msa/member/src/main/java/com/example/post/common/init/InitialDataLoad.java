package com.example.post.common.init;


import com.example.post.member.domain.Member;
import com.example.post.member.domain.Role;
import com.example.post.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


//CommanLineRunner를 구현함으로서 아래 run메서드가 스프링빈으로 등록되는 시점에 자동실행
@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public InitialDataLoad(PasswordEncoder encode, MemberRepository memberRepository) {
        this.passwordEncoder = encode;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }

        memberRepository.save(Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .password(passwordEncoder.encode("12341234")) //야물파일에 적기
                .role(Role.ADMIN)
                .build());



    }
}
