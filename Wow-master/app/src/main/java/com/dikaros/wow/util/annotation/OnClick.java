package com.dikaros.wow.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Dikaros on 2016/5/18.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface OnClick{
    /**
     * 用在View或其子类对象上
     * value表示的是方法名
     * 快速注册onClick方法
     * @return
     */
    public int value();


}
