package com.yiqiniu.easytrans.queue.impl.ons;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.yiqiniu.easytrans.config.EasyTransConifg;
import com.yiqiniu.easytrans.queue.producer.EasyTransMsgPublishResult;
import com.yiqiniu.easytrans.queue.producer.EasyTransMsgPublisher;

public class RocketmqEasyTransMsgPublisherImpl implements EasyTransMsgPublisher {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private DefaultMQProducer producer;
	
	@Resource
	private EasyTransConifg config;
	
	@PostConstruct
	private void init(){
		
		log.info("Rocketmq is starting ");
		producer = new DefaultMQProducer(config.getExtendConfig("easytrans.queue.rocketmq.group"));
		producer.setInstanceName(config.getExtendConfig("easytrans.queue.rocketmq.ProducerId"));
		producer.setNamesrvAddr(config.getExtendConfig("easytrans.queue.rocketmq.addr"));
		try {
			producer.start();
		} catch (MQClientException e) {
			log.error("start error nameStvAddr:" + producer.getNamesrvAddr() + " group:" + config.getExtendConfig("easytrans.queue.rocketmq.group"));
			producer = null;
			return;
		}
		log.info("start success nameStvAddr:" + producer.getNamesrvAddr() + " group:" + config.getExtendConfig("easytrans.queue.rocketmq.group"));
	}
	
	@Override
	public EasyTransMsgPublishResult publish(String topic, String tag, String key, byte[] msgByte) {
		Message message =  new Message(topic, tag, key, msgByte);
		EasyTransMsgPublishResult easyTransMsgPublishResult = new EasyTransMsgPublishResult();
		SendResult send;
		try {
			send = producer.send(message);
			easyTransMsgPublishResult.setMessageId(send.getMsgId());
		} catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
			log.error("send message error",e);
			return null;
		}
	
		
		return easyTransMsgPublishResult;
	}

}
