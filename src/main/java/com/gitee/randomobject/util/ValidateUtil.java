package com.gitee.randomobject.util;

import java.util.List;

/**
 * 参数校验工具类
 */
public class ValidateUtil {
    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNotNull(Object object) {
        return object != null;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    public static boolean isNotEmpty(List list) {
        return list != null && list.size() > 0;
    }

    /**
     * 检测对象是否为空
     * @param o 待检测对象
     * @param msg 自定义抛出信息
     */
    public static boolean checkIsNull(Object o,String msg){

        if(o instanceof String){
            String str = (String)o;
            if (!"".equalsIgnoreCase(str) && str.length()>0){
                return false;
            }else {
                throw new IllegalArgumentException(msg);
            }
        }else{
            if (o !=null){
                return false;
            }else{
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * 检测对象是否为空
     * @param o 待检测对象
     * @return true为空，false不为空
     */
    public static boolean checkIsNull(Object o){

        if(o instanceof String){
            String str = (String)o;
            if (!"".equalsIgnoreCase(str) && str.length()>0){
                return false;
            }
        }else{
            if (o !=null){
                return false;
            }
        }
        return true;
    }
}
