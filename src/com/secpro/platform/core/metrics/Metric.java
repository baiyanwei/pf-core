package com.secpro.platform.core.metrics;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Martin Bai.
 * This annotation allows developers to supply metrics information about the service
 * Jun 1, 2012
 */
@Retention(RUNTIME) @Target({FIELD, METHOD})
public @interface Metric {
	
	/**
	 * This is the description of the property.
	 */
	String description() default "\u0000";	

    /**
     * ignoreInSummary means whether this method or property is ignore in the summary method.
     * because we can not call summary method in summary method, nor it will loop for every
     * @return
     */
    boolean ignoreInSummary() default false;
    
    /**
     * This is used to map the name of the annotated entity(Field or Method) to the property key in the json
     * we collected and send to operations.
     * 
     * If you want to change this value, you need everybody know!!
     * @return
     */
    String property() default "";
}
