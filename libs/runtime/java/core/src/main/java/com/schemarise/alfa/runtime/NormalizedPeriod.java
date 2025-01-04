package com.schemarise.alfa.runtime;

import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class NormalizedPeriod implements ChronoPeriod, Comparable<NormalizedPeriod> {

    private Period under;
    private static Map<String, NormalizedPeriod> nperiods = new WeakHashMap<>();

    public static NormalizedPeriod of(String s) {

        NormalizedPeriod existing = nperiods.get(s);

        if (existing != null)
            return existing;

        Period np = Period.parse(s).normalized();
        NormalizedPeriod l = new NormalizedPeriod(np);
        nperiods.put(s, l);

        return l;
    }

    public static NormalizedPeriod of(Period p) {
        String s = p.toString();

        NormalizedPeriod existing = nperiods.get(s);

        if (existing != null)
            return existing;

        Period np = p.normalized();
        NormalizedPeriod l = new NormalizedPeriod(np);
        nperiods.put(s, l);

        return l;
    }

    private NormalizedPeriod(Period under) {
        this.under = under;
    }

    public Period getRawValue() {
        return under;
    }

    @Override
    public long get(TemporalUnit unit) {
        return under.get(unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return under.getUnits();
    }

    @Override
    public IsoChronology getChronology() {
        return under.getChronology();
    }

    @Override
    public boolean isZero() {
        return under.isZero();
    }

    @Override
    public boolean isNegative() {
        return under.isNegative();
    }

    public int getYears() {
        return under.getYears();
    }

    public int getMonths() {
        return under.getMonths();
    }

    public int getDays() {
        return under.getDays();
    }

    public NormalizedPeriod withYears(int years) {
        return new NormalizedPeriod(under.withYears(years));
    }

    public NormalizedPeriod withMonths(int months) {
        return new NormalizedPeriod(under.withMonths(months));
    }

    public NormalizedPeriod withDays(int days) {
        return new NormalizedPeriod(under.withDays(days));
    }

    @Override
    public NormalizedPeriod plus(TemporalAmount amountToAdd) {
        return new NormalizedPeriod(under.plus(amountToAdd));
    }

    public NormalizedPeriod plusYears(long yearsToAdd) {
        return new NormalizedPeriod(under.plusYears(yearsToAdd));
    }

    public NormalizedPeriod plusMonths(long monthsToAdd) {
        return new NormalizedPeriod(under.plusMonths(monthsToAdd));
    }

    public NormalizedPeriod plusDays(long daysToAdd) {
        return new NormalizedPeriod(under.plusDays(daysToAdd));
    }

    @Override
    public NormalizedPeriod minus(TemporalAmount amountToSubtract) {
        return new NormalizedPeriod(under.minus(amountToSubtract));
    }

    public NormalizedPeriod minusYears(long yearsToSubtract) {
        return new NormalizedPeriod(under.minusYears(yearsToSubtract));
    }

    public NormalizedPeriod minusMonths(long monthsToSubtract) {
        return new NormalizedPeriod(under.minusMonths(monthsToSubtract));
    }

    public NormalizedPeriod minusDays(long daysToSubtract) {
        return new NormalizedPeriod(under.minusDays(daysToSubtract));
    }

    @Override
    public NormalizedPeriod multipliedBy(int scalar) {
        return new NormalizedPeriod(under.multipliedBy(scalar));
    }

    @Override
    public NormalizedPeriod negated() {
        return new NormalizedPeriod(under.negated());
    }

    @Override
    public NormalizedPeriod normalized() {
        return this;
    }

    public long toTotalMonths() {
        return under.toTotalMonths();
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return under.addTo(temporal);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return under.subtractFrom(temporal);
    }

//    @Override
//    public boolean equals(Object obj) {
//        return under.equals(obj);
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NormalizedPeriod that = (NormalizedPeriod) o;
        return compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return under.hashCode();
    }

    @Override
    public String toString() {
        return under.toString();
    }

    @Override
    public int compareTo(NormalizedPeriod o) {

        int y = Integer.compare(under.getYears(), o.getYears());
        if (y != 0)
            return y;

        int m = Integer.compare(under.getMonths(), o.getMonths());
        if (m != 0)
            return m;

        return Integer.compare(under.getDays(), o.getDays());
    }
}
