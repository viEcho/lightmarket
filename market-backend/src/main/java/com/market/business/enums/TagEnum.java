package com.market.business.enums;


import lombok.Getter;


/**
 * 标签枚举
 *
 * @author: echo
 * @date: 2026/1/21
 */
@Getter
public enum  TagEnum implements BaseEnum<Integer>{

    Crypto(1, "Crypto"),
    Technology(2, "Technology"),
    Politics(3, "Politics"),
    Sports(4, "Sports"),
    Finance(5, "Finance"),
    Entertainment(6, "Entertainment"),
    Other(7, "Other"),
    ;

    private final Integer code;
    private final String desc;

    TagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
