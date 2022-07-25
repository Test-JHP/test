package com.kakao.pretest.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class NaverKeywordSearchResponse {

    @JsonProperty
    private List<Item> items;

    @ToString
    @Getter
    public static class Item {
        @JsonProperty
        private String title;
        @JsonProperty
        private String address;
    }
}

