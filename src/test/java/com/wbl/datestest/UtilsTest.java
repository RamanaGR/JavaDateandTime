package com.wbl.datestest;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.wbl.dates.Utils;
import com.wbl.dates.WorkPeriod;


public class UtilsTest {

    @Test
    public void testGenerateWorkPeriods() {
        LocalDate thur24May2017 = LocalDate.of(2020, 5, 22);
        List<WorkPeriod> workPeriods = Utils.generateWorkPeriods(thur24May2017, 3);
        assertEquals(6, workPeriods.size());
        assertEquals(Arrays.asList(FRIDAY, MONDAY, TUESDAY),
                workPeriods.stream()
                        .map(WorkPeriod::getStartTime)
                        .map(LocalDateTime::getDayOfWeek)
                        .distinct()
                        .collect(toList()));
    }
}
