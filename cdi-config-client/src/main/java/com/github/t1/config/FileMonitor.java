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
    private Thread thread;

    @Setter
    private long interval = initInterval();

    private int initInterval() {
        String property = System.getProperty(FileMonitor.class.getName() + ".interval");
        return (property == null) ? 1_000 : Integer.parseInt(property);
    }

    private void start() {
        if (thread == null) {
            this.thread = new Thread("file monitor " + fileMonitorThreadCount++) {
                private int count;

                @Override
                public void run() {
                    log.debug("start watcher thread with interval {}", interval);
                    run: while (!watchers.isEmpty()) {
                        try {
                            Thread.sleep(interval);
                            log.trace("run {}", count++);

                            for (FileWatcher watcher : watchers) {
                                watcher.run();
                            }
                        } catch (InterruptedException e) {
                            log.debug("watcher thread interrupted");
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
            if (watchers.isEmpty()) {
                log.info("wait for watcher thread to stop");
                try {
                    thread.interrupt();
                    thread.join();
                    thread = null;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
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
