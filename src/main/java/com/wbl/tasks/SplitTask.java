package com.wbl.tasks;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Stream;

import com.wbl.dates.Task;
import com.wbl.dates.TaskPart;
import com.wbl.dates.WorkPeriod;


public class SplitTask {
    /**
     * Example1 Split Tasks.
     *
     * @param args String
     */
    public static void main(String[] args) {

        Task t1 = new Task(30, "t1");
        Task t2 = new Task(60, "t2");
        Task t3 = new Task(30, "t3");

        LocalDate testDate = LocalDate.of(2018, 5, 22);
        LocalTime testTime = LocalTime.of(9, 0);
        LocalDateTime startDt = LocalDateTime.of(testDate, testTime);

        WorkPeriod wp1 = new WorkPeriod(startDt, Duration.ofHours(2));

        wp1.setTaskParts(Stream.of(t1, t2, t3)
                .map(TaskPart::wholeOf)
                .collect(toList()));

        Optional<WorkPeriod> wp2 = wp1.split(startDt.plusHours(1));

        System.out.println(wp2.toString());
        System.out.println(wp1.toString());
    }
}

