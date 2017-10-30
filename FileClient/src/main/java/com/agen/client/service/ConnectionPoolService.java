package com.agen.client.service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.agen.netty.entity.User;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * 
 * <label>
 *		连接池.
 * </label>
 * <p>
 *		<pre>
 *			与服务端建立的连接池
 *		</pre>
 * </p>
 * @ClassName ConnectionPoolService
 * @author TGJ  
 * @date 2017年10月25日 下午1:17:47 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Service("clientConnectionPoolService")
public class ConnectionPoolService {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionPoolService.class);

	@Value("${client.port}")
	private Integer port;

	@Value("${client.host}")
	private String host;

	@Value("${client.retryDelay}")
	private Long retryDelay;
	
	@Value("${client.writeableRepeatValidate}")
	private Boolean writeableRepeatValidate;
	
	@Value("${client.writeableRepeatValidateTime}")
	private int writeableRepeatValidateTime;
	
	@Value("${client.writeableRepeatValidateSleepTime}")
	private Long writeableRepeatValidateSleepTime;
	
	@Autowired
	@Qualifier("clientBootstrap")
	private Bootstrap bootstrap;
	
	@Autowired
	@Qualifier("clientAttributeKey")
	private AttributeKey<Object> key;
	
	private AtomicInteger ai = new AtomicInteger(0);

	@Autowired
	@Qualifier("clientConnectionPool")
	private List<Channel> connectionPool;
	
	@Autowired
	@Qualifier("clientChannelFutureListener")
	private ChannelFutureListener channelFutureListener;

	@Autowired
	@Qualifier("clientScheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;
	
	@PostConstruct
	public void initCheck() {
		if (writeableRepeatValidateTime < 0) {
			writeableRepeatValidateTime = 1000;
			throw new IllegalArgumentException("writeableRepeatValidateTime must be greater than 0");
		}
		if (writeableRepeatValidateSleepTime < 0) {
			writeableRepeatValidateSleepTime = 2L;
			throw new IllegalArgumentException("writeableRepeatValidateSleepTime must be greater than 1");
		}
	}
	
	public Channel getChannel() {
		Channel channel = null;
		if (connectionPool.isEmpty()) return channel;
		synchronized (this) {
			int repeatTime = 0;
			while (true) {
				int index = ai.incrementAndGet();
				if (index < connectionPool.size()) {
					channel = connectionPool.get(index);
					try {
						channel = validate(channel);
						if (null != channel) return channel;
					} catch (InterruptedException e) {
						// don't do anything
					}
				} else {
					repeatTime++;
					ai.set(-1);
					if (repeatTime == 2) return null;
				}
			}
		}
	}
		
	private Channel validate(Channel channel) throws InterruptedException {
		if (channel.isOpen() && channel.isActive()) {
			if (writeableRepeatValidate) {
				int writableTime = 0;
				while (!channel.isWritable() && writableTime < writeableRepeatValidateTime) {  
					channel.flush();								
					writableTime++;
					Thread.sleep(writeableRepeatValidateSleepTime);
				}
			}
			return channel.isWritable() ? channel : null;
		} else {
			LOG.error("连接不可用" + channel.isOpen() + ", " + channel.isWritable());
			close(channel);
			retryCon();
			return null;
		}
	}

	public void dealService(Channel channel, User user) {
		synchronized (this) {
			if (null != channel && null != user && !connectionPool.contains(channel)) {
				connectionPool.add(channel);
				setUser(channel, user);
				LOG.info("user connected --> uuid:" + user.getSessionId() + " --> totalcon:" + connectionPool.size());
			}
		}
	}
	
	private void setUser(Channel channel, User user) {
		 Attribute<Object> obj = channel.attr(key);
		 obj.set(user);
	}
	
	public User getUser(Channel channel) {
		 Attribute<Object> obj = channel.attr(key);
		 return null == obj.get() ? null : (User) obj.get();
	}

	public User remove(Channel channel) {
		if (null != channel) {
			connectionPool.remove(channel);
			return getUser(channel);
		}
		return null;
	}

	public User close(Channel channel) {
		if (null != channel) {
			User user = remove(channel);
			if (channel.isOpen())
				channel.close();
			return user;
		}
		return null;
	}
	
	public User close(Channel channel, boolean retry) {
		if (null == channel) return null;
		User user = close(channel);
		if (retry && null != user) 
			retryCon();
		return user;
	}

	/**
	 * 
	 * <label>
	 *		放到线程池里面重试连接.
	 * </label>
	 * <p>
	 *		<pre>
	 *			
	 *		</pre>
	 * </p>
	 * @Title retryCon void
	 */
	public void retryCon() {
		if (!scheduledExecutorService.isShutdown()) return;
		try {
			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.addListener(channelFutureListener);
		} catch (InterruptedException e) {
			LOG.error("Error occurs in the channel, retry connect server failed" + e.getMessage());
			scheduledExecutorService.schedule(() -> retryCon(), retryDelay, TimeUnit.SECONDS);
		}
	}
}
