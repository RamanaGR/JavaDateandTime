package com.pluralsight.jsr310.m2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class WorkPeriod implements Comparable<WorkPeriod> {

	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private List<TaskPart> taskParts;

	static final Duration MINIMUM_DURATION = Duration.ofMinutes(5);

	WorkPeriod(LocalDateTime startTime, LocalDateTime endTime) throws IllegalArgumentException {
		this(startTime, endTime, new ArrayList<>());
	}

	public WorkPeriod(LocalDateTime startTime, LocalDateTime endTime, List<TaskPart> taskParts) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.taskParts = taskParts;
		validatePeriodTimes(startTime, endTime);
	}

	private void validatePeriodTimes(LocalDateTime startTime, LocalDateTime endTime) {
		if (endTime.isAfter(startTime.truncatedTo(DAYS).plusDays(2))) {
			// display code doesn't cover this unlikely case
			throw new IllegalArgumentException("Periods cannot span more than two days");
		}
	}

	public WorkPeriod(LocalDateTime startTime, Duration d) {
		this(startTime, startTime.plus(d));
	}

	public Duration getDuration() {
		return Duration.between(startTime, endTime);
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	@Override
	public String toString() {
		String workPeriodHeader = "\n\tWork Period: " + startTime.toString() + " to " + endTime.toString();
		StringBuilder sb = new StringBuilder(workPeriodHeader);
		for (TaskPart t : taskParts) {
			sb.append("\n\t\t").append(t);
		}
		return sb.toString();
	}

	List<TaskPart> getTaskParts() {
		return taskParts;
	}

	public void setTaskParts(List<TaskPart> taskParts) {
		this.taskParts = taskParts;
	}

	public Optional<WorkPeriod> split(LocalDateTime splitTime) {
		if ((startTime.isBefore(splitTime) && ! splitTime.isAfter(endTime))) {
			WorkPeriod newPeriod = new WorkPeriod(startTime, splitTime);
			startTime = splitTime;
			if (!taskParts.isEmpty()) {
				NavigableMap<LocalDateTime, TaskPart> timeToTaskPartMap = new TreeMap<>();
				LocalDateTime taskStartTime = newPeriod.getStartTime();
				for (TaskPart taskPart : taskParts) {
					timeToTaskPartMap.put(taskStartTime, taskPart);
					taskStartTime = taskStartTime.plus(taskPart.getDuration());
				}
				newPeriod.setTaskParts(new ArrayList<>(timeToTaskPartMap.headMap(splitTime).values()));
				setTaskParts(new ArrayList<>(timeToTaskPartMap.tailMap(splitTime).values()));

				Map.Entry<LocalDateTime, TaskPart> taskPartEntry = timeToTaskPartMap.lowerEntry(splitTime);
				TaskPart partToSplit = taskPartEntry.getValue();
				LocalDateTime partStartTime = taskPartEntry.getKey();
				Duration partDuration = partToSplit.getDuration();
				if (splitTime.isAfter(partStartTime) && partStartTime.plus(partDuration).isAfter(splitTime)) {
					TaskPart newTaskPart = partToSplit.split(Duration.between(partStartTime, splitTime));
					getTaskParts().add(0, newTaskPart);
				}
			}
			return Optional.of(newPeriod);
		} else {
			return Optional.empty();
		}
	}

	void addTaskPart(TaskPart taskPart) {
		taskParts.add(taskPart);
	}

	// Convenience method to assist displaying a schedule by the day
	Optional<WorkPeriod> split() {
		LocalDateTime midnight = startTime.plusDays(1).truncatedTo(DAYS);
		return split(midnight);
	}

	@Override
	public int compareTo(WorkPeriod otherWorkPeriod) {
		return startTime.compareTo(otherWorkPeriod.startTime);
	}

	Duration getTasksDuration() {
		return getTaskParts().stream()
				.map(TaskPart::getDuration)
				.reduce(Duration.ZERO, Duration::plus);
	}

	public static WorkPeriod copy(WorkPeriod original) {
		WorkPeriod newPeriod = new WorkPeriod(original.startTime, original.endTime);
		newPeriod.setTaskParts(original.getTaskParts());
		return newPeriod;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WorkPeriod that = (WorkPeriod) o;

		return startTime.equals(that.startTime) && endTime.equals(that.endTime) && taskParts.equals(that.taskParts);
	}

	@Override
	public int hashCode() {
		int result = startTime.hashCode();
		result = 31 * result + endTime.hashCode();
		result = 31 * result + taskParts.hashCode();
		return result;
	}
}




































