package com.kakao.pretest.search.dto;

import com.kakao.pretest.global.utils.AddressUtils;
import com.kakao.pretest.global.utils.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
public class Result {

    private final List<Item> itemList;

    @ToString
    @Getter
    public static class Item {

        private final String engineType;
        private final int priority;
        private final String title;
        private final String address;

        @Builder
        public Item (String engineType, int priority, String title, String address) {
            this.engineType = engineType;
            this.priority = priority;
            this.title = StringUtils.removeTag(title); // Tag 제거
            this.address = AddressUtils.translate(address); // 주소 정제
        }

        /**
         * 일치 여부 확인
         * 공백 제거 후, 주소 Hash 값 비교
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
        if (itemList == null || itemList.size() == 0) return "NONE";
        return itemList.iterator().next().engineType;
    }

    public int getPriority() {
        if (itemList == null || itemList.size() == 0) return Integer.MAX_VALUE;
        return itemList.iterator().next().priority;
    }

}
