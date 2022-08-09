package org.sugarcubes.cloner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * @author Maxim Butov
 */
public class ForkJoinPoolTest {

    static AtomicInteger counter = new AtomicInteger();

    static class Task extends RecursiveAction {

        final int parent;
        final int number = counter.incrementAndGet();
        final int depth;
        final int count;

        private List<Task> subtasks;

        public Task(int parent, int depth, int count) {
            this.parent = parent;
            this.depth = depth;
            this.count = count;
        }

        @Override
        protected void compute() {
            System.out.println(Thread.currentThread().getName() + " Task " + number + " from " + parent);
            if (depth > 0) {
                List<Task> tasks = new ArrayList<>();
                for (int k = 1; k <= count; k++) {
                    tasks.add(new Task(number, depth - 1, count));
                }
                tasks.forEach(Task::fork);
                subtasks = tasks;
            }
        }

        public void joinWithSubtasks() {
            join();
            if (subtasks != null) {
                subtasks.forEach(Task::joinWithSubtasks);
            }
        }

    }

    @Test
    void testForkJoin() {
        ForkJoinPool pool = new ForkJoinPool(3);
        println("BEFORE");
        Task task = new Task(0, 10, 2);
        task.fork();
        task.joinWithSubtasks();
        println("AFTER");
    }

    static synchronized void println(String str) {
        System.out.println(str);
        System.out.flush();
    }

}
