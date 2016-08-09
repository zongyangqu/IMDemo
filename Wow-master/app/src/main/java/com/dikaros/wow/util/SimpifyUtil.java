package com.dikaros.wow.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.dikaros.wow.util.annotation.FindColor;
import com.dikaros.wow.util.annotation.FindDimen;
import com.dikaros.wow.util.annotation.FindDrawable;
import com.dikaros.wow.util.annotation.FindString;
import com.dikaros.wow.util.annotation.FindView;
import com.dikaros.wow.util.annotation.OnClick;
import com.dikaros.wow.util.annotation.OnItemClick;
import com.dikaros.wow.util.annotation.OnItemLongClick;
import com.dikaros.wow.util.annotation.OnLongClick;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 使用注解快速找到view
 * 使用注解快速设置监听器
 * Created by Dikaros on 2016/5/18.
 */
public class SimpifyUtil {

    //映射名称与id
    static HashMap<String, Integer> idMap;
    //映射名称与drawable
    static HashMap<String, Integer> drawableMap;

    static HashMap<String, Integer> colorMap;
    static HashMap<String, Integer> stringMap;
    static HashMap<String, Integer> dimenMap;


    /**
     * 初始化view id map
     *
     * @param activity
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    private static void initIdMap(Context activity) throws ClassNotFoundException, IllegalAccessException {

        if (idMap == null) {
            idMap = new HashMap<>();
            Class r = Class.forName(activity.getPackageName() + ".R$id");
            Field[] ids = r.getDeclaredFields();

            for (Field f :
                    ids) {
                //获取数据
                if (f.getType() == int.class) {
                    idMap.put(f.getName(), (int) f.get(r));
                }
            }
        }


    }

    /**
     * 初始化drawable id map
     *
     * @param activity
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    private static void initDrawableMap(Context activity) throws ClassNotFoundException, IllegalAccessException {
        if (drawableMap == null) {
            drawableMap = new HashMap<>();
            Class r2 = Class.forName(activity.getPackageName() + ".R$drawable");
            Field[] drawables = r2.getDeclaredFields();
            for (Field f :
                    drawables) {
                //获取数据
                if (f.getType() == int.class) {
                    drawableMap.put(f.getName(), (int) f.get(r2));
                }
            }
        }


    }


    /**
     * 初始化color id map
     *
     * @param activity
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    private static void initColorMap(Context activity) throws ClassNotFoundException, IllegalAccessException {
        if (colorMap == null) {
            colorMap = new HashMap<>();
            Class r3 = Class.forName(activity.getPackageName() + ".R$color");
            Field[] colors = r3.getDeclaredFields();
            for (Field f :
                    colors) {
                //获取数据
                if (f.getType() == int.class) {
                    colorMap.put(f.getName(), (int) f.get(r3));
                }
            }
        }


    }

    /**
     * 初始化string id map
     *
     * @param activity
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    private static void initStringMap(Context activity) throws ClassNotFoundException, IllegalAccessException {
        if (stringMap == null) {
            stringMap = new HashMap<>();
            Class r4 = Class.forName(activity.getPackageName() + ".R$string");
            Field[] strings = r4.getDeclaredFields();
            for (Field f :
                    strings) {
                //获取数据
                if (f.getType() == int.class) {
                    stringMap.put(f.getName(), (int) f.get(r4));
                }
            }
        }
    }

    private static void initDimenMap(Activity activity) throws ClassNotFoundException, IllegalAccessException {
        if (dimenMap == null) {
            dimenMap = new HashMap<>();
            Class r5 = Class.forName(activity.getPackageName() + ".R$dimen");
            Field[] strings = r5.getDeclaredFields();
            for (Field f :
                    strings) {
                //获取数据
                if (f.getType() == int.class) {
                    dimenMap.put(f.getName(), (int) f.get(r5));
                }
            }
        }
    }

    /**
     * 查找到所有的view
     * 注意View
     */
    public static void findAll(final Activity activity) {


        try {
            //获取所有的成员变量
            Field[] fields = activity.getClass().getDeclaredFields();
            //遍历
            for (Field field : fields) {
                //属性的类型
                Class fieldType = field.getType();
                //属性名
                String fieldName = field.getName();
                //设置属性可以修改
                field.setAccessible(true);
                //如果属性是view并且被FindView这个注解修饰的并且是view的子类
                if (field.isAnnotationPresent(FindView.class)) {
//                    Log.d("SimplifyUtil", field.getName());
                    //成员变量类型


                    //获取注解的value
                    FindView fv = field.getAnnotation(FindView.class);

                    //通过findViewById方法获取的内容
                    Object newView;
                    //如果是-1（默认值）
                    if (fv.value() == -1) {
                        initIdMap(activity);
                        newView = fieldType.cast(activity.findViewById(idMap.get(fieldName)));
                    } else {
                        newView = fieldType.cast(activity.findViewById(fv.value()));
                    }

                    if (newView == null) {
                        throw new Exception("属性名" + field.getName() + "与xml中的id不一致");
                    }

                    //设置找到的view
                    field.set(activity, newView);

                }

                //注册Drawable
                if (field.isAnnotationPresent(FindDrawable.class)) {
                    FindDrawable fd = field.getAnnotation(FindDrawable.class);

                    Object newDrawable;
                    if (fd.value() == -1) {
                        initDrawableMap(activity);
                        newDrawable = fieldType.cast(activity.getResources().getDrawable(drawableMap.get(fieldName)));
                    } else {
                        newDrawable = fieldType.cast(activity.getResources().getDrawable(fd.value()));
                    }

                    if (newDrawable == null) {
                        throw new Exception("属性名" + field.getName() + "与xml中的id不一致");
                    }
                    //设置找到的view
                    field.set(activity, newDrawable);
                }

                if (field.isAnnotationPresent(FindColor.class)) {
                    FindColor fc = field.getAnnotation(FindColor.class);
                    int newColor = -100;
                    if (fc.value() == -1) {
                        initColorMap(activity);
                        newColor = activity.getResources().getColor(colorMap.get(fieldName));
                    } else {
                        newColor = activity.getResources().getColor(fc.value());
                    }
                    if (newColor == -100) {
                        throw new Exception("属性名" + field.getName() + "与xml中的id不一致");
                    }
                    field.set(activity, newColor);

                }

                if (field.isAnnotationPresent(FindString.class)) {
                    FindString fc = field.getAnnotation(FindString.class);
                    String newString;
                    if (fc.value() == -1) {
                        initStringMap(activity);
                        newString = activity.getResources().getString(stringMap.get(fieldName));
                    } else {
                        newString = activity.getResources().getString(fc.value());
                    }
                    if (newString == null) {
                        throw new Exception("属性名" + field.getName() + "与xml中的id不一致");
                    }
                    field.set(activity, newString);
                }

                if (field.isAnnotationPresent(FindDimen.class)) {
                    FindDimen fd = field.getAnnotation(FindDimen.class);
                    String newString;
                    if (fd.value() == -1) {
                        initDimenMap(activity);
                        newString = activity.getResources().getString(dimenMap.get(fieldName));
                    } else {
                        newString = activity.getResources().getString(fd.value());
                    }
                    if (newString == null) {
                        throw new Exception("属性名" + field.getName() + "与xml中的id不一致");
                    }
                    field.set(activity, newString);
                }
                //设置属性不可修改
                field.setAccessible(false);

            }

            //设置方法的事件
            Method[] methods = activity.getClass().getDeclaredMethods();
            for (final Method method : methods) {
                if (method.isAnnotationPresent(OnClick.class)) {
                    OnClick onClick = method.getAnnotation(OnClick.class);
                    int rid = onClick.value();
                    activity.findViewById(rid).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (method.getParameterTypes().length == 1) {
                                    method.invoke(activity, v);
                                } else {
                                    method.invoke(activity);
                                }
                            } catch (
                                    Exception e
                                    )

                            {
                                Log.e("simpifyUtil_Method", e.getMessage() + "");
                            }
                        }
                    });
                }
                if (method.isAnnotationPresent(OnItemClick.class)) {
                    OnItemClick onItemClick = method.getAnnotation(OnItemClick.class);
                    int rid = onItemClick.value();
                    if (activity.findViewById(rid) instanceof AdapterView) {
                        ((AdapterView) activity.findViewById(rid)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    if (method.getParameterTypes().length == 4) {
                                        method.invoke(activity, parent, view, position, id);
                                    } else if (method.getParameterTypes().length == 1) {
                                        method.invoke(activity, position);
                                    } else if (method.getParameterTypes().length == 3) {
                                        method.invoke(activity, parent, view, position);
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                if (method.isAnnotationPresent(OnLongClick.class)) {
                    OnLongClick onLongClick = method.getAnnotation(OnLongClick.class);
                    int rid = onLongClick.value();
                    activity.findViewById(rid).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            try {
                                if (method.getParameterTypes().length == 1) {
                                    method.invoke(activity, v);
                                } else {
                                    method.invoke(activity);
                                }
                            } catch (
                                    Exception e
                                    )

                            {
                                Log.e("simpifyUtil_Method", e.getMessage() + "");
                            }
                            return true;
                        }
                    });
                }

                if (method.isAnnotationPresent(OnItemLongClick.class)) {
                    OnItemLongClick onItemLongClick = method.getAnnotation(OnItemLongClick.class);
                    int rid = onItemLongClick.value();
                    if (activity.findViewById(rid) instanceof AdapterView) {
                        ((AdapterView) activity.findViewById(rid)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    if (method.getParameterTypes().length == 4) {
                                        method.invoke(activity, parent, view, position, id);
                                    } else if (method.getParameterTypes().length == 1) {
                                        method.invoke(activity, position);
                                    } else if (method.getParameterTypes().length == 3) {
                                        method.invoke(activity, parent, view, position);
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        });
                    }
                }
            }
        } catch (
                Exception e
                )

        {
            Log.e("simpifyUtil", e.getMessage() + "");
        }

    }


    /**
     * 这种情况不能用于加载非view资源
     *
     * @param o
     * @param view
     */
    public static void findAll(Object o, View view) {
        try {


            //获取所有的成员变量
            Field[] fields = o.getClass().getDeclaredFields();
            //遍历
            for (Field field : fields) {
                //成员变量类型
                Class fieldType = field.getType();
                //如果属性是view并且被FindView这个注解修饰的并且是view的子类
                if (field.isAnnotationPresent(FindView.class)) {


                    //获取注解的value
                    FindView fv = field.getAnnotation(FindView.class);

                    //通过findViewById方法获取的内容
                    Object newView;
                    //如果是-1（默认值）
                    if (fv.value() == -1) {
                        throw new Exception("请指定id");

                    } else {
                        newView = fieldType.cast(view.findViewById(fv.value()));
                    }

                    if (newView == null) {
                        throw new Exception("属性名" + field.getName() + "与xml中的id不一致");
                    }
                    //设置属性可以访问
                    field.setAccessible(true);

                    //设置找到的view
                    field.set(o, newView);
                    //关闭属性可以访问
                    field.setAccessible(false);

                }

                //设置属性不可修改
                field.setAccessible(false);

            }
        } catch (Exception e) {
            Log.e("simpifyUtil", e.getMessage());
        }
    }
}
