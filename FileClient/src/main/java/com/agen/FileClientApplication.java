package com.agen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * <label>
 *		启动类.
 * </label>
 * <p>
 *		<pre>
 *			
 *		</pre>
 * </p>
 * @ClassName FileClientApplication
 * @author tgj  
 * @date 2017年10月30日 上午9:01:44 
 *    
 * @Copyright 2017 www.agen.com Inc. All rights reserved.
 */
@SpringBootApplication
public class FileClientApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileClientApplication.class);
		app.setWebEnvironment(false);
		app.run(args);
	}
}
