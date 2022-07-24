package com.kakao.pretest.global.utils;

public class StringUtils {
    public static String removeTag(final String text) {
        if(text == null) return "";  // NPE 방지
        return text.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>","");
    }
}
