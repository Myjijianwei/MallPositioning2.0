package com.project.mapapp.config.RabbitMQ;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 主队列配置
    @Bean
    public Queue applyQueue() {
        return QueueBuilder.durable("apply_queue")
                .withArgument("x-dead-letter-exchange", "dlx.apply_exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.apply_routing_key")
                .withArgument("x-message-ttl", 60000) // 消息存活时间60秒
                .withArgument("x-max-retries", 3) // 最大重试次数
                .build();
    }

    @Bean
    public DirectExchange applyExchange() {
        return new DirectExchange("apply_exchange", true, false);
    }

    @Bean
    public Binding applyBinding() {
        return BindingBuilder.bind(applyQueue())
                .to(applyExchange())
                .with("apply_routing_key");
    }

    // 死信队列配置
    @Bean
    public Queue dlxApplyQueue() {
        return QueueBuilder.durable("dlx.apply_queue")
                .withArgument("x-queue-mode", "lazy") // 懒加载模式
                .build();
    }

    @Bean
    public DirectExchange dlxApplyExchange() {
        return new DirectExchange("dlx.apply_exchange", true, false);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxApplyQueue())
                .to(dlxApplyExchange())
                .with("dlx.apply_routing_key");
    }

    // 重试队列配置
    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable("apply_retry_queue")
                .withArgument("x-dead-letter-exchange", "apply_exchange")
                .withArgument("x-dead-letter-routing-key", "apply_routing_key")
                .withArgument("x-message-ttl", 30000) // 30秒后重试
                .build();
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(applyExchange())
                .with("apply_retry_routing_key");
    }
}
