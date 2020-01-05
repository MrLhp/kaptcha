package org.captcha;


import org.apache.commons.lang3.RandomUtils;

public class Constants {
    private static final String validUrl = "https://gjdyzjb.cn/validPic?v=";
    public static String getValidUrl() {
        return validUrl+ RandomUtils.nextInt(0,100);
    }
}
