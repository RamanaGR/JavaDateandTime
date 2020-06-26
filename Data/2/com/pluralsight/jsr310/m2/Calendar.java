package com.pluralsight.jsr310.m2;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

public class Calendar {

	final private NavigableSet<WorkPeriod> workPeriods = new TreeSet<>(); // ordered by start time
	final private List<Task> tasks = new ArrayList<>();                   // ordered by priority

	public Schedule createSchedule(Clock clock) {
		//TODO (maybe) save overwritePeriodsByEvents from having to consider periods and events in the past
		LocalDateTime ldt = LocalDateTime.now(clock);

		List<TaskPart> remainingTaskParts = tasks.stream().map(TaskPart::wholeOf).collect(toList());

		List<WorkPeriod> scheduledPeriods = new ArrayList<>();
		for (WorkPeriod p : workPeriods) {
			LocalDateTime effectiveStartTime = p.getStartTime().isAfter(ldt) ? p.getStartTime() : ldt;
			// TODO doesn't allow for DST changes during WorkPeriod
			if (WorkPeriod.MINIMUM_DURATION.minus(Duration.between(effectiveStartTime, p.getEndTime())).isNegative()) {
				p.setTaskParts(remainingTaskParts);
				scheduledPeriods.add(p.split(p.getEndTime()).orElseThrow(IllegalStateException::new));
				remainingTaskParts = p.getTaskParts();
			}
		}
		return new Schedule(scheduledPeriods, remainingTaskParts.isEmpty());
	}

	public Calendar addWorkPeriod(WorkPeriod p) {
		WorkPeriod preceding = workPeriods.floor(p);
		WorkPeriod following = workPeriods.ceiling(p);
		if (preceding != null && ! preceding.getEndTime().isBefore(p.getStartTime())) {
			throw new IllegalArgumentException("Work Periods cannot overlap: " + preceding + "," + p);
		} else if (following != null && ! following.getStartTime().isAfter(p.getEndTime())) {
			throw new IllegalArgumentException("Work Periods cannot overlap: " + p + "," + following);
		}
		workPeriods.add(p);
		return this;
	}

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
}
