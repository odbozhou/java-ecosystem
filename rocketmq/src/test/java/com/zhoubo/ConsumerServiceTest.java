package com.zhoubo;

import org.apache.rocketmq.client.exception.MQClientException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author : zhoubo
 * @Project: java-framework
 * @Package com.zhoubo
 * @Description: TODO
 * @date Date : 2018-11-08 下午10:51
 */
public class ConsumerServiceTest {

    @Test
    public void receiveMessage() throws MQClientException, InterruptedException {
        ConsumerService consumerService = new ConsumerService();
        consumerService.receiveMessage();
        synchronized (this) {
            this.wait();
        }
    }

    @Test
    public void receiveOrderedMessage() throws InterruptedException, MQClientException {
        ConsumerService consumerService = new ConsumerService();
        consumerService.receiveOrderedMessage();
        synchronized (this) {
            this.wait();
        }
    }
}