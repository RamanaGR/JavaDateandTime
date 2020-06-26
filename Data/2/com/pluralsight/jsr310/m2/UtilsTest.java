package com.pluralsight.jsr310.m2;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class UtilsTest {

	@Test
	public void testGenerateWorkPeriods() {
		LocalDate thur24May2017 = LocalDate.of(2018, 5, 24);
		List<WorkPeriod> workPeriods = Utils.generateWorkPeriods(thur24May2017, 3);
		assertEquals(6, workPeriods.size());
		assertEquals(Arrays.asList(THURSDAY, FRIDAY, MONDAY),
				workPeriods.stream()
						.map(WorkPeriod::getStartTime)
						.map(LocalDateTime::getDayOfWeek)
						.distinct()
						.collect(toList()));
	}
}
