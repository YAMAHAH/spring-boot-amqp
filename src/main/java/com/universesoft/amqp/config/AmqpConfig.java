package com.universesoft.amqp.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.universesoft.amqp.props.RabbitmqProps;
import com.universesoft.amqp.util.MessageFatalExceptionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.parser.Feature;
//import com.alibaba.fastjson.parser.ParserConfig;
import com.rabbitmq.client.Channel;
//import com.wlf.demo.pojo.CacheMessage;
//import com.wlf.demo.pojo.MetaMessage;
//import com.wlf.demo.pojo.Order;
//import com.wlf.demo.props.RabbitmqProps;
//import com.wlf.demo.util.AttactMessageFilter;
//import com.wlf.demo.util.CacheCorrelationData;
//import com.wlf.demo.util.MessageCacheManager;
//import com.wlf.demo.util.MessageCacheUtil;
//import com.wlf.demo.util.MessageFatalExceptionStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
public class AmqpConfig {

    public static final String EXCHANGE   = "spring-boot-exchange";
    public static final String ROUTINGKEY = "spring-boot-routingKey";

    private static final Logger logger = LoggerFactory.getLogger(AmqpConfig.class);

    public static final String ROUNTING_KEY_PREFIX="wlf.bussiness";

    public static final String ORDER_SAVE_ROUTING_KEY="order.save";


    @Autowired
    private RabbitmqProps rabbitmqProps;

//    @Autowired
    public AmqpConfig(){
        //this.rabbitmqProps = rabbitmqProps;

    }

    /**
     *
     * rabbitMq连接
     *
     * @return
     */
    @Bean
//    @ConfigurationProperties(prefix="spring.rabbitmq")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setChannelCacheSize(100);
        connectionFactory.setAddresses(rabbitmqProps.getAddresses());
        connectionFactory.setUsername(rabbitmqProps.getUsername());
        connectionFactory.setPassword(rabbitmqProps.getPassword());
        connectionFactory.setVirtualHost("/");
        //开启确认机制，可监听消息是否到达交换机
        connectionFactory.setPublisherConfirms(rabbitmqProps.isPublisherConfirms());
        //mandatory，不可路由时回调
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }

    /**
     *
     * 用于恢复交换机，队列
     *
     * @return
     */
//    @Bean
//    public AmqpAdmin amqpAdmin() {
//        return new RabbitAdmin(connectionFactory());
//    }

    /**
     *
     * rabbitTemplate必须是prototype
     *
     * @return
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());

        template.setMandatory(true);

        //确认机制，监听无法到达交换机时的回调
//        template.setConfirmCallback((correlationData, ack, cause) -> {
//            logger.debug("confirm回调！");
//
////            if (ack) {
////            } else {
////                logger.info(correlationData + "没有使用缓存，消息将被丢弃，不会尝试重发！");
////            }
//        });

        //不可路由时回调mandatory
        template.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            logger.debug("return回调！");
            logger.info("没有找到任何匹配的队列！" +
                    "message:" + message +
                    ",replyCode:" + replyCode +
                    ",replyText:" + replyText +
                    ",exchange:" + exchange +
                    ",routingKey:" + routingKey);
            logger.debug("加入备份缓存！");
        });
        return template;
    }

    /**
     *
     * 业务交换机direct方式
     *
     */
//    @Bean
//    public DirectExchange defaultExchange() {
//        return new DirectExchange(rabbitmqProps.getExchange());
//    }

    /**
     *
     * 业务交换机tipic方式
     *
     */
//    @Bean
//    public TopicExchange bussinessExchange(){
//        return new TopicExchange(rabbitmqProps.getExchange());
//    }

    /**
     *
     * 死信交换机topic方式
     *
     * @return
     */
//    @Bean
//    public TopicExchange dlxExchange() {
//        return new TopicExchange("dlxExchange");
//    }

    /**
     *
     * 业务队列
     *
     * @return
     */
//    @Bean
//    public Queue queue() {
//        Map<String,Object> params=new HashMap<String,Object>();
//        params.put("x-dead-letter-exchange", "dlxExchange");
//        params.put("x-message-ttl", 6000);
//        return new Queue(rabbitmqProps.getQueueName(), true, false, false,params);
//    }

    /**
     *
     * 死信队列
     *
     * @return
     */
//    @Bean
//    public Queue dlxQueue(){
//        Map<String,Object> params=new HashMap<String,Object>();
//        Queue queue=new Queue("dlxQueue", true, false, false,params);
//        return queue;
//    }

    /**
     *
     * 订单业务绑定
     *
     * @return
     */
//    @Bean
//    public Binding binding() {
//        return BindingBuilder.bind(queue()).to(bussinessExchange()).with(rabbitmqProps.getKeys().get("orderRouting"));
//    }

    /**
     *
     * 死信队列绑定
     *
     * @return
     */
//    @Bean
//    public Binding dlxBinding() {
//        //topic的方式
//        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(rabbitmqProps.getKeys().get("orderRouting"));
//    }

    /**
     * 针对消费者配置
     * 1. 设置交换机类型
     * 2. 将队列绑定到交换机
     *
     *
     FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     HeadersExchange ：通过添加属性key-value匹配
     DirectExchange:按照routingkey分发到指定队列
     TopicExchange:多关键字匹配
     */
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue queue() {
        return new Queue("spring-boot-queue", true); //队列持久
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(defaultExchange()).with(AmqpConfig.ROUTINGKEY);
    }

    @Bean
    public SimpleMessageListenerContainer messageContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(queue());
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
        container.setMessageListener(new ChannelAwareMessageListener() {

            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                byte[] body = message.getBody();
                System.out.println("receive msg : " + new String(body));
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //确认消息成功消费
            }
        });
        return container;
    }
    /**
     *
     * 使用@RabbitListener必须用该对象，不建议手动ack
     *
     * @return
     */
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory());
//        factory.setConcurrentConsumers(3);
//        factory.setMaxConcurrentConsumers(10);
//        //尽管这里设置了可重入队列，但是消费端抛出AmqpRejectAndDontRequeueException也可使其不可重入队列
//        //消费端通过控制AmqpRejectAndDontRequeueException来分情况进行是否可重入队列而不是一味重发是很好的方案
//        //一般不可重入队列后，可放入死信队列，然后集中分情况进行处理
//        factory.setDefaultRequeueRejected(true);
//        //自动ack
//        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
//        //设置致命错误，使其不进入死信
//        factory.setErrorHandler(errorHandler());
//
//        return factory;
//    }

    /**
     *
     * 消费端致命错误不进入死信重发
     *
     * @return
     */
    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(new MessageFatalExceptionStrategy());
    }

}
