package com.agen.server.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agen.netty.entity.HeartRate;
import com.agen.server.service.AuthenticationService;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * <label>
 *		心跳处理器.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName HeartRateHandler
 * @author tgj  
 * @date 2017年10月30日 上午9:12:27 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("serverHeartRateHandler")
@Sharable
public class HeartRateHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	@Qualifier("serverAuthenticationService")
	private AuthenticationService authenticationService;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HeartRate) {
			HeartRate hr = (HeartRate) msg;
			if ("ping".equals(hr.getMsg())) {
				ctx.writeAndFlush(new HeartRate("pong"));
			} else {
				authenticationService.close(ctx.channel());
			}
			return;
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state().equals(IdleState.READER_IDLE)) {
				authenticationService.close(ctx.channel());
				return;
			} else if (event.state().equals(IdleState.WRITER_IDLE)) {
				// 未进行写操作
			} else if (event.state().equals(IdleState.ALL_IDLE)) {
				// 未进行读写
			}
		}
		ctx.fireUserEventTriggered(evt);
	}
}
