package de.unibielefeld.techfak.tdpe.prorema.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * @author Benedikt Volkmer
 *         Created on 6/19/16.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class LocalDateInterval implements Comparable<LocalDateInterval> {

    @NonNull private final LocalDate start;
    @NonNull private final LocalDate end;

    /**
     * Checks if this localDateInterval overlaps with the other at some localDateInterval.
     *
     * @param o other localDateInterval
     * @return true if it overlaps, false else
     */
    public boolean overlaps(@NonNull LocalDateInterval o) {
        return  o.equals(this)
                || (start.isBefore(o.end) && o.start.isBefore(end));
    }

    /**
     * Checks if this localDateInterval encloses the other localDateInterval.
     *
     * @param o other localDateInterval
     * @return true if equal or encloses, false if not
     */
    public boolean encloses(@NonNull LocalDateInterval o) {
        return (start.isBefore(o.getStart()) && end.isAfter(o.getEnd()))
                || (start.isEqual(o.getStart()) && end.isAfter(o.getEnd()))
                || (start.isBefore(o.getStart()) && end.isEqual(o.getEnd()))
                || (start.isEqual(o.getStart()) && end.isEqual(o.getEnd()));
    }

    /**
     * Get period of this interval.
     *
     * @return period between start and end of this interval.
     */
    public int toPeriod() {
        int endYear = end.getYear();
        int startYear = start.getYear();
        int ret;
        if (endYear == startYear) {
            ret = end.getDayOfYear()            // week is from monday to sunday,
                    - start.getDayOfYear()      // these are 6 days,
                    + 1;                        // so in two weeks it would have 13 days instead of
        } else {                                // 14 but it would be 2 full weeks,
            if (endYear > startYear) {          // so it adds +1 to get the 14 for new calculations
                ret = LocalDate.of(startYear, 12 ,31).getDayOfYear()
                        - start.getDayOfYear()
                        + end.getDayOfYear()
                        + 1;
            } else {
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Get the difference by the other Interval.
     *
     * @param o the other interval
     * @return Return this interval without the overlap of the other, null if this is enclosed by the other or equal
     */
    public LocalDateInterval getDifference(LocalDateInterval o) {
        LocalDateInterval overlap = getOverlap(this, o);
        if (overlap == null) {
            return of(this);
        } else if (overlap.encloses(this)) {
            return null;
        } else if (overlap.start.compareTo(this.start) == 0) {
            return new LocalDateInterval(overlap.end, this.end);
        } else if (overlap.end.compareTo(this.end) == 0) {
            return new LocalDateInterval(this.start, overlap.start);
        } else {
            throw new RuntimeException("Impossible path executed.");
        }
    }

    /**
     * Compare this to the other interval.
     * <p><ul>
     * <li>= 0 if this encloses the other or the other encloses this. This includes equality.</li>
     * <li>< 0 if this.start is less then the other start</li>
     * <li>> 0 if this.end is greater the the other end</li>
     * </ul>
     * </p>
     *
     * @param o the comparable opposite
     * @return See above
     */
    @Override public int compareTo(LocalDateInterval o) {
        if (o.encloses(this) || this.encloses(o)) {
            return 0;
        } else {
            int startDifference = this.start.compareTo(o.start);
            int endDifference = this.end.compareTo(o.end);
            return (startDifference + endDifference) / 2;
        }
    }

    /**
     * Generate copy of the specified localDateInterval.
     *
     * @param localDateInterval localDateInterval to create copy of
     * @return a copied localDateInterval
     */
    public static LocalDateInterval of(@NonNull LocalDateInterval localDateInterval) {
        return new LocalDateInterval(localDateInterval.start, localDateInterval.end);
    }

    /**
     * Get the localDateInterval that two intervals overlap.
     *
     * @param a one localDateInterval to get overlap of
     * @param b other localDateInterval to get overlap of
     * @return overlapping localDateInterval, or null if not overlapping
     */
    public static LocalDateInterval getOverlap(@NonNull LocalDateInterval a, @NonNull LocalDateInterval b) {
        if (!a.overlaps(b)) {
            return null;
        } else if (b.encloses(a)) {
            return of(a);
        } else if (a.encloses(b)) {
            return of(b);
        } else if (a.start.compareTo(b.start) <= 0 && a.end.compareTo(b.end) <= 0) {
            return new LocalDateInterval(b.start, a.end);
        } else if (b.start.compareTo(a.start) <= 0 && b.end.compareTo(a.end) <= 0) {
            return new LocalDateInterval(a.start, b.end);
        }

        throw new RuntimeException("Impossible path executed.");
    }
}
