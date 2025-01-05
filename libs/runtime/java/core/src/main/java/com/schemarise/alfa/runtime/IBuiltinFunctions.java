package com.schemarise.alfa.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.Try;
import schemarise.alfa.runtime.model.TryFailure;
import schemarise.alfa.runtime.model.Pair;

/**
 * Always used boxed Java types as type inference is based on those.
 */
public interface IBuiltinFunctions {

    String FnIsNone = "isNone";
    String FnIsSome = "isSome";
    String FnDistinct = "distinct";
    String FnFlatten = "flatten";
    String FnGroupBy = "groupBy";
    String FnAggregate = "aggregate";
    String FnDuplicates = "duplicates";
    String FnGetOrElse = "getOrElse";
    String FnSome = "some";
    String FnToDate = "toDate";
    String FnToDatetime = "toDatetime";
    String FnToDatetimetz = "toDatetimetz";
    String FnToTime = "toTime";
    String FnToDecimal = "toDecimal";
    String FnToDouble = "toDouble";
    String FnToDuration = "toDuration";
    String FnToPeriod = "toPeriod";
    String FnToInt = "toInt";
    String FnToList = "toList";
    String FnToSet = "toSet";
    String FnNewEitherLeft = "newEitherLeft";
    String FnNewEitherRight = "newEitherRight";
    String FnIsLeft = "isLeft";
    String FnIsRight = "isRight";
    String FnNewTryValue = "newTryValue";
    String FnNewTryFailure = "newTryFailure";
    String FnIsTryFailure = "isTryFailure";
    String FnNewUUID = "newUUID";
    String FnNow = "now";
    String FnToday = "today";
    String FnYear = "year";
    String FnMonth = "month";
    String FnDay = "day";
    String FnWeekday = "weekday";
    String FnHour = "hour";
    String FnMinute = "minute";
    String FnMillisecond = "millisecond";
    String FnSecond = "second";
    String FnDateDiff = "dateDiff";
    String FnStddev = "stddev";
    String FnVariance = "variance";
    String FnAverage = "average";
    String FnQuartile = "quartile";
    String FnPercentile = "percentile";
    String FnToEnum = "toEnum";
    String FnPublish = "publish";
    String FnKeyExists = "keyExists";
    String FnToUpper = "toUpper";
    String FnTransform = "transform";
    String FnLen = "len";
    String FnAdd = "add";
    String FnGet = "get";
    String FnPut = "put";
    String FnToString = "toString";
    String FnValues = "values";
    String FnAbs = "abs";
    String FnSqrt = "sqrt";
    String FnLog = "log";
    String FnMin = "min";
    String FnMax = "max";
    String FnIndexOf = "indexOf";
    String FnIsEmpty = "isEmpty";
    String FnStartsWith = "startsWith";
    String FnEndsWith = "endsWith";
    String FnSubstring = "substring";
    String FnMatches = "matches";
    String FnContains = "contains";
    String FnReplaceAll = "replaceAll";
    String FnSplit = "split";
    String FnCompare = "compare";
    String FnToMap = "toMap";
    String FnDelete = "delete";
    String FnDebug = "debug";
    String FnIsSet = "isSet";
    String FnTimestamp = "timestamp";
    String FnSave = "save";
    String FnKeys = "keys";
    String FnUnwrap = "unwrap";
    String FnExists = "exists";
    String FnQuery = "query";
    String FnToLower = "toLower";
    String FnSort = "sort";
    String FnLookup = "lookup";
    String FnFilter = "filter";
    String FnCeil = "ceil";
    String FnFloor = "floor";
    String FnRound = "round";
    String FnRandom = "random";
    String FnMap = "map";
    String FnLeft = "left";
    String FnRight = "right";
    String FnReduce = "reduce";


    @CommonFunc
    Void debug(Object s);

    @CommonFunc
    <T> Void add(@NonUdtFieldReference Set<T> s, T e);

    @CommonFunc
    <T> Void add(@NonUdtFieldReference List<T> s, T e);

    @CommonFunc
    <T> LocalDate add(LocalDate d, Duration i);

    @CommonFunc
    <T> LocalDateTime add(LocalDateTime d, Duration i);

    @CommonFunc
    <T> LocalTime add(LocalTime d, Duration i);

    @CommonFunc
    <T> LocalDate add(LocalDate d, NormalizedPeriod i);

    @CommonFunc
    <T> LocalDateTime add(LocalDateTime d, NormalizedPeriod i);

