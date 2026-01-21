package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import com.market.business.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;

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
        log.error("系统异常", e);
        ExceptionUtil.getStackMessage(e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(InvocationTargetException.class)
    public Result<Void> handleInvocationTargetException(InvocationTargetException e) {
        Throwable target = e.getTargetException();

        // 如果是真正的业务异常
        if (target instanceof GlobalException ge) {
            log.error("业务异常", ge);
            ExceptionUtil.getStackMessage(e);
            return Result.fail(ge.getCode(),ge.getMessage());
        }

        if (target instanceof IllegalArgumentException iae) {
            log.error("参数异常(反射): {}", iae.getMessage(), iae);
            ExceptionUtil.getStackMessage(e);
            return Result.build(ResponseCodeEnum.VALIDATE_ERROR);
        }
        return Result.fail(e.getMessage());
    }


    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> error(BindException e) {
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("参数绑定失败: {}", errorMsg, e);
        ExceptionUtil.getStackMessage(e);
        return Result.fail(errorMsg);
    }

    /**
     * 处理@RequestBody参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> error(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("参数校验失败: {}", errorMsg, e);
        ExceptionUtil.getStackMessage(e);
        return Result.fail(errorMsg);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(GlobalException.class)
    public Result<Void> error(GlobalException e) {
        log.error("业务异常: {}", e.getMessage(), e);
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
        log.error("SQL语法异常", e);
        ExceptionUtil.getStackMessage(e);
        return Result.build(ResponseCodeEnum.BAD_SQL_GRAMMAR);
    }
}
