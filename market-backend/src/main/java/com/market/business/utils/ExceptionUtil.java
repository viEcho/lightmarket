package com.market.business.utils;

import com.market.business.enums.ResponseCodeEnum;
import com.market.business.global.GlobalException;
import com.market.business.global.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 异常工具类
 */
@Slf4j
public class ExceptionUtil {
    /**
     * @description: 打印异常堆栈信息（优化版，显示类、方法、行号）
     * @author: echo
     */
    @SuppressWarnings("all")
    public static String getStackMessage(Throwable e) {
        if (e == null) {
            return "Unknown exception";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append("【异常类型】").append(e.getClass().getName()).append("\n");
        sb.append("【异常信息】").append(e.getMessage()).append("\n");

        // 获取堆栈跟踪
        StackTraceElement[] stackTrace = e.getStackTrace();

        // 找到第一个非 Java/JDK 内部的类（即业务代码）
        int businessCodeIndex = -1;
        for (int i = 0; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            // 跳过 JDK、反射、Spring 等框架类
            if (!className.startsWith("java.") &&
                    !className.startsWith("javax.") &&
                    !className.startsWith("sun.") &&
                    !className.startsWith("com.sun.") &&
                    !className.startsWith("org.springframework.") &&
                    !className.startsWith("org.apache.") &&
                    !className.startsWith("com.baomidou.") &&
                    !className.startsWith("jdk.internal.")) {
                businessCodeIndex = i;
                break;
            }
        }

        // 找到触发异常的 SQL 语句（如果是 SQL 异常）
        if (e instanceof java.sql.SQLException || e.getCause() instanceof java.sql.SQLException) {
            Throwable sqlException = e instanceof java.sql.SQLException ? e : e.getCause();
            sb.append("【SQL 异常】\n");
            extractSQLInfo(sb, sqlException);
        }

        sb.append("【异常位置】\n");

        if (businessCodeIndex >= 0) {
            // 显示业务代码位置
            for (int i = businessCodeIndex; i < Math.min(businessCodeIndex + 5, stackTrace.length); i++) {
                StackTraceElement element = stackTrace[i];
                sb.append(String.format("  at %s.%s(%s:%d)\n",
                        element.getClassName(),
                        element.getMethodName(),
                        element.getFileName(),
                        element.getLineNumber()
                ));
            }
        } else {
            // 如果没找到业务代码，显示前几行
            for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                StackTraceElement element = stackTrace[i];
                sb.append(String.format("  at %s.%s(%s:%d)\n",
                        element.getClassName(),
                        element.getMethodName(),
                        element.getFileName(),
                        element.getLineNumber()
                ));
            }
        }

        // 如果有 Cause，显示根异常
        if (e.getCause() != null && e.getCause() != e) {
            sb.append("【根异常】\n");
            Throwable cause = e.getCause();
            sb.append("  类型: ").append(cause.getClass().getName()).append("\n");
            sb.append("  信息: ").append(cause.getMessage()).append("\n");

            // 显示根异常的位置
            StackTraceElement[] causeTrace = cause.getStackTrace();
            for (int i = 0; i < Math.min(3, causeTrace.length); i++) {
                StackTraceElement element = causeTrace[i];
                // 只显示业务代码相关的堆栈
                String className = element.getClassName();
                if (!className.startsWith("java.") &&
                        !className.startsWith("javax.") &&
                        !className.startsWith("sun.") &&
                        !className.startsWith("org.springframework.") &&
                        !className.startsWith("org.apache.")) {
                    sb.append(String.format("  at %s.%s(%s:%d)\n",
                            element.getClassName(),
                            element.getMethodName(),
                            element.getFileName(),
                            element.getLineNumber()
                    ));
                    break; // 只显示第一行业务代码
                }
            }
        }

        sb.append("=".repeat(80));
        String errorMsg = sb.toString();

        // 使用 log.error 打印，带换行
        log.error(errorMsg);

        return errorMsg;
    }

    /**
     * 提取 SQL 异常信息
     */
    private static void extractSQLInfo(StringBuilder sb, Throwable sqlException) {
        // 从异常信息中提取 SQL 语句
        String message = sqlException.getMessage();
        if (message != null) {
            // 常见的 SQL 错误信息格式
            if (message.contains("SQL")) {
                sb.append("  ").append(message).append("\n");
            } else {
                sb.append("  ").append(message).append("\n");
            }
        }

        // 获取 SQL 状态码
        if (sqlException instanceof SQLException sqlEx) {
            if (sqlEx.getSQLState() != null) {
                sb.append("  SQLState: ").append(sqlEx.getSQLState()).append("\n");
            }
            if (sqlEx.getErrorCode() != 0) {
                sb.append("  ErrorCode: ").append(sqlEx.getErrorCode()).append("\n");
            }
        }
    }

    /**
     * @description: 自定义异常封装
     * 增加sql异常自定义返回 且截取Cause前的异常信息返回
     * @author: echo
     */
    public static <R> void checkResponse(Exception e, Result<R> vo) {
        vo.setSuccess(false);
        vo.setCode(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getCode());
        vo.setMsg(ResponseCodeEnum.UNKNOWN_REASON_ERROR.getDesc());
        if (e instanceof GlobalException) {
            vo.setCode(((GlobalException) e).getCode());
            vo.setMsg(((GlobalException) e).getMsg());
        } else if (e instanceof SQLException) {
            vo.setCode(ResponseCodeEnum.BAD_SQL_GRAMMAR.getCode());
            vo.setMsg(ResponseCodeEnum.BAD_SQL_GRAMMAR.getDesc());
        } else if (e instanceof RuntimeException) {
            String message = e.getMessage();
            if (!StringUtils.isEmpty(message) && message.contains("Cause")) {
                message = message.substring(0, message.indexOf("Cause"));
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
        } else if (Objects.equals(0, object.toString().length())) {
            throw new IllegalArgumentException(message);
        }
    }
}
