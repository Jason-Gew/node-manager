package gew.nodemanager.server.config;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Jason/GeW
 * @since 2017-03-24
 */

@Component
@Validated
@EnableAsync
@Configuration
@ConfigurationProperties(prefix = "thread-pool")
public class AsyncThreadPoolConfig implements InitializingBean {

    private Integer coreSize;

    @NotNull
    private Integer maxSize;

    @NotNull
    private Integer queueCapacity;

    @NotNull
    private Integer keepAliveSec;

    private boolean waitForComplete;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.coreSize == null || this.coreSize < 1) {
            this.coreSize = Runtime.getRuntime().availableProcessors();
        }
    }

    @Bean("msgProcessThreadPool")
    public TaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(coreSize);           // Core Thread Pool Size

        executor.setMaxPoolSize(maxSize);             // Maximum Thread Pool Size (should >= Core PoOL Size)

        executor.setQueueCapacity(queueCapacity);     // Waiting Queue Capacity

        executor.setKeepAliveSeconds(keepAliveSec);   // Keep Alive Seconds

        executor.setThreadNamePrefix("MsgProcess-Thread-");        // Thread Pool Name

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // Reject New Thread Policy

        executor.setWaitForTasksToCompleteOnShutdown(waitForComplete); // Close After Finishing All Tasks

        return executor;
    }

    public Integer getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(Integer coreSize) {
        this.coreSize = coreSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public Integer getKeepAliveSec() {
        return keepAliveSec;
    }

    public void setKeepAliveSec(Integer keepAliveSec) {
        this.keepAliveSec = keepAliveSec;
    }

    public boolean isWaitForComplete() {
        return waitForComplete;
    }

    public void setWaitForComplete(boolean waitForComplete) {
        this.waitForComplete = waitForComplete;
    }
}