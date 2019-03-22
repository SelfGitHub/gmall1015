package com.atguigu.gmall.order.controller;

import ch.qos.logback.core.net.SyslogOutputStream;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestData {
    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        System.out.println(format);
        System.out.println(System.currentTimeMillis());

        Calendar instance = Calendar.getInstance();
        System.out.println(instance.getTime());
        System.out.println(Calendar.DATE);
        instance.add(Calendar.DATE,1);

        System.out.println(instance.getTime());
    }
}
