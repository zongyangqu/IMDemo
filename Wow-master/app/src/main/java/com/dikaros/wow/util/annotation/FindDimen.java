package com.dikaros.wow.util.annotation;

/**
 * Created by Dikaros on 2016/6/10.
 */
public @interface FindDimen {
    /**
     * value值，如果存在则使用这个当做id
     * 如果不存在就默认id和属性名相同
     * @return
     */
    public int value() default -1;
}
