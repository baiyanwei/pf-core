package com.secpro.platform.core.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.secpro.platform.log.utils.PlatformLogger;


/**
 * @author baiyanwei
 * This server will fill the target service fields from configuration XML file.
 */
@ServiceInfo(description = "Provides the ability to merge extension properties with configured properties.")
public class PropertyLoaderService implements IService {
	private static PlatformLogger _logger = PlatformLogger.getLogger(PropertyLoaderService.class);
	public final static String SERVICE_CONFIGURATION_PATH = "serviceConfigurationPath";
	public final static String DEFAULT_SERVICE_CONFIGURATION_PATH = "configuration/service.xml";
	
	private Document _userDocument = null;

	private XPath _xpath = XPathFactory.newInstance().newXPath();

	private String confidsAttributePath = "/@confids";

	public PropertyLoaderService() {
	}

	public void start() throws Exception {
		readDocument(getServiceConfigurationPath());
		_logger.info("PropertyLoaderService is started~");
	}

	public void stop() throws Exception {

	}

	/**
	 * Gets the file path.
	 * 
	 * @return
	 */
	public String getServiceConfigurationPath() {
		String path = System.getProperty(SERVICE_CONFIGURATION_PATH);
		if (path == null || path.trim().length() == 0) {
			path = DEFAULT_SERVICE_CONFIGURATION_PATH;
		}
		return path;
	}

