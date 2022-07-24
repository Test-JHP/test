package com.kakao.pretest.search.service;

import com.kakao.pretest.search.data.LocalSearchEngine;
import com.kakao.pretest.search.data.dto.KeywordSearchResponse;
import com.kakao.pretest.search.data.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchService {

    // Bean 으로 등록된 LocalSearchEngine 구현체들을 DI
    private final List<LocalSearchEngine> localSearchEngineList;

    @Cacheable("QueryKeyword")
    public List<String> searchKeyword(final String query) {

        // 비동기로 외부 API 호출 처리
        final var monoList = localSearchEngineList.stream()
                .map(localSearchEngine -> localSearchEngine.search(query))
                .collect(Collectors.toList());

        // Mono.zip Method 를 통해 비동기 호출 결과들를 Merge 한다.
        final var blockOptional =
                Mono.zip(monoList, objects -> Arrays.stream(objects)
                        .map(o -> (KeywordSearchResponse) o)
                        .map(KeywordSearchResponse::toResult)
                        .collect(Collectors.toList()))
                .blockOptional();

        // API 명세에 맞도록 결과 Return Method
        return blockOptional.map(this::makeSearchApiResponse).orElse(Collections.emptyList());
    }

    /**
     * 예시1)
     * 키워드: 곱창
     * 카카오 결과: A곱창, B곱창, C곱창, D곱창
     * 네이버 결과: A곱창, E곱창, D곱창, C곱창
     * 결과값: A곱창, C곱창, D곱창, B곱창, E곱창
     *
     * 기본적으로 카카오 결과를 기준 순서로 사용합니다.
     * A, C, D는 카카오 결과 네이버 결과 모두 존재해서 상위로 정렬
     * B, E는 두 결과 중 하나의 결과에만 존재 (둘 중 하나에만 존재하는 경우, 카카오 결과를 우선 배치 후 네이버 결과 배치)
     */
    private List<String> makeSearchApiResponse(final List<Result> resultList) {

        final int maxItemCount = 5; // 검색 기준 갯수, 해당 갯수에 따라 출력이 조정 된다.

        final var results = resultList.stream()
                .sorted(Comparator.comparingInt(Result::getPriority)) // 엔진 우선순위 정렬
                .collect(Collectors.toList());

        // 검색 기준 갯수(5개) 를 초과해서 조회된 결과들 중 남은 것들을 Queue 에 저장.
        var remainderManagementQueue = results.stream()
                .filter(result -> result.getItemCount() > maxItemCount) // 기준 갯수 초과 필터링
                .map(result -> Result.builder()
                        .itemList(result.getItemList().subList(maxItemCount, result.getItemList().size()))
                        .build())
                .flatMap(result -> result.getItemList().stream())
                .collect(Collectors.toCollection(LinkedList::new));  // 출력 순서 보장을 위해 Queue 에 그대로 넣는다.

        // 특정 서비스가 기준 갯수 이하 일때, 최대한 기준 갯수를 맞추는 로직
        var preprocessingMap = new HashMap<String, List<Result.Item>>(); // 결과 처리를 위한 전처리 Map
        for(final var result : results) {
            if (result.getItemCount() < maxItemCount) {  // 검색 갯수가 검색 기준 갯수(5개) 미만인 것들이 있다면,
                preprocessingMap.put(result.getEngineInfo(), result.getItemList()); // 일단 결과 Map 에 넣는다.
                if (remainderManagementQueue.isEmpty()) continue; // queue 가 비어있다면 부족한 갯수만큼 채워 줄 수가 없으므로 continue
                int count = maxItemCount - result.getItemCount(); // 부족한 갯수만큼 Queue 에서 빼서 추가한다.
                for (int i = 0; i < count; i++) {
                    if (remainderManagementQueue.isEmpty()) break; // queue 가 비어있다면 break
                    final var poll = remainderManagementQueue.poll();
                    if (preprocessingMap.containsKey(poll.getEngineType())) {
                        var items = preprocessingMap.get(poll.getEngineType());
                        items.add(poll);
                        preprocessingMap.put(poll.getEngineType(), items);
                    } else {
                        preprocessingMap.put(poll.getEngineType(), List.of(poll));
                    }
                }
            } else { // 검색 갯수가 검색 기준 갯수(5개)를 넘는다면, 기준 갯수 만큼 넣는다.
                if (preprocessingMap.containsKey(result.getEngineInfo())) { // 위에 if 문에서 먼저 들어갔을 경우도 있으니 전처리 맵에 들어가 있는지 Check 한다.
                    var items = preprocessingMap.get(result.getEngineInfo());
                    var resultItemList = result.getItemList().subList(0, maxItemCount);
                    resultItemList.addAll(items); // 원래 넣을 item 을 앞에 넣는다.
                    preprocessingMap.put(result.getEngineInfo(), resultItemList);
                } else { // 결과 맵에 포함되어있지 않는다면, 5개를 바로 넣는다.
                    preprocessingMap.put(result.getEngineInfo(), result.getItemList().subList(0, maxItemCount));
                }
            }
        }

        // 카카오 제외 나머지 Set (카카오 외에 중복은 따로 관리를 하지 않는다.)
        var excludeKakaoSet = preprocessingMap.values()
                .stream()
                .filter(items -> !items.isEmpty())
                .filter(items -> !"KAKAO".equals(items.iterator().next().getEngineType()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var completeList = new ArrayList<Result.Item>();    // 최종 결과 처리용 List
        final var kakaoItemList = preprocessingMap.get("KAKAO"); // 카카오 API 결과 내역
        if (kakaoItemList != null && !kakaoItemList.isEmpty()) {   // 카카오 API 결과가 비어있는게 아니라면, 중복을 체크 한다.
            var duplicateList = new ArrayList<Result.Item>();      // 카카오 중복 Item List
            var noneDuplicateList = new ArrayList<Result.Item>();  // 중복이 아닌 Item List
            for (final var kakaoItem : kakaoItemList) {
                if (excludeKakaoSet.contains(kakaoItem)) { // 카카오 검색 결과와 중복이라면,
                    log.debug("Duplicate Item : {}", kakaoItem);
                    duplicateList.add(kakaoItem);          // 중복 List 에 add
                    excludeKakaoSet.remove(kakaoItem);     // 기존 카카오 제외 set 에서 remove
                } else {                                   // 카카오 검색 결과와 중복이 아니라면,
                    noneDuplicateList.add(kakaoItem);      // 중복이 아닌 List 에 add
                }
            }
            completeList.addAll(duplicateList);
            completeList.addAll(noneDuplicateList);
        }

        completeList.addAll(excludeKakaoSet.stream()
                .sorted(Comparator.comparingInt(Result.Item::getPriority))
                .collect(Collectors.toList()));

        if (log.isDebugEnabled()) {
            completeList.forEach(item -> log.debug("Complete item list : {}", item));
        }

        return completeList.stream()
                .map(Result.Item::getTitle)
                .collect(Collectors.toList());
    }
}
