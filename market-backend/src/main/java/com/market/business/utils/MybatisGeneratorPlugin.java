package com.market.business.utils;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * mybatis生成器插件
 *
 * @author echo
 * @date 2024/03/08
 */
@Slf4j
public class MybatisGeneratorPlugin extends PluginAdapter {
    /**
     * 当前git用户名
     */
    private static String CURRENT_GIT_USER_NAME = "echo";

    /**
     * 生成代码方法
     * ==此方法重复运行完会覆盖同名的文件，请谨慎使用==
     * 使用后需注释掉此方法，如需运行请在mybatis-generator.xml添加表后放开运行
     */
    public static void main(String[] args) {
        args = new String[]{"-configfile", System.getProperty("user.dir") + "/src/main/resources/mybatis-generator.xml", "-overwrite"};
        // ShellRunner.main(args);
    }

    static {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("git", "config", "--global", "user.name");
        try {
            Process process = processBuilder.start();
            // 读取标准输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            CURRENT_GIT_USER_NAME = reader.readLine();
        } catch (IOException e) {
            log.error("MybatisGeneratorPlugin 获取当前git用户名异常");
        }
    }


    /**
     * 验证
     */
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 实体类生成时添加注释
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加import
        topLevelClass.addImportedType("lombok.Data");
        // 添加注解
        topLevelClass.addAnnotation("@Data");
        topLevelClass.addAnnotation("@TableName(\"" + introspectedTable.getFullyQualifiedTable() + "\")");
        if (StringUtility.stringHasValue(introspectedTable.getRemarks())) {
            topLevelClass.addJavaDocLine("/**");
            topLevelClass.addJavaDocLine(" * " + introspectedTable.getRemarks());
            topLevelClass.addJavaDocLine(" *");
            topLevelClass.addJavaDocLine(" * @author " + CURRENT_GIT_USER_NAME);
            topLevelClass.addJavaDocLine(" * @date " + new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
            topLevelClass.addJavaDocLine(" */");
        } else {
            topLevelClass.addJavaDocLine("/**");
            topLevelClass.addJavaDocLine(" * TODO 请添加类注释");
            topLevelClass.addJavaDocLine(" *");
            topLevelClass.addJavaDocLine(" * @author " + CURRENT_GIT_USER_NAME);
            topLevelClass.addJavaDocLine(" * @date " + new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
            topLevelClass.addJavaDocLine(" */");
        }

        // 生成字段常量
        generateFieldConstants(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * 生成字段常量
     */
    private void generateFieldConstants(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            String fieldName = introspectedColumn.getJavaProperty();
            String constantName = camelToUnderscore(fieldName).toUpperCase();
            String columnName = introspectedColumn.getActualColumnName();

            FullyQualifiedJavaType type = new FullyQualifiedJavaType("String");
            Field field = new Field(fieldName, type);
            field.setVisibility(JavaVisibility.PUBLIC);
            field.setStatic(true);
            field.setFinal(true);
            field.setName(constantName);
            field.setType(type);
            field.setInitializationString("\"" + columnName + "\"");

            topLevelClass.addField(field);
        }
    }

    /**
     * 驼峰命名转下划线
     */
    private String camelToUnderscore(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 字段生成时添加注释
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * " + introspectedColumn.getRemarks());
            field.addJavaDocLine(" */");
        }
        return true;
    }

    /**
     * mapper生成时添加注释
     */
    @Override
    public boolean clientGenerated(Interface var1, IntrospectedTable var2) {
        var1.addJavaDocLine("\n");
        var1.addJavaDocLine("/**");
        var1.addJavaDocLine(" * TODO 请添加类注释");
        var1.addJavaDocLine(" *");
        var1.addJavaDocLine(" * @author " + CURRENT_GIT_USER_NAME);
        var1.addJavaDocLine(" * @date " + new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
        var1.addJavaDocLine(" */");
        return true;
    }


    /**
     * 去掉生成getter方法
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        return false;
    }

    /**
     * 去掉生成setter方法
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        return false;
    }

    /**
     * =====以下方法是去掉mapper及xml中不必要的DML操作方法，使得生成的代码保持干净=====
     */
    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method var1, Interface var2, IntrospectedTable var3) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method var1, Interface var2, IntrospectedTable var3) {
        return false;
    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(Method var1, Interface var2, IntrospectedTable var3) {
        return false;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement var1, IntrospectedTable var2) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement var1, IntrospectedTable var2) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method var1, Interface var2, IntrospectedTable var3) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement var1, IntrospectedTable var2) {
        return false;
    }
}