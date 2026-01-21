package com.market.business.utils;

import com.market.business.enums.ResponseCodeEnum;
import com.market.business.global.GlobalException;
import com.market.business.global.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 异常工具类
 */
@Slf4j
public class ExceptionUtil {
    /**
     * @description: 打印异常堆栈信息
     * @author: echo
     */
    @SuppressWarnings("all")
    public static String getStackMessage(Exception e){
        // 流
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            // 将出错的信息输出到 PrintWriter！
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (sw!=null){
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw!=null){
                pw.close();
            }
        }
        assert sw != null;
        String msg = sw.toString();
        log.error("occur error:{}", msg);
        return msg;
    }
    /**
     * @description: 自定义异常封装
     * 增加sql异常自定义返回 且截取Cause前的异常信息返回
     * @author: echo
     */
    public static <R> void checkResponse(Exception e, Result<R> vo){
        vo.setSuccess(false);
        vo.setCode(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getCode());
        vo.setMsg(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getDesc());
        if(e instanceof GlobalException){
            vo.setCode(((GlobalException) e).getCode());
            vo.setMsg(((GlobalException) e).getMsg());
        }else if(e instanceof SQLException){
            vo.setCode(ResponseCodeEnum.BAD_SQL_GRAMMAR.getCode());
            vo.setMsg(ResponseCodeEnum.BAD_SQL_GRAMMAR.getDesc());
        }else if(e instanceof RuntimeException){
            String message = e.getMessage();
            if(!StringUtils.isEmpty(message) && message.contains("Cause")){
                message = message.substring(0,message.indexOf("Cause"));
            }
            vo.setMsg(message);
        }
    }

    /**
     * @description: 非空断言
     * @author: echo
     */
    public static void assertNotNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @description: 非空或非空字符串断言
     * @author: echo
     */
    public static void assertNotBlank(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }else if(Objects.equals(0,object.toString().length())){
            throw new IllegalArgumentException(message);
        }
    }
}
