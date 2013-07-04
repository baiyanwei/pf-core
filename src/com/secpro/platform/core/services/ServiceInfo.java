package com.secpro.platform.core.services;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation allows developers to supply information about the
 * service that will be used during the creation and registration.
 * 
 * Developers supply the description of the service as will as the 
 * configuration path that allows operations to overwrite the 
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceInfo {

	/**
	 * A human readable string that describes what the services
	 * does.
	 * 
	 * @return
	 */
    String description() default "";
    
    /**
     * This is an XPath statement that allows the service to load
     * information from the routers configuration file. Developers use
     * in combination with JAXB annotations.
     * 
     * @return
     */
    String configurationPath() default "";
}
