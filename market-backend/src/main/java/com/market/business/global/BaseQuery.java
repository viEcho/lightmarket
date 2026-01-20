package com.market.business.global;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础查询条件
 */
@Data
public class BaseQuery implements Serializable {

    private String userId;

    private int currentPage = 1;

    private int pageSize = 10;

    private String startTime;

    private String endTime;
}
