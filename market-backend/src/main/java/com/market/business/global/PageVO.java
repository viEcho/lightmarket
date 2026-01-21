package com.market.business.global;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @description: 分页实体
 * @author: echo
 * @date: 2022/5/18
 */
@Data
@Schema(description = "分页结果")
public class PageVO<T> {

    @Schema(description = "当前页码", example = "1")
    private int num;

    @Schema(description = "每页大小", example = "6")
    private int size;

    @Schema(description = "总数据量", example = "10")
    private int total;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "扩展数据")
    private Object ext;

    @Schema(description = "是否有下一页（用于前端滚动加载）", example = "true")
    private boolean nextPage = true;

    public PageVO(){

    }

    public PageVO(int num, int size, int total) {
        this.num = num;
        this.size = size;
        this.setTotal(total); // 使用 setTotal 方法自动计算 nextPage
    }

    public void setList(List<?> pageList, Class<T> elementType) {
        this.list = JSONUtil.toList(JSONUtil.parseArray(pageList), elementType);
    }

    @SuppressWarnings("unchecked")
    public void setPageList(List<?> pageList) {
        this.list = (List<T>) pageList;
    }

    @SuppressWarnings("unchecked")
    public PageVO<T> setList(List<T> pageList) {
        this.list = pageList;
        return this;
    }

    /**
     * 设置总数据量，并自动计算是否有下一页
     */
    public void setTotal(int total) {
        this.total = total;
        if (total <= 0 || size <= 0) {
            this.nextPage = false;
        } else {
            int lastPageNum = total % size == 0 ? total / size : total / size + 1;
            this.nextPage = this.num < lastPageNum;
        }
    }

    /**
     * 设置当前页码，并重新计算是否有下一页
     */
    public void setNum(int num) {
        this.num = num;
        // 如果 total 已经设置，重新计算 nextPage
        if (this.total > 0) {
            setTotal(this.total);
        }
    }

    /**
     * 设置每页大小，并重新计算是否有下一页
     */
    public void setSize(int size) {
        this.size = size;
        // 如果 total 已经设置，重新计算 nextPage
        if (this.total > 0) {
            setTotal(this.total);
        }
    }

    /**
     * 计算总页数
     */
    public int getTotalPages() {
        if (total <= 0 || size <= 0) {
            return 0;
        }
        return (total + size - 1) / size;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPreviousPage() {
        return this.num > 1;
    }

    /**
     * 是否有下一页（与 nextPage 相同）
     */
    public boolean hasNextPage() {
        return this.nextPage;
    }
}
