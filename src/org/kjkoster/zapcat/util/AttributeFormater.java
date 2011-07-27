package org.kjkoster.zapcat.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: mingfang
 * Date: 4/14/11
 * Time: 4:01 PM
 */
public class AttributeFormater {

    static ThreadLocal<NumberFormat> formatter = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {
            return new DecimalFormat("#.#########");
        }
    };

    public static String format(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number) {
            Number number = (Number) value;
            if(number.equals(Double.MIN_VALUE) || number.equals(Double.MAX_VALUE)){
                return formatNumber(0);
            }else{
                return formatNumber(number);
            }
        } else {
            return value.toString();
        }
    }

    public static String formatNumber(Number number) {
        return formatter.get().format(number);
    }

    public static void main(String[] args) {
//        Double value = Double.valueOf(1000000000000000d);
        Double value = Double.valueOf(0.000000001d);
//        Double value = Double.valueOf(Double.MAX_VALUE);
        System.out.println(value);
        System.out.println(format(value));
    }

}
