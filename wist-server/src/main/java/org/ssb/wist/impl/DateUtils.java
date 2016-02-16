package org.ssb.wist.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
  public static Date stringToDate(String input) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return dateFormat.parse(input);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return null;
  }
}
