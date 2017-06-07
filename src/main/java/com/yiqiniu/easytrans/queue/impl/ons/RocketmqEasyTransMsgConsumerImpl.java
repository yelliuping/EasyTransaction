package com.yiqiniu.easytrans.queue.impl.ons;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.yiqiniu.easytrans.config.EasyTransConifg;
import com.yiqiniu.easytrans.protocol.EasyTransRequest;
import com.yiqiniu.easytrans.queue.consumer.EasyTransConsumeAction;
import com.yiqiniu.easytrans.queue.consumer.EasyTransMsgConsumer;
import com.yiqiniu.easytrans.queue.consumer.EasyTransMsgListener;
import com.yiqiniu.easytrans.serialization.ObjectSerializer;

@Lazy
public class RocketmqEasyTransMsgConsumerImpl implements EasyTransMsgConsumer {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static  DefaultMQPushConsumer consumer;
	
	@Resource
	private ObjectSerializer serializer;
	
	@Resource
	private EasyTransConifg config;
	
	@PostConstruct
	private void init(){
		consumer = new DefaultMQPushConsumer(config.getExtendConfig("easytrans.queue.rocketmq.group")); 
	    consumer.setInstanceName(config.getExtendConfig("easytrans.queue.rocketmq.ConsumerId"));  
	    consumer.setNamesrvAddr(config.getExtendConfig("easytrans.queue.rocketmq.addr")); 

	}
	
	
	
	@Override
	public void subscribe(String topic, Collection<String> tag,
			final EasyTransMsgListener listener) {
		
		  try {
				 consumer.subscribe(config.getExtendConfig("easytrans.queue.rocketmq.topic"),"*");
			     consumer.registerMessageListener(new MessageListenerConcurrently(){

					@Override
					public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
							ConsumeConcurrentlyContext context) {
						try{
							if(msgs!=null&&msgs.size()>0){
								for(MessageExt msg:msgs){
									EasyTransConsumeAction consume = listener.consume((EasyTransRequest<?, ?>) serializer.deserialize(msg.getBody()));
									System.out.println(" Receive New Topic:"+msg.getTopic()+",message:"+new String(msg.getBody(),"utf-8"));
								}
							}else{
								System.out.println("Receive New Messages error msgs is empty");
							}
						}catch(Throwable e){
							System.out.println("consumeMessage error:"+e);
						}
						return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
					}
			    	 
			     });
				 consumer.start();
				} catch (MQClientException e) {
					e.printStackTrace();
					return;
				}  
	}

	@Override
	public String getConsumerId() {
		return config.getExtendConfig("easytrans.queue.rocketmq.ConsumerId");
	}



	

}
