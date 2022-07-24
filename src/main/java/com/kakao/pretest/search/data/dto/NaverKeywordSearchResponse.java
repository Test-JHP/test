package com.kakao.pretest.search.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NaverKeywordSearchResponse implements KeywordSearchResponse {

    private List<Item> items;

    @Data
    public static class Item {
        private String title;
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

