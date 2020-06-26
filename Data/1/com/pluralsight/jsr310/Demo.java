package com.pluralsight.jsr310;

public class Demo {

	public static void main(String[] args) {

		Clock testClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
		LocalDate testDate = LocalDate.now(testClock);

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
				ZoneId.of(("America/New_York")));

		// create a working schedule
		Schedule schedule = calendar.createSchedule(testClock);

		// and print it out
		System.out.println(schedule);

	}
}