    @CommonFunc
    <T> LocalTime add(LocalTime d, NormalizedPeriod i);

    @CollectionFunc
    <T> Boolean contains(List<T> s, T e);

    @CollectionFunc
    <T> Boolean contains(Set<T> s, T e);

    @CollectionFunc
    <K, V> Boolean contains(Map<K, V> s, K k);

    @CollectionFunc
    <T> Integer indexOf(List<T> s, T e);

    @StringFunc
    Integer indexOf(String fullString, String pattern);

    @CommonFunc
    <T extends Object> Boolean isNone(Optional<T> t);

    @CommonFunc
    <T extends Object> Boolean isSome(Optional<T> t);

    @CommonFunc
    Boolean isSet(Supplier<Boolean> t);

//    String concat(List<String> s);
//    <T> List<T> join(List<List<T>> s);
//    <T> Set<T> join(Set<Set<T>> s);

    @StringFunc
    String left(String s, Integer items);

    @CollectionFunc
    <T> List<T> left(List<T> s, Integer items);

    @CollectionFunc
    <T> Integer len(List<T> s);

    @CollectionFunc
    <T> Integer len(Set<T> s);

    @CollectionFunc
    <T> Long len(Stream<T> s);

    @CollectionFunc
    <K, V> Integer len(Map<K, V> s);

    @StringFunc
    Integer len(String s);

    @StringFunc
    String right(String s, Integer items);

    @CollectionFunc
    <T> List<T> right(List<T> s, Integer items);

    @CollectionFunc
    <T> Void delete(List<T> s, Integer index);

    @CollectionFunc
    <T> Void delete(Set<T> s, T e);

    @CollectionFunc
    <K, V> Void delete(Map<K, V> s, K k);

    @CollectionFunc
    <T> List<T> filter(List<T> s, Predicate<T> e);

    @CollectionFunc
    <T> Set<T> filter(Set<T> s, Predicate<T> e);

    @CollectionFunc
    <T> List<T> filter(Stream<T> s, Predicate<T> e);

    @CollectionFunc
    <K, V> Map<K, V> filter(Map<K, V> s, BiPredicate<K, V> e);

    @CollectionFunc
    <T> Boolean isEmpty(List<T> s);

    @CollectionFunc
    <T> Boolean isEmpty(Set<T> s);

    @CollectionFunc
    <K, T> Boolean isEmpty(Map<K, T> s);

//    @StringFunc
//    Integer isEmpty(String s);

//    @CustomTypeResolution
//    <E extends Entity, K extends Key> K keyOf(E e);

    @CannotInferReturnType
    <T, R> List<R> map(List<T> s, Function<T, R> e);

    @CannotInferReturnType
    <T, R> List<R> map(Stream<T> s, Function<T, R> e);

    @CannotInferReturnType
    <T, R> Set<R> map(Set<T> s, Function<T, R> e);

    @CannotInferReturnType
    <K, V, KR, VR> Map<KR, VR> map(Map<K, V> s, BiFunction<K, V, KR> keyLambda, BiFunction<K, V, VR> valueLambda);

//    <T> Optional<T> flatMap( String qualifiedName );

    @CollectionFunc
    <K, V> Map<K, V> put(@NonUdtFieldReference Map<K, V> m, K k, V v);

    @CommonFunc
    <T> T get(Try<T> e);

    @CommonFunc
    <T> T getOrElse(Optional<T> e, T elseVal);

    @CommonFunc
    <T> T get(Optional<T> e);

    @CollectionFunc
    <K, V> Optional<V> get(Map<K, V> m, K k);

    @CollectionFunc
    <K, V> V getOrElse(Map<K, V> m, K k, V elze);

    @CollectionFunc
    <T> Optional<T> get(List<T> l, Integer index);

    @CollectionFunc
    <R, K, V> R reduce(Map<K, V> m, R acc, TriFunction<R, K, V, R> f);

    @CollectionFunc
    <T, R> R reduce(List<T> m, R acc, BiFunction<R, T, R> f);

    @CollectionFunc
    <T, R> R reduce(Set<T> m, R acc, BiFunction<R, T, R> f);

    @CollectionFunc
    <T, R> R reduce(Stream<T> m, R acc, BiFunction<R, T, R> f);

