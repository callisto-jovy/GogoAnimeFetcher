/*
 * Copyright (c) 2021. Roman P.
 * All code is owned by Roman P. APIs are mentioned.
 * Last modified: 03.01.21, 20:22
 */

package net.bplaced.abzzezz.gogoanime.util;


import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskExecutor {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean cancelled;

    public <R> void executeAsync(Callable<R> callable) {
        executor.execute(() -> {
            try {
                callable.call();
            } catch (final Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Task execution failed: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        });
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