	/**
	 * This method loads up the properties document. It will load two based on
	 * the file name and default file name properties.
	 * 
	 * @param fileName
	 * @param defaultName
	 */
	public boolean readDocument(String fileName) {
		try {
			_userDocument = parseStream(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			_logger.exception("Read Service document Exception", e);
		}
		return _userDocument != null;
	}

	/**
	 * This method loads up the properties document. It will take the supplied
	 * input stream and use it as the method to load the xml document.
	 * 
	 * @param fileName
	 * @param defaultName
	 */
	public boolean create(InputStream inputStream) {
		_userDocument = parseStream(inputStream);
		return _userDocument != null;
	}

	/*
	 * PUBLIC DATA GETS THAT WILL USE THE DOCUMENT
	 */
	public int getInt(String attributeName, IConfigurationElement configurationElement) {
		try {
			String value = findAndReplace(attributeName, configurationElement);
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	public boolean getBoolean(String attributeName, IConfigurationElement configurationElement) {
		String value = findAndReplace(attributeName, configurationElement);
		return Boolean.parseBoolean(value);
	}

	public String getString(String attributeName, IConfigurationElement configurationElement) {
		return findAndReplace(attributeName, configurationElement);
	}

	public String[] getPropertyString(String configurationPath, IConfigurationElement configurationElement) {
		return findAndReplaceProperty(configurationPath, configurationElement);
	}

	public NodeList getList(String path) {
		try {
			return (NodeList) _xpath.evaluate(path, _userDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object getValueByType(String object, Class<?> clazz) {
		if (clazz == null || clazz.equals(String.class)) {
			return object;
		} else if (clazz.equals(Long.class)) {
			return new Long(object);
		} else if (clazz.equals(Integer.class)) {
			return new Integer(object);
		} else if (clazz.equals(Float.class)) {
			return new Float(object);
		} else if (clazz.equals(Double.class)) {
			return new Double(object);
		} else if (clazz.equals(Boolean.class)) {
			return new Boolean(object);
		} else if (clazz.equals(List.class)) {
			String[] value = object.split(",");
			List<String> valueList = new ArrayList<String>(value.length);
			for (int i = 0; i < value.length; i++) {
				valueList.add(value[i]);
			}
			return valueList;
		} else {
			return object;
		}
	}
	/*
	 * DATA GETS WITH REFERENCE ITEM.
	 */
	public Object getValue(String path, Object defaultValue, QName type, Class<?> clazz, boolean firstTimeInject) {
		try {
			Node node = (Node) _xpath.evaluate(path, _userDocument, XPathConstants.NODE);
			Object object = _xpath.evaluate(path, _userDocument, type);
			if (object == null || node == null) {
				if (!firstTimeInject)
					return null;
				if (clazz == null || clazz.equals(String.class)) {
					return (String) defaultValue;
				} else if (clazz.equals(Long.class) == true) {
					return new Long((String) defaultValue);
				} else if (clazz.equals(Integer.class) == true) {
					return new Integer((String) defaultValue);
				} else if (clazz.equals(Float.class) == true) {
					return new Float((String) defaultValue);
				} else if (clazz.equals(Double.class) == true) {
					return new Double((String) defaultValue);
				} else if (clazz.equals(Boolean.class) == true) {
					return new Boolean((String) defaultValue);
				} else if (clazz.equals(List.class)) {
					String values = (String) defaultValue;
					String[] value = values.split(",");
					List<String> valueList = new ArrayList<String>(value.length);
					for (int i = 0; i < value.length; i++) {
						valueList.add(value[i]);
					}
					return valueList;
				} else {
					return defaultValue;
				}
			} else if (clazz.equals(Long.class) == true) {
				return new Long(((Number) object).longValue());
			} else if (clazz.equals(Integer.class) == true) {
				return new Integer(((Number) object).intValue());
			} else if (clazz.equals(Float.class) == true) {
				return new Float(((Number) object).floatValue());
			} else if (clazz.equals(Double.class) == true) {
				return new Double(((Number) object).doubleValue());
			} else if (clazz.equals(Boolean.class) == true) {
				return new Boolean((String) object);
			} else if (clazz.equals(List.class)) {
				String values = (String) object;
				String[] value = values.split(",");
				List<String> valueList = new ArrayList<String>(value.length);
				for (int i = 0; i < value.length; i++) {
					valueList.add(value[i]);
				}
				return valueList;
			} else {
				return (String) object;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	/**
	 * This method injects the properties from configuration file
	 * 
	 * @param service
	 */
	public void injectServiceProperties(IService service) {
		Class<?> clazz = service.getClass();
		ServiceInfo serviceInfo = clazz.getAnnotation(ServiceInfo.class);
		if (serviceInfo == null || serviceInfo.configurationPath().length() == 0) {
			return;
		}
		String configurationPath = serviceInfo.configurationPath();
		// This method will loop through the
		// variables of the object and set their values.
		// inject(service, configurationPath, true);
		injectServiceProperties(service, configurationPath);
	}

	/**
	 * This method works the same ways as the one that doesn't supply the
	 * configuration path we use this method in the storage system to allow
	 * multiple service objects with different properties to be created.
	 */
	public void injectServiceProperties(IService service, String configurationPath) {

		inject(service, configurationPath, true);
		injectUsingConfIds(service, configurationPath);
		// This method will loop through the
		// variables of the object and set their values.
	}

	private void injectUsingConfIds(IService service, String configurationPath) {
		MessageFormat f = new MessageFormat("//config[@id=''{0}'']");
		// List<String> ids = getAllConfIds(service);
		List<String> ids = getAllConfIds(configurationPath);
		for (String id : ids) {
			inject(service, f.format(new String[] { id }), false);
		}
	}

	private List<String> getAllConfIds(String path) {
		List<String> ids = new ArrayList<String>();
		String attributeV = null;
		try {
			attributeV = (String) _xpath.evaluate(path + confidsAttributePath, _userDocument, XPathConstants.STRING);
		} catch (XPathExpressionException e) {

		}
		if (attributeV != null && !attributeV.isEmpty()) {
			ids = Arrays.asList(attributeV.split(","));
		}
		return ids;
	}

	/**
	 * Re-implement to support the multi-layer configuration
	 * 
	 * @param object
	 * @param xpathContext
	 * @param firstTimeInject
	 *            This is used to predict that the already set value will be
	 *            over writeen by default value.
	 */
	private void inject(Object object, String xpathContext, boolean firstTimeInject) {

		Class<?> clazz = object.getClass();
		// iterate to the top parent class
		while (!clazz.equals(Object.class)) {

			// go through all the declared fields
			Field[] serviceFields = clazz.getDeclaredFields();

			for (Field field : serviceFields) {

				try {
					// get accessibility
					boolean accessible = field.isAccessible();
					if (!accessible) {
						// make the private field accessible
						field.setAccessible(true);
					}
					XmlElementWrapper xmlElementWrapper = field.getAnnotation(XmlElementWrapper.class);
					if (xmlElementWrapper != null) {
						// List object
						XmlElement xmlElement = field.getAnnotation(XmlElement.class);
						String actualPath = setupXPathContext(setupXPathContext(xpathContext, xmlElementWrapper.name()), xmlElement.name());
						XPathExpression expr = _xpath.compile(actualPath);
						NodeList nodes = (NodeList) expr.evaluate(_userDocument, XPathConstants.NODESET);
						int length = nodes.getLength();
						Method addMethod = field.getType().getMethod("add", Object.class);
						for (int i = 0; i < length; i++) {
							Object child = xmlElement.type().newInstance();
							//check the child node type 
							QName type = getXPathType(xmlElement.type());
							if (customized(type)) {
								//if is user define type.
								inject(child, actualPath + "[" + (i + 1) + "]", firstTimeInject);
							} else {
								//if xmlElement's type is String,number,the basic type.
								child = getValue(actualPath + "[" + (i + 1) + "]", xmlElement.defaultValue(), type, xmlElement.type(), firstTimeInject);
							}
							addMethod.invoke(field.get(object), child);
						}
					} else {
						XmlElement xmlElement = field.getAnnotation(XmlElement.class);
						if (xmlElement != null) {
							QName type = getXPathType(xmlElement.type());
							if (customized(type)) {
								inject(field.get(object), setupXPathContext(xpathContext, xmlElement.name()), firstTimeInject);
							} else {
								Object o = getValue(setupXPathContext(xpathContext, xmlElement.name()), xmlElement.defaultValue(), type, xmlElement.type(), firstTimeInject);

								if (o != null) {
									field.set(object, o);
								}

							}
						} else {
							XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);
							if (xmlAttribute != null) {
								Object o = getValue(xpathContext + "/@" + xmlAttribute.name(), "", XPathConstants.STRING, String.class, firstTimeInject);

								if (o != null) {
									field.set(object, o);
								}
							}
						}

					}

					// restore, if we changed it.
					if (!accessible) {
						field.setAccessible(accessible);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			clazz = clazz.getSuperclass();
		}
	}

	private QName getXPathType(Class<?> clazz) {

		if (clazz == null || clazz.equals(XmlElement.DEFAULT.class) || clazz.equals(String.class) || clazz.equals(Boolean.class) || clazz.equals(List.class)) {
			return XPathConstants.STRING;
		} else if (clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Float.class) || clazz.equals(Double.class)) {
			return XPathConstants.NUMBER;
		} else {
			return XPathConstants.NODE;
		}
	}

	private boolean customized(QName type) {

		return type.equals(XPathConstants.NODE) || type.equals(XPathConstants.NODESET);
	}

	private String setupXPathContext(String xpathContext, String xmlElement) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(xpathContext);
		if (!xpathContext.endsWith("/")) {
			stringBuffer.append("/");
		}
		stringBuffer.append(xmlElement);
		return stringBuffer.toString();
	}

	public void loadCustomProperties(HashMap<String, String> propertiesMap, String configurationPath) {
		try {
			String value = "";
			String name = "";
			if (configurationPath != null && configurationPath.length() != 0) {
				NodeList xpathValue = (NodeList) _xpath.evaluate(configurationPath + "/properties/property", _userDocument, XPathConstants.NODESET);
				for (int index = 0; index < xpathValue.getLength(); index++) {
					Element element = (Element) xpathValue.item(index);
					if (element != null) {
						name = element.getAttribute("name");
						value = element.getAttribute("value");
						NodeList values = element.getElementsByTagName("value");
						if (values.getLength() == 1) {
							value = ((Element) values.item(0)).getTextContent();
						}

						// Only add the properties that weren't already added.
						if (propertiesMap.containsKey("name") == false) {
							propertiesMap.put(name, value);
						}
					}
				}
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public void loadExtensionProperties(IConfigurationElement[] properties, Object owner) {
		if (properties == null || properties.length == 0 || owner == null) {
			return;
		}

		String value = null;
		String name = null;
		HashMap<String, String> configPPMap = new HashMap<String, String>();

		for (IConfigurationElement p : properties) {
			name = p.getAttribute(IConfiguration.PROPERTY_KEY_CONF_TITLE);
			value = p.getAttribute(IConfiguration.PROPERTY_VALUE_CONF_TITLE);
			if (name == null) {
				continue;
			}
			configPPMap.put(name.trim(), value);
		}
		//
		Field[] fields = owner.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			XmlElement xmlElement = fields[i].getAnnotation(XmlElement.class);
			if (xmlElement == null) {
				continue;
			}
			String newValue = xmlElement.defaultValue();
			String xmlName = xmlElement.name();
			if (configPPMap.containsKey(xmlName)) {
				newValue = configPPMap.get(xmlName);
			}
			//
			Class<?> objectType = xmlElement.type();
			if (objectType == null) {
				objectType = String.class;
			}
			Object o = this.getValueByType(newValue, objectType);
			if (o == null) {
				continue;
			}
			try {
				fields[i].set(owner, o);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	/*
	 * PRIVATE METHODS
	 */
	private Document parseStream(InputStream inputStream) {

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// parse the stream into a document
			return documentBuilder.parse(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method looks up the attribute from the configuration element and
	 * then from the configuration document. If the value is found in the
	 * configuration document then that value is what is returned.
	 * 
	 * @param attributeName
	 * @param configurationElement
	 * @return
	 */
	private String findAndReplace(String attributeName, IConfigurationElement configurationElement) {
		String valueString = configurationElement.getAttribute(attributeName);
		try {
			String configurationPath = configurationElement.getAttribute(SERVICE_CONFIGURATION_PATH);
			if (configurationPath != null && configurationPath.length() != 0) {
				String xpathValue;
				xpathValue = _xpath.evaluate(configurationPath + "/@" + attributeName, _userDocument);
				if (xpathValue != null && xpathValue.length() != 0) {
					valueString = xpathValue;
				}
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return valueString;
	}

	/**
	 * This method looks up the attribute from the configuration element and
	 * then from the configuration document. If the value is found in the
	 * configuration document then that value is what is returned.
	 * 
	 * @param attributeName
	 * @param configurationElement
	 * @return
	 */
	private String[] findAndReplaceProperty(String configurationPath, IConfigurationElement configurationElement) {
		String name = configurationElement.getAttribute("name");
		String value = configurationElement.getAttribute("value");
		IConfigurationElement[] elements = configurationElement.getChildren("value");
		if (elements != null && elements.length == 1) {
			value = elements[0].getValue();
		}

		try {
			if (configurationPath != null && configurationPath.length() != 0) {
				NodeList xpathValue = (NodeList) _xpath.evaluate(configurationPath + "/properties/property[@name='" + name + "']", _userDocument, XPathConstants.NODESET);
				for (int index = 0; index < xpathValue.getLength(); index++) {
					Element element = (Element) xpathValue.item(index);
					if (element != null) {
						value = element.getAttribute("value");
						NodeList values = element.getElementsByTagName("value");
						if (values.getLength() == 1) {
							value = ((Element) values.item(0)).getTextContent();
						}
					}
				}
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return new String[] { name, value };
	}
}
