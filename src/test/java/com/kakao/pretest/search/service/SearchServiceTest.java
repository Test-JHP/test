package com.kakao.pretest.search.service;

import com.kakao.pretest.search.dto.Result;
import com.kakao.pretest.search.engine.LocalSearchEngine;
import com.kakao.pretest.search.engine.impl.KakaoLocalSearchEngine;
import com.kakao.pretest.search.engine.impl.NaverLocalSearchEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
        var kakaoItemList = new ArrayList<Result.Item>();
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("A 곱창").address("A 곱창").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("B 곱창").address("B 곱창").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("C 곱창").address("C 곱창").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("D 곱창").address("D 곱창").build());
        var kakaoResult = Result.builder().itemList(kakaoItemList).build();

        var naverItemList = new ArrayList<Result.Item>();
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("A 곱창").address("A 곱창").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("E 곱창").address("E 곱창").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("D 곱창").address("D 곱창").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("C 곱창").address("C 곱창").build());
        var naverResult = Result.builder().itemList(naverItemList).build();

        //mocking
        when(localSearchEngineList.stream()).thenReturn(List.of(kakaoLocalSearchEngine, naverLocalSearchEngine).stream());
        when(kakaoLocalSearchEngine.search(any())).thenReturn(Mono.just(kakaoResult));
        when(naverLocalSearchEngine.search(any())).thenReturn(Mono.just(naverResult));

        //when
        var list = searchService.searchKeyword("곱창");

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
        var kakaoItemList = new ArrayList<Result.Item>();
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("카카오뱅크").address("카카오뱅크").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("우리은행").address("우리은행").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("국민은행").address("국민은행").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("부산은행").address("부산은행").build());
        kakaoItemList.add(Result.Item.builder().engineType("KAKAO").priority(1).title("새마을금고").address("새마을금고").build());
        var kakaoResult = Result.builder().itemList(kakaoItemList).build();

        var naverItemList = new ArrayList<Result.Item>();
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("카카오뱅크").address("카카오뱅크").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("부산은행").address("부산은행").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("하나은행").address("하나은행").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("국민은행").address("국민은행").build());
        naverItemList.add(Result.Item.builder().engineType("NAVER").priority(2).title("기업은행").address("기업은행").build());
        var naverResult = Result.builder().itemList(naverItemList).build();

        //mocking
        when(localSearchEngineList.stream()).thenReturn(List.of(kakaoLocalSearchEngine, naverLocalSearchEngine).stream());
        when(kakaoLocalSearchEngine.search(any())).thenReturn(Mono.just(kakaoResult));
        when(naverLocalSearchEngine.search(any())).thenReturn(Mono.just(naverResult));

        //when
        var list = searchService.searchKeyword("은행");

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
