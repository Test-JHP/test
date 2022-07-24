package com.kakao.pretest.search.controller;

import com.kakao.pretest.search.service.RankService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalSearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RankService rankService;

    @Test
    @DisplayName("검색 키워드 목록 API 테스트 정상")
    public void 검색_키워드_목록_API_테스트_정상() throws Exception {
        // given
        for (int i = 0 ;i < 10; i++)
            rankService.increaseCount("박지호");
        for (int i = 0 ;i < 20; i++)
            rankService.increaseCount("곱창");
        for (int i = 0 ;i < 30; i++)
            rankService.increaseCount("은행");

        mvc.perform(
                get("/api/search/local/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].query").value("은행"))
                .andExpect(jsonPath("$.[0].count").value(30))
                .andExpect(jsonPath("$.[1].query").value("곱창"))
                .andExpect(jsonPath("$.[1].count").value(20))
                .andExpect(jsonPath("$.[2].query").value("박지호"))
                .andExpect(jsonPath("$.[2].count").value(10))
        ;
    }


    @Test
    @DisplayName("장소 검색 서비스 API 테스트 정상")
    public void 장소_검색_서비스_API_조회_테스트_정상() throws Exception {
        mvc.perform(
                get("/api/search/local")
                        .queryParam("query", "곱창"))
                .andExpect(status().isOk());
    }

}
