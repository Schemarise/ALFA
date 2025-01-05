package com.schemarise.alfa.runtime.codec.table;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.utils.stream.IBlockingStream;
import com.schemarise.alfa.runtime_int.mstream.DefaultBlockingStream;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime_int.IntImpl;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class TableCodec {
    public static ITable toTable(Collection<? extends AlfaObject> alfaObjects) {
        return IntImpl.getTableCodecInstance().toTable(alfaObjects);
    }

    public static ITable toTable(AlfaObject alfaObject) {
        return IntImpl.getTableCodecInstance().toTable(alfaObject);
    }

    public static <T extends AlfaObject> Stream<T> importRowBasedObjects(CodecConfig bc,
                                                                         String expectedType,
                                                                         Optional<List<String>> dataColumnNames,
                                                                         Stream<List<Object>> rowBasedData,
                                                                         Map<String, Function<Object, Object>> preProcessors) {
        return IntImpl.getTableCodecInstance().importRowBasedObjects(bc, expectedType, dataColumnNames, rowBasedData, preProcessors);
    }

    public static <T extends AlfaObject> T importRowBasedObject(CodecConfig bc,
                                                                String expectedType,
                                                                Optional<List<String>> dataColumnNames,
                                                                List<Object> rowBasedData,
                                                                String sourceLineInfo,
                                                                Map<String, Function<Object, Object>> preProcessors) {
        return IntImpl.getTableCodecInstance().importRowBasedObject(bc, expectedType, dataColumnNames, rowBasedData, sourceLineInfo, preProcessors);
    }

    public static <T extends AlfaObject> Stream<T> importCsv(Path path, CsvReaderConfig csvCfg,
                                                             CodecConfig bc,
                                                             String expectedType,
                                                             Optional<List<String>> dataColumnNames,
                                                             Map<String, Function<Object, Object>> preProcessors) throws Exception {
        InputStream s = readAsStream(path);
        Stream<AlfaObject> res = importCsv(s, csvCfg, bc, expectedType, dataColumnNames, preProcessors);
        return (Stream<T>) res;
    }

    public static <T extends AlfaObject> Stream<T> importCsv(InputStream is, CsvReaderConfig csvCfg,
                                                             CodecConfig cc,
                                                             String expectedType,
                                                             final Optional<List<String>> dataColumnNames,
                                                             Map<String, Function<Object, Object>> preProcessors) {

        IBlockingStream<AlfaObject> queue = new DefaultBlockingStream<>();

        // the line reader
        Runnable r = () -> {
            CsvParser p = new CsvParser(csvCfg.getCsvParserSettings());
            ResultIterator<String[], ParsingContext> it = p.iterate(is).iterator();


            AtomicLong rowsRead = new AtomicLong();
            AtomicLong rowsProcessed = new AtomicLong();

            Holder<Optional<List<String>>> colnames = new Holder<>(dataColumnNames);

            while (it.hasNext()) {
                String[] csvline = it.next();
                final long rowNo = rowsRead.incrementAndGet();

                if (rowNo == 1 && csvCfg.isHasHeader()) {
                    if (csvCfg.isUseHeader()) {
                        if (dataColumnNames.isPresent()) {
                            throw new AlfaRuntimeException("If CSV use-header is set, list of data-columns-names cannot be set");
                        }
                        colnames.setValue(Optional.of(Arrays.asList(csvline)));
                    }
                    rowsProcessed.incrementAndGet();
                    continue;
                }

                // each line is handed to a worker
                cc.getExecutorService().submit(() -> {
                    AlfaObject ax = null;
                    try {
                        long rowsSoFar = cc.getAssertListener().incrementTotalRecords();
                        ax = importRowBasedObject(cc, expectedType, colnames.getValue(), Arrays.asList(csvline), "Line:" + rowsSoFar, preProcessors);
                        // worker deposits AO to disruptor
                        queue.deposit(ax);
                    } catch (AlfaRuntimeException t) {
                        cc.getAssertListener().addFailure(t.toValidationAlert("Failed to process line no " + rowNo));
                    } catch (Throwable t) {
                        String msg = t.getMessage() == null ? t.getClass().getName() : t.getMessage();
                        cc.getAssertListener().addFailure(ValidationAlert.builder().
                                setMessage("Failed to process line " + rowNo + ". " + msg));

                        var l = Logger.getOrCreateDefault();
                        if (l.isTraceEnabled()) {
                            l.trace(Logger.stacktraceToString(t, 20));
                        }
                    } finally {
                        rowsProcessed.incrementAndGet();
                    }

                    return ax;
                });
            }

            // Wait for everything to be processed before pushing the null
            wait(10, false, 100 * 60, rowsRead, rowsProcessed, expectedType); // check every 10 ms, for 1 mins
            wait(5000, true, 12 * 60, rowsRead, rowsProcessed, expectedType);    // check every 5 sec for 60 mins

            // Wait for everything to be processed before pushing the null

            if (rowsRead.get() != rowsProcessed.get()) {
                Logger.getOrCreateDefault().error("Did not complete in 10 minutes. Exit processing " + expectedType);
            }

            queue.deposit(null);
        };

        // start the line reader/controller thread
        cc.getExecutorService().submit(r);

        return (Stream<T>) queue.getStream();
    }

    private static void wait(int interval, boolean log, int limit, AtomicLong rowsRead, AtomicLong rowsProcessed, String expectedType) {
        long iterations = 0;


        while (rowsRead.get() != rowsProcessed.get()) {
            try {
                Thread.sleep(interval);
                iterations++;

                if (iterations > limit) {
                    break;
                }

                if (log) {
                    Logger.getOrCreateDefault().info("Reading " + expectedType + " tabular data. " + rowsRead.get() + " of " + rowsRead.get());
                }

            } catch (InterruptedException e) {
            }
        }
    }

    private static InputStream readAsStream(Path path) throws Exception {
        if (path.toString().endsWith(".zip")) {
            ZipFile zipFile = new ZipFile(path.toFile());

            FileInputStream fileInputStream = new FileInputStream(path.toFile());
            ZipInputStream zin = new ZipInputStream(fileInputStream);
            return zipFile.getInputStream(zin.getNextEntry());
        } else {
            return Files.newInputStream(path);
        }
    }

    public static class CsvReaderConfig {
        private CsvParserSettings _csvParserSettings = new CsvParserSettings();
        private boolean hasHeader;


        public boolean isHasHeader() {
            return hasHeader;
        }

        public boolean isUseHeader() {
            return useHeader;
        }

        private boolean useHeader;


        public static CsvReaderConfig defaultCsvReaderConfig() {
            return new CsvReaderConfig();
        }

        public void setCsvHasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader;
        }

        public void setCsvUseHeader(boolean useHeader) {
            this.useHeader = useHeader;
        }

        public CsvReaderConfig() {
            _csvParserSettings.setHeaderExtractionEnabled(false);
        }

//        public CsvReaderConfig(CsvParserSettings csvParser, int skipLines) {
//            this._csvParserSettings = csvParser;
//        }

        public void setColumnDelimiter(String d) {
            _csvParserSettings.getFormat().setDelimiter(d);
        }


        public CsvParserSettings getCsvParserSettings() {
            return _csvParserSettings;
        }

        public void setCsvParser(CsvParserSettings csvParser) {
            this._csvParserSettings = csvParser;
        }
    }

    private static class BufferedReaderIterator implements Iterator<String[]> {
        private BufferedReader br;
        private java.lang.String line;

        public BufferedReaderIterator(BufferedReader aBR) {
            (br = aBR).getClass();
            advance();
        }

        public boolean hasNext() {
            return line != null;
        }

        public String[] next() {
            String retval = line;
            advance();
            return retval.split(",");
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported on BufferedReader iteration.");
        }

        private void advance() {
            try {
                line = br.readLine();
            } catch (IOException e) { /* TODO */}
        }
    }
}
