package com.kakao.pretest.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ToString
public class NaverKeywordSearchResponse implements KeywordSearchResponse {

    @JsonProperty
    private List<Item> items;

    @ToString
    public static class Item {
        @JsonProperty
        private String title;
        @JsonProperty
        private String address;
    }

    @Override
    public Result toResult() {

        if (items == null) items = new ArrayList<>(); // NPE

        List<Result.Item> resultList = items.stream()
                .map(item -> Result.Item.builder()
                        .engineType("NAVER")
                        .priority(2)
                        .title(item.title)
                        .address(item.address)
                        .build())
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            resultList.forEach(result -> log.debug("{}", result));
        }

        return Result.builder()
                .itemList(resultList)
                .build();
    }
}

