package com.agen.server.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

import com.agen.netty.entity.User;
import com.agen.server.handler.AuthenticationHandler;
import com.agen.server.handler.ExceptionHandler;
import com.agen.server.handler.FileReceiveHandler;
import com.agen.server.handler.HeartRateHandler;

import io.netty.bootstrap.ServerBootstrap;
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
 *		总配置.
 * </label>
 * <p>
 *		<pre>
 *			基本整个项目的配置都在此	
 *		</pre>
 * </p>
 * @ClassName Config
 * @author tgj  
 * @date 2017年10月30日 上午9:10:14 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Configuration("serverConfig")
public class Config {
	
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);
	private static final String USER = "user";

	@Value("${server.uploadThreadNum}")
	private Integer uploadThreadNum;

	@Value("${server.maxConnetionNum}")
	private Integer maxConnetionNum;

	@Value("${server.readerIdleTime}")
	private Integer readerIdleTime;

	// @Value("${server.writerIdleTime}")
	private Integer writerIdleTime = 0;

	// @Value("${server.allIdleTime}")
	private Integer allIdleTime = 0;

	@Value("${server.fileMaxLength}")
	private Integer fileMaxLength;

	@Value("#{systemProperties['os.name']}")
	private String osName;

	@Autowired
	private ApplicationContext applicationContext;

	@Bean(name = "serverBossGroup")
	public EventLoopGroup getBossGroup() {
		return "Linux".equals(osName) ? new EpollEventLoopGroup() : new NioEventLoopGroup();
	}

	@Bean(name = "serverWorkerGroup")
	public EventLoopGroup getWorkerGroup() {
		return "Linux".equals(osName) ? new EpollEventLoopGroup(uploadThreadNum)
				: new NioEventLoopGroup(uploadThreadNum);
	}

	@Bean("serverObjectEncoder")
	public ObjectEncoder getObjectEncoder() {
		return new ObjectEncoder();
	}

	@Bean("serverObjectDecoder")
	@Scope("prototype")
	public ObjectDecoder getObjectDecoder() {
		return new ObjectDecoder(fileMaxLength,
				ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader()));
	}

	@Bean("serverAuthentication")
	public Map<String, User> getAuthentication() {
		return new ConcurrentHashMap<>(maxConnetionNum);
	}
	
	@Bean("serverBootstrap")
	public ServerBootstrap getServerBootstrap() {
		return new ServerBootstrap();
	}

	@Bean("serverChannelInitializer")
	@Scope("prototype")
	public ChannelInitializer<SocketChannel> getChannelInitializer(@Qualifier("serverObjectEncoder") ObjectEncoder objectEncoder) {
		return new ChannelInitializer<SocketChannel>() { // 配置具体的数据处理方式
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(objectEncoder);
				socketChannel.pipeline().addLast(applicationContext.getBean("serverObjectDecoder", ObjectDecoder.class));
				getChannelHandler().forEach(ch -> socketChannel.pipeline().addLast(ch));
			}
		};
	}

	@Bean("serverIdleStateHandler")
	@Scope("prototype")
	public IdleStateHandler getIdleStateHandler() {
		return new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS);
	}

	// 所有处理器，都应该在此添加，但请注意顺序
	private List<ChannelHandler> getChannelHandler() {
		List<ChannelHandler> list = new ArrayList<>();
		list.add(applicationContext.getBean("serverIdleStateHandler", IdleStateHandler.class));
		list.add(applicationContext.getBean("serverHeartRateHandler", HeartRateHandler.class));
		list.add(applicationContext.getBean("serverAuthenticationHandler", AuthenticationHandler.class));
		// 如果需要添加处理器，请添加到下面
		list.add(applicationContext.getBean("serverFileReceiveHandler", FileReceiveHandler.class));

		// 如非必要，请保持exceptionHandler在最后进行统一异常处理
		list.add(applicationContext.getBean("serverExceptionHandler", ExceptionHandler.class));
		return list;
	}

	@Bean("serverAttributeKey")
	public AttributeKey<Object> getAttributeKey() {
		return AttributeKey.valueOf(USER);
	}

	@Bean("serverApplicationListener")
	public ApplicationListener<ContextClosedEvent> getContextClosedEvent() {
		return new ApplicationListener<ContextClosedEvent>() {
			@Override
			public void onApplicationEvent(ContextClosedEvent event) {
				LOG.info("======================正在关闭中======================");
			}
		};
	}

}
