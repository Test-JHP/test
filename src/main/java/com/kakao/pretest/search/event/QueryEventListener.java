package com.kakao.pretest.search.event;

import com.kakao.pretest.search.dto.QueryEvent;
import com.kakao.pretest.search.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryEventListener {

    private final RankService rankService;

    @Async
    @EventListener
    public void queryEventListener(final QueryEvent queryEvent) {
        log.debug("Increase Query Count : {} ", queryEvent);
        rankService.increaseCount(queryEvent.getQuery());
    }


}
