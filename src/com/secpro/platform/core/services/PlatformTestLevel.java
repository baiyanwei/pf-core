package com.secpro.platform.core.services;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author baiyanwei
 * define the test level on type.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformTestLevel {
	
	/**
	 * @return
	 * get the test level
	 */
	String testLevel() default "level-1";
}
