package com.github.t1.config;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMonitor {
    private static int fileMonitorThreadCount;

    @Setter
    private long interval = 1_000;

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
    }

    private final List<FileWatcher> watchers = new ArrayList<>();
    private Thread thread;

    private void start() {
        if (thread == null) {
            this.thread = new Thread("file monitor " + fileMonitorThreadCount++) {
                private int count;

                @Override
                public void run() {
                    log.debug("start watcher thread");
                    run: while (!watchers.isEmpty()) {
                        try {
                            Thread.sleep(interval);
                            log.debug("run {}", count++);

                            for (FileWatcher watcher : watchers) {
                                watcher.run();
                            }
                        } catch (InterruptedException e) {
                            break run;
                        }
                    }
                    log.debug("stop watcher thread");
                }
            };
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    public void add(Path path, Runnable runnable) {
        this.watchers.add(new FileWatcher(path, runnable));
        start();
    }

    public void remove(Path path) {
        this.watchers.remove(path);
        if (watchers.isEmpty()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
