package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SliceView
 * @Description 内存分页片
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SliceView<T> {

    private long total;
    private long pageNo;
    private long pageSize;
    private List<T> records = new ArrayList<>();
}
