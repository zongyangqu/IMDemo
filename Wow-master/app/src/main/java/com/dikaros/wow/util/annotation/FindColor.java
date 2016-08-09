package com.dikaros.wow.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Dikaros on 2016/6/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface FindColor {
    /**
     * value值，如果存在则使用这个当做id
     * 如果不存在就默认id和属性名相同
     * @return
     */
    public int value() default -1;
}
