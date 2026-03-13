package com.example.post.member.controller;

import com.example.post.common.auth.JwtTokenProvider;
import com.example.post.member.domain.Member;
import com.example.post.member.dto.*;
import com.example.post.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    private MemberService memberService;
    private JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //    1. 회원가입
    @PostMapping("/create")

    public ResponseEntity<?> create(@RequestBody CreateDto dto){
        Long id =memberService.save(dto); //이메일 찾아서 있으면 에러, 없으면 저장
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

//    2. user로그인
    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody MemberLoginReqDto dto){
        Member member = memberService.login(dto);

        String accessToken =jwtTokenProvider.createToken(member);
///        refresh토큰생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto memberLoginResDto=MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(memberLoginResDto);
    }


//    3. 회원목록조회
    @GetMapping("/list")
    public List<MemberListDto> list(){
        List<MemberListDto> dto = memberService.findAll();
        return dto;
    }


//    4. 내 정보 조회
    @GetMapping("/myinfo")
//    X로 시작하는 헤더명은  개발자가 인위적으로 만든 Header인 경우에 관례적으로 사용
    public MyDto myinfo(@RequestHeader("X-User-Email")String email){ //그럼 email어떻게 넘길거냐 토큰에서 빼왔는데
        MyDto dto =memberService.myinfo(email);
        return dto;
    }


//    5. 회원 상세내역조회
    @GetMapping("/detail/{id}")
    public MemberDtailDto detail(@PathVariable Long id){
        return memberService.findById(id);

    }


    @PostMapping("/refresh-at") //rt파라미터 XX(위험-url이 그대로 남는다)
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){
///        rt검증(1.토큰 자체 검증 2. redis조회 검증) //1번 굳이 다시 검증하는 이유: ttl설정을 안해놔서 만료시간을 알수없다??
        Member member =jwtTokenProvider.validateRt(dto.getRefreshToken());

///        at신규 생성
        String accessToken = jwtTokenProvider.createToken(member);
///        refresh토큰생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto memberLoginResDto=MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(memberLoginResDto);
    }

}
