package com.agen.netty.entity;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 
 * <label>
 *		用户实体.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName User
 * @author TGJ  
 * @date 2017年10月25日 下午1:19:56 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
public class User extends Base {

	/**
	 * @Fields serialVersionUID:TODO
	 */
	private static final long serialVersionUID = 1L;
	private String credential;
	private Date loginDate;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public String getCredential() {
		return credential;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	@Override
	public String toString() {
		return "User [credential=" + credential + ", loginDate="
				+ (null != loginDate ? DateFormatUtils.format(loginDate, "yyyy-MM-dd HH:mm:SS") : "") + ", message="
				+ message + "]";
	}

}
