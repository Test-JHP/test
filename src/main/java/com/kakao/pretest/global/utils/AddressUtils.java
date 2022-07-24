package com.kakao.pretest.global.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AddressUtils {

    // 주소 정제
    public static String translate(final String text) {
        if(text == null) return ""; // NPE 방지
        String[] split = text.replaceAll("(광역시|특별시|특별자치도)", "")
                .replace("경기도", "경기")
                .replace("전라북도", "전북")
                .replace("전라남도", "전남")
                .replace("경상북도", "경북")
                .replace("경상남도", "경남")
                .replace("충청북도", "충북")
                .replace("충청남도", "충남")
                .split(" ");
        return Arrays.stream(split)
                .limit(4)   // 상세 주소는 삭제, (시 군 구 동 번지 까지만 비교)
                .collect(Collectors.joining(" "));
    }
}
