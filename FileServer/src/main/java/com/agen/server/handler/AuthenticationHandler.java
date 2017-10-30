package com.agen.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agen.netty.entity.Base;
import com.agen.netty.entity.User;
import com.agen.server.service.AuthenticationService;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * <label>
 *		实证处理器.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName AuthenticationHandler
 * @author tgj  
 * @date 2017年10月30日 上午9:11:36 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("serverAuthenticationHandler")
@Sharable
public class AuthenticationHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationHandler.class);

	@Autowired
	@Qualifier("serverAuthenticationService")
	private AuthenticationService authenticationService;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof User) {
			User user = authenticationService.dealService((User) msg);
			if (user.getSessionId() != null) {
				authenticationService.setAttr(ctx.channel(), user.getSessionId());
				ctx.writeAndFlush(user);
			} else if (user.getMessage() != null) {
				LOG.warn(user.getMessage() + "--->" + user.toString());
				ctx.writeAndFlush(user);
				authenticationService.close(ctx.channel());
			}
			return;
		} else if (msg instanceof Base) {
			if (!authenticationService.validate((Base) msg)) {
				authenticationService.close(ctx.channel());
				return;
			}
		}
		ctx.fireChannelRead(msg);
	}

}
