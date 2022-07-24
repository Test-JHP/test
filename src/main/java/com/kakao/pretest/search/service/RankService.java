package com.kakao.pretest.search.service;

import com.kakao.pretest.search.dto.RankingDto;
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

    public List<RankingDto> getRanking() {
        // ZSet Operation reverseRangeWithScores을 통해 검색어 상위 10개를 구한다.
        final var typedTuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(REDIS_QUERY_RANKING_KEY, 0, 9);

        return typedTuples != null ? typedTuples.stream()
                .filter(objectTypedTuple -> objectTypedTuple.getScore() != null)
                .map(objectTypedTuple -> RankingDto.builder()
                        .query(String.valueOf(objectTypedTuple.getValue()))
                        .count(objectTypedTuple.getScore().intValue())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    public void increaseCount(final String query) {
        try {
            // 검색어를 value 에 저장하고, score 를  +1
            redisTemplate.opsForZSet().incrementScore(REDIS_QUERY_RANKING_KEY, query,1);
        } catch (Exception e) {
            log.error("REDIS QUERY RANKING_KEY ERROR {} ", e.getMessage());
        }
    }
}
