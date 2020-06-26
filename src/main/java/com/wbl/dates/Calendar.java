package com.wbl.dates;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;


public class Calendar {

    public final NavigableSet<Event> events = new TreeSet<>();              // ordered by start time
    private final List<Task> tasks = new ArrayList<>();                   // ordered by priority
    public final NavigableSet<WorkPeriod> workPeriods = new TreeSet<>(); // ordered by start time

    /**
     * Method to create Schedule With events.
     *
     * @param clock Time
     * @return Schedule
     */
    public Schedule createSchedule(Clock clock) {
        //TODO (maybe) save overwritePeriodsByEvents from having to consider periods and events in the past
        NavigableSet<WorkPeriod> overwrittenPeriods = overwritePeriodsByEvents(workPeriods, events, clock.getZone());
        LocalDateTime ldt = LocalDateTime.now(clock);

        List<TaskPart> remainingTaskParts = tasks.stream().map(TaskPart::wholeOf).collect(toList());

        List<WorkPeriod> scheduledPeriods = new ArrayList<>();
        for (WorkPeriod p : overwrittenPeriods) {
            LocalDateTime effectiveStartTime = p.getStartTime().isAfter(ldt) ? p.getStartTime() : ldt;
            // TODO doesn't allow for DST changes during WorkPeriod
            if (WorkPeriod.MINIMUM_DURATION.minus(Duration.between(effectiveStartTime, p.getEndTime())).isNegative()) {
                p.setTaskParts(remainingTaskParts);
                scheduledPeriods.add(p.split(p.getEndTime()).orElseThrow(IllegalStateException::new));
                remainingTaskParts = p.getTaskParts();
            }
        }
        return new Schedule(clock.getZone(), scheduledPeriods, events, remainingTaskParts.isEmpty());
    }

    public NavigableSet<WorkPeriod> overwritePeriodsByEvents(ZoneId zone) {
        return overwritePeriodsByEvents(workPeriods, events, zone);
    }

    private NavigableSet<WorkPeriod> overwritePeriodsByEvents(NavigableSet<WorkPeriod> workPeriods,
                                                              NavigableSet<Event> events,
                                                              ZoneId zone) {
        NavigableSet<WorkPeriod> rawPeriods = workPeriods.stream()
                .map(WorkPeriod::copy)
                .collect(toCollection(TreeSet::new));
        NavigableSet<WorkPeriod> overwrittenPeriods = new TreeSet<>();
        WorkPeriod period = rawPeriods.pollFirst();
        Event event = events.isEmpty() ? null : events.first();
        while (period != null && event != null) {
            if (!period.getEndTime().isAfter(event.getLocalStartDateTime(zone))) {
                // non-overlapping, period first
                overwrittenPeriods.add(period);
                period = rawPeriods.higher(period);
            } else if (!period.getStartTime().isBefore(event.getLocalEndDateTime(zone))) {
                // non-overlapping, event first
                event = events.higher(event);
            } else if (period.getStartTime().isBefore(event.getLocalStartDateTime(zone))) {
                // overlapping, period starts first
                overwrittenPeriods.add(period.split(event.getLocalStartDateTime(zone)).get());
            } else if (period.getEndTime().isAfter(event.getLocalEndDateTime(zone))) {
                // overlapping, event starts first or at same time
                period.split(event.getLocalEndDateTime(zone)).get();
                event = events.higher(event);
            } else {
                // event encloses period
                period = workPeriods.higher(period);
            }
        }
        if (period != null) {
            overwrittenPeriods.add(period);
            overwrittenPeriods.addAll(rawPeriods.tailSet(period));
        }
        return overwrittenPeriods;
    }

    /**
     * Method to Add Each WorkPeriod.
     *
     * @param period WorkPeriod
     * @return Calender
     */
    public Calendar addWorkPeriod(WorkPeriod period) {
        WorkPeriod preceding = workPeriods.floor(period);
        WorkPeriod following = workPeriods.ceiling(period);
        if (preceding != null && !preceding.getEndTime().isBefore(period.getStartTime())) {
            throw new IllegalArgumentException("Work Periods cannot overlap: " + preceding + "," + period);
        } else if (following != null && !following.getStartTime().isAfter(period.getEndTime())) {
            throw new IllegalArgumentException("Work Periods cannot overlap: " + period + "," + following);
        }
        workPeriods.add(period);
        return this;
    }

    /**
     * Method to addWorkPeriods.
     *
     * @param periods List of Periods
     * @return Calender
     */
    public Calendar addWorkPeriods(List<WorkPeriod> periods) {
        for (WorkPeriod wp : periods) {
            addWorkPeriod(wp);
        }
        return this;
    }

    public Calendar addTask(int hours, int minutes, String description) {
        addTask(new Task(hours, minutes, description));
        return this;
    }

    public Calendar addTask(Task task) {
        tasks.add(task);
        return this;
    }

    public Calendar addEvent(Event evt) {
        events.add(evt);
        return this;
    }

    public Calendar addEvent(ZonedDateTime eventDateTime, Duration duration, String description) {
        addEvent(Event.of(eventDateTime, eventDateTime.plus(duration), description));
        return this;
    }
}
