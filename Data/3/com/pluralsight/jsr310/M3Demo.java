package com.pluralsight.jsr310;

import com.pluralsight.jsr310.m3.Calendar;
import com.pluralsight.jsr310.m3.Schedule;
import com.pluralsight.jsr310.m3.Utils;

import java.time.*;

public class M3Demo {
//Event Overlap
	public static void main(String[] args) {

		Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
		LocalDate testDate = LocalDate.now(clock);

		// create a calendar
		Calendar calendar = new Calendar();

		// add some tasks to the calendar
		calendar.addTask(1, 0, "Answer urgent e-mail");
		calendar.addTask(4, 0, "Write deployment report");
		calendar.addTask(4, 0, "Plan security configuration");

		// add some work periods to the calendar
		calendar.addWorkPeriods(Utils.generateWorkPeriods(testDate, 3));

		// add an event to the calendar, specifying its time zone
		ZonedDateTime meetingTime = ZonedDateTime.of(testDate.atTime(8, 30),
				ZoneId.of("America/New_York"));
		calendar.addEvent(meetingTime, Duration.ofHours(1), "Conference call with NYC");

		// create a working schedule
		Schedule schedule = calendar.createSchedule(clock);
		System.out.println(schedule);
	}
}
