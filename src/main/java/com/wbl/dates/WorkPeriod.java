package com.wbl.dates;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import org.threeten.extra.Interval;

public class WorkPeriod implements Comparable<WorkPeriod> {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<TaskPart> taskParts;

    static final Duration MINIMUM_DURATION = Duration.ofMinutes(5);

    public WorkPeriod(LocalDateTime startTime, LocalDateTime endTime) throws IllegalArgumentException {
        this(startTime, endTime, new ArrayList<>());
    }

    /**
     * Parameterized Constructor.
     *
     * @param startTime LocalDateTime
     * @param endTime   LocalDateTime
     * @param taskParts List of TaskParts
     */
    public WorkPeriod(LocalDateTime startTime, LocalDateTime endTime, List<TaskPart> taskParts) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskParts = taskParts;
        validatePeriodTimes(startTime, endTime);
    }

    /**
     * <p>For example, if you had the datetime of 28 Mar 2002
     * 13:45:01.231, if you passed with HOUR, it would return 28 Mar
     * 2002 13:00:00.000.  If this was passed with MONTH, it would
     * return 1 Mar 2002 0:00:00.000.</p>
     *
     * @param startTime LocalDateTime
     * @param endTime   LocalDateTime
     */
    private void validatePeriodTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isAfter(startTime.truncatedTo(DAYS).plusDays(2))) {
            // display code doesn't cover this unlikely case
            throw new IllegalArgumentException("Periods cannot span more than two days");
        }
    }

    public WorkPeriod(LocalDateTime startTime, Duration dur) {
        this(startTime, startTime.plus(dur));
    }

    /**
     * Method to get duration BW StartTime and EndTime.
     *
     * @param zoneId Zone
     * @return Duration BW StartTime and EndTime
     */
    public Duration getDuration(ZoneId zoneId) {
        ZonedDateTime startZdt = ZonedDateTime.of(startTime, zoneId);
        ZonedDateTime endZdt = ZonedDateTime.of(endTime, zoneId);
        return Duration.between(startZdt, endZdt);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        String workPeriodHeader = "\n\tWork Period: "
                + timeFormatter.format(startTime)
                + " to "
                + timeFormatter.format(endTime);
        StringBuilder sb = new StringBuilder(workPeriodHeader);
        for (TaskPart t : taskParts) {
            sb.append("\n\t\t").append(t);
        }
        return sb.toString();
    }

    public List<TaskPart> getTaskParts() {
        return taskParts;
    }

    public void setTaskParts(List<TaskPart> taskParts) {
        this.taskParts = taskParts;
    }

    /**
     * Method to split WorkPeriod into parts.
     *
     * @param splitTime time
     * @return Optional WorkPeriod
     */
    public Optional<WorkPeriod> split(LocalDateTime splitTime) {
        if ((startTime.isBefore(splitTime) && !splitTime.isAfter(endTime))) {
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
                    // TODO doesn't allow for DST changes during WorkPeriod being split
                    TaskPart newTaskPart = partToSplit.split(Duration.between(partStartTime, splitTime));
                    getTaskParts().add(0, newTaskPart);
                }
            }
            return Optional.of(newPeriod);
        } else {
            return Optional.empty();
        }
    }

    // Convenience method to assist displaying a schedule by the day
    public Optional<WorkPeriod> split() {
        LocalDateTime midnight = startTime.plusDays(1).truncatedTo(DAYS);
        return split(midnight);
    }

    public void addTaskPart(TaskPart taskPart) {
        taskParts.add(taskPart);
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

    /**
     * Method to copy workperiod.
     *
     * @param original workperiod
     * @return Workperiod
     */
    public static WorkPeriod copy(WorkPeriod original) {
        WorkPeriod newPeriod = new WorkPeriod(original.startTime, original.endTime);
        newPeriod.setTaskParts(original.getTaskParts());
        return newPeriod;
    }

    public Interval toInterval(ZoneId zone) {
        return Interval.of(ZonedDateTime.of(startTime, zone).toInstant(), ZonedDateTime.of(endTime, zone).toInstant());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        WorkPeriod that = (WorkPeriod) obj;

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




































