package com.wbl.dates;

import java.time.Duration;

public class TaskPart {

    private final Task owner;
    private final int partSequenceNumber;
    private Duration duration;

    /**
     * Parameterized Constructor.
     *
     * @param owner task
     * @param duration time
     * @param partSequenceNumber part numbers
     */
    public TaskPart(Task owner, Duration duration, int partSequenceNumber) {
        this.owner = owner;
        this.duration = duration;
        this.partSequenceNumber = partSequenceNumber;
    }

    public Task getOwner() {
        return owner;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TaskPart taskPart = (TaskPart) obj;

        return partSequenceNumber == taskPart.partSequenceNumber && owner.equals(taskPart.owner)
                && duration.equals(taskPart.duration);
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + duration.hashCode();
        result = 31 * result + partSequenceNumber;
        return result;
    }

    @Override
    public String toString() {
        int taskPartCount = owner.getTaskPartCount();
        return owner.getDescription()
                + (taskPartCount != 1 ? "(" + partSequenceNumber + "/" + taskPartCount + ")" : "")
                + ", " + Utils.formatDuration(duration);
    }

    public static TaskPart wholeOf(Task task) {
        return task.createTaskPart(task.getDuration());
    }

    /**
     * Method to split Task into TaskParts.
     *
     * @param beforeSplitDuration Duration
     * @return TaskParts
     */
    public TaskPart split(Duration beforeSplitDuration) {
        TaskPart tp2 = getOwner().createTaskPart(getDuration().minus(beforeSplitDuration));
        duration = beforeSplitDuration;
        return tp2;
    }
}
