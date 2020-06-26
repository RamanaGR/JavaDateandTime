package com.wbl.tasks;

import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import java.util.List;
import java.util.stream.Stream;

import com.wbl.dates.Event;


public class TemporalAdj {
    /**
     * Example to Schedule standup Meeting on every Monday.
     *
     * @param start      LocalDateTime
     * @param eventCount count
     * @param duration   time
     * @param zone       Zone
     * @return List of events
     */
    public static List<Event> generateReviews(LocalDateTime start, int eventCount, Duration duration, ZoneId zone) {

        TemporalAdjuster followingReviewDate = startDate -> startDate
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

        return Stream.iterate(start.toLocalDate(), d -> d.with(followingReviewDate))
                .limit(eventCount)
                .map(d -> ZonedDateTime.of(d, start.toLocalTime(), zone))
                .map(zonedDt -> new Event(zonedDt, duration, "standup"))
                .collect(toList());
    }
}
