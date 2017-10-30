package com.agen.server.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 
 * <label>
 *		netty服务启动类.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName Server
 * @author tgj  
 * @date 2017年10月30日 上午9:12:38 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("serverServer")
@ConfigurationProperties
public class Server implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${server.port}")
	private Integer port;

	@Value("${server.tcpCacheSize}")
	private Integer tcpCacheSize;

	@Value("${server.receiveCacheSize}")
	private Integer receiveCacheSize;

	@Autowired
	@Qualifier("serverBossGroup")
	private EventLoopGroup bossGroup;

	@Autowired
	@Qualifier("serverWorkerGroup")
	private EventLoopGroup workerGroup;

	@Autowired
	@Qualifier("serverBootstrap")
	private ServerBootstrap bootstrap; // 辅助工具类，用于服务器通道的一系列配置

	@Autowired
	private ApplicationContext applicationContext;

	public ChannelInitializer<?> getChannelHandler() {
		return applicationContext.getBean("serverChannelInitializer", ChannelInitializer.class);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) { // 非web环境可以不用这行
			initNettyServer();
		}
	}

	public void initNettyServer() {
		if (null == bootstrap.config().group()) {
			bootstrap.group(workerGroup); // 绑定线程组
		}
		bootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class) // 按系统指定连接模式
				.handler(new LoggingHandler(LogLevel.INFO)).childHandler(getChannelHandler())
				.option(ChannelOption.SO_BACKLOG, tcpCacheSize) // 设置TCP缓冲区
				.option(ChannelOption.SO_RCVBUF, receiveCacheSize) // 设置接受数据缓冲大小
				.childOption(ChannelOption.SO_KEEPALIVE, true); // 保持连接

		try {
			ChannelFuture future = bootstrap.bind(port).sync();
			if (future.isSuccess()) {
				System.out.println("Monitor server on at port: " + port + ".");
			}
			// future.channel().closeFuture().sync(); // 在spring boot
			// 环境下在这个回调中将导致应用无法关闭
		} catch (Exception e) {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
