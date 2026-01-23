package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * @description: 统一返回封装
 */
@Data
@Schema(name = "全局的统一返回结果")
public class Result<R> implements Serializable {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "返回状态码")
    private Integer code;

    @Schema(description = "返回消息")
    private String msg;

    @Schema(description = "返回的数据")
    private R data;

    @Schema(description = "返回的扩展数据")
    private Object ext;

    public Result() {
        this.success = ResponseCodeEnum.SUCCESS.getSuccess();
        this.code = ResponseCodeEnum.SUCCESS.getCode();
        this.msg = ResponseCodeEnum.SUCCESS.getDesc();
    }

    public Result(Boolean success, Integer code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public Result(Boolean success, Integer code, String msg, R data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(R data) {
        this.success = ResponseCodeEnum.SUCCESS.getSuccess();
        this.code = ResponseCodeEnum.SUCCESS.getCode();
        this.msg = ResponseCodeEnum.SUCCESS.getDesc();
        this.data = data;
    }

    public Result(ResponseCodeEnum responseCodeEnum) {
        this.success = responseCodeEnum.getSuccess();
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDesc();
    }

    public Result(ResponseCodeEnum responseCodeEnum, R data) {
        this.success = responseCodeEnum.getSuccess();
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDesc();
        this.data = data;
    }

    /**
     * 成功返回（无数据）
     */
    public static <R> Result<R> success() {
        return new Result<>(ResponseCodeEnum.SUCCESS);
    }

    /**
     * 成功返回（带数据）
     */
    public static <R> Result<R> success(R data) {
        return new Result<>(ResponseCodeEnum.SUCCESS, data);
    }

    /**
     * 失败返回
     */
    public static <R> Result<R> fail() {
        return new Result<>(ResponseCodeEnum.UNKNOWN_REASON_ERROR);
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <R> Result<R> fail(String msg) {
        return new Result<>(
                ResponseCodeEnum.UNKNOWN_REASON_ERROR.getSuccess(),
                ResponseCodeEnum.UNKNOWN_REASON_ERROR.getCode(),
                msg
        );
    }

    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <R> Result<R> fail(Integer code, String msg) {
        return new Result<>(
                false,
                code,
                msg
        );
    }

    /**
     * 根据枚举返回
     */
    public static <R> Result<R> build(ResponseCodeEnum responseCodeEnum) {
        return new Result<>(responseCodeEnum);
    }

    /**
     * 根据枚举返回（带数据）
     */
    public static <R> Result<R> build(ResponseCodeEnum responseCodeEnum, R data) {
        return new Result<>(responseCodeEnum, data);
    }



    /**
     * 链式调用设置success
     */
    public Result<R> success(Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * 链式调用设置msg
     */
    public Result<R> msg(String msg) {
        this.msg = msg;
        return this;
    }

    /**
     * 链式调用设置code
     */
    public Result<R> code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * 链式调用设置data
     */
    public Result<R> data(R data) {
        this.data = data;
        return this;
    }

    /**
     * 链式调用设置ext
     */
    public Result<R> ext(Object ext) {
        this.ext = ext;
        return this;
    }
}
