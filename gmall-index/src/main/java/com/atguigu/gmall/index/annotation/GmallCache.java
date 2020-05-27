package com.atguigu.gmall.index.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//@Inherited // 子类可继承
@Documented
public @interface GmallCache {

    /**
     * 缓存key的前缀
     * @return
     */
    String prefix() default "cache";

    //缓存到期时间默认30分钟
    int timeout() default 30;

    //防止雪崩的一个随机数范围
    int bound() default 5;
}