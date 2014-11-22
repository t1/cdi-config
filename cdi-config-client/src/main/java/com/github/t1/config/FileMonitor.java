package com.github.t1.config;

import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Executors;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMonitor {
    private static class FileWatcher {
        private final Path path;
        private FileTime lastModified;
        private final Runnable runnable;

        public FileWatcher(Path path, Runnable runnable) {
            this.path = path;
            this.runnable = runnable;
            this.lastModified = lastModified();
        }

        @SneakyThrows(IOException.class)
        private FileTime lastModified() {
            return Files.getLastModifiedTime(path);
        }

        public void run() {
            if (!lastModified.equals(lastModified())) {
                lastModified = lastModified();
                runnable.run();
            }
        }

        @Override
        public String toString() {
            return "file watcher for " + path;
        }
    }

    private final List<FileWatcher> watchers = new ArrayList<>();
    private boolean started;

    @Setter
    private long interval = initInterval();

    private int initInterval() {
        String property = System.getProperty(FileMonitor.class.getName() + ".interval");
        return (property == null) ? 1_000 : Integer.parseInt(property);
    }

    synchronized private void start() {
        if (started)
            return;
        started = true;
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            private int count;

            @Override
            public void run() {
                log.trace("run {}", count++);

                for (FileWatcher watcher : watchers) {
                    watcher.run();
                }
            }
        }, 0, interval, MILLISECONDS);
    }

    public void add(Path path, Runnable runnable) {
        log.debug("add watcher for {}", path);
        this.watchers.add(new FileWatcher(path, runnable));
        start();
    }

    public void remove(Path path) {
        log.debug("remove watcher for {}", path);
        FileWatcher watcher = findFor(path);
        if (watcher == null) {
            log.warn("tried to remove unknown watcher for {}", path);
        } else {
            this.watchers.remove(watcher);
        }
    }

    private FileWatcher findFor(Path path) {
        for (FileWatcher watcher : watchers) {
            if (watcher.path.equals(path)) {
                return watcher;
            }
        }
        return null;
    }
}
