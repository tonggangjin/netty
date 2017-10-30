package com.agen.server.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.agen.netty.entity.Base;
import com.agen.netty.entity.User;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * 
 * <label>
 *		实验Service.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName AuthenticationService
 * @author tgj  
 * @date 2017年10月30日 上午9:13:02 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@Service("serverAuthenticationService")
public class AuthenticationService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

	@Value("${server.maxConnetionNum}")
	private Integer maxConnetionNum;
	
	@Autowired
	@Qualifier("serverAttributeKey")
	private AttributeKey<Object> attributeKey;

	private List<String> credentials;
	@Value("${server.user.credentials}")
	public void init(String[] credentials) {
		this.credentials = new ArrayList<>(credentials.length);
		this.credentials.addAll(Arrays.asList(credentials));
	}

	@Autowired
	@Qualifier("serverAuthentication")
	private Map<String, User> authentication;

	/**
	 * 
	 * <label> 验证密匙. </label>
	 * <p>
	 * 
	 * <pre>
	 *			通过此方法验证密匙是否通过，并记录状态
	 * </pre>
	 * </p>
	 * 
	 * @Title dealService
	 * @param user
	 * @return User
	 */
	public User dealService(User user) {
		switch (validate(user)) {
		case msgError:
			user.setMessage("认证失败（信息有误）");
			return user;
		case maxCon:
			user.setMessage("认证失败（达到最大连接数）");
			return user;
		default:
			break;
		}

		User result = authentication(user);
		synchronized (this) {
			if (Objects.isNull(result)) {
				user.setMessage("认证失败（密匙错误）");
			} else {
				authentication.put(result.getSessionId(), result);
			}
			LOG.info("totalUser:" + authentication.size());
		}
		return user;
	}

	private ValidateStatus validate(User user) {
		if (maxConnetionNum <= authentication.size())
			return ValidateStatus.maxCon;
		if (null == user || Objects.nonNull(user.getSessionId()) || Objects.nonNull(user.getLoginDate())
				|| Objects.isNull(user.getCredential()))
			return ValidateStatus.msgError;
		return ValidateStatus.success;
	}

	private enum ValidateStatus {
		// 信用有误
		msgError,
		// 达到最大连接数
		maxCon,
		// 验证通过
		success
	}

	/**
	 * 
	 * <label> 过滤验证请求. </label>
	 * <p>
	 * 
	 * <pre>
	 *			验证请求是否合法，即验证是否已经难过正确的密匙验证过的
	 * </pre>
	 * </p>
	 * 
	 * @Title validate
	 * @param validate
	 * @return boolean
	 */
	public boolean validate(Base validate) {
		if (Objects.isNull(validate) || Objects.isNull(validate.getSessionId()))
			return false;
		return authentication.containsKey(validate.getSessionId());
	}

	private User authentication(User user) {
		if (credentials.contains(user.getCredential())) {
			user.setLoginDate(new Date());
			user.setSessionId(UUID.randomUUID().toString());
			return user;
		}
		return null;
	}

	/**
	 * 
	 * <label> 删除用户. </label>
	 * <p>
	 * 
	 * <pre>
	 * 根据Key从内存中删除用户
	 * </pre>
	 * </p>
	 * 
	 * @Title removeUser
	 * @param sessionId
	 * @return User
	 */
	private User removeUser(String sessionId) {
		if (Objects.isNull(sessionId))
			return null;
		return authentication.remove(sessionId);
	}

	public User close(Channel channel) {
		if (channel == null) return null;
		Object key = getAttr(channel);
		channel.close();
		if (key != null)
			return removeUser(key.toString());
		return null;
	}
	
	public Object setAttr(Channel channel, Object obj) {
		if (channel == null || obj == null) return null;
		Attribute<Object> attr = channel.attr(attributeKey);
		return attr.getAndSet(obj);
	}
	
	public Object getAttr(Channel channel) {
		if (channel == null) return null;
		Attribute<Object> attr = channel.attr(attributeKey);
		return attr.get();
	}
}
