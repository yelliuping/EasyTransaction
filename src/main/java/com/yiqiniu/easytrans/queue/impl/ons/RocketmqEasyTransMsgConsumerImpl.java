package com.yiqiniu.easytrans.queue.impl.ons;

import java.util.Collection;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.yiqiniu.easytrans.config.EasyTransConifg;
import com.yiqiniu.easytrans.protocol.EasyTransRequest;
import com.yiqiniu.easytrans.queue.consumer.EasyTransConsumeAction;
import com.yiqiniu.easytrans.queue.consumer.EasyTransMsgConsumer;
import com.yiqiniu.easytrans.queue.consumer.EasyTransMsgListener;
import com.yiqiniu.easytrans.serialization.ObjectSerializer;

@Lazy
public class RocketmqEasyTransMsgConsumerImpl implements EasyTransMsgConsumer {

	private static  DefaultMQPushConsumer consumer;
	
	@Resource
	private ObjectSerializer serializer;
	
	@Resource
	private EasyTransConifg config;
	
	@PostConstruct
	private void init(){
		/*Properties properties = new Properties();
		properties.put(PropertyKeyConst.ONSAddr,); // 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.AccessKey, config.getExtendConfig("easytrans.queue.ons.producer.key")); // 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.SecretKey, config.getExtendConfig("easytrans.queue.ons.producer.secrect"));// 此处以公有云生产环境为例
		properties.put(PropertyKeyConst.ConsumerId, config.getExtendConfig("easytrans.queue.ons.consumer.name")); // 您在控制台创建的Producer ID
		consumer = ONSFactory.createConsumer(properties);
		consumer.start();*/
		
		
		consumer.setNamesrvAddr(config.getExtendConfig("easytrans.queue.rocketmq.addr")); 
		consumer = new DefaultMQPushConsumer(config.getExtendConfig("easytrans.queue.rocketmq.group")); 
	    consumer.setInstanceName(config.getExtendConfig("easytrans.queue.rocketmq.ConsumerId"));  

	}
	
	
	private String getAliTagsString(Collection<String> topicSubs) {
		StringBuilder sb = new StringBuilder();
		for(String s:topicSubs){
			sb.append(s);
			sb.append("||");
		}
		return sb.substring(0, sb.length() - 2);
	}
	
	
	@Override
	public void subscribe(String topic, Collection<String> tag,
			final EasyTransMsgListener listener) {
		
		consumer.subscribe(topic, getAliTagsString(tag), new MessageListener() {
			@Override
			public Action consume(Message message, ConsumeContext context) {
				EasyTransConsumeAction consume = listener.consume((EasyTransRequest<?, ?>) serializer.deserialize(message.getBody()));
				return Action.valueOf(consume.name());
			}});
	}

	@Override
	public String getConsumerId() {
		return config.getExtendConfig("easytrans.queue.ons.consumer.name");
	}



	

}
