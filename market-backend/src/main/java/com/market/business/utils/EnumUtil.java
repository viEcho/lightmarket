package com.market.business.utils;

import com.market.business.enums.BaseEnum;
import com.market.business.vo.Option;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 枚举工具类
 *
 * @author echo
 * @date 2026/01/21
 */
public class EnumUtil {

    /**
     * 将逗号分隔的 code 字符串转换为 Option 列表
     *
     * @param codesStr 逗号分隔的 code 字符串，如 "1,2,3"
     * @param enumClass 枚举类
     * @return Option 列表
     */
    public static <T extends BaseEnum<Integer>, E extends Enum<E>> List<Option<Integer>> convertCodesToOptions(String codesStr, Class<T> enumClass) {
        if (StringUtils.isBlank(codesStr)) {
            return new ArrayList<>();
        }

        // 分割字符串并转换为 Option 列表
        return Arrays.stream(codesStr.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(codeStr -> {
                    try {
                        Integer code = Integer.valueOf(codeStr);
                        return findOptionByCode(enumClass, code);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 根据 code 从枚举类中查找对应的 Option
     */
    private static <T extends BaseEnum<Integer>, E extends Enum<E>> Option<Integer> findOptionByCode(Class<T> enumClass, Integer code) {
        if (!enumClass.isEnum()) {
            return null;
        }

        T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            return null;
        }

        for (T enumConstant : enumConstants) {
            if (enumConstant.getCode().equals(code)) {
                return new Option<>(code, enumConstant.getDesc());
            }
        }

        return null;
    }

    /**
     * 根据 code 获取枚举的描述
     */
    public static <T extends BaseEnum<Integer>, E extends Enum<E>> String getDescByCode(Class<T> enumClass, Integer code) {
        Option<Integer> option = findOptionByCode(enumClass, code);
        return option != null ? option.getDesc() : null;
    }
}
