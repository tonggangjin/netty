package com.agen.server.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agen.netty.entity.UploadFile;
import com.agen.server.service.FileService;
import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * <label>
 *		文件接收处理器.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName FileReceiveHandler
 * @author tgj  
 * @date 2017年10月30日 上午9:12:15 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Component("serverFileReceiveHandler")
@Sharable
public class FileReceiveHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	@Qualifier("serverFileService")
	private FileService fileService;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof UploadFile) {
			JSONObject jsonObject = fileService.dealService((UploadFile) msg);
			ctx.writeAndFlush(jsonObject);
			return;
		}
		ctx.fireChannelRead(msg);
	}

}
