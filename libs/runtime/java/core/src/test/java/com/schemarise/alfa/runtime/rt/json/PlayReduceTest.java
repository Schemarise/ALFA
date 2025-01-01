package com.schemarise.alfa.runtime.json;

import java.util.*;

/* w w w . j  a  v  a  2  s.c  o  m*/
public class PlayReduceTest {

    public static void main(String[] args) throws Exception {


        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 23),
                        new Person("David", 12));

        Map<String, Person> personsMap = new LinkedHashMap<String, Person>();

        personsMap.put("Max", new Person("Max", 18));
        personsMap.put("Peter", new Person("Peter", 23));
        personsMap.put("Pamela", new Person("Pamela", 23));
        personsMap.put("David", new Person("David", 12));


        personsMap.entrySet().stream().reduce(0,
                (sum, p) -> {
                    System.out.format("accumulator: sum=%s; person=%s\n", sum, p);
                    return sum += p.getValue().age;
                },

                (sum1, sum2) -> {
                    throw new RuntimeException("");
                });


        personsMap.entrySet().stream().reduce(0,
                (sum, p) -> {
                    System.out.format("accumulator: sum=%s; person=%s\n", sum, p);
                    return sum += p.getValue().age;
                },

                (sum1, sum2) -> {
                    System.out.format("combiner: sum1=%s; sum2=%s\n", sum1, sum2);
                    return sum1 + sum2;
                }
        );

        System.out.println("\n\n\n");

        Integer ageSum = persons
                .stream()
                .reduce(0,
                        (sum, p) -> {
                            System.out.format("accumulator: sum=%s; person=%s\n", sum, p);
                            return sum += p.age;
                        },
                        (sum1, sum2) -> {
                            System.out.format("combiner: sum1=%s; sum2=%s\n", sum1, sum2);
                            return sum1 + sum2;
                        });

        System.out.println(ageSum);
    }

}

class Person {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return name;
    }
}