package com.agen.client.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agen.client.service.ConnectionPoolService;
import com.agen.netty.entity.HeartRate;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * <label>
 *		连接检测.
 * </label>
 * <p>
 *		<pre>
 *			通过心跳检测重新建立连接，与断开连接
 *		</pre>
 * </p>
 * @ClassName HeartRateHandler
 * @author TGJ  
 * @date 2017年10月25日 下午1:17:06 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("clientHeartRateHandler")
@Sharable
public class HeartRateHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	@Qualifier("clientConnectionPoolService")
	private ConnectionPoolService connectionPoolService;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HeartRate) {
			HeartRate hr = (HeartRate) msg;
			if (!"pong".equals(hr.getMsg())) {
				connectionPoolService.close(ctx.channel(), true);
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
				connectionPoolService.close(ctx.channel(), true);
				return;
			} else if (event.state().equals(IdleState.WRITER_IDLE)) {
				// 未进行写操作
			} else if (event.state().equals(IdleState.ALL_IDLE)) {
				// 未进行读写
				// 发送心跳消息
				ctx.writeAndFlush(new HeartRate("ping"));
				return;
			}
		}
		ctx.fireUserEventTriggered(evt);
	}
}
