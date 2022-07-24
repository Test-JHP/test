package com.kakao.pretest.search.data.dto;

import com.kakao.pretest.global.utils.AddressUtils;
import com.kakao.pretest.global.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
