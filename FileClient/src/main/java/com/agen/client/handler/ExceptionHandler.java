package com.agen.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agen.client.service.ConnectionPoolService;
import com.agen.netty.entity.User;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

@Component("clientExceptionHandler")
@Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
	
	@Autowired
	@Qualifier("clientConnectionPoolService")
	private ConnectionPoolService connectionPoolService;
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		User user = connectionPoolService.close(ctx.channel());
		LOG.error(user + "异常关闭", cause);
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		User user = connectionPoolService.close(ctx.channel());
		LOG.warn(user + "正常关闭");
	}
}
