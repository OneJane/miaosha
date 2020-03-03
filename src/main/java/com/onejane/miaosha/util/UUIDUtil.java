package com.onejane.miaosha.util;

import java.util.UUID;


public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
