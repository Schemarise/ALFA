package com.schemarise.alfa.runtime;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

public class ReusableStream<T> implements Stream<T> {
    private final Stream<T> under;

    public ReusableStream(Stream<T> other) {
        under = other;
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return under.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return under.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return under.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return under.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return under.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return under.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return under.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return under.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return under.flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return under.distinct();
    }

    @Override
    public Stream<T> sorted() {
        return under.sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return under.sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return under.peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return under.limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return under.skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        under.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        under.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return under.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return under.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return under.reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return under.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return under.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return under.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return under.collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return under.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return under.max(comparator);
    }

    @Override
    public long count() {
        return under.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return under.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return under.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return under.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return under.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return under.findAny();
    }

    public static <T1> Builder<T1> builder() {
        return Stream.builder();
    }

    public static <T1> Stream<T1> empty() {
        return Stream.empty();
    }

    public static <T1> Stream<T1> of(T1 t1) {
        return Stream.of(t1);
    }

    @SafeVarargs
    public static <T1> Stream<T1> of(T1... values) {
        return Stream.of(values);
    }

    public static <T1> Stream<T1> iterate(T1 seed, UnaryOperator<T1> f) {
        return Stream.iterate(seed, f);
    }

    public static <T1> Stream<T1> generate(Supplier<T1> s) {
        return Stream.generate(s);
    }

    public static <T1> Stream<T1> concat(Stream<? extends T1> a, Stream<? extends T1> b) {
        return Stream.concat(a, b);
    }

    @Override
    public Iterator<T> iterator() {
        return under.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return under.spliterator();
    }

    @Override
    public boolean isParallel() {
        return under.isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return under.sequential();
    }

    @Override
    public Stream<T> parallel() {
        return under.parallel();
    }

    @Override
    public Stream<T> unordered() {
        return under.unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return under.onClose(closeHandler);
    }

    @Override
    public void close() {
        under.close();
    }
}
