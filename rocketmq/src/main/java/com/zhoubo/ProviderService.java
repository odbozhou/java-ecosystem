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
import java.util.ArrayList;
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
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("orderedConsumerGroup");
        defaultMQProducer.setNamesrvAddr("127.0.0.1:9876");
        defaultMQProducer.start();
        //
        String[] tags = {"createOrder", "pay", "deliver"};
        List<OrderMessage> orderMessages = createOrderMessage();

        for (int i = 0; i < orderMessages.size(); i++) {
            int orderId = orderMessages.get(i).getOrderId();
            String tag = orderMessages.get(i).getOrderTag();
            Message msg = new Message("testTopic", tag, "ordered Hello World".getBytes(RemotingHelper.DEFAULT_CHARSET));
            System.out.println("orderId: " + i);
            defaultMQProducer.send(msg, new MessageQueueSelector() {
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    System.out.println("mqs size = " + mqs.size());
                    Integer i = (Integer) arg;
                    System.out.println("arg: " + arg);
                    return mqs.get(i % mqs.size());
                }
            }, orderId);
        }

    }

    private List<OrderMessage> createOrderMessage() {
        List<OrderMessage> orderMessages = new ArrayList<OrderMessage>(8);
        OrderMessage orderMessage1 = new OrderMessage();
        orderMessage1.setOrderId(0);
        orderMessage1.setOrderTag("createOrder");

        OrderMessage orderMessage2 = new OrderMessage();
        orderMessage2.setOrderId(1);
        orderMessage2.setOrderTag("createOrder");

        OrderMessage orderMessage3 = new OrderMessage();
        orderMessage3.setOrderId(2);
        orderMessage3.setOrderTag("createOrder");

        OrderMessage orderMessage4 = new OrderMessage();
        orderMessage4.setOrderId(0);
        orderMessage4.setOrderTag("pay");

        OrderMessage orderMessage5 = new OrderMessage();
        orderMessage5.setOrderId(0);
        orderMessage5.setOrderTag("createOrder");

        OrderMessage orderMessage6 = new OrderMessage();
        orderMessage6.setOrderId(0);
        orderMessage6.setOrderTag("deliver");

        orderMessages.add(orderMessage1);
        orderMessages.add(orderMessage2);
        orderMessages.add(orderMessage3);
        orderMessages.add(orderMessage4);
        orderMessages.add(orderMessage5);
        orderMessages.add(orderMessage6);

        return orderMessages;

    }
}

class OrderMessage {
    private Integer orderId;
    private String orderTag;
    private String data;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderTag() {
        return orderTag;
    }

    public void setOrderTag(String orderTag) {
        this.orderTag = orderTag;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
