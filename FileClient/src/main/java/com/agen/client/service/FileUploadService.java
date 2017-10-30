package com.agen.client.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.agen.netty.entity.UploadFile;
import com.agen.netty.entity.User;

import io.netty.channel.Channel;

/**
 * 
 * <label>
 *		测试上传功能.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName FileUploadService
 * @author tgj  
 * @date 2017年10月30日 上午9:03:32 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Service("clientFileUploadService")
public class FileUploadService {

	private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);

	@Autowired
	@Qualifier("clientConnectionPoolService")
	private ConnectionPoolService connectionPoolService;

	@PostConstruct
	public void initTest() {
//		File file = new File("c:/test");
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				try {
//					Thread.sleep(5000);
//					File[] files = file.listFiles();
//					for (File file : files) {
//						if (!fileUpload(file)) {
//							LOG.error(file.getAbsolutePath() + ", 上传失败");
//						}
//					}
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}).start();
	}

	public boolean fileUpload(File file) {
		if (null == file || !file.exists() || !file.isFile())
			return false;
		Channel channel = connectionPoolService.getChannel();
		if (null == channel)
			return false;
		User user = connectionPoolService.getUser(channel);
		if (null == user)
			return false;
		UploadFile uf = null;
		try {
			uf = createUploadFile(file, channel, user);
			channel.writeAndFlush(uf);
			return true;
		} catch (IOException e) {
			LOG.error("上传文件异常, data:" + (null != uf ? uf.toString() : ""), e);
		}
		return false;
	}

	private UploadFile createUploadFile(File file, Channel channel, User user) throws IOException {
		UploadFile uf = new UploadFile();
		uf.setUuid(UUID.randomUUID().toString());
		uf.setFileLen(file.length());
		uf.setFileName(file.getName());
		uf.setFilePath(file.getAbsolutePath());
		uf.setSessionId(user.getSessionId());

		StringBuilder sb = new StringBuilder();
		sb.append(uf.getUuid()).append(uf.getFileName()).append(uf.getFileLen()).append(uf.getFileName())
				.append(uf.getUuid()).append(uf.getFileLen())
				.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm")).append(uf.getSessionId());
		uf.setFileMd5(DigestUtils.md5Hex(sb.toString()));

		uf.setBytes(Files.readAllBytes(file.toPath()));
		return uf;
	}
}
