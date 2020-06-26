package com.pluralsight.jsr310;

import com.pluralsight.jsr310.m3.Event;

import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class M3P {

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
