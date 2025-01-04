package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.ILogger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AlfaStopWatch {
    private final ILogger logger;
    private Instant start;
    private List<Duration> laps = new ArrayList<>();

    public AlfaStopWatch(ILogger l) {
        logger = l;
    }

    public void startLap() {
        if (logger.isDebugEnabled())
            start = Instant.now();
    }

    public void stopLap(String prefix) {
        if (logger.isDebugEnabled()) {
            laps.add(Duration.between(start, Instant.now()));
            periodicLog(true, prefix);
        }
    }


    private void periodicLog(boolean force, String prefix) {
        if (force || (laps.size() == 1000 && laps.size() > 2)) {
            long total = laps.stream().map(e -> e.toNanos()).mapToLong(Long::longValue).sum();

            logger.debug(prefix + " Elapsed time for 100k iterations : " + (((double) total) / (1000000 * laps.size())) + "ms");
            laps.clear();
        }
    }
}
