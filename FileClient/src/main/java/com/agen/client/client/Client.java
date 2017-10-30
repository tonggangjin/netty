package com.agen.client.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.agen.client.service.ConnectionPoolService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 
 * <label>
 *		客服端连接.
 * </label>
 * <p>
 *		<pre>
 *			此类主要是连接服务端的作用
 *		</pre>
 * </p>
 * @ClassName Client
 * @author TGJ  
 * @date 2017年10月25日 下午1:11:58 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("clientClient")
public class Client implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(Client.class);

	@Value("${client.port}")
	private Integer port;

	@Value("${client.host}")
	private String host;

	@Value("${client.retryDelay}")
	private Long retryDelay;

	@Value("${client.uploadChanelNum}")
	private Integer uploadChanelNum;

	@Value("${client.tcpCacheSize}")
	private Integer tcpCacheSize;

	@Value("${client.receiveCacheSize}")
	private Integer receiveCacheSize;

	@Autowired
	@Qualifier("clientWorkerGroup")
	private EventLoopGroup workerGroup;

	@Autowired
	@Qualifier("clientBootstrap")
	private Bootstrap bootstrap;

	@Autowired
	@Qualifier("clientChannelFutureListener")
	private ChannelFutureListener channelFutureListener;

	@Autowired
	@Qualifier("clientConnectionPoolService")
	private ConnectionPoolService connectionPoolService;

	@Autowired
	private ApplicationContext applicationContext;

	public ChannelInitializer<?> getChannelHandler() {
		return applicationContext.getBean("clientChannelInitializer", ChannelInitializer.class);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) { // 非web环境可以不用这行
			initNettyClient();
		}
	}
	
	/**
	 * 
	 * <label>
	 *		建立连接.
	 * </label>
	 * <p>
	 *		<pre>
	 *			按指定的参数与服务器建立连接
	 *		</pre>
	 * </p>
	 * @Title initNettyClient void
	 */
	public void initNettyClient() {
		if (null == bootstrap.config().group()) {
			bootstrap.group(workerGroup); // 绑定线程组
		}
		bootstrap.channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class) // 按系统指定连接模式
				.handler(new LoggingHandler(LogLevel.INFO)).handler(getChannelHandler())
				.option(ChannelOption.SO_SNDBUF, tcpCacheSize) // 设置TCP缓冲区
				.option(ChannelOption.SO_RCVBUF, receiveCacheSize) // 设置接受数据缓冲大小
				.option(ChannelOption.SO_KEEPALIVE, true); // 保持连接
		for (int i = 0; i < uploadChanelNum; i++) {
			try {
				ChannelFuture future = bootstrap.connect(host, port).sync();
				future.addListener(channelFutureListener);
//				future.channel().closeFuture().sync(); // 在spring boot
				// 环境下在这个回调中将导致应用无法关闭
			} catch (Exception e) {
				LOG.warn("Error occurs in the channel, connect server failed" + e.getMessage());
				// workerGroup.shutdownGracefully();
				connectionPoolService.retryCon();
			}
		}
	}
}
