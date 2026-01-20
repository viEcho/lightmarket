package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义全局异常
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "全局异常")
public class GlobalException extends RuntimeException{

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "异常信息")
    private String msg;

    public GlobalException(){
        super();
    }

    /**
     * 接收自定传递的状态码和异常消息
     */
    public GlobalException(Integer code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }
    /**
     * 接收自定传递的状态码和异常消息
     */
    public GlobalException(String msg){
        super(msg);
        this.code = ResponseCodeEnum.UNKNOWN_REASON_ERROR.getCode();
        this.msg = msg;
    }

    /**
     * 接收枚举类型参数
     */
    public GlobalException(ResponseCodeEnum responseCodeEnum){
        super(responseCodeEnum.getDesc());
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDesc();
    }

    @Override
    public String toString() {
        return "GlobalException{code=" + code +" message=" + this.getMessage() +"}";
    }
}
