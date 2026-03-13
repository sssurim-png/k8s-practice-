package com.example.post.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
    public class MemberLoginResDto{
        private String accessToken;
        private String refreshToken;

    }

