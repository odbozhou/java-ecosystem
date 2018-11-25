/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.spring.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.starter.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.starter.config.TransactionHandlerRegistry;
import org.apache.rocketmq.spring.starter.core.RocketMQListener;
import org.apache.rocketmq.spring.starter.core.RocketMQTemplate;
import org.apache.rocketmq.spring.starter.enums.ConsumeMode;
import org.apache.rocketmq.spring.starter.supports.DefaultRocketMQListenerContainer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.rocketmq.spring.starter.supports.DefaultRocketMQListenerContainerConstants.*;

@Configuration
@Order
@Slf4j
@ComponentScan("org.apache.rocketmq.spring.starter")
public class RocketMQAutoConfiguration {

    @Bean
    public RocketMQProperties rocketMQProperties() {
        RocketMQProperties rocketMQProperties = new RocketMQProperties();
        rocketMQProperties.setNameServer("127.0.0.1:9876");
        RocketMQProperties.Producer producer = new RocketMQProperties.Producer();
        producer.setGroup("testGroup");
        rocketMQProperties.setProducer(producer);
        return rocketMQProperties;
    }

    @Bean
    @Order(1)
    public DefaultMQProducer mqProducer(RocketMQProperties rocketMQProperties) {

        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        String groupName = producerConfig.getGroup();
        Assert.hasText(groupName, "[spring.rocketmq.producer.group] must not be null");

        DefaultMQProducer producer = new DefaultMQProducer(producerConfig.getGroup());
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        producer.setSendMsgTimeout(producerConfig.getSendMsgTimeout());
        producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMsgBodyOverHowmuch());
        producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryAnotherBrokerWhenNotStoreOk());

        return producer;
    }

    @Bean
    public ObjectMapper rocketMQMessageObjectMapper() {
        return new ObjectMapper();
    }

    @Bean(destroyMethod = "destroy")
    @Order(2)
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer mqProducer,
                                             @Autowired(required = false)
                                             @Qualifier("rocketMQMessageObjectMapper")
                                                     ObjectMapper objectMapper) {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(mqProducer);
        if (Objects.nonNull(objectMapper)) {
            rocketMQTemplate.setObjectMapper(objectMapper);
        }

        return rocketMQTemplate;
    }

    @Configuration
    @Order
    public static class ListenerContainerConfiguration implements ApplicationContextAware, InitializingBean {
        private ConfigurableApplicationContext applicationContext;

        private AtomicLong counter = new AtomicLong(0);

        @Resource
        private StandardEnvironment environment;

        @Resource
        private RocketMQProperties rocketMQProperties;

        private ObjectMapper objectMapper;

        public ListenerContainerConfiguration() {
        }

        @Autowired(required = false)
        public ListenerContainerConfiguration(
                @Qualifier("rocketMQMessageObjectMapper") ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        }

        @Override
        public void afterPropertiesSet() {
            Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(RocketMQMessageListener.class);

            if (Objects.nonNull(beans)) {
                beans.forEach(this::registerContainer);
            }
        }

        private void registerContainer(String beanName, Object bean) {
            Class<?> clazz = AopUtils.getTargetClass(bean);

            if (!RocketMQListener.class.isAssignableFrom(bean.getClass())) {
                throw new IllegalStateException(clazz + " is not instance of " + RocketMQListener.class.getName());
            }

            RocketMQListener rocketMQListener = (RocketMQListener) bean;
            RocketMQMessageListener annotation = clazz.getAnnotation(RocketMQMessageListener.class);
            validate(annotation);
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(DefaultRocketMQListenerContainer.class);
            beanBuilder.addPropertyValue(PROP_NAMESERVER, rocketMQProperties.getNameServer());
            beanBuilder.addPropertyValue(PROP_TOPIC, environment.resolvePlaceholders(annotation.topic()));

            beanBuilder.addPropertyValue(PROP_CONSUMER_GROUP, environment.resolvePlaceholders(annotation.consumerGroup()));
            beanBuilder.addPropertyValue(PROP_CONSUME_MODE, annotation.consumeMode());
            beanBuilder.addPropertyValue(PROP_CONSUME_THREAD_MAX, annotation.consumeThreadMax());
            beanBuilder.addPropertyValue(PROP_MESSAGE_MODEL, annotation.messageModel());
            beanBuilder.addPropertyValue(PROP_SELECTOR_EXPRESS, environment.resolvePlaceholders(annotation.selectorExpress()));
            beanBuilder.addPropertyValue(PROP_SELECTOR_TYPE, annotation.selectorType());
            beanBuilder.addPropertyValue(PROP_ROCKETMQ_LISTENER, rocketMQListener);
            if (Objects.nonNull(objectMapper)) {
                beanBuilder.addPropertyValue(PROP_OBJECT_MAPPER, objectMapper);
            }
            beanBuilder.setDestroyMethodName(METHOD_DESTROY);

            String containerBeanName = String.format("%s_%s", DefaultRocketMQListenerContainer.class.getName(), counter.incrementAndGet());
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
            beanFactory.registerBeanDefinition(containerBeanName, beanBuilder.getBeanDefinition());

            DefaultRocketMQListenerContainer container = beanFactory.getBean(containerBeanName, DefaultRocketMQListenerContainer.class);

            if (!container.isStarted()) {
                try {
                    container.start();
                } catch (Exception e) {
                    log.error("started container failed. {}", container, e);
                    throw new RuntimeException(e);
                }
            }

            log.info("register rocketMQ listener to container, listenerBeanName:{}, containerBeanName:{}", beanName, containerBeanName);
        }

        private void validate(RocketMQMessageListener annotation) {
            if (annotation.consumeMode() == ConsumeMode.ORDERLY &&
                    annotation.messageModel() == MessageModel.BROADCASTING)
                throw new BeanDefinitionValidationException("Bad annotation definition in @RocketMQMessageListener, messageModel BROADCASTING does not support ORDERLY message!");
        }
    }


    @Bean
    @Order(3)
    public TransactionHandlerRegistry transactionHandlerRegistry() {
        return new TransactionHandlerRegistry();
    }

    @SuppressWarnings("rawtypes")
    @Bean(name = RocketMQConfigUtils.ROCKETMQ_TRANSACTION_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public RocketMQTransactionAnnotationProcessor RocketMQTransactionAnnotationProcessor() {
        return new RocketMQTransactionAnnotationProcessor();
    }
}
