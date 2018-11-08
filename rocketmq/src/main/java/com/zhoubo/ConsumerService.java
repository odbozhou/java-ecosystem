package com.zhoubo;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author : zhoubo
 * @Project: java-framework
 * @Package com.zhoubo
 * @Description: ConsumerService
 * @date Date : 2018-11-08 下午10:42
 */
public class ConsumerService {
    
    public void receiveMessage() throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer("consuerGroup");
        defaultMQPushConsumer.setNamesrvAddr("127.0.0.1:9876");
//        defaultMQPushConsumer.getConsumeFromWhere();
        defaultMQPushConsumer.subscribe("testTopic", "*");
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.println("msgs: " + msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
    }

    public void receiveOrderedMessage() throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer("orderedConsumerGroup");
        defaultMQPushConsumer.setNamesrvAddr("127.0.0.1:9876");
        defaultMQPushConsumer.subscribe("testTopic", "taga || tagb || tagc");
        defaultMQPushConsumer.registerMessageListener(new MessageListenerOrderly() {
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                context.setAutoCommit(false);
                return null;
            }
        });
    }
}
