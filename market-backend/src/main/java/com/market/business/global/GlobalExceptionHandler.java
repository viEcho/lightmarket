package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import com.market.business.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理所有的Exception
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> error(Exception e) {
        ExceptionUtil.getStackMessage(e);
        return Result.fail();
    }


    /**
     * 处理入参错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> error(IllegalArgumentException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.build(ResponseCodeEnum.VALIDATE_ERROR);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> error(BindException e) {
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ExceptionUtil.getStackMessage(e);
        return Result.fail(errorMsg);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(GlobalException.class)
    public Result<Void> error(GlobalException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.<Void>fail()
                .msg(e.getMessage())
                .code(e.getCode());
    }

    /**
     * SQL语法异常
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public Result<Void> error(BadSqlGrammarException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.build(ResponseCodeEnum.BAD_SQL_GRAMMAR);
    }
}
