package com.kakao.pretest.search.event;

import com.kakao.pretest.search.dto.QueryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class QueryEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(final String query) {
        applicationEventPublisher.publishEvent(QueryEvent.builder().query(query).build());
    }

}
