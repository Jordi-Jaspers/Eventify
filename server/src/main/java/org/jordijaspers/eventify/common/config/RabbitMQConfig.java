package org.jordijaspers.eventify.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The configuration for the RabbitMQ Message Queue.
 */
@Slf4j
@EnableRabbit
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    public static final int BATCH_SIZE = 1000;
    public static final int QUEUE_CAPACITY = 10000;

    public static final String QUEUE_NAME = "monitoring.events.queue";
    public static final String EXCHANGE_NAME = "monitoring.events";
    public static final String ROUTING_KEY_PREFIX = "monitoring.event.batch.";

    private final ObjectMapper objectMapper;

    private final ConnectionFactory connectionFactory;

    /**
     * Creates the event exchange.
     *
     * @return the event exchange
     */
    @Bean
    public TopicExchange eventExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    /**
     * Creates the event queue.
     *
     * @return the event queue
     */
    @Bean
    public Queue eventQueue() {
        return QueueBuilder
            .durable(QUEUE_NAME)
            .withArgument("x-message-ttl", 60000)
            .withArgument("x-queue-mode", "lazy")
            .build();
    }

    /**
     * Creates the event binding.
     *
     * @param eventQueue    A queue for the event
     * @param eventExchange An exchange for the event
     * @return the event binding
     */
    @Bean
    public Binding eventBinding(final Queue eventQueue, final TopicExchange eventExchange) {
        return BindingBuilder
            .bind(eventQueue)
            .to(eventExchange)
            .with(ROUTING_KEY_PREFIX + "#");
    }

    /**
     * Creates the message converter.
     *
     * @return the message converter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Creates the RabbitMQ template.
     *
     * @return the RabbitMQ template
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setConfirmCallback((correlation, ack, cause) -> {
            if (!ack) {
                log.error("Message with correlation ID {} was not acknowledged: {}", correlation, cause);
            }
        });
        return template;
    }

    /**
     * Creates the RabbitMQ listener container factory.
     *
     * @return the RabbitMQ listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setPrefetchCount(250);
        factory.setConcurrentConsumers(4);
        factory.setMaxConcurrentConsumers(8);
        return factory;
    }
}
