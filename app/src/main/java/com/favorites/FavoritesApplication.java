package com.favorites;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ComponentScan(basePackages = {"com.sqss.voice.*","com.favorites.*",})
@EntityScan(basePackages = {"com.sqss.voice.*","com.favorites.*"})
@EnableJpaRepositories(basePackages = {"com.sqss.voice.*","com.favorites.*"})
@EnableScheduling //开启基于注解的定时任务功能
@SpringBootApplication
public class FavoritesApplication extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(FavoritesApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(FavoritesApplication.class, args);
    }
    
    @EnableAsync  //开启基于注解的异步调用功能
    @Configuration
    class TaskPoolConfig {
        // 核心线程数（setCorePoolSize）10：线程池创建时候初始化的线程数
        // 最大线程数（setMaxPoolSize）20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        // 缓冲队列（setQueueCapacity）200：用来缓冲执行任务的队列
        // 允许线程的空闲时间（setKeepAliveSeconds）60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        // 线程池名的前缀（setThreadNamePrefix）：设置好了之后可以方便我们定位处理任务所在的线程池
        // 线程池对拒绝任务的处理策略（setRejectedExecutionHandler）：这里采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在 execute
        // 方法的调用线程中运行被拒绝的任务（setWaitForTasksToCompleteOnShutdown）；如果执行程序已关闭，则会丢弃该任务
        // setWaitForTasksToCompleteOnShutdown（true）该方法就是这里的关键，用来设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean，这样这些异步任务的销毁就会先于Redis线程池的销毁。
        // 同时，这里还设置了setAwaitTerminationSeconds(60)，该方法用来设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
        @Bean("taskExecutor")
        public Executor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(20);
            executor.setQueueCapacity(200);
            executor.setKeepAliveSeconds(60);
            executor.setThreadNamePrefix("taskExecutor-");
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            return executor;
        }
    }
}