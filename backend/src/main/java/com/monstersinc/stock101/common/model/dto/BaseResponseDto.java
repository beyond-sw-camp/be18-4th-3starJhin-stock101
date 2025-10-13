package com.monstersinc.stock101.common.model.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
@ToString
public class BaseResponseDto<T> {
    protected final int code;

    protected final String message;

    protected final List<T> items;

    public BaseResponseDto(HttpStatus status, T item) {
        this.code = status.value();
        this.message = status.getReasonPhrase();
        this.items = Collections.singletonList(item);
    }

    protected BaseResponseDto(HttpStatus status, List<T> items) {
        this.code = status.value();
        this.message = status.getReasonPhrase();
        this.items = items;
    }
}