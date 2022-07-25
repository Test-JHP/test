package com.kakao.pretest.search.dto;

import lombok.Builder;

@Builder
public class Ranking {
    private final String query;   // 검색어
    private final int count;      // 횟수
}
