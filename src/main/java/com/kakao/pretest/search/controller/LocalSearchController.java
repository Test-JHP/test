package com.kakao.pretest.search.controller;

import com.kakao.pretest.search.dto.Ranking;
import com.kakao.pretest.search.event.QueryEventPublisher;
import com.kakao.pretest.search.service.RankService;
import com.kakao.pretest.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class LocalSearchController {

    private final SearchService searchService;
    private final RankService rankService;

    private final QueryEventPublisher queryEventPublisher;

    @GetMapping("/api/search/local")
    public List<String> search(
            @Valid @NotBlank @RequestParam(value = "query") final String query) {
        queryEventPublisher.publishEvent(query); // 조회수 증가 처리를 위한 Event 발행
        return searchService.searchKeyword(query);
    }

    @GetMapping("/api/query/ranking")
    public List<Ranking> getRanking() {
        return rankService.getRanking();
    }

}
