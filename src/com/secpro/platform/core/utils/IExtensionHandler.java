package com.secpro.platform.core.utils;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * 
 * @author Jammy
 * CR - I like this lets move it to core
 */
public interface IExtensionHandler {

	public void extend(IConfigurationElement configurationElement);

}
