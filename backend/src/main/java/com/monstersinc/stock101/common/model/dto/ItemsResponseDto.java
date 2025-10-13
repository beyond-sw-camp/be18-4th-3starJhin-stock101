package com.monstersinc.stock101.common.model.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@ToString
public class ItemsResponseDto<T> extends BaseResponseDto<T> {
    private final int numOfRows;

    private final int totalCount;

    public ItemsResponseDto(HttpStatus status, List<T> items, int totalCount) {
        super(status, items);
        this.numOfRows = items.size();
        this.totalCount = totalCount;
    }

    // 전체 조회용 헬퍼
    public static <T> ItemsResponseDto<T> ofAll(HttpStatus status, List<T> items) {
        return new ItemsResponseDto<>(status, items, items.size());
    }
}