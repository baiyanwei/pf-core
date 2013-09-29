package com.secpro.platform.core.metrics;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;

/**
 * @author baiyanwei
 * 
 *         this calss supplies common methods, delegates all methods of the
 *         DynamicBean Jun 1, 2012
 */
public abstract class AbstractMetricMBean implements DynamicMBean {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(AbstractMetricMBean.class);

	protected MBeanInfo _mBeanInfo;
	protected HashMap<String, Field> _metricFields = null;
	protected HashMap<String, Method> _metricMethods = new HashMap<String, Method>();

	public AbstractMetricMBean() {
		// collect metrics
		collectMetricFields();
		collectMetricMethods();
	}

	/**
	 * register service into MBean Server
	 * 
	 * @param jmxObjectName
	 * @param service
	 */
	public void registerMBean(String jmxObjectName, IService service) {
		if (Assert.isEmptyString(jmxObjectName) == true) {
			return;
		}
		if (Assert.isNull(service) == true) {
			return;
		}
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName objectName = new ObjectName(jmxObjectName);
			mBeanServer.registerMBean(service, objectName);
		} catch (Exception e) {
			theLogger.exception("JMX error when registering the MonitoringService to JMX", e);
		}
	}

	/**
	 * unregister server from MBean Server
	 * 
	 * @param jmxObjectName
	 * @param service
	 */
	public void unRegisterMBean(String jmxObjectName) {
		if (Assert.isEmptyString(jmxObjectName) == true) {
			return;
		}

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName objectName = new ObjectName(jmxObjectName);
			mBeanServer.unregisterMBean(objectName);
		} catch (Exception e) {
			theLogger.exception("JMX error when unregistering the MonitoringService", e);
		}
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		Field field = _metricFields.get(attribute);
		if (field == null)
			return null;

		try {
			Class<?> cls = field.getClass();
			if (cls == String.class)
				return field.get(this);

			if (cls == Long.class || cls == long.class)
				return field.getLong(this);

			return field.get(this);

		} catch (Exception e) {
			theLogger.exception(e);
		}
		return null;
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		if (attributes == null || attributes.length == 0)
			return new AttributeList();

		AttributeList resultList = new AttributeList();
		for (String object : attributes) {
			try {
				resultList.add(new Attribute(object, getAttribute(object)));
			} catch (Exception e) {
			}
		}

		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		if (_mBeanInfo == null) {
			Class<?> cls = this.getClass();

			MBeanAttributeInfo[] mBeanAttributeInfos = new MBeanAttributeInfo[_metricFields.keySet().size()];
			Iterator<String> fieldNames = _metricFields.keySet().iterator();
			for (int i = 0; fieldNames.hasNext(); i++) {
				String name = fieldNames.next();
				Field field = _metricFields.get(name);
				Metric metric = field.getAnnotation(Metric.class);
				mBeanAttributeInfos[i] = new MBeanAttributeInfo(field.getName(), field.getClass().getName(), metric.description(), true, false, false);
			}

			Iterator<String> methodNames = _metricMethods.keySet().iterator();
			MBeanOperationInfo[] mBeanOperationInfos = new MBeanOperationInfo[_metricMethods.keySet().size()];
			for (int i = 0; methodNames.hasNext(); i++) {
				String name = methodNames.next();
				Method method = _metricMethods.get(name);
				Metric metric = method.getAnnotation(Metric.class);
				mBeanOperationInfos[i] = new MBeanOperationInfo(method.getName(), metric.description(), null, method.getReturnType().getName(), MBeanOperationInfo.ACTION);
			}

			_mBeanInfo = new MBeanInfo(cls.getName(), cls.getName(), mBeanAttributeInfos, null, mBeanOperationInfos, null);
		}
		return _mBeanInfo;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		if (!_metricMethods.keySet().contains(actionName))
			return null;

		try {
			return _metricMethods.get(actionName).invoke(this, params);
		} catch (Exception e) {
			theLogger.exception(e);
		}
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		// could not change the attribute
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		// could not change the attributes
		return null;
	}

	@Metric(description = "get summary metric of MonitoringService", ignoreInSummary = true)
	public String getSummaryOfMetric() {
		StringBuffer summary = new StringBuffer("---- Summary of the ").append(this.getClass().getName()).append(" metrics ---- \n");
		Iterator<String> methodNames = _metricMethods.keySet().iterator();
		while (methodNames.hasNext()) {
			String name = methodNames.next();

			Method method = _metricMethods.get(name);
			Metric metric = method.getAnnotation(Metric.class);

			if (metric.ignoreInSummary())
				continue;

			// NOTE : We DO NOT pass any params when invoking the method
			try {
				summary.append(method.getName()).append(":");
				summary.append(method.invoke(this, new Object[0]));
				summary.append("\n");
			} catch (Exception e) {
			}
		}

		Iterator<String> filedNames = _metricFields.keySet().iterator();
		while (filedNames.hasNext()) {
			String name = filedNames.next();

			Field field = _metricFields.get(name);
			// NOTE : We DO NOT pass any params when invoking the method
			try {
				summary.append(field.getName()).append(":");
				summary.append(getAttribute(field.getName()));
				summary.append("\n");
			} catch (Exception e) {
			}
		}
		return summary.toString();
	}

	private void collectMetricFields() {
		_metricFields = new HashMap<String, Field>();
		Field[] classFields = this.getClass().getDeclaredFields();
		for (Field field : classFields) {
			Metric metric = field.getAnnotation(Metric.class);
			if (metric != null) {
				_metricFields.put(field.getName(), field);
			}
		}
	}

	private void collectMetricMethods() {
		Method[] classMethods = this.getClass().getMethods();
		_metricMethods = new HashMap<String, Method>();
		for (Method method : classMethods) {
			Metric metric = method.getAnnotation(Metric.class);
			if (metric != null) {
				_metricMethods.put(method.getName(), method);
			}
		}
	}

}
