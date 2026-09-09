package com.taskflow.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.function.Function;

/**
 * 统一分页响应结构，避免把 MyBatis-Plus 的 IPage 泄漏给前端。
 */
public record PageResult<T>(List<T> items, long total, long page, long size, long pages) {

    public static <S, T> PageResult<T> from(IPage<S> src, Function<S, T> mapper) {
        List<T> items = src.getRecords().stream().map(mapper).toList();
        return new PageResult<>(items, src.getTotal(), src.getCurrent(), src.getSize(), src.getPages());
    }
}
