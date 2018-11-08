package com.zhoubo;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.common.RemotingUtil;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhoubo
 */
public class ProviderService {

    public void syncSendMessage() throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("syncProducerGroup");
        defaultMQProducer.setNamesrvAddr("127.0.0.1:9876");
        defaultMQProducer.start();
        for (int i = 0; i < 10; i++) {
            SendResult sendResult = defaultMQProducer.send(new Message("testTopic", "Hello World!".getBytes()));
            System.out.println(Thread.currentThread().getName() + " onSuccess");
            System.out.println(sendResult.toString());
        }
        Thread.sleep(1000);
        System.out.println(Thread.currentThread().getName() + " end");
        defaultMQProducer.shutdown();
    }

    public void asyncSendMessage() throws RemotingException, MQClientException, InterruptedException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("asyncProducerGroup");
        defaultMQProducer.setNamesrvAddr("127.0.0.1:9876");
        defaultMQProducer.start();
        System.out.println(Thread.currentThread().getName() + " start");
        final CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            defaultMQProducer.send(new Message("testTopic", "tagA", ("Asynchronize Hello World !" + i).getBytes()), new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    countDownLatch.countDown();
                    System.out.println(Thread.currentThread().getName() + " " + sendResult.getMsgId() + " onSuccess");
                }

                public void onException(Throwable e) {
                    System.out.println(Thread.currentThread().getName() + e);
                }
            });
        }
        System.out.println(Thread.currentThread().getName() + " end");
        countDownLatch.await();
        defaultMQProducer.shutdown();
    }

    public void sendOneWay() throws MQClientException, RemotingException, InterruptedException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("onewayProducer");
        defaultMQProducer.setNamesrvAddr("127.0.0.1:9876");
        defaultMQProducer.start();
        defaultMQProducer.sendOneway(new Message("testTopic", "tagA", "oneway Hello World!".getBytes()));
        defaultMQProducer.shutdown();
    }

    public void sendOrdered() throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("orderedProducerGroup");
        defaultMQProducer.setNamesrvAddr("127.0.0.1:9876");
        defaultMQProducer.start();
        String[] tags = {"taga", "tagb", "tagc", "tagd", "tage"};
        for (int i = 0; i < 100; i++) {
            int orderId = i;
            Message msg = new Message("testTopic", tags[i % tags.length], "ordered Hello World".getBytes(RemotingHelper.DEFAULT_CHARSET));
            System.out.println("orderId: " + i);
            defaultMQProducer.send(msg, new MessageQueueSelector() {
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer i = (Integer) arg;
                    System.out.println("arg: " + arg);
                    return mqs.get(i % mqs.size());
                }
            }, orderId);
        }

    }
}
