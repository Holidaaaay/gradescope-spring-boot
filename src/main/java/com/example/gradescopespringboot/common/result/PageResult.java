package com.example.gradescopespringboot.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    /**
     * Current page data list
     */
    private List<T> list;

    /**
     * Total records across all pages
     */
    private Long total;

    /**
     * Current page number (1-based)
     */
    private Integer pageNum;

    /**
     * Page size
     */
    private Integer pageSize;

    /**
     * Total pages
     */
    private Integer pages;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }
}
