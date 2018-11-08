package com.zhoubo;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * @author : zhoubo
 * @Project: java-framework
 * @Package com.zhoubo
 * @Description: ProviderServiceTest
 * @date Date : 2018-11-08 下午10:34
 */
public class ProviderServiceTest {

    @org.junit.Test
    public void syncSendMessage() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        ProviderService providerService = new ProviderService();
        providerService.syncSendMessage();
    }

    @Test
    public void asyncSendMessage() throws RemotingException, MQClientException, InterruptedException {
        ProviderService providerService = new ProviderService();
        providerService.asyncSendMessage();
    }

    @Test
    public void sendOneWay() throws RemotingException, MQClientException, InterruptedException {
        ProviderService providerService = new ProviderService();
        providerService.sendOneWay();
    }

    @Test
    public void sendOrdered() throws RemotingException, MQClientException, InterruptedException, UnsupportedEncodingException, MQBrokerException {
        ProviderService providerService = new ProviderService();
        providerService.sendOrdered();
    }
}