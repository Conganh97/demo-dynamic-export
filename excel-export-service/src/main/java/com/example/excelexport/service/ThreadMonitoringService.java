package com.example.excelexport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ThreadMonitoringService {

    @Autowired
    @Qualifier("exportTaskExecutor")
    private Executor exportTaskExecutor;

    public Map<String, Object> getThreadPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        
        if (exportTaskExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) exportTaskExecutor;
            ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
            
            status.put("threadPoolType", "ThreadPoolTaskExecutor");
            status.put("corePoolSize", executor.getCorePoolSize());
            status.put("maxPoolSize", executor.getMaxPoolSize());
            status.put("queueCapacity", executor.getQueueCapacity());
            status.put("activeCount", threadPool.getActiveCount());
            status.put("poolSize", threadPool.getPoolSize());
            status.put("queueSize", threadPool.getQueue().size());
            status.put("completedTaskCount", threadPool.getCompletedTaskCount());
            status.put("taskCount", threadPool.getTaskCount());
            status.put("largestPoolSize", threadPool.getLargestPoolSize());
            status.put("threadNamePrefix", executor.getThreadNamePrefix());
        } else {
            status.put("threadPoolType", exportTaskExecutor.getClass().getSimpleName());
            status.put("message", "Detailed monitoring not available for this executor type");
        }
        
        return status;
    }

    public Map<String, Object> getCurrentThreadInfo() {
        Map<String, Object> threadInfo = new HashMap<>();
        Thread currentThread = Thread.currentThread();
        
        threadInfo.put("currentThreadName", currentThread.getName());
        threadInfo.put("currentThreadId", currentThread.getId());
        threadInfo.put("threadGroupName", currentThread.getThreadGroup().getName());
        threadInfo.put("isMainThread", currentThread.getName().contains("main"));
        threadInfo.put("isAsyncThread", currentThread.getName().contains("ExportAsync"));
        threadInfo.put("activeThreadCount", Thread.activeCount());
        
        return threadInfo;
    }
}
