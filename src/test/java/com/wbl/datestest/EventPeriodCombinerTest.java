package com.wbl.datestest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.NavigableSet;

import org.junit.Before;
import org.junit.Test;

import com.wbl.dates.Calendar;
import com.wbl.dates.Event;
import com.wbl.dates.WorkPeriod;

public class EventPeriodCombinerTest {

    private Clock clock;
    private Calendar calendar;
    private ZonedDateTime startZDateTime;
    private LocalDate startLocalDate;

    /**
     * Setup Method to Initialize Objects.
     */
    @Before
    public void setup() {
        calendar = new Calendar();
        clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        startZDateTime = ZonedDateTime.now(clock);
        startLocalDate = LocalDate.from(startZDateTime);
    }

    @Test
    public void testNoWorkPeriods() {
        calendar.addEvent(Event.of(startZDateTime, startZDateTime.plusHours(1), ""));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertTrue(combined.isEmpty());
    }

    @Test
    public void testSingleWorkPeriod() {
        WorkPeriod p1 = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(2, 0));
        calendar.addWorkPeriod(p1);

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod p2 = combined.first();
        assertEquals(p2.getStartTime(), p1.getStartTime());
        assertEquals(p2.getEndTime(), p2.getEndTime());
    }

    @Test
    public void testNoOverlapPeriodFirst() {
        calendar.addEvent(Event.of(startZDateTime.withHour(3), startZDateTime.withHour(4), ""));
        WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(2, 0));
        calendar.addWorkPeriod(period);

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.first();
        assertEquals(period.getStartTime(), per.getStartTime());
        assertEquals(period.getEndTime(), per.getEndTime());
    }

    @Test
    public void testNoOverlapEventFirst() {
        calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(2), ""));
        WorkPeriod period = new WorkPeriod(startLocalDate.atTime(3, 0), startLocalDate.atTime(4, 0));
        calendar.addWorkPeriod(period);

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.first();
        assertEquals(period.getStartTime(), per.getStartTime());
        assertEquals(period.getEndTime(), per.getEndTime());
    }

    @Test
    public void testSimpleOverlapPeriodFirst() {
        Event event = Event.of(startZDateTime.withHour(2), startZDateTime.withHour(4), "");
        calendar.addEvent(event);
        WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(3, 0));
        calendar.addWorkPeriod(period);

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.first();
        assertEquals(period.getStartTime(), per.getStartTime());
        assertEquals(startZDateTime.withHour(2).toLocalDateTime(), per.getEndTime());
    }

    @Test
    public void testSimpleOverlapEventFirst() {
        calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(3), ""));
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(4, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.first();
        assertEquals(startZDateTime.withHour(3).toLocalDateTime(), per.getStartTime());
        assertEquals(startLocalDate.atTime(4, 0), per.getEndTime());
    }

    @Test
    public void testPeriodSurroundsEvent() {
        Event event = Event.of(startZDateTime.withHour(2), startZDateTime.withHour(3), "");
        calendar.addEvent(event);
        WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(4, 0));
        calendar.addWorkPeriod(period);

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(2, combined.size());
        WorkPeriod per = combined.pollFirst();
        assertEquals(startLocalDate.atTime(1, 0), per.getStartTime());
        assertEquals(startZDateTime.withHour(2).toLocalDateTime(), per.getEndTime());
        per = combined.pollFirst();
        assertEquals(startZDateTime.withHour(3).toLocalDateTime(), per.getStartTime());
        assertEquals(startLocalDate.atTime(4, 0), per.getEndTime());
    }

    @Test
    public void testEventSurroundsPeriod() {
        calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(4), ""));
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(3, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertTrue(combined.isEmpty());
    }

    @Test
    public void testSimultaneousStartEventLonger() {
        calendar.addEvent(Event.of(startZDateTime, startZDateTime.withHour(3), ""));
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atStartOfDay(), startLocalDate.atTime(3, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertTrue(combined.isEmpty());
    }

    @Test
    public void testSimultaneousStartPeriodLonger() {
        Event event = Event.of(startZDateTime.withHour(1), startZDateTime.withHour(3), "");
        calendar.addEvent(event);
        WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(4, 0));
        calendar.addWorkPeriod(period);

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.pollFirst();
        assertEquals(startZDateTime.withHour(3).toLocalDateTime(), per.getStartTime());
        assertEquals(period.getEndTime(), per.getEndTime());
    }

    @Test
    public void testSimultaneousEndEventLonger() {
        calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(4), ""));
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(4, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertTrue(combined.isEmpty());
    }

    @Test
    public void testSimultaneousEndPeriodLonger() {
        Event event = Event.of(startZDateTime.withHour(2), startZDateTime.withHour(4), "");
        calendar.addEvent(event);
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(4, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.pollFirst();
        assertEquals(startLocalDate.atTime(1, 0), per.getStartTime());
        assertEquals(startZDateTime.withHour(2).toLocalDateTime(), per.getEndTime());
    }

    @Test
    public void testAbuttingPeriodFirst() {
        calendar.addEvent(Event.of(startZDateTime.withHour(2), startZDateTime.withHour(3), ""));
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(2, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.pollFirst();
        assertEquals(startLocalDate.atTime(1, 0), per.getStartTime());
        assertEquals(startLocalDate.atTime(2, 0), per.getEndTime());
    }

    @Test
    public void testAbuttingEventFirst() {
        calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(2), ""));
        calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(3, 0)));

        NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

        assertEquals(1, combined.size());
        WorkPeriod per = combined.pollFirst();
        assertEquals(startLocalDate.atTime(2, 0), per.getStartTime());
        assertEquals(startLocalDate.atTime(3, 0), per.getEndTime());
    }
}
