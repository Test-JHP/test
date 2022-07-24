package com.kakao.pretest.search.service;

import com.kakao.pretest.search.data.KakaoLocalSearchEngine;
import com.kakao.pretest.search.data.LocalSearchEngine;
import com.kakao.pretest.search.data.NaverLocalSearchEngine;
import com.kakao.pretest.search.data.dto.KakaoKeywordSearchResponse;
import com.kakao.pretest.search.data.dto.NaverKeywordSearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @InjectMocks
    private SearchService searchService;

    @Mock
    private KakaoLocalSearchEngine kakaoLocalSearchEngine;
    @Mock
    private NaverLocalSearchEngine naverLocalSearchEngine;
    @Mock
    private List<LocalSearchEngine> localSearchEngineList;


    @Test
    public void 검색_API_후처리_테스트_정상_CASE_1() {

        //given
        KakaoKeywordSearchResponse.Item kakaoItem1 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem2 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem3 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem4 = new KakaoKeywordSearchResponse.Item();

        ReflectionTestUtils.setField(kakaoItem1, "title", "A 곱창");
        ReflectionTestUtils.setField(kakaoItem1, "address", "A 곱창");
        ReflectionTestUtils.setField(kakaoItem2, "title", "B 곱창");
        ReflectionTestUtils.setField(kakaoItem2, "address", "B 곱창");
        ReflectionTestUtils.setField(kakaoItem3, "title", "C 곱창");
        ReflectionTestUtils.setField(kakaoItem3, "address", "C 곱창");
        ReflectionTestUtils.setField(kakaoItem4, "title", "D 곱창");
        ReflectionTestUtils.setField(kakaoItem4, "address", "D 곱창");

        KakaoKeywordSearchResponse kakaoKeywordSearchResponse = KakaoKeywordSearchResponse.builder()
                .items(List.of(kakaoItem1, kakaoItem2, kakaoItem3, kakaoItem4))
                .build();

        NaverKeywordSearchResponse.Item naverItem1 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem2 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem3 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem4 = new NaverKeywordSearchResponse.Item();

        ReflectionTestUtils.setField(naverItem1, "title", "A 곱창");
        ReflectionTestUtils.setField(naverItem1, "address", "A 곱창");
        ReflectionTestUtils.setField(naverItem2, "title", "E 곱창");
        ReflectionTestUtils.setField(naverItem2, "address", "E 곱창");
        ReflectionTestUtils.setField(naverItem3, "title", "D 곱창");
        ReflectionTestUtils.setField(naverItem3, "address", "D 곱창");
        ReflectionTestUtils.setField(naverItem4, "title", "C 곱창");
        ReflectionTestUtils.setField(naverItem4, "address", "C 곱창");

        NaverKeywordSearchResponse naverKeywordSearchResponse = NaverKeywordSearchResponse.builder()
                .items(List.of(naverItem1, naverItem2, naverItem3, naverItem4))
                .build();

        //mocking
        when(localSearchEngineList.stream()).thenReturn(List.of(kakaoLocalSearchEngine, naverLocalSearchEngine).stream());
        when(kakaoLocalSearchEngine.search(any())).thenReturn(Mono.just(kakaoKeywordSearchResponse));
        when(naverLocalSearchEngine.search(any())).thenReturn(Mono.just(naverKeywordSearchResponse));

        //when
        List<String> list = searchService.searchKeyword("곱창");

        //then
        assertEquals(list.get(0), "A 곱창");
        assertEquals(list.get(1), "C 곱창");
        assertEquals(list.get(2), "D 곱창");
        assertEquals(list.get(3), "B 곱창");
        assertEquals(list.get(4), "E 곱창");

    }


    @Test
    public void 검색_API_후처리_테스트_정상_CASE_2() {

        //given
        KakaoKeywordSearchResponse.Item kakaoItem1 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem2 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem3 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem4 = new KakaoKeywordSearchResponse.Item();
        KakaoKeywordSearchResponse.Item kakaoItem5 = new KakaoKeywordSearchResponse.Item();

        ReflectionTestUtils.setField(kakaoItem1, "title", "카카오뱅크");
        ReflectionTestUtils.setField(kakaoItem1, "address", "카카오뱅크");
        ReflectionTestUtils.setField(kakaoItem2, "title", "우리은행");
        ReflectionTestUtils.setField(kakaoItem2, "address", "우리은행");
        ReflectionTestUtils.setField(kakaoItem3, "title", "국민은행");
        ReflectionTestUtils.setField(kakaoItem3, "address", "국민은행");
        ReflectionTestUtils.setField(kakaoItem4, "title", "부산은행");
        ReflectionTestUtils.setField(kakaoItem4, "address", "부산은행");
        ReflectionTestUtils.setField(kakaoItem5, "title", "새마을금고");
        ReflectionTestUtils.setField(kakaoItem5, "address", "새마을금고");

        KakaoKeywordSearchResponse kakaoKeywordSearchResponse = KakaoKeywordSearchResponse.builder()
                .items(List.of(kakaoItem1, kakaoItem2, kakaoItem3, kakaoItem4, kakaoItem5))
                .build();

        NaverKeywordSearchResponse.Item naverItem1 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem2 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem3 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem4 = new NaverKeywordSearchResponse.Item();
        NaverKeywordSearchResponse.Item naverItem5 = new NaverKeywordSearchResponse.Item();

        ReflectionTestUtils.setField(naverItem1, "title", "카카오뱅크");
        ReflectionTestUtils.setField(naverItem1, "address", "카카오뱅크");
        ReflectionTestUtils.setField(naverItem2, "title", "부산은행");
        ReflectionTestUtils.setField(naverItem2, "address", "부산은행");
        ReflectionTestUtils.setField(naverItem3, "title", "하나은행");
        ReflectionTestUtils.setField(naverItem3, "address", "하나은행");
        ReflectionTestUtils.setField(naverItem4, "title", "국민은행");
        ReflectionTestUtils.setField(naverItem4, "address", "국민은행");
        ReflectionTestUtils.setField(naverItem5, "title", "기업은행");
        ReflectionTestUtils.setField(naverItem5, "address", "기업은행");

        NaverKeywordSearchResponse naverKeywordSearchResponse = NaverKeywordSearchResponse.builder()
                .items(List.of(naverItem1, naverItem2, naverItem3, naverItem4, naverItem5))
                .build();

        //mocking
        when(localSearchEngineList.stream()).thenReturn(List.of(kakaoLocalSearchEngine, naverLocalSearchEngine).stream());
        when(kakaoLocalSearchEngine.search(any())).thenReturn(Mono.just(kakaoKeywordSearchResponse));
        when(naverLocalSearchEngine.search(any())).thenReturn(Mono.just(naverKeywordSearchResponse));

        //when
        List<String> list = searchService.searchKeyword("은행");

        //then
        assertEquals(list.get(0), "카카오뱅크");
        assertEquals(list.get(1), "국민은행");
        assertEquals(list.get(2), "부산은행");
        assertEquals(list.get(3), "우리은행");
        assertEquals(list.get(4), "새마을금고");
        assertEquals(list.get(5), "하나은행");
        assertEquals(list.get(6), "기업은행");

    }

}
