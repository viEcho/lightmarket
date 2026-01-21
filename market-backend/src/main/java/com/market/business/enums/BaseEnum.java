package com.market.business.enums;

/**
 * 枚举抽象
 * @author: echo
 * @date: 2026/1/21
 */
public interface BaseEnum<T> {
   T getCode();
   String getDesc();
}
