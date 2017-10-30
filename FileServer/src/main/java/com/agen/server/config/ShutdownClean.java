package com.agen.server.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.netty.channel.EventLoopGroup;

/**
 * 
 * <label>
 *		关闭勾子.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName ShutdownClean
 * @author tgj  
 * @date 2017年10月30日 上午9:11:15 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("serverShutdownClean")
public class ShutdownClean implements DisposableBean {

	@Autowired
	@Qualifier("serverBossGroup")
	private EventLoopGroup bossGroup;

	@Autowired
	@Qualifier("serverWorkerGroup")
	private EventLoopGroup workerGroup;

	@Override
	public void destroy() throws Exception {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

}