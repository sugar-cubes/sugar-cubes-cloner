package org.sugarcubes.cloner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import com.google.common.base.Stopwatch;

/**
 * An utility for comparing ReflectionCloner vs SerializableCloner performance.
 *
 * @author Maxim Butov
 */
public class PerformanceMeter {

    private static Object randomObjectTree(int depth) {
        if (depth == 0) {
            return RandomStringUtils.randomAlphanumeric(32);
        }
        int count = RandomUtils.nextInt(0, depth + 1);
        if (RandomUtils.nextBoolean()) {
            List list = new ArrayList();
            for (int k = 0; k < count; k++) {
                list.add(randomObjectTree(RandomUtils.nextInt(0, depth)));
            }
            return list;
        }
        else {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int k = 0; k < count; k++) {
                map.put(RandomStringUtils.randomAlphanumeric(32), randomObjectTree(RandomUtils.nextInt(0, depth)));
            }
            return map;
        }
    }

    public static void main(String[] args) throws Throwable {

        Stopwatch sw = Stopwatch.createUnstarted();

        final int iterations = 100;
        final int count = 100;
        final int depth = 16;

        List objects = new ArrayList();

        sw.start();
        for (int k = 0; k < count; k++) {
            objects.add(randomObjectTree(depth));
        }
        long creation = sw.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println("creation = " + creation);

        sw.start();
        for (int k = 0; k < iterations; k++) {
            Cloner reflectionCloner = Cloners.reflection();
            reflectionCloner.clone(objects);
        }
        long reflection = sw.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println("reflection = " + reflection);

        sw.start();
        for (int k = 0; k < iterations; k++) {
            Cloner unsafeCloner = Cloners.reflection();
            unsafeCloner.clone(objects);
        }
        long unsafe = sw.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println("unsafe = " + unsafe);

        sw.start();
        for (int k = 0; k < iterations; k++) {
            Cloner serializationCloner = Cloners.serialization();
            serializationCloner.clone(objects);
        }
        long serilization = sw.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println("serilization = " + serilization);

    }

}
