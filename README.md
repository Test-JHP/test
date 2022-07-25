# 사전 과제 - 장소 검색 API

## 목차

- [개발 환경](#개발-환경)
- [빌드 및 실행 방법](#빌드-및-실행-방법)
- [기능 요구 사항](#기능-요구-사항)
- [기술 제약 사항](#기술-제약-사항)
- [구현 내용 및 해결 전략](#구현-내용-및-해결-전략)
- [기술적 요구 사항 답변](#기술적-요구-사항-답변)
- [테스트 방법](#테스트-방법)

## 개발 환경

- 기본 환경
    - IDE : IntelliJ IDEA Ultimate Ver.
    - OS :  macOS
    - GIT
- Application
    - JAVA 11
    - Spring boot 2.7.1
    - WebClient
    - Redis (Embedded)
    - Gradle
    - Junit5

## 빌드 및 실행 방법

### 터미널 환경

- java, git 은 설치 되어 있어야 한다.

```bash
$ git clone https://github.com/Black-knight-P/k-cafe-hw.git
$ cd k-cafe-hw
$ ./gradlew clean build
$ java -jar ./build/libs/pretest-0.0.1-SNAPSHOT.jar
```

- 접속 Base URI: `http://localhost:8080`

## 기능 요구 사항

- 개발 API 목록
    1. `장소 검색 서비스 API`
    2. `검색 키워드 목록 API`
- 각 API 별 요구 사항
    1. `장소 검색 서비스 API`
        1. 카카오 검색 API, 네이버 검색 API - 를 통해 각각 최대 5개씩, 총 10개의 키워드 관련 장소를 검색합니다. (특정 서비스 검색 결과가 5개 이하면 최대한 총 10개에 맞게 적용)
        2. 카카오 장소 검색 API의 결과를 기준으로 두 API 검색 결과에 동일하게 나타나는 문서(장소)가 상위에 올 수 있도록 정렬해주세요.
    2. `검색 키워드 목록 API`
        1. 사용자들이 많이 검색한 순서대로, 최대 10개의 검색 키워드 목록을 제공합니다.
        2. 키워드 별로 검색된 횟수도 함께 표기해 주세요.
        3. 비즈니스 로직은 모두 서버에서 구현합니다.

## 기술 제약 사항

- Java 8 이상 또는 Kotlin 언어로 구현
- Spring Boot 사용
- Gradle 또는 Maven 기반 프로젝트
- 저장소가 필요할 경우 자유롭게 선택( 예: h2, in-memory 자료구조 등 )
- 외부 라이브러리 및 오픈소스 사용 가능
- 구현한 API 테스트 방법 작성

## 구현 내용 및 해결 전략

### 1. 장소 검색 서비스 API

- 해결 전략
    - 독립적인 N개의 외부 API를 호출 해야 되므로, Spring Webflux 기반 WebClient Library를 활용하여, 비동기로 구현하였다.
    - Google 및 기타 외부 API 추가 요건에 개발 공수를 최소화 하기 위해 Api 처리 Engine을 추상화 하여 기존 소스 수정 없이 새로운 코드 추가만으로 구현하였다.
    - 검색어 횟수 처리 기능과의 결합도를 낮추기 위해 ApplicationEventPublisher로 Event를 발행하여 별도의 Thread에서 검색어 횟수에 대한 증가 처리로 구현하였다.

- Request

```bash
http://localhost:8080/api/search/local?query={곱창}
```

```bash
GET /api/search/local?query={query} HTTP/1.1
```

- Response

```json
[
  "해성막창집 본점",
  "세광양대창 교대본점",
  "백화양곱창 6호",
  "곱 마포점",
  "별미곱창 본점",
  "평양집",
  "청어람 망원점"
]
```


- LocalSearchController.java
```java
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
	...중략
}
```
- 검색 API 호출과 검색 조회수 처리의 결합도를 낮추기 위해 ApplicationEventPublisher로 이벤트 처리
- QueryEvent를 발행하여, 검색 서비스와는 별도의 Thread로 Redis에 스코어링한다.


- LocalSearchEngine (Interface)

```java
public interface LocalSearchEngine {
    Mono<? extends KeywordSearchResponse> search(final String query);
}
```
- 확장과 유지 보수를 위해 KeywordSearchResponse 를 상속 받은 객체로 리턴 타입을 강제 하였다.


- KakaoLocalSearchEngine.java (Implements)

```java
@Slf4j
@Component
public class KakaoLocalSearchEngine implements LocalSearchEngine {

    private final String KAKAO_ACCESS_KEY;
    private final WebClient webClient;

    public KakaoLocalSearchEngine (@Value("${kakao-access-key}") String kakaoAccessKey,
                                   WebClient webClient) {
        this.KAKAO_ACCESS_KEY = kakaoAccessKey;
        this.webClient = webClient;
    }

    @Override
    public Mono<KakaoKeywordSearchResponse> search(final String query) {
        log.debug("Start Search {}", this.getClass());
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.add("Authorization", "KakaoAK " + KAKAO_ACCESS_KEY);
                })
                .retrieve()
                .bodyToMono(KakaoKeywordSearchResponse.class)
                .doOnSuccess(kakaoKeywordSearchResponse -> log.debug("Complete API : {}", kakaoKeywordSearchResponse))
                .doOnError(throwable -> log.error("{}", throwable.getMessage()))
                .onErrorReturn(KakaoKeywordSearchResponse.builder().build()) // 에러 발생시, Empty Mono<KeywordSearchResponse> 리턴
                ;
    }

}
```
- 검색 Engine은 WebClient Liberary를 활용하여, 비동기로 외부 Api를 호출하고, 결과가 돌아올 Mono 객체를 Return 한다.
- 4xx, 5xx Error 발생시 비어있는 결과가 담겨있는 Mono 객체를 Return하여 전체 서비스에 지장이 없도록 구현하였다.


- SearchService.java
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class SearchService {

    // Bean 으로 등록된 LocalSearchEngine의 구현체들을 DI
    private final List<LocalSearchEngine> localSearchEngineList; 

    @Cacheable("QueryKeyword")
    public List<String> searchKeyword(final String query) {
        // 비동기로 외부 API 호출 처리
        final var monoList = localSearchEngineList.stream()
                .map(localSearchEngine -> localSearchEngine.search(query))
                .collect(Collectors.toList());

				// Mono.zip Method 를 통해 비동기 호출 결과들을 Merge 한다.
        final var optionalResults = Mono.zip(monoList,
                objects -> Arrays.stream(objects)
                        .map(o -> (KeywordSearchResponse) o)
                        .map(KeywordSearchResponse::toResult)
                        .collect(Collectors.toList()))
                .blockOptional();

				// API 명세에 맞도록 결과 Return Method
        return optionalResults.map(this::makeSearchApiResponse).orElse(Collections.emptyList());
    }
	...중략
}
```
- LocalSearchEngine의 Impl들을 통해 비동기로 호출된 API들을 해당 서비스의 결과를 Merge한다.
- 반응성과 효율성을 위해 @Cacheable을 활용하였다.


- SearchService.java
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class SearchService {

    // Bean 으로 등록된 LocalSearchEngine의 구현체들을 DI
    private final List<LocalSearchEngine> localSearchEngineList;

		...중략
	 
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
```

- Stream API와 여러 자료구조를 활용하여, 주어진 요건에 맞게 결과를 리턴하는 Method를 개발하였다.
- 구현의 상세 내용은 Code 내 주석으로 대체한다.


- Result.java API 결과를 처리하기 위한 공통 객체
```java
@Data
@AllArgsConstructor
@Builder
public class Result {

    private List<Item> itemList;

    @Data
    public static class Item {

        private String engineType;
        private int priority;
        private String title;
        private String address;

        @Builder
        public Item (String engineType, int priority, String title, String address) {
            this.engineType = engineType;
            this.priority = priority;
            this.title = StringUtils.removeTag(title); // Tag 제거
            this.address = AddressUtils.translate(address); // 주소 정제
        }

        /**
         * 일치 여부 확인
         * 1. 공백 제거 후, 주소 Hash 값 비교
         */
        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;

            if (this.getClass() != o.getClass())
                return false;

            return this.hashCode() == o.hashCode(); // Override Hashcode 로 일치 여부 비교
        }

        @Override
        public int hashCode() {
            return address.replace(" ", "").hashCode(); // 공백 제거후 Hash 값 생성
        }

        public int getPriority() {
            return this.priority;
        }
    }

    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public String getEngineInfo() {
        if (itemList.size() == 0) return "NONE";
        return itemList.iterator().next().engineType;
    }

    /**
     * 내부 Item 의 우선 순위 Return
     */
    public int getPriority() {
        if (itemList.size() == 0) return Integer.MAX_VALUE;
        return itemList.iterator().next().priority;
    }

}
```
- Item의 equals와 hashCode를 Override하여, Stream API의 distinct()와 Set과 Map을 통한 중복 제거를 용이하게 구현하였다.
- 검색 결과의 일치 여부는 주소 정보를 일부 정제하여, 일치 여부로 판단하였다.



### 2. 검색 키워드 목록 API

- 해결 전략
    - 검색 조회수는 빈번한 Update를 사용하기 때문에 rdb는 사용하지 않았다.
    - 일괄로 조회수를 Count했다가 Rdb에 Update하는 방법도 있지만 동시성과 실시간 정확성을 염두하였다.
    - Redis의 Sorted Set 자료 구조를 활용하여 구현하였다.
    - 대량의 데이터가 들어갈 경우 입력시에 느려질 수 있으나 비동기 Event Listener를 활용하여 별도 Thread로 처리하여 입력시 느리다는 단점을 상쇄하였고, 조회시 매우 빠르다는 장점만을 활용하였다.

- Request

```bash
http://localhost:8080/api/search/local/ranking
```

```bash
GET /api/search/local/ranking HTTP/1.1 
```

- Response

```json
[
  {
    "query": "은행",
    "count": 58
  },
  {
    "query": "라면",
    "count": 13
  },
  {
    "query": "테스트",
    "count": 8
  },
  {
    "query": "곱창",
    "count": 8
  },
  {
    "query": "박지호",
    "count": 2
  }
]
```

- RankingService.java

```java
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
	 ...중략
}
```

- Redis zset 자료구조(Sorted set)에서 조회수 상위 10개를 가져와서 결과를 반환한다.

- QueryEventListener.java

```java
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
```

- 검색 서비스 호출시 발행된 QueryEvent를 수신하여 별도의 Thread로 검색 횟수 증가 서비스를 호출 한다.

- RankingService.java

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class RankService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String REDIS_QUERY_RANKING_KEY = "QUERY:RANKING";

	  ... 중략

    public void increaseCount(final String query) {
        try {
            // 검색어를 value 에 저장하고, score 를  +1
            redisTemplate.opsForZSet().incrementScore(REDIS_QUERY_RANKING_KEY, query,1);
        } catch (Exception e) {
            // 별도 재처리 프로세스 구현?
            log.error("REDIS QUERY RANKING_KEY ERROR {} ", e.getMessage());
        }
    }
}
```

- redis ZSet 오퍼레이션을 활용하여, 검색 Query Key의 Score(Value)를 1증가 시킨다.

## 기술적 요구 사항 답변

- 동시성 이슈가 발생할 수 있는 부분을 염두에 둔 설계 및 구현 (예시. 키워드 별로 검색된 횟수)
    - Single Thread 기반 Redis의 Atomic 보장을 활용해서 해당 이슈는 해결 하였다.
    - Sorted Set 자료 구조를 사용하여 O(Log n) 시간 복잡도로 검색어 상위 10개 추출이 가능하다.
    
- 카카오, 네이버 등 검색 API 제공자의 “다양한” 장애 발생 상황에 대한 고려
    - 두 API 호출이 서로 영향이 없도록 WebClient로 비동기로 호출 처리하였다.
    - WebClient에 API Error Handler를 추가하여 4xx,5xx 에러시 Empty 데이터를 반환하여 전체 로직은 흐르도록 처리 하였다.
    
- 구글 장소 검색 등 새로운 검색 API 제공자의 추가 시 변경 영역 최소화에 대한 고려
    - 추상화 된 LocalSeachEngine과 LocalSearchResponse를 Google API에 맞게 구현을 한다면, 기존 소스에는 변경 영역이 없이 기능 추가가 가능하다. LocalSearchEngine은 @Component 추가 하면 DI를 통해 SearchService에서 주입받아서 처리된다.
    
- 서비스 오류 및 장애 처리 방법에 대한 고려
    - Redis 장애시, 외부 API 호출에 대한 기능은 정상적으로 처리가 되도록 구현하였다.
    - 검색 횟수 처리는 Event Publisher와 Event Listener를 통해 별도의 비동기 Thread로 처리가 되므로 검색 서비스에는 영향이 없도록 구현되어있다.
    - 외부 API가 일부 장애 시 타 API의 정상처리에 영향이 없도록 독립적으로 처리되도록 구현하였다.
    - 다수의 비동기 호출에 대해서 Merge(Block) 처리시, 에러가 난 API에 대해서는 타 API의 결과로 보정되어 응답으로 처리한다.

- 대용량 트래픽 처리를 위한 반응성(Low Latency), 확장성(Scalability), 가용성(Availability)을 높이기 위한 고려
    - 반응성 : Async IO 기반 Http Library 사용, Caching, Redis Sorted Set을 활용한 데이터 처리
    - 확장성 : 인스턴스 추가가 수평적으로 확장이 가능하다. (단, Embedded Redis는 변경하여야 한다.)
    - 가용성 : 검색 API의 경우, 외부 서버 및 Redis 서버의 장애에도 Request에 대한 정상 응답은 보장한다.
  
- 지속적 유지 보수 및 확장에 용이한 아키텍처에 대한 설계
    - 확장이 용이하도록 검색 엔진을 추상화하여 추가 엔진 구현만으로 기존 소스의 수정 없이 유연한 외부 API 추가 확장이 가능하도록 구현 하였다.
    - 통합 & 단위 테스트 코드를 작성하여, 지속적 유지 보수가 가능하도록 구성하였다.
    

## 테스트 방법

1. Project 내, src/test/http/test-requests.http 파일을 이용한 API 테스트
2. cURL 
    
    ```bash
    # 장소 검색 서비스 API
    curl -G -X GET 'http://localhost:8080/api/search/local' --data-urlencode 'query=곱창'
    
    # 검색 키워드 목록 API 
    curl -X GET 'http://localhost:8080/api/search/local/ranking'
    ```
3. 통합 테스트 코드 실행은 Embedded Redis Config 영향으로 Windows PC 에서는 불가능 하다.