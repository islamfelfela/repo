package com.elm.scriptrunner.scriptfields


import com.elm.scriptrunner.library.CommonUtil
import groovy.transform.Field

import java.sql.Timestamp
import java.time.DayOfWeek
import java.time.Instant;
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.Duration


@Field def WORK_HOUR_START = 8;
@Field def WORK_HOUR_END = 17;
@Field def MINUTES = 60;

@Field def WORKING_HOURS_PER_DAY = WORK_HOUR_END - WORK_HOUR_START;
@Field def WORKING_MINUTES_PER_DAY = WORKING_HOURS_PER_DAY * MINUTES;

def getWorkingMinutesSince(final Timestamp startTime) {
    Timestamp now = Timestamp.from(Instant.now());
    return getWorkingMinutes(startTime, now);
}

def getWorkingMinutes(final Timestamp startTime, final Timestamp endTime) {
    if (null == startTime || null == endTime) {
        throw new IllegalStateException();
    }
    if (endTime.before(startTime)) {
        return 0;
    }

    LocalDateTime from = startTime.toLocalDateTime();
    LocalDateTime to = endTime.toLocalDateTime();

    LocalDate fromDay = from.toLocalDate();
    LocalDate toDay = to.toLocalDate();
    def allDaysBetween
    def allWorkingMinutes

    if (ChronoUnit.DAYS.between(fromDay, toDay) == 0) {
        allWorkingMinutes = WORKING_MINUTES_PER_DAY
    }else{
//        allDaysBetween = (int) (ChronoUnit.DAYS.between(fromDay, toDay) + 1)
        allDaysBetween = (int) (ChronoUnit.DAYS.between(fromDay, toDay))
//        log.warn(allDaysBetween)
        allWorkingMinutes = (0..allDaysBetween)
            .findAll{i -> isWorkingDay(from.plusDays(i))}
            .collect().size() * WORKING_MINUTES_PER_DAY

//        log.warn(WORKING_MINUTES_PER_DAY)

    }
    // from - working_day_from_start
    def tailRedundantMinutes = 0;
    if (isWorkingDay(from)) {
        if (isWorkingHours(from)) {
            tailRedundantMinutes = Duration.between(fromDay.atTime(WORK_HOUR_START, 0), from).toMinutes();
        } else if (from.getHour() > WORK_HOUR_START) {
            tailRedundantMinutes = WORKING_MINUTES_PER_DAY;
        }
    }

    // working_day_end - to
    def headRedundanMinutes = 0;
    if (isWorkingDay(to)) {
        if (isWorkingHours(to)) {
            headRedundanMinutes = Duration.between(to, toDay.atTime(WORK_HOUR_END, 0)).toMinutes();
        } else if (from.getHour() < WORK_HOUR_START) {
            headRedundanMinutes = WORKING_MINUTES_PER_DAY;
        }
    }

//    log.warn(isWorkingDay(to))
//    log.warn(isWorkingDay(from))
//    log.warn(allWorkingMinutes)
//    log.warn(tailRedundantMinutes)
//    log.warn(headRedundanMinutes)

    return  (allWorkingMinutes - tailRedundantMinutes - headRedundanMinutes)
}

def isWorkingDay( LocalDateTime time) {
    def holidays = [DayOfWeek.FRIDAY.getValue(),DayOfWeek.SATURDAY.getValue()]
    return !holidays.contains(time.getDayOfWeek().getValue())
}

def isWorkingHours( LocalDateTime time) {
    int hour = time.getHour();
    return WORK_HOUR_START <= hour && hour <= WORK_HOUR_END;
}

//return (getWorkingMinutes(new Timestamp(1600843500000),new Timestamp(1603359900000))/60)
