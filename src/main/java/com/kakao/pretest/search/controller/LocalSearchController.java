package com.kakao.pretest.search.controller;

import com.kakao.pretest.search.data.QueryEventPublisher;
import com.kakao.pretest.search.dto.RankingDto;
import com.kakao.pretest.search.service.RankService;
import com.kakao.pretest.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LocalSearchController {

    private final SearchService searchService;
    private final RankService rankService;

    // 검색어 조회 횟수 증가 처리를 위한 Event Publisher
    private final QueryEventPublisher queryEventPublisher;

    @GetMapping("/api/search/local")
    public List<String> searchLocalKeyword(@RequestParam(value = "query") final String query) {
        queryEventPublisher.publishEvent(query); // 조회수 증가 처리를 위한 Event 발행
        return searchService.searchKeyword(query);
    }

    @GetMapping("/api/search/local/ranking")
    public List<RankingDto> getQueryRanking() {
        return rankService.getRanking();
    }




    @ExceptionHandler(value = MissingServletRequestParameterException.class )
    public ResponseEntity<Map<String, Object>> paramExceptionHandler() {
        var respMsg = new HashMap<String, Object>();
        respMsg.put("message", HttpStatus.BAD_REQUEST.getReasonPhrase());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(respMsg);
    }

    @ExceptionHandler(value = Exception.class )
    public ResponseEntity<Map<String, Object>> commonExceptionHandler() {
        var respMsg = new HashMap<String, Object>();
        respMsg.put("message", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value()).body(respMsg);
    }

}
