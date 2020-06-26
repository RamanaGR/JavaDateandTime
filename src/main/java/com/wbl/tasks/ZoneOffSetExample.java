package com.wbl.tasks;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.List;

import com.wbl.dates.Utils;
import com.wbl.dates.WorkPeriod;

public class ZoneOffSetExample {
    /**
     * Which work period will be usable on my travel day.
     *
     * @param args Strings
     */
    public static void main(String[] args) {

        List<WorkPeriod> wps = Utils.generateWorkPeriods(LocalDate.now(), 3);
        ZoneOffset departureZone = ZoneOffset.of("+0");

        ZoneOffset destinationZone = ZoneOffset.of("-5");
        LocalDateTime destinationLandingTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 0));

        OffsetDateTime landingTime = OffsetDateTime.of(destinationLandingTime, destinationZone);
        LocalDateTime departureZoneLandingTime =
                LocalDateTime.ofInstant(landingTime.toInstant(), departureZone);

        List<WorkPeriod> usableWorkPeriods = wps.stream()
                .filter(wp -> wp.getEndTime().isAfter(LocalDateTime.now()))
                .filter(wp -> wp.getEndTime().isBefore(departureZoneLandingTime)
                        || wp.getStartTime().isAfter(departureZoneLandingTime))
                .collect(toList());
        System.out.println(usableWorkPeriods);
    }
}
