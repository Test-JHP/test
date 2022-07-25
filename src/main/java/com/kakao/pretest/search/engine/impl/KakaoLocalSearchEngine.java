package com.kakao.pretest.search.engine.impl;

import com.kakao.pretest.search.dto.KakaoKeywordSearchResponse;
import com.kakao.pretest.search.engine.LocalSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KakaoLocalSearchEngine implements LocalSearchEngine {

    private final String KAKAO_ACCESS_KEY;
    private final WebClient webClient;

    public KakaoLocalSearchEngine (@Value("${kakao-access-key}") String kakaoAccessKey,
                                   WebClient webClient) {
        this.KAKAO_ACCESS_KEY = kakaoAccessKey;
        this.webClient = webClient;
    }

    @Override
    public Mono<KakaoKeywordSearchResponse> search(final String query) {
        log.debug("Start Search {}", this.getClass());
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.add("Authorization", "KakaoAK " + KAKAO_ACCESS_KEY);
                })
                .retrieve()
                .bodyToMono(KakaoKeywordSearchResponse.class)
                .doOnSuccess(kakaoKeywordSearchResponse -> log.debug("Complete API : {}", kakaoKeywordSearchResponse))
                .doOnError(throwable -> log.error("{}", throwable.getMessage()))
                .onErrorReturn(new KakaoKeywordSearchResponse()) // 에러 발생시, Empty Mono<KeywordSearchResponse> 리턴
                ;
    }

}
