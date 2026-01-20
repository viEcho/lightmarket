package com.market.business.entity;

import java.util.Date;
import lombok.Data;

/**
 * 用户表
 *
 * @author viEcho
 * @date 2026/01/20
 */
@Data
public class User {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 对外用户唯一标识
     */
    private String uid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 逻辑删除标识
     */
    private Byte deleteFlag;

    /**
     * 添加时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
}