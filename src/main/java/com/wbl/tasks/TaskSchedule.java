package com.wbl.tasks;


import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.wbl.dates.Calendar;
import com.wbl.dates.Schedule;
import com.wbl.dates.Utils;

public class TaskSchedule {
    /**
     * Example 1 Scheduling Tasks with Events.
     *
     * @param args Strings
     */
    public static void main(String[] args) {
        Clock testClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        System.out.println(testClock);
        LocalDate testDate = LocalDate.now(testClock);
        System.out.println(testDate);
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
        calendar.addEvent(meetingTime, Duration.ofHours(1), "Conference call with NYC");

        // create a working schedule
        Schedule schedule = calendar.createSchedule(testClock);

        // and print it out
        System.out.println(schedule);

    }
}
