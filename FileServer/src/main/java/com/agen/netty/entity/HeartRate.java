package com.agen.netty.entity;

/**
 * 
 * <label>
 *		心跳实体.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName HeartRate
 * @author tgj  
 * @date 2017年10月30日 上午9:09:29 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
public class HeartRate extends Base {

	/**
	 * @Fields serialVersionUID:TODO
	 */
	private static final long serialVersionUID = 1L;

	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public HeartRate(String msg) {
		super();
		this.msg = msg;
	}

	public HeartRate() {
		super();
	}

}
