package com.kakao.pretest.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ToString
public class KakaoKeywordSearchResponse implements KeywordSearchResponse {

    @JsonProperty("documents")
    private List<Item> items;

    @ToString
    public static class Item {
        @JsonProperty("place_name")
        private String title;
        @JsonProperty("address_name")
        private String address;
    }

    @Override
    public Result toResult() {

        if (items == null) items = new ArrayList<>(); // NPE

        List<Result.Item> resultList = items.stream()
                .map(item -> Result.Item.builder()
                        .engineType("KAKAO")
                        .priority(1)
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
