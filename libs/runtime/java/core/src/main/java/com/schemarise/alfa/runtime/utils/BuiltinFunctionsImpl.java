package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Enum;
import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.Try;
import schemarise.alfa.runtime.model.TryFailure;
import schemarise.alfa.runtime.model.Pair;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuiltinFunctionsImpl implements IBuiltinFunctions {

    private final RuntimeContext runtimeContext;

    public BuiltinFunctionsImpl(RuntimeContext rc) {
        this.runtimeContext = rc;
    }

    @Override
    public Void debug(Object s) {
        System.out.println(LocalDateTime.now() + " [ALFA-DEBUG] : " + s);
        System.out.flush();
        return null;
    }

    @Override
    public <T> Void add(Set<T> s, T e) {
        s.add(e);
        return null;
    }

    @Override
    public <T> Void add(List<T> s, T e) {
        s.add(e);
        return null;
    }

    @Override
    public <T> LocalDate add(LocalDate d, Duration i) {
        return d.plus(i);
    }

    @Override
    public <T> LocalDateTime add(LocalDateTime d, Duration i) {
        return d.plus(i);
    }

    @Override
    public <T> LocalTime add(LocalTime d, Duration i) {
        return d.plus(i);
    }

    @Override
    public <T> LocalDate add(LocalDate d, NormalizedPeriod i) {
        return d.plus(i);
    }

    @Override
    public <T> LocalDateTime add(LocalDateTime d, NormalizedPeriod i) {
        return d.plus(i);
    }

    @Override
    public <T> LocalTime add(LocalTime d, NormalizedPeriod i) {
        return d.plus(i);
    }

    @Override
    public <T> Boolean contains(List<T> s, T e) {
        return s.contains(e);
    }

    @Override
    public <T> Boolean contains(Set<T> s, T e) {
        return s.contains(e);
    }

    @Override
    public <K, V> Boolean contains(Map<K, V> s, K k) {
        return s.containsKey(k);
    }

    @Override
    public <T> Integer indexOf(List<T> s, T e) {
        return s.indexOf(e);
    }

    @Override
    public Integer indexOf(String fullString, String pattern) {
        return fullString.indexOf(pattern);
    }

    @CommonFunc
    public Boolean isSet(Supplier<Boolean> t) {
        return t.get();
    }

    @Override
    public <T> Boolean isNone(Optional<T> t) {
        return !t.isPresent();
    }

    @Override
    public <T> Boolean isSome(Optional<T> t) {
        return t.isPresent();
    }

    @Override
    public String left(String s, Integer items) {
        return s.substring(0, items);
    }

    @Override
    public <T> List<T> left(List<T> s, Integer items) {
        return s.subList(0, items);
    }

    @Override
    public <T> Integer len(List<T> s) {
        return s.size();
    }

    @Override
    public <T> Long len(Stream<T> s) {
        return s.count();
    }

    @Override
    public <T> Integer len(Set<T> s) {
        return s.size();
    }

    @Override
    public <K, V> Integer len(Map<K, V> s) {
        return s.size();
    }

    @Override
    public Integer len(String s) {
        return s.length();
    }

    @Override
    public String right(String s, Integer items) {
        return s.substring(s.length() - items);
    }

    @Override
    public <T> List<T> right(List<T> s, Integer items) {
        return s.subList(s.size() - items, s.size());
    }

    @Override
    public <T> Void delete(List<T> s, Integer index) {
        s.remove(index);
        return null;
    }

    @Override
    public <T> Void delete(Set<T> s, T e) {
        s.remove(e);
        return null;
    }

    @Override
    public <K, V> Void delete(Map<K, V> s, K k) {
        s.remove(k);
        return null;
    }

    @Override
    public <T> List<T> filter(List<T> s, Predicate<T> e) {
        return s.stream().filter(e).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> filter(Stream<T> s, Predicate<T> e) {
        return s.filter(e).collect(Collectors.toList());
    }

    @Override
    public <T> Set<T> filter(Set<T> s, Predicate<T> e) {
        return s.stream().filter(e).collect(Collectors.toSet());
    }

    @Override
    public <K, V> Map<K, V> filter(Map<K, V> s, BiPredicate<K, V> e) {
        Predicate<? super Map.Entry<K, V>> p = (Predicate<Map.Entry<K, V>>) kvEntry -> e.test(kvEntry.getKey(), kvEntry.getValue());
        return s.entrySet().stream().filter(p).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public <T> Boolean isEmpty(List<T> s) {
        return s.isEmpty();
    }

    @Override
    public <T> Boolean isEmpty(Set<T> s) {
        return s.isEmpty();
    }

    @Override
    public <K, T> Boolean isEmpty(Map<K, T> s) {
        return s.isEmpty();
    }

//    @Override
//    public <E extends Entity, K extends Key> K keyOf(E e) {
//        return (K) e.key().get();
//    }


    @Override
    public <T, R> List<R> map(List<T> s, Function<T, R> e) {
        return s.stream().map(e).collect(Collectors.toList());
    }

    @Override
    public <T, R> List<R> map(Stream<T> s, Function<T, R> e) {
        return s.map(e).collect(Collectors.toList());
    }

    @Override
    public <T, R> Set<R> map(Set<T> s, Function<T, R> e) {
        return s.stream().map(e).collect(Collectors.toSet());
    }

    @Override
    public <K, V, KR, VR> Map<KR, VR> map(Map<K, V> s, BiFunction<K, V, KR> keyLambda, BiFunction<K, V, VR> valueLambda) {

        Map<KR, VR> res = new HashMap<>();

        s.entrySet().stream().forEach(e -> {
            KR k = keyLambda.apply(e.getKey(), e.getValue());
            VR v = valueLambda.apply(e.getKey(), e.getValue());
            res.put(k, v);
        });

        return res;
    }

    @Override
    public <K, V> Map<K, V> put(Map<K, V> m, K k, V v) {
        Map<K, V> res = new HashMap<>();
        res.putAll(m);
        res.put(k, v);
        return res;
    }

    @Override
    public <T> T get(Try<T> e) {
        return e.getResult();
    }

    @Override
    public <T> T getOrElse(Optional<T> e, T elseVal) {
        return e.orElse(elseVal);
    }

    @Override
    public <T> T get(Optional<T> e) {
        return e.get();
    }

    @Override
    public <K, V> Optional<V> get(Map<K, V> m, K k) {
        return Optional.ofNullable(m.get(k));
    }

    @Override
    public <K, V> V getOrElse(Map<K, V> m, K k, V elze) {
        V v = m.get(k);
        if (v == null)
            return elze;
        else
            return v;
    }

    @Override
    public <T> Optional<T> get(List<T> l, Integer index) {
        return Optional.empty();
    }

    @Override
    public <T, R> R reduce(List<T> m, R acc, BiFunction<R, T, R> f) {
        return reduce(m.stream(), acc, f);
    }

    @Override
    public <T, R> R reduce(Stream<T> m, R acc, BiFunction<R, T, R> f) {
        return (R) m.reduce(acc, f, combiner(acc));
    }

    private <R> BinaryOperator combiner(R acc) {
        if (acc instanceof Double)
            return (BinaryOperator<Double>) (r, r2) -> r + r2;
        else if (acc instanceof Integer)
            return (BinaryOperator<Double>) (r, r2) -> r + r2;
        else if (acc instanceof Long)
            return (BinaryOperator<Long>) (r, r2) -> r + r2;
        else if (acc instanceof BigDecimal)
            return (BinaryOperator<Long>) (r, r2) -> r + r2;
        else if (acc instanceof Float)
            return (BinaryOperator<Float>) (r, r2) -> r + r2;
        else if (acc instanceof String)
            return (BinaryOperator<Float>) (r, r2) -> r + r2;
        else
            throw new UnsupportedOperationException("Combiner not supported for type " + acc.getClass().getName());
    }

    @Override
    public <T, R> R reduce(Set<T> m, R acc, BiFunction<R, T, R> f) {
        return reduce(m.stream(), acc, f);
    }

    @Override
    public <K, V> List<V> values(Map<K, V> m) {
        return new ArrayList(m.values());
    }

    @Override
    public <K, V> Set<K> keys(Map<K, V> s) {
        return s.keySet();
    }

    @Override
    public <T> Optional<T> some(T t) {
        return Optional.of(t);
    }

    @Override
    public LocalDate toDate(String s) {
        return LocalDate.parse(s);
    }

    @Override
    public LocalDate toDate(LocalDateTime s) {
        return s.toLocalDate();
    }

    @Override
    public LocalDate toDate(ZonedDateTime s) {
        return s.toLocalDate();
    }

    @Override
    public LocalDateTime toDatetime(String s) {
        return LocalDateTime.parse(s);
    }

    @Override
    public ZonedDateTime toDatetimetz(String s) {
        return ZonedDateTime.parse(s);
    }

    @Override
    public LocalDateTime toDatetime(LocalDate s) {
        return LocalDateTime.of(s, LocalTime.MIDNIGHT);
    }

    @Override
    public LocalTime toTime(String s) {
        return LocalTime.parse(s);
    }

    @Override
    public LocalTime toTime(LocalDateTime s) {
        return s.toLocalTime();
    }

    @Override
    public BigDecimal toDecimal(String s) {
        return new BigDecimal(s);
    }

    @Override
    public BigDecimal toDecimal(Integer s) {
        return BigDecimal.valueOf(s);
    }

    @Override
    public BigDecimal toDecimal(Long s) {
        return BigDecimal.valueOf(s);
    }

    @Override
    public BigDecimal toDecimal(Double s) {
        return BigDecimal.valueOf(s);
    }

    @Override
    public Double toDouble(String s) {
        return Double.parseDouble(s);
    }

    @Override
    public Double toDouble(Integer s) {
        return Double.valueOf(s);
    }

    @Override
    public Double toDouble(Long s) {
        return Double.valueOf(s);
    }

    @Override
    public Duration toDuration(String s) {
        return Duration.parse(s);
    }

    @Override
    public NormalizedPeriod toPeriod(String s) {
        return NormalizedPeriod.of(s);
    }

    @Override
    public Integer toInt(String s) {
        return Integer.parseInt(s);
    }

    @Override
    public <T> List<T> toList(Set<T> s) {
        return new ArrayList<>(s);
    }

    @Override
    public <T> Set<T> toSet(Stream<T> s) {
        return s.collect(Collectors.toSet());
    }

    @Override
    public <T> List<T> toList(Stream<T> s) {
        return s.collect(Collectors.toList());
    }

    @Override
    public <T> Set<T> toSet(List<T> s) {
        return new HashSet<>(s);
    }

    @Override
    public <R, K, V> R reduce(Map<K, V> m, R acc, TriFunction<R, K, V, R> f) {
        return null;// TODO
    }

    @Override
    public <T, K, V> Map<K, V> toMap(List<T> e, Function<T, K> kfn, Function<T, V> vfn) {
        Map<K, V> m = new HashMap<>();
        e.stream().forEach(x -> m.put(kfn.apply(x), vfn.apply(x)));
        return m;
    }

    @Override
    public <T, K, V> Map<K, V> toMap(Set<T> e, Function<T, K> kfn, Function<T, V> vfn) {
        Map<K, V> m = new HashMap<>();
        e.stream().forEach(x -> m.put(kfn.apply(x), vfn.apply(x)));
        return m;
    }

    @Override
    public String toString(String e) {
        return "\"" + e + "\"";
    }

    @Override
    public String toString(UUID e) {
        return e.toString();
    }

    @Override
    public String toString(Integer e) {
        return e.toString();
    }

    @Override
    public String toString(Double e) {
        return e.toString();
    }

    @Override
    public String toString(Long e) {
        return e.toString();
    }

    @Override
    public String toString(BigDecimal e) {
        return e.toString();
    }

    @Override
    public String toString(Boolean e) {
        return e.toString();
    }

    @Override
    public String toString(LocalDate e) {
        return e.toString();
    }

    @Override
    public String toString(LocalDateTime e) {
        return e.toString();
    }

    @Override
    public String toString(LocalTime e) {
        return e.toString();
    }

    @Override
    public String toString(Duration e) {
        return e.toString();
    }

    @Override
    public <T> String toString(T e) {

        StringBuffer sb = new StringBuffer();

        if (e instanceof List) {
            List a = (List) e;
            sb.append("[");
            List<String> f = (List<String>) a.stream().map(x -> toString(x)).collect(Collectors.toList());

            if (a.size() > 0 && (a.get(0) instanceof Collection || a.get(0) instanceof Map)) {
                sb.append(String.join(",\n ", f));
            } else
                sb.append(String.join(",", f));

            sb.append("]");
        } else if (e instanceof Map) {
            Map a = (Map) e;
            sb.append("{");
            List<String> f = (List<String>) a.keySet().stream().map(k -> toString(k) + ":" + toString(a.get(k))).collect(Collectors.toList());
            sb.append(String.join(",\n ", f));

            sb.append("}");
        } else if (e instanceof Set) {
            Set a = (Set) e;
            sb.append("{");

            List<String> f = (List<String>) a.stream().map(x -> toString(x)).collect(Collectors.toList());

            if (a.size() > 0 && (a.iterator().next() instanceof Collection || a.iterator().next() instanceof Map)) {
                sb.append(String.join(",\n ", f));
            } else
                sb.append(String.join(",", f));

            sb.append("}");
        } else if (e instanceof String)
            sb.append(toString((String) e));
        else {
            sb.append(e.toString());
        }

        return sb.toString();
    }

    @Override
    public <L, R> Either<L, R> newEitherLeft(L e) {
        return Either.builder().setLeft(e).build();
    }

    @Override
    public <L, R> Either<L, R> newEitherRight(R e) {
        return Either.builder().setRight(e).build();
    }

    @Override
    public <L, R> L left(Either<L, R> e) {
        return e.getLeft();
    }

    @Override
    public <L, R> R right(Either<L, R> e) {
        return e.getRight();
    }

    @Override
    public <L, R> L left(Pair<L, R> e) {
        return e.getLeft();
    }

    @Override
    public <L, R> R right(Pair<L, R> e) {
        return e.getRight();
    }

    @Override
    public <L, R> Boolean isLeft(Either<L, R> e) {
        return e.isLeft();
    }

    @Override
    public <L, R> Boolean isRight(Either<L, R> e) {
        return e.isRight();
    }

    @Override
    public <T> Try<T> newTryValue(T e) {
        return AlfaUtils.createTryValue(e);
    }

    @Override
    public <T> Try<T> newTryFailure(String e) {
        return Try.builder().setFailure(TryFailure.builder().setMessage(e).build()).build();
    }

    @Override
    public <T> Boolean isTryFailure(Try<T> e) {
        return e.isFailure();
    }

    @Override
    public UUID newUUID() {
        return UUID.randomUUID();
    }

    @Override
    public LocalTime now() {
        return LocalTime.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public LocalDateTime timestamp() {
        return LocalDateTime.now();
    }

    @Override
    public Integer year(LocalDate e) {
        return e.getYear();
    }

    @Override
    public Integer year(LocalDateTime e) {
        return e.getYear();
    }

    @Override
    public Integer month(LocalDate e) {
        return e.getMonthValue();
    }

    @Override
    public Integer month(LocalDateTime e) {
        return e.getMonthValue();
    }

    @Override
    public Integer day(LocalDate e) {
        return e.getDayOfMonth();
    }

    @Override
    public Integer day(Duration e) {
        return Math.toIntExact(e.get(ChronoUnit.DAYS));
    }

    @Override
    public Integer day(LocalDateTime e) {
        return e.getDayOfMonth();
    }

    @Override
    public Integer weekday(LocalDateTime e) {
        return e.getDayOfWeek().getValue();
    }

    @Override
    public Integer weekday(LocalDate e) {
        return e.getDayOfWeek().ordinal();
    }

    @Override
    public Integer hour(LocalDateTime e) {
        return e.getHour();
    }

    @Override
    public Integer hour(Duration e) {
        return Math.toIntExact(e.get(ChronoUnit.HOURS));
    }

    @Override
    public Integer hour(LocalTime e) {
        return e.getHour();
    }

    @Override
    public Integer minute(LocalDateTime e) {
        return e.getMinute();
    }

    @Override
    public Integer minute(LocalTime e) {
        return e.getMinute();
    }

    @Override
    public Integer minute(Duration e) {
        return Math.toIntExact(e.get(ChronoUnit.MINUTES));
    }

    @Override
    public Integer millisecond(LocalDateTime e) {
        return 0;
    }

    @Override
    public Integer millisecond(LocalTime e) {
        return 0;
    }

    @Override
    public Integer second(LocalDateTime e) {
        return e.getSecond();
    }

    @Override
    public Integer second(Duration e) {
        return Math.toIntExact(e.get(ChronoUnit.SECONDS));
    }

    @Override
    public Integer second(LocalTime e) {
        return e.getSecond();
    }

    @Override
    public Long dateDiff(LocalDate a, LocalDate b) {
        return ChronoUnit.DAYS.between(a, b);
    }

    @Override
    public Integer abs(Integer a) {
        return Math.abs(a);
    }

    @Override
    public Long abs(Long a) {
        return Math.abs(a);
    }

    @Override
    public Double abs(Double a) {
        return Math.abs(a);
    }

    @Override
    public Double ceil(Double a) {
        return Math.ceil(a);
    }

    @Override
    public Double floor(Double a) {
        return Math.floor(a);
    }

    @Override
    public Double log(Double a) {
        return Math.log(a);
    }

    @Override
    public long round(Double a) {
        return Math.round(a);
    }

    @Override
    public Double sqrt(Double a) {
        return Math.sqrt(a);
    }

    @Override
    public Double sqrt(Long a) {
        return Math.sqrt(a);
    }

    @Override
    public Double sqrt(Integer a) {
        return Math.sqrt(a);
    }

    @Override
    public Double random() {
        return Math.random();
    }

    @Override
    public Boolean endsWith(String main, String sub) {
        return main.endsWith(sub);
    }

    @Override
    public Boolean matches(String regex, String value) {
        return value.matches(regex);
    }

    @Override
    public Boolean startsWith(String main, String sub) {
        return main.startsWith(sub);
    }

    @Override
    public List<String> split(String main, String delimiter) {
        return Arrays.asList(main.split(delimiter));
    }

    @Override
    public String toLower(String main) {
        return main.toLowerCase();
    }

    @Override
    public String toUpper(String main) {
        return main.toUpperCase();
    }

    @Override
    public String replaceAll(String main, String oldStr, String newStr) {
        return main.replaceAll(oldStr, newStr);
    }

    @Override
    public String substring(String main, Integer start) {
        return main.substring(start);
    }

    @Override
    public String substring(String main, Integer start, Integer end) {
        return main.substring(start, end);
    }

    @Override
    public Integer min(Integer l, Integer r) {
        return Math.min(l.intValue(), r.intValue());
    }

    @Override
    public Integer max(Integer l, Integer r) {
        return l > r ? l : r;
    }

    @Override
    public Long min(Long l, Long r) {
        return l < r ? l : r;
    }

    @Override
    public Long max(Long l, Long r) {
        return l > r ? l : r;
    }

    @Override
    public Double min(Double l, Double r) {
        return l < r ? l : r;
    }

    @Override
    public Double max(Double l, Double r) {
        return l > r ? l : r;
    }

    @Override
    public BigDecimal min(BigDecimal l, BigDecimal r) {
        return l.compareTo(r) < 0 ? l : r;
    }

    @Override
    public BigDecimal max(BigDecimal l, BigDecimal r) {
        return l.compareTo(r) > 0 ? l : r;
    }

    @Override
    public LocalDate min(LocalDate l, LocalDate r) {
        return l.compareTo(r) < 0 ? l : r;
    }

    @Override
    public LocalDate max(LocalDate l, LocalDate r) {
        return l.compareTo(r) > 0 ? l : r;
    }

    @Override
    public LocalDateTime min(LocalDateTime l, LocalDateTime r) {
        return l.compareTo(r) < 0 ? l : r;
    }

    @Override
    public LocalDateTime max(LocalDateTime l, LocalDateTime r) {
        return l.compareTo(r) > 0 ? l : r;
    }

    @Override
    public LocalTime min(LocalTime l, LocalTime r) {
        return l.compareTo(r) < 0 ? l : r;
    }

    @Override
    public LocalTime max(LocalTime l, LocalTime r) {
        return l.compareTo(r) > 0 ? l : r;
    }

    @Override
    public <T extends Comparable<T>> T min(Set<T> al, BiFunction<T, T, Integer> r) {
        return Collections.min(al, (o1, o2) -> r.apply(o1, o2));
    }

    @Override
    public <T extends Comparable<T>> T min(Set<T> l) {
        return Collections.min(l);
    }

    @Override
    public <T extends Comparable<T>> Integer compare(T l, T r) {
        return l.compareTo(r);
    }

    @Override
    public <T extends Comparable<T>> T max(Set<T> al, BiFunction<T, T, Integer> r) {
        return Collections.max(al, (o1, o2) -> r.apply(o1, o2));
    }

    @Override
    public <T extends Comparable<T>> T max(Set<T> l) {
        return Collections.max(l);
    }

    @Override
    public <T extends Comparable<T>> T min(List<T> l, BiFunction<T, T, Integer> r) {
        return Collections.min(l, (o1, o2) -> r.apply(o1, o2));
    }

    @Override
    public <T extends Comparable<T>> T min(List<T> l) {
        return Collections.min(l);
    }

    @Override
    public <T extends Comparable<T>> T max(List<T> l, BiFunction<T, T, Integer> r) {
        List<T> al = new ArrayList<T>();
        al.addAll(l);
        return Collections.max(al, (o1, o2) -> r.apply(o1, o2));
    }

    @Override
    public <T extends Comparable<T>> T max(List<T> l) {
        return Collections.max(l);
    }

    @Override
    public <T extends Comparable<T>> T min(Stream<T> l, BiFunction<T, T, Integer> r) {
        return min(l.collect(Collectors.toList()), r);
    }

    @Override
    public <T extends Comparable<T>> T min(Stream<T> l) {
        return min(l.collect(Collectors.toList()));
    }

    @Override
    public <T extends Comparable<T>> T max(Stream<T> l, BiFunction<T, T, Integer> r) {
        return max(l.collect(Collectors.toList()), r);
    }

    @Override
    public <T extends Comparable<T>> T max(Stream<T> l) {
        return max(l.collect(Collectors.toList()));
    }

    @Override
    public <T> List<T> sort(Set<T> l, BiFunction<T, T, Integer> r) {
        List<T> al = new ArrayList<T>();
        al.addAll(l);
        Collections.sort(al, (o1, o2) -> r.apply(o1, o2));
        return al;
    }

    @Override
    public <T extends Comparable<T>> List<T> sort(Set<T> l) {
        List<T> al = new ArrayList<T>();
        al.addAll(l);
        Collections.sort(al);
        return al;
    }

    @Override
    public <T> List<T> sort(List<T> l, BiFunction<T, T, Integer> r) {
        List<T> al = new ArrayList<T>();
        al.addAll(l);
        Collections.sort(al, (o1, o2) -> r.apply(o1, o2));
        return al;
    }

    @Override
    public <T extends Comparable<T>> List<T> sort(List<T> l) {
        List<T> al = new ArrayList<T>();
        al.addAll(l);
        Collections.sort(al);
        return al;
    }

    @Override
    public <T extends Comparable<T>> List<T> sort(Stream<T> l) {
        return null;
    }

    @Override
    public <T> List<T> sort(Stream<T> l, BiFunction<T, T, Integer> r) {
        return null;
    }

    @Override
    public <E> List<E> query(Optional<AlfaObject> currentObject, E entityType, Predicate<E> e, Map<String, Integer> sort, int limit) {
        throw new AlfaRuntimeException("Should be delegated to RuntimeContext query()");
    }

    @Override
    public <E> List<E> query(Optional<AlfaObject> currentObject, E entityType, Predicate<E> e, Map<String, Integer> sort, int limit, String storeName) {
        throw new AlfaRuntimeException("Should be delegated to RuntimeContext query()");
    }

    @Override
    public <E extends Entity, K extends Key> Optional<E> lookup(String entityType, K k) {
        return runtimeContext.lookup(entityType, k, Optional.empty());
    }

    @Override
    public <E extends Entity, K extends Key> Optional<E> lookup(String entityType, K k, String storeName) {
        return runtimeContext.lookup(entityType, k, storeName);
    }

    @Override
    public <E extends Entity> void save(E entity) {
        runtimeContext.save(entity, Optional.empty());
    }

    @Override
    public <E extends Entity> void save(E entity, String storeName) {
        runtimeContext.save(entity, storeName);
    }

    @Override
    public <E extends AlfaObject> void publish(String queueName, E alfaObj) {
        runtimeContext.publish(queueName, alfaObj);
    }

    @Override
    public <E extends Entity> Boolean exists(String entityType, Predicate<E> e) {
        throw new AlfaRuntimeException("Should be delegated to RuntimeContext exists()");
    }

    @Override
    public <E extends Entity> Boolean exists(String entityType, Predicate<E> e, String storeName) {
        throw new AlfaRuntimeException("Should be delegated to RuntimeContext exists()");
    }

    @Override
    public <E extends Entity, K extends Key> Boolean keyExists(String entityType, K k) {
        return runtimeContext.keyExists(entityType, k, Optional.empty());
    }

    @Override
    public <E extends Entity, K extends Key> Boolean keyExists(String entityType, K k, String storeName) {
        return runtimeContext.keyExists(entityType, k, storeName);
    }

    @Override
    public <Rv, Tv> Map<Rv, List<Tv>> groupBy(List<Tv> s, Function<Tv, Rv> e) {
        return _groupBy(s.stream(), e);
    }

    @Override
    public <Rv, Tv> List<Pair<Rv, List<Tv>>> groupBy(Stream<Tv> s, Function<Tv, Rv> e) {
        Map<Rv, List<Tv>> sx = _groupBy(s, e);
        Stream<Pair<Rv, List<Tv>>> r = sx.entrySet().stream().map(ex -> Pair.builder().
                setLeft(ex.getKey()).
                setRight(ex.getValue()).build());

        return r.collect(Collectors.toList());
    }

    private <Rv, Tv> Map<Rv, List<Tv>> _groupBy(Stream<Tv> s, Function<Tv, Rv> e) {
        return s.collect(Collectors.groupingBy(e));
    }

    @Override
    public <Rv, Tv> Map<Rv, List<Tv>> duplicates(List<Tv> s, Function<Tv, Rv> e) {
        return _duplicates(s.stream(), e);
    }

    private <Rv, Tv> Map<Rv, List<Tv>> _duplicates(Stream<Tv> s, Function<Tv, Rv> e) {
        Map<Rv, List<Tv>> grped = s.collect(Collectors.groupingBy(e));
        Map<Rv, List<Tv>> res = grped.entrySet().stream().filter(ed -> ed.getValue().size() > 1).collect(Collectors.toMap(el -> el.getKey(), el -> el.getValue()));
        return res;
    }

    @Override
    public <Rv, Tv> List<Pair<Rv, List<Tv>>> duplicates(Stream<Tv> s, Function<Tv, Rv> e) {
        Map<Rv, List<Tv>> d = _duplicates(s, e);
        Stream<Pair<Rv, List<Tv>>> r = d.entrySet().stream().map(en -> Pair.builder().setLeft(en.getKey()).setRight(en.getValue()).build());
        return r.collect(Collectors.toList());
    }

    @Override
    public <Rv, Tv> Map<Rv, List<Tv>> duplicates(Set<Tv> s, Function<Tv, Rv> e) {
        return _duplicates(s.stream(), e);
    }

    @Override
    public <Rv, Tv> Set<Rv> distinct(List<Tv> s, Function<Tv, Rv> e) {
        return s.stream().map(e).collect(Collectors.toSet());
    }

    @Override
    public <Rv, Tv> Set<Rv> distinct(Set<Tv> s, Function<Tv, Rv> e) {
        return s.stream().map(e).collect(Collectors.toSet());
    }

    @Override
    public <Rv, Tv> List<Rv> distinct(Stream<Tv> s, Function<Tv, Rv> e) {
        return new ArrayList<>(s.map(e).collect(Collectors.toSet()));
    }

    @Override
    public <Rv, Tv> Map<Rv, List<Tv>> groupBy(Set<Tv> s, Function<Tv, Rv> e) {
        return _groupBy(s.stream(), e);
    }

    @Override
    public <T, K, V> Map<K, V> aggregate(List<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda) {
        return _aggregate(s.stream(), e, acc, lambda);
    }

    @Override
    public <T, K, V> Map<K, V> aggregate(Set<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda) {
        return _aggregate(s.stream(), e, acc, lambda);
    }

    @Override
    public <T, K, V> List<Pair<K, V>> aggregate(Stream<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda) {
        Map<K, V> sx = _aggregate(s, e, acc, lambda);
        Stream<Pair<K, V>> r = sx.entrySet().stream().map(x -> Pair.builder().
                setLeft(x.getKey()).
                setRight(x.getValue()).build());

        return r.collect(Collectors.toList());
    }

    private <T, K, V> Map<K, V> _aggregate(Stream<T> s, Function<T, K> e, V acc, BiFunction<V, T, V> lambda) {

        Map<K, V> r = new HashMap<>();

        s.forEach(x -> {
            K k = e.apply(x);

            V curr = r.get(k);
            if (curr == null)
                curr = acc;

            V val = lambda.apply(curr, x);
            r.put(k, val);
        });

//        Map<K, List<T>> g = s.collect(Collectors.groupingBy(e));
//        Map<K, V> aggr = g.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, vals -> reduce(vals.getValue(), acc, lambda)));

        return r;
    }

    @Override
    public <T extends Number> Double stddev(List<T> l) {
        double variance = variance(l);
        double stddev = Math.sqrt(variance / l.size());
        return stddev;
    }

    private int medianIndex(int l, int r) {
        int n = r - l + 1;
        n = (n + 1) / 2 - 1;
        return n + l;
    }

    @Override
    public <T extends Comparable> Double quartile(List<T> unsorted, int quart) {

        ArrayList<T> l = new ArrayList<>(unsorted);
        Collections.sort(l);

        int n = l.size();

        if (n == 0)
            throw new AlfaRuntimeException("Cannot calculate quartile on empty list");

        if (quart < 0 && quart > 4)
            throw new AlfaRuntimeException("2nd argument to quartile should be 0, 1, 2, 3 or 4");

        int midIndex = medianIndex(0, n);

        T result = null;

        switch (quart) {
            case 0:
                result = l.get(0);

            case 1:
                result = l.get(medianIndex(0, midIndex));

            case 2:
                result = l.get(midIndex);

            case 3:
                result = l.get(midIndex + medianIndex(midIndex + 1, n));

            case 4:
                result = l.get(l.size() - 1);
        }

        return ((Number) result).doubleValue();
    }

    @Override
    public <T extends Comparable> Double percentile(List<T> unsorted, int percentile) {

        int len = unsorted.size();

        if (len == 0)
            throw new AlfaRuntimeException("Cannot calculate percentile on empty list");


        if (percentile < 0 || percentile > 100)
            throw new AlfaRuntimeException("percentile needs to be between 0 and 100");

        if (len == 1)
            return ((Number) unsorted.get(0)).doubleValue();

        List<T> l = new ArrayList<>(unsorted);
        Collections.sort(l);

//        int index = (int) Math.ceil( ( (double) percentile ) / 100.0 * ( (double) len ) ) - 1;
//
//        if ( index == -1 )
//            index = 0;
//
//        return l.get(index);

        return evaluatePercentile(l, percentile);
    }

    //    https://github.com/jalkanen/speed4j/blob/master/src/main/java/com/ecyrd/speed4j/util/Percentile.java
    private double evaluateSortedercentile(final double[] sorted, final double p) {
        double n = sorted.length;
        double pos = p * (n + 1) / 100;
        double fpos = Math.floor(pos);
        int intPos = (int) fpos;
        double dif = pos - fpos;

        if (pos < 1) {
            return sorted[0];
        }
        if (pos >= n) {
            return sorted[sorted.length - 1];
        }
        double lower = sorted[intPos - 1];
        double upper = sorted[intPos];
        return lower + dif * (upper - lower);
    }

    public <T> Double evaluatePercentile(List<T> sortedList, int p) {
        // Sort array.  We avoid a third copy here by just creating the
        // list directly.
        double[] sorted = new double[sortedList.size()];
        for (int i = 0; i < sortedList.size(); i++) {
            sorted[i] = ((Number) sortedList.get(i)).doubleValue();
        }

        return evaluateSortedercentile(sorted, p);
    }

    @Override
    public <T extends Number> Double average(List<T> l) {
        int length = l.size();
        double sum = l.stream().map(e -> e.doubleValue()).reduce(0.0, (acc, e) -> acc + e);
        double avg = sum / length;

        return avg;
    }

    @Override
    public <T extends Number> Double variance(List<T> l) {
        double mean = average(l);

        double variance = l.stream().map(e -> (Double) e).reduce(0.0, (acc, e) -> Math.pow(e - mean, 2));
        return variance;
    }

    @Override
    public <T extends Number> Double stddev(Stream<T> l) {
        return stddev(l.collect(Collectors.toList()));
    }

    @Override
    public <T extends Number> Double average(Stream<T> l) {
        return average((l.collect(Collectors.toList())));
    }

    @Override
    public <T extends Comparable> Double quartile(Stream<T> l, int quart) {
        return quartile((l.collect(Collectors.toList())), quart);
    }

    @Override
    public <T extends Comparable> Double percentile(Stream<T> l, int percent) {
        return percentile((l.collect(Collectors.toList())), percent);
    }

    @Override
    public <T extends Number> Double variance(Stream<T> l) {
        return variance((l.collect(Collectors.toList())));
    }

    @Override
    public <T, R> List<R> flatten(List<T> data) {
        if (data.size() > 0 && data.get(0) instanceof List) {
            List<R> ret = new ArrayList<>();

            data.forEach(e -> {
                List l = (List) e;
                ret.addAll(flatten(l));
            });

            return ret;
        } else
            return (List<R>) data;
    }

    @Override
    public <T, R> Set<R> flatten(Set<T> data) {
        if (data.size() > 0 && data.iterator().next() instanceof Set) {
            Set<R> ret = new HashSet<>();

            data.forEach(e -> {
                Set l = (Set) e;
                ret.addAll(flatten(l));
            });

            return ret;
        } else
            return (Set<R>) data;
    }

    @Override
    public <T extends Enum> Optional<T> toEnum(String enumDataType, String enumConst) {
        return Optional.ofNullable((T) ClassUtils.getByEnumConst(enumDataType, enumConst, true));
    }

    @Override
    public <E> Set<String> enumValues(Enum s) {
        return ClassUtils.getMeta(s.getClass().getTypeName()).getModel().getAllFieldsMeta().keySet();
    }

    @Override
    public Set<String> enumValues(String s) {
        return ClassUtils.getMeta(s).getModel().getAllFieldsMeta().keySet();
    }

    @Override
    public Set<String> enumValues(String s, List<String> deflts) {
        if (ClassUtils.isDefined(s))
            return ClassUtils.getMeta(s).getModel().getAllFieldsMeta().keySet();
        else
            return new HashSet<>(deflts);
    }

    @Override
    public String toFormattedTable(List d) {
        return d.toString();
    }

    // -- Collectors
//
//    public <T, A, R> Collector<T, ?, R> filter(Collector<T, A, R> downstream, Predicate<T> e) {
//        return Jdk12Collectors.filtering(e, downstream);
//    }
//
//
//    public static <T> Set<T> findDuplicateByGrouping(List<T> list) {
//
//        return list.stream()
//                .collect(Collectors.groupingBy(Function.identity()
//                        , Collectors.counting()))    // create a map {1=1, 2=1, 3=2, 4=2, 5=1, 7=1, 9=2}
//                .entrySet().stream()                 // Map -> Stream
//                .filter(m -> m.getValue() > 1)       // if map value > 1, duplicate element
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toSet());
//
//    }
//
//    public <T, A, R> Collector<T, ?, R>  duplicates(Collector<T, A, R> downstream, Function<T, R> fn) {
//        Collector<T, ?, Map<R, R>> grouped = Jdk12Collectors.groupingBy(fn, downstream);
//        Jdk12Collectors.filtering( ed -> ed. > 1, grouped);
//
//
//
//        Map<Rv, List<Tv>> grped = s.collect(Collectors.groupingBy(e));
//        Map<Rv, List<Tv>> res = grped.entrySet().stream().filter(ed -> ed.getValue().size() > 1).
//                collect(Collectors.toMap(el -> el.getKey(), el -> el.getValue()));
//
//        return res;
//    }
}
