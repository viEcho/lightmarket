package com.market.business.enums;

import com.market.business.vo.Option;
import lombok.Getter;

import java.util.List;

/**
 * ai配置枚举
 *
 * @author: echo
 * @date: 2026/1/21
 */
@Getter
public enum AiEnum implements BaseEnum<Integer>{

    CHAT_GPT(1, "ChatGpt", ""),
    CLAUDE(2, "Claude",""),
    GEMINI(3,"Gemini",""),
    PRE_PLEXITY(4, "Preplexity",""),
    GROK(5, "Grok",""),
    WEN_XIN(6,"文心一言",""),
    TONG_YI(7,"通义千问",""),
    ZHI_PU(8,"智普清言",""),
    KIMI(9,"Kimi",""),
    XUN_FEI(10,"讯飞星火",""),
    ;

    private final Integer code;
    private final String desc;
    private final String api_key;

    AiEnum(Integer code, String desc, String apiKey) {
        this.code = code;
        this.desc = desc;
        api_key = apiKey;
    }
}
