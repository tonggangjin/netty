package com.agen.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextClosedEvent;

import com.agen.client.handler.AuthenticationHandler;
import com.agen.client.handler.ExceptionHandler;
import com.agen.client.handler.HeartRateHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

/**
 * 
 * <label>
 *		客服端配置类.
 * </label>
 * <p>
 *		<pre>
 *			基本客服端相关的全部配置
 *		</pre>
 * </p>
 * @ClassName Config
 * @author TGJ  
 * @date 2017年10月25日 下午1:13:16 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Configuration("clientConfig")
public class Config {
	
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);
	private static final String USER = "user";

	@Value("${client.uploadThreadNum}")
	private Integer uploadThreadNum;

	@Value("${client.readerIdleTime}")
	private Integer readerIdleTime;

	// @Value("${client.writerIdleTime}")
	private Integer writerIdleTime = 0;

	@Value("${client.allIdleTime}")
	private Integer allIdleTime = 0;
	
	@Value("#{systemProperties['os.name']}")
	private String osName;
	
	@Bean("clientAttributeKey")
	public AttributeKey<Object> getAttributeKey() {
		return AttributeKey.valueOf(USER);
	}

	@Autowired
	private ApplicationContext applicationContext;

	@Bean("clientWorkerGroup")
	public EventLoopGroup getWorkerGroup() {
		return "Linux".equals(osName) ? new EpollEventLoopGroup(uploadThreadNum)
				: new NioEventLoopGroup(uploadThreadNum);
	}

	@Bean("clientObjectEncoder")
	public ObjectEncoder getObjectEncoder() {
		return new ObjectEncoder();
	}

	@Bean("clientObjectDecoder")
	@Scope("prototype")
	public ObjectDecoder getObjectDecoder() {
		return new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader()));
	}

	@Bean("clientChannelInitializer")
	@Scope("prototype")
	public ChannelInitializer<SocketChannel> getChannelInitializer(@Qualifier("clientObjectEncoder") ObjectEncoder objectEncoder) {
		return new ChannelInitializer<SocketChannel>() { // 配置具体的数据处理方式
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(objectEncoder);
				socketChannel.pipeline().addLast(applicationContext.getBean("clientObjectDecoder", ObjectDecoder.class));
				getChannelHandler().forEach(ch -> socketChannel.pipeline().addLast(ch));
			}
		};
	}
	
	@Bean("clientCdleStateHandler")
	@Scope("prototype")
	public IdleStateHandler getIdleStateHandler() {
		return new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS);
	}

	// 所有处理器，都应该在此添加，但请注意顺序
	private List<ChannelHandler> getChannelHandler() {
		List<ChannelHandler> list = new ArrayList<>();
		list.add(applicationContext.getBean("clientCdleStateHandler", IdleStateHandler.class));
		list.add(applicationContext.getBean("clientHeartRateHandler", HeartRateHandler.class));
		list.add(applicationContext.getBean("clientAuthenticationHandler", AuthenticationHandler.class));
		// 如果需要添加处理器，请添加到下面

		// 如非必要，请保持exceptionHandler在最后进行统一异常处理
		list.add(applicationContext.getBean("clientExceptionHandler", ExceptionHandler.class));
		return list;
	}

	@Bean("clientConnectionPool")
	public List<Channel> getConnectionPool() {
		return new CopyOnWriteArrayList<>();
	}

	@Bean("clientApplicationListener")
	public ApplicationListener<ContextClosedEvent> getContextClosedEvent(
			@Qualifier("clientScheduledExecutorService") ScheduledExecutorService scheduledExecutorService) {
		return new ApplicationListener<ContextClosedEvent>() {
			@Override
			public void onApplicationEvent(ContextClosedEvent event) {
				scheduledExecutorService.shutdownNow();
				LOG.info("======================正在关闭中======================");
			}
		};
	}

	@Bean("clientScheduledExecutorService")
	public ScheduledExecutorService getScheduledExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}

	/**
	 * 
	 * <label>
	 *		客服端连接配置类.
	 * </label>
	 * <p>
	 *		<pre>
	 *			
	 *		</pre>
	 * </p>
	 * @Title getBootstrap
	 * @return Bootstrap
	 */
	@Bean("clientBootstrap")
	public Bootstrap getBootstrap() {
		return new Bootstrap();
	}

	/**
	 * 
	 * <label>
	 *		连接建立成功后的回调.
	 * </label>
	 * <p>
	 *		<pre>
	 *			
	 *		</pre>
	 * </p>
	 * @Title getChannelFutureListener
	 * @param bootstrap
	 * @return ChannelFutureListener
	 */
	@Bean("clientChannelFutureListener")
	public ChannelFutureListener getChannelFutureListener(@Qualifier("clientBootstrap") Bootstrap bootstrap) {
		return new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if (future.isSuccess()) {
					LOG.info("connect the frontend server success");
					// do somthing
				} else {
					LOG.error("Error occurs in the channel" + future.cause().getMessage());
					// do somthing
				}
			}
		};
	}

}
