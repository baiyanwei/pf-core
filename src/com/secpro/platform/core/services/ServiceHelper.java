package com.secpro.platform.core.services;

import java.util.HashMap;
import java.util.Hashtable;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.secpro.platform.core.Activator;

/**
 * @author Martin
 * The service helper is very important part of platform ,all services will be started in this helper ,
 * helper can loading property for every service,and register and lookup a type of service from it.
 * using helper to register a service into OSGI. 
 */
public class ServiceHelper<T extends IService> {
	@SuppressWarnings("rawtypes")
	private static HashMap<IService, ServiceRegistration> _registationMap = new HashMap<IService, ServiceRegistration>();

	/**
	 * register a service into OSGI frame.
	 * 
	 * @param <T>
	 * @param service
	 * @return
	 */
	public static <T extends IService> T registerService(T service) {
		return registerService(service, true, true);
	}

	public static <T extends IService> T registerService(T service, boolean isStartup, boolean isPropertyes) {
		try {
			if (isPropertyes) {
				PropertyLoaderService propertyLoaderService = findService(PropertyLoaderService.class);
				// if propertyLoaderService is null, we will do nothing. Because
				// PropertyLoaderService is the first service that to be
				// registered
				if (propertyLoaderService != null) {
					// inject the variables from configure file
					propertyLoaderService.injectServiceProperties(service);
				}
			}
			if (isStartup == true) {
				service.start();
			}
			Hashtable<String, Object> properties = new Hashtable<String, Object>();
			ServiceInfo serviceAnnotation = service.getClass().getAnnotation(ServiceInfo.class);
			if (serviceAnnotation != null) {
				properties.put("description", serviceAnnotation.description());
			}
			@SuppressWarnings("rawtypes")
			ServiceRegistration registration = Activator.getContext().registerService(service.getClass().getName(), service, properties);

			_registationMap.put(service, registration);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return service;
	}

	public static <T extends IService> T findService(Class<?> clazz) {
		return findService(clazz.getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends IService> T findService(String clazz) {
		ServiceReference serviceReference = Activator.getContext().getServiceReference(clazz);
		if (serviceReference == null) {
			return null;
		}
		return (T) Activator.getContext().getService(serviceReference);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends IService> void unregisterService(T service) throws Exception {
		if (service != null) {
			service.stop();
			ServiceRegistration unregisterService = null;
			synchronized (_registationMap) {
				unregisterService = _registationMap.remove(service);
			}
			if (unregisterService != null) {
				unregisterService.unregister();
			}
		}
	}
}
