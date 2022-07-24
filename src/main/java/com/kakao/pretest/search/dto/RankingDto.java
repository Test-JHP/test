package com.kakao.pretest.search.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RankingDto {
    private String query;   // 검색어
    private int count;      // 횟수
}
