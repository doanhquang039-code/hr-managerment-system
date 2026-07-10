package com.example.hr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình thread pool bất đồng bộ cho video upload.
 * Tách riêng khỏi Tomcat thread pool để upload file lớn không block request khác.
 */
@Configuration
@EnableAsync
public class VideoUploadAsyncConfig {

    @Value("${video.upload.async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${video.upload.async.max-pool-size:4}")
    private int maxPoolSize;

    @Value("${video.upload.async.queue-capacity:20}")
    private int queueCapacity;

    /**
     * Bean executor riêng cho video upload.
     * Dùng qualifier "videoUploadExecutor" trong @Async.
     */
    @Bean(name = "videoUploadExecutor")
    public Executor videoUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("video-upload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Đợi upload đang chạy hoàn thành khi shutdown
        executor.initialize();
        return executor;
    }
}
