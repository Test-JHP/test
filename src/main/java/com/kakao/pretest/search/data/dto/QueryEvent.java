package com.kakao.pretest.search.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryEvent {
    private final String query;
}
