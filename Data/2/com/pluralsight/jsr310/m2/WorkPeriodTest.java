package com.pluralsight.jsr310.m2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

public class WorkPeriodTest {

	private LocalDate startLocalDate;

	@Before
	public void setup() {
		startLocalDate = LocalDate.EPOCH;
	}

	// for slides
	@Test
	public void basicSplitTest() {
		LocalDateTime startTime = startLocalDate.atTime(23, 0);
		LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
		WorkPeriod p = new WorkPeriod(startTime, endTime);
		Optional<WorkPeriod> newPeriod = p.split();

		LocalDateTime midnight = startLocalDate.plusDays(1).atStartOfDay();
		assertEquals(Optional.of(new WorkPeriod(startTime,midnight)), newPeriod);
		assertEquals(new WorkPeriod(midnight, endTime),p);
	}

	@Test
	public void testSplitTwoDayPeriod() {
		LocalDateTime startTime = startLocalDate.atTime(0, 0);
		LocalDateTime endTime = startLocalDate.plusDays(2).atTime(0, 0);
		WorkPeriod p = new WorkPeriod(startTime, endTime);
		Optional<WorkPeriod> split = p.split();

		LocalDateTime midnight = startLocalDate.plusDays(1).atStartOfDay();
		assertEquals(Optional.of(new WorkPeriod(startTime, midnight)), split);
		assertEquals(new WorkPeriod(midnight, endTime), p);
	}

	@Test
	public void testSplitWithSingleTaskPart() {
		LocalDateTime startTime = startLocalDate.atTime(23, 0);
		LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
		WorkPeriod p = new WorkPeriod(startTime, endTime);
		Task t = new Task(0, 120,"");
		p.addTaskPart(TaskPart.wholeOf(t));
		Optional<WorkPeriod> newFirstHalf = p.split();

		LocalDateTime midnight = startLocalDate.plusDays(1).atStartOfDay();
		WorkPeriod expectedFirstHalf = new WorkPeriod(startTime, midnight);
		expectedFirstHalf.addTaskPart(new TaskPart(t, Duration.ofMinutes(60), 1));
		assertEquals(Optional.of(expectedFirstHalf), newFirstHalf);

		WorkPeriod expectedSecondHalf = new WorkPeriod(midnight, endTime);
		expectedSecondHalf.addTaskPart(new TaskPart(t, Duration.ofMinutes(60), 2));
		assertEquals(expectedSecondHalf, p);
	}

	@Test
	public void testSplitWithTaskPartBoundaryOnSplit() {
		LocalDateTime startTime = startLocalDate.atTime(23, 0);
		LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
		LocalDateTime midnight = startTime.plusDays(1).toLocalDate().atStartOfDay();
		WorkPeriod p = new WorkPeriod(startTime, endTime);
		Task t1 = new Task(0, 60, "");
		TaskPart tp1 = TaskPart.wholeOf(t1);
		Task t2 = new Task(0, 60, "");
		TaskPart tp2 = TaskPart.wholeOf(t2);
		p.addTaskPart(tp1);
		p.addTaskPart(tp2);
		Optional<WorkPeriod> newFirstHalf = p.split();

		WorkPeriod expectedFirstHalf = new WorkPeriod(startTime, midnight);
		expectedFirstHalf.addTaskPart(tp1);
		assertEquals(Optional.of(expectedFirstHalf), newFirstHalf);
		WorkPeriod expectedSecondHalf = new WorkPeriod(midnight, endTime);
		expectedSecondHalf.addTaskPart(tp2);
		assertEquals(expectedSecondHalf, p);
	}

	@Test
	public void testSplitWithTaskPartBoundaryNotOnSplit() {
		LocalDateTime startTime = startLocalDate.atTime(23, 0);
		LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
		WorkPeriod p = new WorkPeriod(startTime, endTime);
		Task t1 = new Task(0, 40, "");
		TaskPart tp1 = TaskPart.wholeOf(t1);
		Task t2 = new Task(0, 40, "");
		TaskPart tp2 = TaskPart.wholeOf(t2);
		Task t3 = new Task(0, 20, "");
		TaskPart tp3 = TaskPart.wholeOf(t3);
		p.addTaskPart(tp1);
		p.addTaskPart(tp2);
		p.addTaskPart(tp3);
		Optional<WorkPeriod> newFirstHalf = p.split();

		LocalDateTime midnight = endTime.withHour(0);
		WorkPeriod expectedFirstHalf = new WorkPeriod(startTime, midnight);
		expectedFirstHalf.addTaskPart(tp1);
		expectedFirstHalf.addTaskPart(new TaskPart(t2, Duration.ofMinutes(20), 1));
		assertEquals(Optional.of(expectedFirstHalf), newFirstHalf);

		WorkPeriod expectedSecondHalf = new WorkPeriod(midnight, endTime);
		expectedSecondHalf.addTaskPart(new TaskPart(t2, Duration.ofMinutes(20), 2));
		expectedSecondHalf.addTaskPart(tp3);
		assertEquals(expectedSecondHalf, p);
	}

	@Test
	public void testSplitOnEndTime() {
		WorkPeriod p = new WorkPeriod(startLocalDate.atTime(22,0),startLocalDate.atTime(23,0));
		p.split(startLocalDate.atTime(23, 0));
		assertTrue(p.getEndTime().equals(p.getStartTime()));
	}

	@Test
	public void testSplitOnStartTime() {
		WorkPeriod p = new WorkPeriod(startLocalDate.atTime(22,0),startLocalDate.atTime(23,0));
		Optional<WorkPeriod> split = p.split(startLocalDate.atTime(22, 0));
		assertFalse(split.isPresent());
	}

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testOverLongPeriodRejected() {
		LocalDateTime startTime = startLocalDate.atTime(0, 1);
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Periods cannot span more than two days");
		new WorkPeriod(startTime, startTime.plusDays(2));
	}

	@Test
	public void startMidnightMoreThan24Hours() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("cannot span more than two days");
		new WorkPeriod(startLocalDate.atTime(0,0), startLocalDate.plusDays(2).atTime(0,1));
	}
}
