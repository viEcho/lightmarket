package com.market.business.vo;

import com.market.business.enums.BaseEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * TODO 添加注释
 *
 * @author: echo
 * @date: 2026/1/21
 */
@Setter
@Getter
public class Option<V> implements Serializable {

    public V code;
    public String desc;

    public Option() {
    }

    public Option(V code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static <T, E> Function<T, Option<E>> converter(Function<? super T, E> valueMapper, Function<? super T, String> labelMapper) {
        return t -> new Option<>(valueMapper.apply(t), labelMapper.apply(t));
    }

    // 这里封装一个方法，将枚举转换成List<Option<V>>
    
    /**
     * 通用枚举转换方法
     */
    public static <E extends BaseEnum<T>, T> List<Option<T>> fromEnum(Class<E> enumClass) {
        E[] enumConstants = enumClass.getEnumConstants();
        List<Option<T>> options = new ArrayList<>();

        for (E enumConstant : enumConstants) {
            options.add(new Option<>(enumConstant.getCode(), enumConstant.getDesc()));
        }

        return options;
    }


}
