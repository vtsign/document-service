package tech.vtsign.documentservice.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

public class DateUtil {
    public static LocalDateTime[] getDateBetween(String type) {
        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.atStartOfDay(),
                endDate = today.atTime(LocalTime.MAX);
        switch (type) {
            case "date":
                startDate = today.atStartOfDay();
                endDate = LocalTime.MAX.atDate(today);
                break;
            case "week":
                startDate = today.with(previousOrSame(MONDAY)).atStartOfDay();
                endDate = today.with(nextOrSame(SUNDAY)).atTime(LocalTime.MAX);
                break;
            case "month":
                startDate = today.withDayOfMonth(1).atStartOfDay();
                endDate = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
            case "year":
                startDate = today.withDayOfYear(1).atStartOfDay();
                endDate = today.withDayOfYear(today.lengthOfYear()).atTime(LocalTime.MAX);
                break;
        }
        return new LocalDateTime[]{startDate, endDate};
    }
}
