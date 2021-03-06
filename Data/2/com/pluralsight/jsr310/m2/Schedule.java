package com.pluralsight.jsr310.m2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.*;

public class Schedule {

	private final List<WorkPeriod> scheduledPeriods;

	List<WorkPeriod> getScheduledPeriods() {
		return scheduledPeriods;
	}

	boolean isSuccessful() {
		return successful;
	}

	private final boolean successful;

	public Schedule(List<WorkPeriod> scheduledPeriods, boolean success) {
		this.scheduledPeriods = scheduledPeriods;
		this.successful = success;
	}

	@Override
	public String toString() {

		if (!successful) return "Schedule unsuccessful: insufficent time for tasks";

		List<WorkPeriod> printablePeriods = scheduledPeriods.stream().map(WorkPeriod::copy).collect(toList());
		List<WorkPeriod> periodSplitByMidnight = printablePeriods.stream()
				.map(WorkPeriod::split)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
		printablePeriods.addAll(periodSplitByMidnight);

		NavigableMap<LocalDateTime, String> dateTimeToPeriodOutput = printablePeriods.stream()
				.collect(groupingBy(WorkPeriod::getStartTime, TreeMap::new, mapping(WorkPeriod::toString, joining())));


		NavigableMap<LocalDate, String> dateToCalendarObjectOutput = dateTimeToPeriodOutput.entrySet().stream()
				.collect(groupingBy(e -> e.getKey().toLocalDate(), TreeMap::new, mapping(Map.Entry::getValue,joining())));

		StringBuilder sb = new StringBuilder();

		for (LocalDate date : dateToCalendarObjectOutput.keySet()) {
			sb.append("\n");
			sb.append(date);
			sb.append(dateToCalendarObjectOutput.get(date));
		}

		return sb.toString();
	}
}
