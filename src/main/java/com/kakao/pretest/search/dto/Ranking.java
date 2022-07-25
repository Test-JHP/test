package com.kakao.pretest.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Ranking {
    private final String query;   // 검색어
    private final int count;      // 횟수
}
