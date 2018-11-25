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
package com.zhou.rocketmq.provider;

import com.zhou.rocketmq.provider.domain.OrderPaidEvent;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.samples.springboot.domain.OrderPaidEvent;
import org.apache.rocketmq.spring.starter.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.starter.RocketMQProperties;
import org.apache.rocketmq.spring.starter.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.starter.core.MessageHelper;
import org.apache.rocketmq.spring.starter.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.starter.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.starter.core.RocketMQTemplate;
import org.apache.rocketmq.spring.starter.supports.RocketMQMessageConst;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer, using RocketMQTemplate sends a variety of messages
 */
@Configuration
@ComponentScan
@Import(RocketMQAutoConfiguration.class)
@Component
public class ProducerApplication {
    private static final String TX_PGROUP_NAME = "myTxProducerGroup";
    @Resource
    public RocketMQTemplate rocketMQTemplate;
    @Value("${spring.rocketmq.transTopic}")
    private String springTransTopic;
    //    @Value("${spring.rocketmq.topic}")
    private String springTopic = "springTopic";
    @Value("${spring.rocketmq.orderTopic}")
    private String orderPaidTopic;
    @Value("${spring.rocketmq.msgExtTopic}")
    private String msgExtTopic;
    @Value("${spring.rocketmq.broadcastTopic}")
    private String broadcastTopic;

    public static void main(String[] args) throws Exception {
//        SpringApplication.run(ProducerApplication.class, args);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ProducerApplication.class);
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(TransactionListenerImpl.class);
        context.registerBeanDefinition("myListener1", beanBuilder.getBeanDefinition());
        context.refresh();
//        assertThat(this.context.containsBean("rocketMQTemplate")).isTrue();
//        assertThat(this.context.getBeansOfType(TransactionListenerImpl.class)).isNotEmpty();
        ProducerApplication producerApplication = new ProducerApplication();
        producerApplication.rocketMQTemplate = (RocketMQTemplate) context.getBean("rocketMQTemplate");
        producerApplication.run(args);

    }

    //    @Override
    public void run(String... args) {
        // Send string
        SendResult sendResult = rocketMQTemplate.syncSend(springTopic, "Hello, World!");
        System.out.printf("string-topic syncSend1 sendResult=%s %n", sendResult);

   /*     // Send string with spring Message
        sendResult = rocketMQTemplate.syncSend(springTopic, MessageBuilder.withPayload("Hello, World! I'm from spring message").build());
        System.out.printf("string-topic syncSend2 sendResult=%s %n", sendResult);

        // Send user-defined object
        rocketMQTemplate.asyncSend(orderPaidTopic, new OrderPaidEvent("T_001", new BigDecimal("88.00")), new SendCallback() {
            public void onSuccess(SendResult var1) {
                System.out.printf("async onSucess SendResult=%s %n", var1);
            }

            public void onException(Throwable var1) {
                System.out.printf("async onException Throwable=%s %n", var1);
            }

        });

        // Send message with special tag
        rocketMQTemplate.convertAndSend(msgExtTopic + ":tag0", "I'm from tag0");  // tag0 will not be consumer-selected
        rocketMQTemplate.convertAndSend(msgExtTopic + ":tag1", "I'm from tag1");

        // Send transactional messages
        testTransaction();*/


//        SendResult sendResult = rocketMQTemplate.syncSend(broadcastTopic, "broadcast msg");
//        System.out.printf("string-topic syncSend1 sendResult=%s %n", sendResult);
    }


    private void testTransaction() throws MessagingException {
        String[] tags = new String[]{"TagA", "TagB", "TagC", "TagD", "TagE"};
        for (int i = 0; i < 10; i++) {
            try {

                Message msg = MessageBuilder.withPayload("Hello RocketMQ " + i).
                        setHeader(RocketMQMessageConst.KEYS, "KEY_" + i).build();
                SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(TX_PGROUP_NAME,
                        springTransTopic + ":" + tags[i % tags.length], msg, null);
                System.out.printf("------ send Transactional msg body = %s , sendResult=%s %n",
                        msg.getPayload(), sendResult.getSendStatus());

                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RocketMQTransactionListener(txProducerGroup = TX_PGROUP_NAME)
    class TransactionListenerImpl implements RocketMQLocalTransactionListener {
        private AtomicInteger transactionIndex = new AtomicInteger(0);

        private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<String, Integer>();

        @Override
        public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            System.out.printf("------ executeLocalTransaction is executed, msgTransactionId=%s %n",
                    MessageHelper.getTransactionId(msg));
            int value = transactionIndex.getAndIncrement();
            int status = value % 3;
            localTrans.put(MessageHelper.getTransactionId(msg), status);
            if (value == 0) {
                // Return local transaction with success(commit), in this case,
                // this message will not be checked in checkLocalTransaction()
                System.out.printf("------ Simulating msg %s related local transaction exec succeeded! %n", msg.getPayload());
                return RocketMQLocalTransactionState.COMMIT_MESSAGE;
            }

            if (value == 1) {
                // Return local transaction with failure(rollback) , in this case,
                // this message will not be checked in checkLocalTransaction()
                System.out.printf("------ Simulating %s related local transaction exec failed! %n", msg.getPayload());
                return RocketMQLocalTransactionState.ROLLBACK_MESSAGE;
            }

            return RocketMQLocalTransactionState.UNKNOW;
        }

        @Override
        public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
            RocketMQLocalTransactionState retState = RocketMQLocalTransactionState.COMMIT_MESSAGE;
            Integer status = localTrans.get(MessageHelper.getTransactionId(msg));
            if (null != status) {
                switch (status) {
                    case 0:
                        retState = RocketMQLocalTransactionState.UNKNOW;
                        break;
                    case 1:
                        retState = RocketMQLocalTransactionState.COMMIT_MESSAGE;
                        break;
                    case 2:
                        retState = RocketMQLocalTransactionState.ROLLBACK_MESSAGE;
                        break;
                }
            }
            System.out.printf("------ !!! checkLocalTransaction is executed once, msgTransactionId=%s, TransactionState=%s %n",
                    MessageHelper.getTransactionId(msg), retState);
            return retState;
        }
    }

}
