package com.market.business.global;

import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.util.List;

/**
 * @description: 分页实体
 * @author: echo
 * @date: 2022/5/18
 */
@Data
public class Page<T> {

    private int num;
    private int size;
    private int total;
    private List<T> list;
    private Object ext;

    public Page(){

    }

    public Page(int i, int size, int size1) {
        this.num = i;
        this.size = size;
        this.total = size1;
    }

    public void setList(List<?> pageList, Class<T> elementType) {
        this.list = JSONUtil.toList(JSONUtil.parseArray(pageList), elementType);
    }

    @SuppressWarnings("unchecked")
    public void setPageList(List<?> pageList) {
        this.list = (List<T>) pageList;
    }

    @SuppressWarnings("unchecked")
    public Page<T> setList(List<T> pageList) {
        this.list = pageList;
        return this;
    }
}
