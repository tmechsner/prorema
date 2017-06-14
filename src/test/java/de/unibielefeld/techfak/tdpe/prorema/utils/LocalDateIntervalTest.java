package de.unibielefeld.techfak.tdpe.prorema.utils;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Random;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 6/19/16.
 */
public class LocalDateIntervalTest {

    private Random random = new Random();

    @Test
    public void toPeriod() throws Exception {
        // given
        Integer offset = (random.nextInt(100)+1)*2;
        LocalDateInterval interval = new LocalDateInterval(LocalDate.now().withDayOfYear(offset/2),
                                                           LocalDate.now().withDayOfYear(offset));
        // when
        Integer result = interval.toPeriod();
        // then
        assertThat(result).as("in one year and offset is " + offset).isEqualTo(offset/2+1);

        // given
        interval = new LocalDateInterval(LocalDate.now().withDayOfYear(1).minusDays(offset), LocalDate.now().withDayOfYear(offset));
        // when
        result = interval.toPeriod();
        // then
        assertThat(result).as("in two years and offset is " + offset).isEqualTo(offset*2);
    }

    @Test
    public void getDifference() throws Exception {
        // given
        LocalDateInterval a = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));
        LocalDateInterval b = new LocalDateInterval(LocalDate.now().minusWeeks(2), LocalDate.now());
        // then
        assertThat(a.getDifference(b)).as("a and b do not overlap").isEqualTo(a);

        // given
        b = new LocalDateInterval(LocalDate.now().minusDays(2), LocalDate.now().plusDays(1));
        // then
        assertThat(a.getDifference(b)).as("b overlaps the first day")
                                      .isEqualTo(new LocalDateInterval(b.getEnd(), a.getEnd()));

        // given
        b = new LocalDateInterval(LocalDate.now().plusWeeks(1).minusDays(1), LocalDate.now().plusWeeks(1).plusDays(1));
        // then
        assertThat(a.getDifference(b)).as("b overlaps the last day")
                                      .isEqualTo(new LocalDateInterval(a.getStart(), b.getStart()));

        // given
        b = LocalDateInterval.of(a);
        // then
        assertThat(a.getDifference(b)).as("a and b are equal").isNull();
    }

    @Test
    public void compareTo() throws Exception {
        // given
        LocalDateInterval a = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));
        LocalDateInterval b = new LocalDateInterval(LocalDate.now().minusWeeks(2), LocalDate.now());
        // then
        assertThat(a.compareTo(b)).as("a is after b").isGreaterThan(0);

        // given
        b = new LocalDateInterval(LocalDate.now().plusWeeks(1), LocalDate.now().plusWeeks(2));
        // then
        assertThat(a.compareTo(b)).as("a is before b").isLessThan(0);

        // given
        b = LocalDateInterval.of(a);
        // then
        assertThat(a.compareTo(b)).as("a is equal to b").isEqualTo(0);
    }

    @Test
    public void overlaps() throws Exception {
        // given
        LocalDateInterval a = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));
        LocalDateInterval b = new LocalDateInterval(LocalDate.now().minusDays(2), LocalDate.now().plusDays(1));

        // then
        assertThat(a.overlaps(b)).as("a overlaps b").isTrue();
        assertThat(b.overlaps(a)).as("b overlaps a").isTrue();

        // given
        b = new LocalDateInterval(LocalDate.now().minusWeeks(2), LocalDate.now());
        // then
        assertThat(a.overlaps(b)).as("a does not overlap b").isFalse();
        assertThat(b.overlaps(a)).as("b does not overlap a").isFalse();
    }

    @Test
    public void encloses() throws Exception {
        // given
        LocalDateInterval a = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));
        LocalDateInterval b = new LocalDateInterval(LocalDate.now().plusDays(1),
                                                    LocalDate.now().plusWeeks(1).minusDays(1));

        // then
        assertThat(a.encloses(b)).as("a encloses b").isTrue();
        assertThat(b.encloses(a)).as("a encloses b").isFalse();
    }

    @Test
    public void of() throws Exception {
        // given
        LocalDateInterval interval = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));

        // when
        LocalDateInterval copy = LocalDateInterval.of(interval);

        // then
        assertThat(interval.equals(copy) && interval != copy).as("copy has to be equal, but not the same").isTrue();
    }

    @Test
    public void getOverlap() throws Exception {
        // given
        LocalDateInterval a = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));
        LocalDateInterval b = new LocalDateInterval(LocalDate.now().minusDays(2), LocalDate.now().plusDays(1));

        // then
        assertThat(LocalDateInterval.getOverlap(a, b))
                .isEqualTo(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusDays(1)));

        // given
        b = new LocalDateInterval(LocalDate.now().plusDays(2), LocalDate.now().plusWeeks(1).plusDays(1));
        // then
        assertThat(LocalDateInterval.getOverlap(a, b))
                .isEqualTo(new LocalDateInterval(LocalDate.now().plusDays(2), LocalDate.now().plusWeeks(1)));

        // given
        b = new LocalDateInterval(LocalDate.now().plusDays(1), LocalDate.now().plusWeeks(1).minusDays(1));
        // then
        assertThat(LocalDateInterval.getOverlap(a, b)).as("a encloses b").isEqualTo(b);
        assertThat(LocalDateInterval.getOverlap(b, a)).as("a encloses b").isEqualTo(b);

        // given
        b = LocalDateInterval.of(a);
        // then
        assertThat(LocalDateInterval.getOverlap(b, a)).as("a is equal to b").isEqualTo(b).isEqualTo(a);

        // given
        b = new LocalDateInterval(LocalDate.MIN, a.getStart());
        // then
        assertThat(LocalDateInterval.getOverlap(a, b)).as("a and b do not overlap").isNull();
    }

}