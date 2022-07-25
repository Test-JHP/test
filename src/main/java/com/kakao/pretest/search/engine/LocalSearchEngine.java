package com.kakao.pretest.search.engine;

import com.kakao.pretest.search.dto.Result;
import reactor.core.publisher.Mono;

public interface LocalSearchEngine {
    Mono<Result> search(final String query);
}
