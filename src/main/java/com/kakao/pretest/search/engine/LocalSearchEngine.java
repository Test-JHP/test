package com.kakao.pretest.search.engine;

import com.kakao.pretest.search.dto.KeywordSearchResponse;
import reactor.core.publisher.Mono;

public interface LocalSearchEngine {
    Mono<? extends KeywordSearchResponse> search(final String query);
}
