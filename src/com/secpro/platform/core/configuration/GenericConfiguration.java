package com.secpro.platform.core.configuration;

import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;

import com.secpro.platform.core.services.PropertyLoaderService;
import com.secpro.platform.core.services.ServiceHelper;

/**
 * This class contains a set of properties that was defined in either the
 * plugin.xml or the router's configuration file.
 * 
 * @author baiyanwei
 * 
 */
public class GenericConfiguration {

	public HashMap<String, String> _properties = new HashMap<String, String>();

	//
	// PUBLIC METHODS
	//
	/**
	 * This method will combine the properties defined by the action with in the
	 * plugin.xml and the properties that have been defined by the routers
	 * configuration.
	 * 
	 */
	public void populateProperties(IConfigurationElement configurationElement) {
		String configurationPath = configurationElement.getAttribute("configurationPath");

		IConfigurationElement[] elements = configurationElement.getChildren("properties");
		if (elements != null && elements.length != 0) {
			elements = elements[0].getChildren("property");
		}

		PropertyLoaderService propertyLoaderService = (PropertyLoaderService) ServiceHelper.findService(PropertyLoaderService.class);

		// Combine the properties that exist in the plugin.xml with the
		// properties
		// in the routers configuration. Only properties that exist in
		// plugin.xml
		// file are added.
		for (int index = 0; index < elements.length; index++) {

			if (configurationPath == null || configurationPath.trim().length() == 0) {
				String name = elements[index].getAttribute("name");
				String value = "";
				IConfigurationElement[] values = elements[index].getChildren("value");
				if (values != null && values.length != 0) {
					value = values[0].getValue();
				} else {
					value = elements[index].getAttribute("value");
				}
				_properties.put(name, value);
			} else {
				String[] nameValuePair = propertyLoaderService.getPropertyString(configurationPath, elements[index]);
				_properties.put(nameValuePair[0], nameValuePair[1]);
			}
		}

		// We need to add any properties that were not defined in the plugin.xml
		// file to the
		// Actions property map.
		if (configurationPath != null && configurationPath.trim().length() != 0) {
			propertyLoaderService.loadCustomProperties(_properties, configurationPath);
		}
	}

}
