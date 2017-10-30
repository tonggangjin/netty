package com.agen.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agen.netty.entity.User;
import com.agen.server.service.AuthenticationService;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * <label>
 *		异常处理器.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName ExceptionHandler
 * @author tgj  
 * @date 2017年10月30日 上午9:11:49 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("serverExceptionHandler")
@Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

	@Autowired
	@Qualifier("serverAuthenticationService")
	private AuthenticationService authenticationService;

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		User user = authenticationService.close(ctx.channel());
		LOG.error(user + "异常关闭", cause);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		User user = authenticationService.close(ctx.channel());
		LOG.warn(user + "正常关闭");
	}
}
