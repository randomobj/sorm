package com.gitee.randomobject.annotation;

import com.gitee.randomobject.domain.ForeignKeyOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {

    /**
     * 关联表
     */
    Class table();

    /**
     * 关联字段
     */
    String field() default "id";

    /**
     * 外键记录被更新和删除时的操作
     */
    ForeignKeyOption foreignKeyOption() default ForeignKeyOption.NOACTION;
}
