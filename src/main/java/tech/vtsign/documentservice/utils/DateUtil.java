package tech.vtsign.documentservice.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

public class DateUtil {
    public static LocalDateTime[] getDateBetween(String type) {
        return getDateBetween(type, LocalDate.now());
    }

    public static LocalDateTime[] getDateBetween(String type, LocalDate today) {
        LocalDateTime startDate, endDate;
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
            default:
                throw new IllegalArgumentException("type not found");
        }
        return new LocalDateTime[]{startDate, endDate};
    }

    public static Map<String, LocalDateTime[]> getListLocalDateTime(String type) {
        return getListLocalDateTime(type, LocalDate.now());
    }

    public static Map<String, LocalDateTime[]> getListLocalDateTime(String type, LocalDate today) {
        Map<String, LocalDateTime[]> map = new LinkedHashMap<>();
        switch (type) {
            case "week":
                while (today.getDayOfWeek() != MONDAY) {
                    map.put(today.getDayOfWeek().name(), getDateBetween("date", today));
                    today = today.minusDays(1);
                }
                map.put(today.getDayOfWeek().name(), getDateBetween("date", today));
                map = reverseMap(map);
                break;
            case "month":
                LocalDate firstDate = today.withDayOfMonth(1);
                int week = 1;
                while (firstDate.getDayOfYear() < today.getDayOfYear() && week <= 5) {
                    LocalDateTime startDate = firstDate.atStartOfDay();
                    LocalDateTime endDate;
                    if (week == 5) {
                        endDate = today.atTime(LocalTime.MAX);
                    } else {
                        endDate = firstDate.plusDays(6).atTime(LocalTime.MAX);
                    }
                    map.put("WEEK " + week, new LocalDateTime[]{startDate, endDate});
                    week++;
                    firstDate = firstDate.plusDays(7);
                }
                break;
            case "year":
                while (today.getMonthValue() != 1) {
                    map.put(today.getMonth().name(), getDateBetween("month", today));
                    today = today.minusMonths(1);
                }
                map.put(today.getMonth().name(), getDateBetween("month", today));
                map = reverseMap(map);
                break;
            case "all":
                while (today.getYear() >= 2021) {
                    map.put(today.getYear() + "", getDateBetween("year", today));
                    today = today.minusYears(1);
                }
                map = reverseMap(map);
                break;

            default:
                throw new IllegalArgumentException("type not found");
        }
        return map;
    }

    private static Map<String, LocalDateTime[]> reverseMap(Map<String, LocalDateTime[]> map) {
        Map<String, LocalDateTime[]> reverseMap = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.reverse(keys);
        for (String key : keys) {
            reverseMap.put(key, map.get(key));
        }
        return reverseMap;
    }
}
