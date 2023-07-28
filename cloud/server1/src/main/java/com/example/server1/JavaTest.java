package com.example.server1;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.function.*;

@FunctionalInterface
interface TA<T> {
    T get();
}

class Test {
    public static String someThing() {
        return "test";
    }
}

public class JavaTest {
    void test1(TA t) {
        System.out.println(t.get());
    }

    // Predicate	T -> boolean	boolean test(T t)
    // Consumer	T -> void	void accept(T t)
    // Supplier	() -> T	T get()
    // Function<T, R>	T -> R	R apply(T t)
    // Comparator	(T, T) -> int	int compare(T o1, T o2)
    // Runnable	() -> void	void run()
    // Callable	() -> T	V call()
    // BiPredicate	(T, U) -> boolean	boolean test(T t, U u)
    // BiConsumer	(T, U) -> void	void accept(T t, U u)
    // BiFunction	(T, U) -> R	R apply(T t, U u)
    public static void main(String[] args) {
        Predicate<String> predicate = (s) -> s.equals("test");
        Consumer<String> consumer = (s) -> System.out.println(s);
        Supplier<String> supplier = () -> "test";
        Function<String, Integer> function = (s) -> s.length();
        Comparator<String> comparator = (s1, s2) -> s1.compareTo(s2);
        Runnable runnable = () -> System.out.println("run");
        Callable<String> callable = () -> "test";
        BiPredicate<String, String> biPredicate = (s1, s2) -> s1.equals(s2);
        BiConsumer<String, String> biConsumer = (s1, s2) -> System.out.println(s1 + s2);
        BiFunction<String, String, Integer> biFunction = (s1, s2) -> s1.length() + s2.length();

        JavaTest javaTest = new JavaTest();
        javaTest.test1(Test::someThing);
    }
}