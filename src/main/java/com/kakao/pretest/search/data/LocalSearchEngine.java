package com.kakao.pretest.search.data;

import com.kakao.pretest.search.data.dto.KeywordSearchResponse;
import reactor.core.publisher.Mono;

public interface LocalSearchEngine {
    Mono<? extends KeywordSearchResponse> search(final String query);
}
