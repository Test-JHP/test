package com.kakao.pretest.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
public class QueryEvent {
    private final String query;
}
