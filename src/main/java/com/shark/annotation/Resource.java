package com.shark.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//作用于成员
@Target(ElementType.FIELD)
//运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {
    //获取注解参数name
    String name() default "";
}
