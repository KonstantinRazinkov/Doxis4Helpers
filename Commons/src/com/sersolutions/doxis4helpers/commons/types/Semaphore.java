package com.sersolutions.doxis4helpers.commons.types;

import java.util.concurrent.TimeUnit;

/**
 * Class of semaphore object
 */
public class Semaphore {
    private java.util.concurrent.Semaphore semaphore;

    private Object locker = new Object();

    /**
     * Init new semaphore only if it was not created before
     * @param countThreads count of threads that could be run in parallel
     */
    public void initSemaphoreIfNull(int countThreads) {
        synchronized (locker) {
            if (semaphore == null) {
                semaphore = new java.util.concurrent.Semaphore(countThreads, true);
            }
        }
    }

    /**
     * Takes semaphore
     * @throws InterruptedException if thread will be interrupted
     */
    public void acquire() throws InterruptedException {
        initSemaphoreIfNull(1);
        semaphore.tryAcquire(30, TimeUnit.SECONDS);
    }

    /**
     * Release semaphore
     */
    public void release() {
        if (semaphore != null) {
            semaphore.release();
        }
    }
}
