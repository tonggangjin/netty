package com.agen.server.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.agen.netty.entity.UploadFile;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * <label>
 *		文件处理server.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName FileService
 * @author tgj  
 * @date 2017年10月30日 上午9:13:13 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Service("serverFileService")
public class FileService {
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

	@Value("${server.filePath}")
	private String filePath;

	public JSONObject dealService(UploadFile uf) {
		JSONObject jsonObject = validate(uf);
		if (!"ok".equals(jsonObject.getString("status"))) {
			LOG.warn("验证出错：" + uf.toString());
			return jsonObject;
		}

		return saveFile(uf);
	}

	private JSONObject saveFile(UploadFile uf) {
		JSONObject jsonObject = new JSONObject();
		File file = new File(filePath + File.separator + uf.getFileName());
		try {
			Files.write(file.toPath(), uf.getBytes(), StandardOpenOption.CREATE);
			jsonObject.put("status", "ok");
		} catch (IOException e) {
			jsonObject.put("status", "error");
			LOG.error("保存文件失败：" + uf.toString(), e);
		}
		return jsonObject;
	}

	private JSONObject validate(UploadFile uf) {
		JSONObject jsonObject = new JSONObject();
		String name, uuid, md5;
		if (uf == null || Objects.isNull((uuid = uf.getUuid())) || Objects.isNull(uf.getFileLen())
				|| Objects.isNull((md5 = uf.getFileMd5())) || Objects.isNull((name = uf.getFileName()))
				|| Objects.isNull(uf.getBytes()) || uf.getBytes().length <= 0) {
			// 以后可以根据需求添加信息
			jsonObject.put("status", "error");
			return jsonObject;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(uuid).append(name).append(uf.getFileLen()).append(name).append(uuid).append(uf.getFileLen())
				.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm")).append(uf.getSessionId());

		jsonObject.put("status", md5.equals(DigestUtils.md5Hex(sb.toString())) ? "ok" : "error");
		// LOG.error(uf.getFileMd5() + "----" +
		// DigestUtils.md5Hex(sb.toString()) + "-----" + md5 + "----> "+
		// jsonObject.toJSONString());

		return jsonObject;
	}
}
