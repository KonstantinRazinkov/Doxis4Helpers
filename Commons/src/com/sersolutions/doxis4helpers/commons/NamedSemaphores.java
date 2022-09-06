package com.sersolutions.doxis4helpers.commons;

import com.sersolutions.doxis4helpers.commons.types.Semaphore;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for working with semaphores
 */
public class NamedSemaphores {


    static ConcurrentHashMap<String, Semaphore> semaphores = new  ConcurrentHashMap<>();

    static Object locker = new Object();

    /**
     * Init some semaphore with number of threads that could use this semaphore
     * @param name of the semaphore
     * @param threadCount number of threads that could run in parallel
     * @return Semaphore object
     * @see Semaphore
     */
    public static Semaphore initSemaphore(String name, int threadCount) {
        synchronized (locker) {
            if (!semaphores.containsKey(name)) {
                Semaphore semaphore = new Semaphore();
                semaphore.initSemaphoreIfNull(threadCount);
                semaphores.putIfAbsent(name, semaphore);
            }
            return semaphores.get(name);
        }
    }

    /**
     * Takes one semaphore
     * @param name of the semaphore
     * @throws InterruptedException if thread will be interrupted
     */
    public static void acquire(String name) throws InterruptedException {
        acquire(name, 1);
    }

    /**
     * Takes semaphore with allowed number of threads
     * @param name of the semaphore
     * @param threadCount number of threads that could run in parallel
     * @throws InterruptedException if thread will be interrupted
     */
    public static void acquire(String name, int threadCount) throws InterruptedException {
        Semaphore semaphore = initSemaphore(name, threadCount);
        semaphore.acquire();
    }

    /**
     * Free semaphore
     * @param name of the semaphore
     */
    public static void release(String name) {
        try {
            Semaphore semaphore = initSemaphore(name, 1);
            semaphore.release();
        } catch (Exception ex) {
        }

    }
}
