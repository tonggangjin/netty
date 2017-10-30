package com.agen.netty.entity;

import java.io.Serializable;

/**
 * 
 * <label>
 *		基类实体.
 * </label>
 * <p>
 *		<pre>
 *			所有实体都应该继承此类
 *		</pre>
 * </p>
 * @ClassName Base
 * @author TGJ  
 * @date 2017年10月25日 下午1:18:40 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
public class Base implements Serializable {

	/**
	 * @Fields serialVersionUID:TODO
	 */
	private static final long serialVersionUID = 1L;
	protected String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
