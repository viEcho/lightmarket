package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import com.market.business.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

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
        return Result.fail(e.getMessage());
    }

    /**
     * 处理反射调用目标异常
     */
    @ExceptionHandler(InvocationTargetException.class)
    public Result<Void> handleInvocationTargetException(InvocationTargetException e) {
        Throwable target = e.getTargetException();

        // 如果是真正的业务异常
        if (target instanceof GlobalException ge) {
            ExceptionUtil.getStackMessage(ge);
            return Result.fail(ge.getCode(), ge.getMessage());
        }

        if (target instanceof IllegalArgumentException) {
            ExceptionUtil.getStackMessage(target);
            return Result.build(ResponseCodeEnum.VALIDATE_ERROR);
        }

        ExceptionUtil.getStackMessage(target);
        return Result.fail(target.getMessage());
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> error(BindException e) {
        ExceptionUtil.getStackMessage(e);
        String errorMsg = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return Result.fail(errorMsg);
    }

    /**
     * 处理@RequestBody参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> error(MethodArgumentNotValidException e) {
        ExceptionUtil.getStackMessage(e);
        String errorMsg = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
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

    /**
     * 数据完整性异常（如外键约束、唯一键冲突等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> error(DataIntegrityViolationException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.fail("数据操作失败，请检查数据完整性约束");
    }

    /**
     * SQL 异常
     */
    @ExceptionHandler(SQLException.class)
    public Result<Void> error(SQLException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.build(ResponseCodeEnum.BAD_SQL_GRAMMAR);
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> error(IllegalArgumentException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.fail(e.getMessage());
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> error(NullPointerException e) {
        ExceptionUtil.getStackMessage(e);
        return Result.fail("系统内部错误：空指针异常");
    }
}
