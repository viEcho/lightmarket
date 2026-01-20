package com.market.business.global;

import com.market.business.enums.ResponseCodeEnum;
import com.market.business.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理的所有的Exception
     */
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler({BindException.class})
    @ResponseBody
    public Result error(BindException e){
        e.printStackTrace();
        e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.fail();
    }

    /**
     * 处理自己写的统一异常
     */
    @ExceptionHandler({GlobalException.class})
    @ResponseBody
    public Result error(GlobalException e){
        log.error(ExceptionUtil.getStackMessage(e));//打印异常堆栈信息
        return Result.fail().msg(e.getMessage()).code(e.getCode());
    }

    /**
     * sql异常
     * 
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    @ResponseBody
    public Result error(BadSqlGrammarException e){
        e.printStackTrace();
        return new Result(ResponseCodeEnum.BAD_SQL_GRAMMAR);
    }
}
