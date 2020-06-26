package com.pluralsight.jsr310;

import com.pluralsight.jsr310.m3.Utils;
import com.pluralsight.jsr310.m3.WorkPeriod;

import java.time.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class M3L {

	public static void main(String[] args) {

		List<WorkPeriod> wps = Utils.generateWorkPeriods(LocalDate.now(), 3);
		ZoneOffset departureZone = ZoneOffset.of("+0");

		ZoneOffset destinationZone = ZoneOffset.of("-5");
		LocalDateTime destinationLandingTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(11,0));

		OffsetDateTime landingTime = OffsetDateTime.of(destinationLandingTime, destinationZone);
		LocalDateTime departureZoneLandingTime =
				LocalDateTime.ofInstant(landingTime.toInstant(), departureZone);

		List<WorkPeriod> usableWorkPeriods = wps.stream()
				.filter(wp -> wp.getEndTime().isAfter(LocalDateTime.now()))
				.filter(wp -> wp.getEndTime().isBefore(departureZoneLandingTime) ||
						wp.getStartTime().isAfter(departureZoneLandingTime))
				.collect(toList());
	}
}
