package com.wbl.dates;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class Utils {

    // fixed (9 - 5:30) work schedule used for simplicity in presentation
    private static final LocalTime AM_START = LocalTime.of(9, 0);
    private static final LocalTime PM_START = LocalTime.of(13, 30);
    private static final Duration WORK_PERIOD_LENGTH = Duration.ofHours(3).plusMinutes(30);

    public static List<WorkPeriod> generateWorkPeriods(LocalDate date, int dayCountInclusive) {
        List<LocalDate> workingDays = generateWorkingDays(date, dayCountInclusive);
        return generateWorkPeriods(workingDays, AM_START, WORK_PERIOD_LENGTH, PM_START, WORK_PERIOD_LENGTH);
    }

    private static List<WorkPeriod> generateWorkPeriods(List<LocalDate> days, LocalTime amStart, Duration amDuration,
                                                        LocalTime pmStart, Duration pmDuration) {
        List<WorkPeriod> periods = new ArrayList<>();
        for (LocalDate d : days) {
            LocalDateTime thisAmStart = LocalDateTime.of(d, amStart);
            periods.add(new WorkPeriod(thisAmStart, thisAmStart.plus(amDuration)));
            LocalDateTime thisPmStart = LocalDateTime.of(d, pmStart);
            periods.add(new WorkPeriod(thisPmStart, thisPmStart.plus(pmDuration)));
        }
        return periods;
    }

    /**
     * Method to Add Events in Working days.
     *
     * @param start    date time
     * @param dayCount number of days
     * @param dur      time
     * @param zone     zoneId
     * @return list of events
     */
    public static List<Event> generateStandups(LocalDateTime start, int dayCount, Duration dur, ZoneId zone) {
        List<LocalDate> workingDays = generateWorkingDays(start.toLocalDate(), dayCount);
        return workingDays.stream()
                .map(d -> ZonedDateTime.of(d, start.toLocalTime(), zone))
                .map(zonedDt -> new Event(zonedDt, dur, "standup"))
                .collect(toList());
    }

    private static boolean isWorkingDay(LocalDate dt) {
        return dt.getDayOfWeek() != DayOfWeek.SATURDAY && dt.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private static List<LocalDate> generateWorkingDays(LocalDate startDate, int dayCount) {
        return Stream.iterate(startDate, d -> d
                .plusDays(1))
                .filter(Utils::isWorkingDay)
                .limit(dayCount)
                .collect(toList());
    }

    /**
     * alternative implementation of generateWorkingDays.
     *
     * @param startDate         LocalDate
     * @param dayCountInclusive number of days
     * @return List of WorkingDays
     */

    public static List<LocalDate> generateWorkingDays_alternative_implementation(LocalDate startDate,
                                                                                 int dayCountInclusive) {
        return Stream.iterate(startDate, d -> d
                .with(nextWorkingDayAdjuster))
                .limit(dayCountInclusive)
                .collect(toList());
    }

    private static TemporalAdjuster nextWorkingDayAdjuster = t -> LocalDate
            .from(t).getDayOfWeek() == DayOfWeek.FRIDAY
            ? t.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            : t.plus(1, DAYS);
    // ************
    // end alternative implementation of generateWorkingDays

    static String formatDuration(Duration dur) {
        long hours = dur.toHours();
        String hoursString = hours == 0 ? "" : hours + (hours == 1 ? "hr " : "hrs ");
        long minutes = dur.minusHours(hours).toMinutes();
        return hoursString + minutes + "mins";
    }
}