    @CollectionFunc
    <Rv, Tv> Map<Rv, List<Tv>> groupBy(List<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> Map<Rv, List<Tv>> duplicates(List<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> Map<Rv, List<Tv>> duplicates(Set<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> List<Pair<Rv, List<Tv>>> duplicates(Stream<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> Set<Rv> distinct(Set<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> Set<Rv> distinct(List<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> List<Rv> distinct(Stream<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> Map<Rv, List<Tv>> groupBy(Set<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <Rv, Tv> List<Pair<Rv, List<Tv>>> groupBy(Stream<Tv> s, Function<Tv, Rv> e);

    @CollectionFunc
    <T, K, V> Map<K, V> aggregate(List<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda);

    @CollectionFunc
    <T, K, V> Map<K, V> aggregate(Set<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda);

    @CollectionFunc
    <T, K, V> List<Pair<K, V>> aggregate(Stream<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda);

    @CollectionFunc
    <T extends Number> Double stddev(List<T> l);

    @CollectionFunc
    <T extends Number> Double average(List<T> l);

    @CollectionFunc
    <T extends Comparable> Double quartile(List<T> l, int quart);

    @CollectionFunc
    <T extends Comparable> Double percentile(List<T> l, int percent);

    @CollectionFunc
    <T extends Number> Double variance(List<T> l);

    @CollectionFunc
    <T extends Number> Double stddev(Stream<T> l);

    @CollectionFunc
    <T extends Number> Double average(Stream<T> l);

    @CollectionFunc
    <T extends Comparable> Double quartile(Stream<T> l, int quart);

    @CollectionFunc
    <T extends Comparable> Double percentile(Stream<T> l, int percent);

    @CollectionFunc
    <T extends Number> Double variance(Stream<T> l);

    @CollectionFunc
    <K, V> List<V> values(Map<K, V> m);

    @CollectionFunc
    <K, V> Set<K> keys(Map<K, V> s);

    @CommonFunc
    <T> Optional<T> some(T t);

    @DatetimeFunc
    @ConversionFunc
    LocalDate toDate(String s);

    @DatetimeFunc
    @ConversionFunc
    LocalDate toDate(ZonedDateTime s);

    @DatetimeFunc
    @ConversionFunc
    LocalDate toDate(LocalDateTime s);

    @DatetimeFunc
    @ConversionFunc
    LocalDateTime toDatetime(String s);

    @DatetimeFunc
    @ConversionFunc
    ZonedDateTime toDatetimetz(String s);

    @DatetimeFunc
    @ConversionFunc
    LocalDateTime toDatetime(LocalDate s);

    @DatetimeFunc
    @ConversionFunc
    LocalTime toTime(String s);

    @DatetimeFunc
    @ConversionFunc
    LocalTime toTime(LocalDateTime s);

    @ConversionFunc
    BigDecimal toDecimal(String s);

    @ConversionFunc
    BigDecimal toDecimal(Integer s);

    @ConversionFunc
    BigDecimal toDecimal(Long s);

    @ConversionFunc
    BigDecimal toDecimal(Double s);

    @ConversionFunc
    Double toDouble(String s);

    @ConversionFunc
    Double toDouble(Integer s);

    @ConversionFunc
    Double toDouble(Long s);

//    @ConversionFunc
//    URI toUri( String s );

    @ConversionFunc
    Duration toDuration(String s);

    @ConversionFunc
    NormalizedPeriod toPeriod(String s);

    @ConversionFunc
    Integer toInt(String s);

    @ConversionFunc
    <T> List<T> toList(Set<T> s);

    @ConversionFunc
    <T> List<T> toList(Stream<T> s);

    @ConversionFunc
    <T> Set<T> toSet(List<T> s);

    @ConversionFunc
    <T> Set<T> toSet(Stream<T> s);

    @ConversionFunc
    @CannotInferReturnType
    <T, K, V> Map<K, V> toMap(List<T> e, Function<T, K> kfn, Function<T, V> vfn);

    @ConversionFunc
    @CannotInferReturnType
    <T, K, V> Map<K, V> toMap(Set<T> e, Function<T, K> kfn, Function<T, V> vfn);


    @ConversionFunc
    String toString(String e);

    @ConversionFunc
    String toString(UUID e);

    @ConversionFunc
    String toString(Integer e);

    @ConversionFunc
    String toString(Double e);

    @ConversionFunc
    String toString(Long e);

    @ConversionFunc
    String toString(BigDecimal e);

    @ConversionFunc
    String toString(Boolean e);

    @ConversionFunc
    String toString(LocalDate e);

    @ConversionFunc
    String toString(LocalDateTime e);

    @ConversionFunc
    String toString(LocalTime e);

    @ConversionFunc
    String toString(Duration e);

    @ConversionFunc
    <T> String toString(T e);


//    @ConversionFunc
//    String toString( URI e );

    @CommonFunc
    @CannotInferReturnType
    <L, R> Either<L, R> newEitherLeft(L e);

    @CommonFunc
    @CannotInferReturnType
    <L, R> Either<L, R> newEitherRight(R e);

    @CommonFunc
    <L, R> L left(Either<L, R> e);

    @CommonFunc
    <L, R> R right(Either<L, R> e);

    @CommonFunc
    <L, R> L left(Pair<L, R> e);

    @CommonFunc
    <L, R> R right(Pair<L, R> e);


    @CommonFunc
    <L, R> Boolean isLeft(Either<L, R> e);

    @CommonFunc
    <L, R> Boolean isRight(Either<L, R> e);


    @CommonFunc
    <T> Try<T> newTryValue(T e);

    @CommonFunc
    @CannotInferReturnType
    <T> Try<T> newTryFailure(String e);

    @CommonFunc
    <T> Boolean isTryFailure(Try<T> e);


    @CreatorFunc
    UUID newUUID();

    @DatetimeFunc
    LocalTime now();

    @DatetimeFunc
    LocalDate today();

    @DatetimeFunc
    LocalDateTime timestamp();

    @DatetimeFunc
    Integer year(LocalDate e);

    @DatetimeFunc
    Integer year(LocalDateTime e);

    @DatetimeFunc
    Integer month(LocalDate e);

    @DatetimeFunc
    Integer month(LocalDateTime e);

    @DatetimeFunc
    Integer day(LocalDate e);

    @DatetimeFunc
    Integer day(Duration e);

    @DatetimeFunc
    Integer day(LocalDateTime e);

    @DatetimeFunc
    Integer weekday(LocalDateTime e);

    @DatetimeFunc
    Integer weekday(LocalDate e);

    @DatetimeFunc
    Integer hour(LocalDateTime e);

    @DatetimeFunc
    Integer hour(Duration e);

    @DatetimeFunc
    Integer hour(LocalTime e);

    @DatetimeFunc
    Integer minute(LocalDateTime e);

    @DatetimeFunc
    Integer minute(LocalTime e);

    @DatetimeFunc
    Integer minute(Duration e);

    @DatetimeFunc
    Integer millisecond(LocalDateTime e);

    @DatetimeFunc
    Integer millisecond(LocalTime e);

    @DatetimeFunc
    Integer second(LocalDateTime e);

    @DatetimeFunc
    Integer second(Duration e);

    @DatetimeFunc
    Integer second(LocalTime e);

    @DatetimeFunc
    Long dateDiff(LocalDate a, LocalDate b);


    @MathFunc
    Integer abs(Integer a);

    @MathFunc
    Long abs(Long a);

    @MathFunc
    Double abs(Double a);

    @MathFunc
    Double ceil(Double a);

    @MathFunc
    Double floor(Double a);

    @MathFunc
    Double log(Double a);

    @MathFunc
    long round(Double a);

    @MathFunc
    Double sqrt(Double a);

    @MathFunc
    Double sqrt(Long a);

    @MathFunc
    Double sqrt(Integer a);

    @MathFunc
    Double random();

    @StringFunc
    Boolean endsWith(String main, String sub);

    @StringFunc
    Boolean matches(String regex, String value);

    @StringFunc
    Boolean startsWith(String main, String sub);

    @StringFunc
    List<String> split(String main, String delimiter);

    @StringFunc
    String toLower(String main);

    @StringFunc
    String toUpper(String main);


    @StringFunc
    String replaceAll(String main, String oldStr, String newStr);

    @StringFunc
    String substring(String main, Integer start);

    @StringFunc
    String substring(String main, Integer start, Integer end);

    @MathFunc
    Integer min(Integer l, Integer r);

    @MathFunc
    Integer max(Integer l, Integer r);

    @MathFunc
    Long min(Long l, Long r);

    @MathFunc
    Long max(Long l, Long r);

    @MathFunc
    Double min(Double l, Double r);

    @MathFunc
    Double max(Double l, Double r);

    @MathFunc
    BigDecimal min(BigDecimal l, BigDecimal r);

    @MathFunc
    BigDecimal max(BigDecimal l, BigDecimal r);

    @DatetimeFunc
    LocalDate min(LocalDate l, LocalDate r);

    @DatetimeFunc
    LocalDate max(LocalDate l, LocalDate r);

    @DatetimeFunc
    LocalDateTime min(LocalDateTime l, LocalDateTime r);

    @DatetimeFunc
    LocalDateTime max(LocalDateTime l, LocalDateTime r);

    @DatetimeFunc
    LocalTime min(LocalTime l, LocalTime r);

    @DatetimeFunc
    LocalTime max(LocalTime l, LocalTime r);

    @CollectionFunc
    <T extends Comparable<T>> T min(Set<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> T min(Set<T> l);

    <T extends Comparable<T>> Integer compare(T l, T r);

    @CollectionFunc
    <T extends Comparable<T>> T max(Set<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> T max(Set<T> l);

    @CollectionFunc
    <T extends Comparable<T>> T min(List<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> T min(List<T> l);

    @CollectionFunc
    <T extends Comparable<T>> T max(List<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> T max(List<T> l);

    @CollectionFunc
    <T extends Comparable<T>> T min(Stream<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> T min(Stream<T> l);

    @CollectionFunc
    <T extends Comparable<T>> T max(Stream<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> T max(Stream<T> l);

    @CollectionFunc
    <T> List<T> sort(Set<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> List<T> sort(Set<T> l);

    @CollectionFunc
    <T> List<T> sort(List<T> l, BiFunction<T, T, Integer> r);

    @CollectionFunc
    <T extends Comparable<T>> List<T> sort(List<T> l);


    @CollectionFunc
    <T extends Comparable<T>> List<T> sort(Stream<T> l);

    @CollectionFunc
    <T> List<T> sort(Stream<T> l, BiFunction<T, T, Integer> r);

    // ------------------------------

    @PersistFunc
    @CustomTypeResolution
    <E> List<E> query(Optional<AlfaObject> currentObject, E entityType, Predicate<E> e, Map<String, Integer> sort, int limit);

    @PersistFunc
    @CustomTypeResolution
    <E> List<E> query(Optional<AlfaObject> currentObject, E entityType, Predicate<E> e, Map<String, Integer> sort, int limit, String storeName);

    @PersistFunc
    @CustomTypeResolution
    <E extends Entity, K extends Key> Optional<E> lookup(String entityType, K k);

    @PersistFunc
    @CustomTypeResolution
    <E extends Entity, K extends Key> Optional<E> lookup(String entityType, K k, String storeName);

    @PersistFunc
    @CustomTypeResolution
    <E extends Entity> void save(E entity);

    @PersistFunc
    @CustomTypeResolution
    <E extends Entity> void save(E entity, String storeName);


    @PersistFunc
    @CustomTypeResolution
    <E extends AlfaObject> void publish(String queueName, E alfaObj);

//    @PersistFunc
//    @CustomTypeResolution
//    <E> Boolean notExists(E entityType, Predicate<E> e);

    @PersistFunc
    @CustomTypeResolution
    <E extends Entity> Boolean exists(String entityType, Predicate<E> e);

    @PersistFunc
    @CustomTypeResolution
    <E extends Entity> Boolean exists(String entityType, Predicate<E> e, String storeName);


    @PersistFunc
    @CustomTypeResolution
    <E extends Entity, K extends Key> Boolean keyExists(String entityType, K k);


    @PersistFunc
    @CustomTypeResolution
    <E extends Entity, K extends Key> Boolean keyExists(String entityType, K k, String storeName);

    <T, R> List<R> flatten(List<T> data);

    <T, R> Set<R> flatten(Set<T> data);

    <T extends com.schemarise.alfa.runtime.Enum> Optional<T> toEnum(String enumDataType, String enumConst);

    <E> Set<String> enumValues(Enum s);

    Set<String> enumValues(String s);

    Set<String> enumValues(String s, List<String> deft);

    String toFormattedTable(List d);

//    interface Try<T> {
//    }
//
//    interface Either<L, R> {
//    }

    interface TriFunction<T, U, V, R> {
    }

    @interface CollectionFunc {
    }

    @interface CommonFunc {
    }

    @interface ConversionFunc {
    }

    @interface CreatorFunc {
    }

    @interface DatetimeFunc {
    }

    @interface MathFunc {
    }

    @interface PersistFunc {
    }

    @interface StringFunc {
    }

    /**
     * Expect user to define return type
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CannotInferReturnType {
    }

    /**
     * Bespoke type resolution to be done
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomTypeResolution {
    }

    /**
     * Placed against a formal to enforce it is not referencing a UDT field
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NonUdtFieldReference {
    }
}
