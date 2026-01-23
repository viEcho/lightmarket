package com.market.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 管理员表
 *
 * @author viEcho
 * @date 2026/01/23
 */
@Data
@TableName("admin_emp")
public class AdminEmp {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Byte gender;

    /**
     * 状态：-1-禁用，1-正常
     */
    private Byte status;

    /**
     * 管理员类型：1-超级管理员，2-普通管理员
     */
    private Byte type;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 逻辑删除：0-正常，1-删除
     */
    private Byte isDeleted;

    public static final String ID = "id";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String NICKNAME = "nickname";

    public static final String AVATAR = "avatar";

    public static final String EMAIL = "email";

    public static final String MOBILE = "mobile";

    public static final String GENDER = "gender";

    public static final String STATUS = "status";

    public static final String TYPE = "type";

    public static final String LAST_LOGIN_IP = "last_login_ip";

    public static final String LAST_LOGIN_TIME = "last_login_time";

    public static final String LOGIN_COUNT = "login_count";

    public static final String REMARK = "remark";

    public static final String DEPT_ID = "dept_id";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";

    public static final String CREATED_BY = "created_by";

    public static final String UPDATED_BY = "updated_by";

    public static final String IS_DELETED = "is_deleted";
}