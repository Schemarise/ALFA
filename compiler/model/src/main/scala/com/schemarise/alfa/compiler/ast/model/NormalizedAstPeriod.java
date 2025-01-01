/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.compiler.ast.model;

import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public class NormalizedAstPeriod implements ChronoPeriod, Comparable<NormalizedAstPeriod> {

    private Period under;

    public NormalizedAstPeriod(Period under) {
        this.under = under.normalized();
    }

    public Period getUnder() {
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

    public NormalizedAstPeriod withYears(int years) {
        return new NormalizedAstPeriod(under.withYears(years));
    }

    public NormalizedAstPeriod withMonths(int months) {
        return new NormalizedAstPeriod(under.withMonths(months));
    }

    public NormalizedAstPeriod withDays(int days) {
        return new NormalizedAstPeriod(under.withDays(days));
    }

    @Override
    public NormalizedAstPeriod plus(TemporalAmount amountToAdd) {
        return new NormalizedAstPeriod(under.plus(amountToAdd));
    }

    public NormalizedAstPeriod plusYears(long yearsToAdd) {
        return new NormalizedAstPeriod(under.plusYears(yearsToAdd));
    }

    public NormalizedAstPeriod plusMonths(long monthsToAdd) {
        return new NormalizedAstPeriod(under.plusMonths(monthsToAdd));
    }

    public NormalizedAstPeriod plusDays(long daysToAdd) {
        return new NormalizedAstPeriod(under.plusDays(daysToAdd));
    }

    @Override
    public NormalizedAstPeriod minus(TemporalAmount amountToSubtract) {
        return new NormalizedAstPeriod(under.minus(amountToSubtract));
    }

    public NormalizedAstPeriod minusYears(long yearsToSubtract) {
        return new NormalizedAstPeriod(under.minusYears(yearsToSubtract));
    }

    public NormalizedAstPeriod minusMonths(long monthsToSubtract) {
        return new NormalizedAstPeriod(under.minusMonths(monthsToSubtract));
    }

    public NormalizedAstPeriod minusDays(long daysToSubtract) {
        return new NormalizedAstPeriod(under.minusDays(daysToSubtract));
    }

    @Override
    public NormalizedAstPeriod multipliedBy(int scalar) {
        return new NormalizedAstPeriod(under.multipliedBy(scalar));
    }

    @Override
    public NormalizedAstPeriod negated() {
        return new NormalizedAstPeriod(under.negated());
    }

    @Override
    public NormalizedAstPeriod normalized() {
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

    @Override
    public boolean equals(Object obj) {
        return under.equals(obj);
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
    public int compareTo(NormalizedAstPeriod o) {

        int y = Integer.compare(under.getYears(), o.getYears());
        if (y != 0)
            return y;

        int m = Integer.compare(under.getMonths(), o.getMonths());
        if (m != 0)
            return m;

        return Integer.compare(under.getDays(), o.getDays());
    }
}
