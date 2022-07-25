package com.kakao.pretest.search.engine.impl;

import com.kakao.pretest.search.dto.NaverKeywordSearchResponse;
import com.kakao.pretest.search.dto.Result;
import com.kakao.pretest.search.engine.LocalSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NaverLocalSearchEngine implements LocalSearchEngine {

    private final String NAVER_CLIENT_ID;
    private final String NAVER_CLIENT_SECRET;
    private final WebClient webClient;

    private final String ENGINE_TYPE = "NAVER";
    private final int priority = 2;

    public NaverLocalSearchEngine(@Value("${naver-client-id}") String clientId,
                                  @Value("${naver-secret}") String secret,
                                  WebClient webClient) {
        this.NAVER_CLIENT_ID = clientId;
        this.NAVER_CLIENT_SECRET = secret;
        this.webClient = webClient;
    }

    @Override
    public Mono<Result> search(final String query) {
        log.debug("Start Search {}", this.getClass());
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/local.json")
                        .queryParam("query", query)
                        .queryParam("display", 5)
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.add("X-Naver-Client-Id", NAVER_CLIENT_ID);
                    httpHeaders.add("X-Naver-Client-Secret", NAVER_CLIENT_SECRET);
                })
                .retrieve()
                .bodyToMono(NaverKeywordSearchResponse.class)
                .doOnSuccess(naverKeywordSearchResponse ->  log.debug("Complete API : {}", naverKeywordSearchResponse))
                .doOnError(throwable -> log.error("{}", throwable.getMessage()))
                .onErrorReturn(new NaverKeywordSearchResponse()) // 에러 발생시, Empty Mono<KeywordSearchResponse> 리턴
                .flatMap(this::mapToResult)
                ;
    }

    private Mono<Result> mapToResult(final NaverKeywordSearchResponse response) {

        if (response == null || response.getItems() == null) {
            return Mono.just(Result.builder()
                    .itemList(Collections.emptyList())
                    .build());
        }
        List<Result.Item> resultList = response.getItems().stream()
                .map(item -> Result.Item.builder()
                        .engineType("NAVER")
                        .priority(2)
                        .title(item.getTitle())
                        .address(item.getAddress())
                        .build())
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            resultList.forEach(result -> log.debug("{}", result));
        }

        return Mono.just(Result.builder()
                .itemList(resultList)
                .build());
    }

}
