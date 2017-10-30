package com.agen.netty.entity;

/**
 * 
 * <label>
 *		文件传输实体.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName UploadFile
 * @author TGJ  
 * @date 2017年10月25日 下午1:19:43 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
public class UploadFile extends Base {

	/**
	 * @Fields serialVersionUID:TODO
	 */
	private static final long serialVersionUID = 1L;

	private String uuid;
	private String fileName;
	private String fileMd5;
	private Long fileLen;
	private String filePath;
	private byte[] bytes;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getFileLen() {
		return fileLen;
	}

	public void setFileLen(Long fileLen) {
		this.fileLen = fileLen;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	@Override
	public String toString() {
		return "UploadFile [uuid=" + uuid + ", fileName=" + fileName + ", fileMd5=" + fileMd5 + ", fileLen=" + fileLen
				+ ", filePath=" + filePath + "]";
	}

}
