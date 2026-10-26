package com.propverse.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InputValidator {
    private InputValidator() {}
    public static int progress(String value) {
        int n=Integer.parseInt(value);
        if(n<0||n>100) throw new IllegalArgumentException("Progress must be between 0 and 100.");
        return n;
    }
    public static LocalDate date(String value) {
        if(value==null||value.isBlank()) return null;
        try { return LocalDate.parse(value.trim()); }
        catch(Exception ex) { throw new IllegalArgumentException("Dates must use YYYY-MM-DD."); }
    }
    public static void orderedDates(String start,String end,String label) {
        LocalDate a=date(start),b=date(end);
        if(a!=null&&b!=null&&b.isBefore(a)) throw new IllegalArgumentException(label+" cannot be before the start date.");
    }
    public static BigDecimal nonNegativeMoney(String value) {
        try { BigDecimal amount=new BigDecimal(value==null||value.isBlank()?"0":value.trim()); if(amount.signum()<0)throw new IllegalArgumentException("Budget cannot be negative."); return amount; }
        catch(NumberFormatException ex) { throw new IllegalArgumentException("Enter a valid budget amount."); }
    }
    public static void required(String value,String label) { if(value==null||value.isBlank())throw new IllegalArgumentException(label+" is required."); }
    public static void email(String value) { if(value!=null&&!value.isBlank()&&!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("Enter a valid email address."); }
    public static void phone(String value) { if(value!=null&&!value.isBlank()&&!value.matches("[+0-9() .-]{7,20}"))throw new IllegalArgumentException("Enter a valid phone number."); }
}
