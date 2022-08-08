package org.sugarcubes.cloner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import org.junit.jupiter.api.Test;

/**
 * @author Maxim Butov
 */
public class ForkJoinPoolTest {

    static class Task extends RecursiveAction {

        final String str;
        final int depth;
        final int count;

        public Task(String str, int depth, int count) {
            this.str = str;
            this.depth = depth;
            this.count = count;
        }

        @Override
        protected void compute() {
            System.out.println(Thread.currentThread().getName() + " Task " + str);
            if (depth > 0) {
                List<Task> tasks=new ArrayList<>();
                for (int k = 1; k <= count; k++) {
                    tasks.add(new Task(str + "-" + k, depth - 1, count));
                }
                tasks.forEach(ForkJoinTask::fork);
                tasks.forEach(ForkJoinTask::join);
            }
        }

    }

    @Test
    void testForkJoin() {
//        ForkJoinPool pool = new ForkJoinPool(3);
        println("BEFORE");
        Task task = new Task("1", 10, 2);
        task.fork().join();
        println("AFTER");
    }

    static synchronized void println(String str) {
        System.out.println(str);
        System.out.flush();
    }

}
