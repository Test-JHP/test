package com.kakao.pretest.search.service;

import com.kakao.pretest.search.dto.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String REDIS_QUERY_RANKING_KEY = "QUERY:RANKING";

    // ZSet Operation reverseRangeWithScores 통해 검색어 상위 10개를 구한다.
    public List<Ranking> getRanking() {
        final var tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(REDIS_QUERY_RANKING_KEY, 0, 9);

        return tuples != null ? tuples.stream()
                .filter(tuple -> tuple.getScore() != null)
                .map(tuple -> Ranking.builder()
                        .query(String.valueOf(tuple.getValue()))
                        .count(tuple.getScore().intValue())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    // 검색어를 value 에 저장하고, score +1
    public void increaseCount(final String query) {
        try {
            redisTemplate.opsForZSet().incrementScore(REDIS_QUERY_RANKING_KEY, query,1);
        } catch (Exception e) {
            log.error("REDIS QUERY RANKING_KEY ERROR {} ", e.getMessage());
        }
    }
}
