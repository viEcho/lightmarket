package com.market.business.entity;

import java.util.Date;
import lombok.Data;

/**
 * TODO 请添加类注释
 *
 * @author viEcho
 * @date 2026/01/20
 */
@Data
public class TestT {
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 创建时间
     */
    private Date created_time;

    /**
     * 创建人
     */
    private String created_by;
}