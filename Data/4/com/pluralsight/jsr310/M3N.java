package com.pluralsight.jsr310;

import com.pluralsight.jsr310.m3.Utils;
import com.pluralsight.jsr310.m3.WorkPeriod;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class M3N {

	public static void main(String[] args) {

		List<WorkPeriod> workPeriods = Utils.generateWorkPeriods(LocalDate.of(2017,10,26), 6);

		ZoneId myZone = ZoneId.of("Europe/London");               // in the UK the clocks went back on 29th October 
		ZoneId theirZone = ZoneId.of("America/Chicago");          // in the US they went back on 5th November

		List<Interval> myIntervals = workPeriods.stream().map(wp -> wp.toInterval(myZone)).collect(toList());
		List<Interval> theirIntervals = workPeriods.stream().map(wp -> wp.toInterval(theirZone)).collect(toList());

		Duration intersectDuration = Duration.ZERO;
		for (Interval myIntvl : myIntervals) {
			for (Interval theirIntvl : theirIntervals) {
				if (myIntvl.isConnected(theirIntvl)) {
					intersectDuration = intersectDuration.plus(myIntvl.intersection(theirIntvl).toDuration());
				}
			}
		}
		System.out.println(intersectDuration);
	}
}
