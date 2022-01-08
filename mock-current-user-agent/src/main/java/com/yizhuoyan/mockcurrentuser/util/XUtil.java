package com.yizhuoyan.mockcurrentuser.util;

/**
 * <p>
 *
 * </p>
 *
 * @author jun.yi@resico.cn
 * @date 2022/1/8 15:13
 */
public interface XUtil {
    static String trim2default(String s, String defaultValue) {
        if (s == null || (s = s.trim()).length() == 0) {
            return defaultValue;
        }
        return s;
    }

    static String trim2null(String s) {
        if (s == null || (s = s.trim()).length() == 0) {
            return null;
        }
        return s;
    }
}
