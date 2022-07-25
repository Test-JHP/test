package com.kakao.pretest.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class KakaoKeywordSearchResponse  {

    @JsonProperty("documents")
    private List<Item> items;

    @ToString
    @Getter
    public static class Item {
        @JsonProperty("place_name")
        private String title;
        @JsonProperty("address_name")
        private String address;
    }
}
