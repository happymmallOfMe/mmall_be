package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * 时间转换类
 *
 * @author Huanyu
 * @date 2018/4/24
 */
public class DateTimeUtil {

    // joda-time

    // str -> Date  eg:2018-04-24 11:37:51 -> Tue Apr 24 11:37:51 CST 2018
    // Date -> str eg:Tue Apr 24 11:37:51 CST 2018 -> 2018-04-24 11:37:51

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }

    public static void main(String[] args) {
        // Tue Apr 24 11:37:51 CST 2018
        // Fri Jan 01 11:11:12 CST 2010
        // 2018-04-24 11:37:51
        System.out.println(new Date().toString());
        System.out.println(DateTimeUtil.strToDate("2010-01-01 11:11:12"));
        System.out.println(DateTimeUtil.dateToStr(new Date()));
    }
}
