package com.agen.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.agen.client.service.ConnectionPoolService;
import com.agen.netty.entity.User;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * 
 * <label>
 *		认证器.
 * </label>
 * <p>
 *		<pre>
 *			与服务端建立连接的认证器
 *		</pre>
 * </p>
 * @ClassName AuthenticationHandler
 * @author TGJ  
 * @date 2017年10月25日 下午1:15:41 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("clientAuthenticationHandler")
@Sharable
public class AuthenticationHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationHandler.class);

	/**
	 * 与服务商建立连接的密匙
	 */
	@Value("${client.user.credential}")
	private String credential;
	
	@Autowired
	@Qualifier("clientConnectionPoolService")
	private ConnectionPoolService connectionPoolService;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		User user = new User();
		user.setCredential(credential);
		ctx.writeAndFlush(user);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof User) {
			User user = (User) msg;
			if (user.getSessionId() != null) {
				connectionPoolService.dealService(ctx.channel(), user);
			} else if (user.getMessage() != null) {
				LOG.error(user.getMessage());
				connectionPoolService.close(ctx.channel(), true);
			}
			return;
		}
		ctx.fireChannelRead(msg);
	}

}
