package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @description: 统一返回封装
 * @author: echo
 * @date: 2021/5/22
 */
@Data
@Schema(name = "全局的统一返回结果")
@AllArgsConstructor
public class Result {
    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "返回状态码")
    private Integer code;

    @Schema(description = "返回消息")
    private String msg;

    @Schema(description = "返回的数据！")
    private Object data;

    public Result() {
        this.success = ResponseCodeEnum.SUCCESS.getSuccess();
        this.code = ResponseCodeEnum.SUCCESS.getCode();
        this.msg= ResponseCodeEnum.SUCCESS.getDesc();
    }

    public Result(Object data){
        this.success = ResponseCodeEnum.SUCCESS.getSuccess();
        this.code = ResponseCodeEnum.SUCCESS.getCode();
        this.msg = ResponseCodeEnum.SUCCESS.getDesc();
        this.data = data;
    }

    public Result(int code,String msg,Object data){
        this.success = ResponseCodeEnum.SUCCESS.getSuccess();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public  Result (ResponseCodeEnum responseCodeEnum){
        this.success = responseCodeEnum.getSuccess();
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDesc();
    }

    public  Result (ResponseCodeEnum responseCodeEnum,Object data){
        this.success = responseCodeEnum.getSuccess();
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDesc();
        this.data = data;
    }

    /**
     * @description: ok返回
     * @author: echo
     * @date: 2021/5/22
     * @param:
     * @return: com.vbills.modules.common.Result
     */
    public static Result success(){
        return new Result()
        .success(ResponseCodeEnum.SUCCESS.getSuccess())
        .code(ResponseCodeEnum.SUCCESS.getCode())
        .msg(ResponseCodeEnum.SUCCESS.getDesc());
    }



    /**
     * @description: error返回
     * @author: echo
     * @date: 2021/5/22
     * @param:
     * @return: com.vbills.modules.common.Result
     */
    public static Result fail(){
        return new Result()
        .success(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getSuccess())
        .code(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getCode())
        .msg(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getDesc());
    }


    public Result success(Boolean success){
        this.setSuccess(success);
        return this;
    }
    public Result msg(String msg){
        this.setMsg(msg);
        return this;
    }
    public Result code(Integer code){
        this.setCode(code);
        return this;
    }
    public Result data(Object obj){
        this.setData(obj);
        return this;
    }

}
