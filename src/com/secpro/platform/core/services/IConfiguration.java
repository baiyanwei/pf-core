package com.secpro.platform.core.services;

public interface IConfiguration {
	public static String ID_CONF_TITLE = "id";
	public static String NAME_CONF_TITLE = "name";
	public static String DESCRIPTION_CONF_TITLE = "description";
	public static String IMPLEMENT_CLASS_CONF_TITLE = "implement_class";
	public static String PROPERTY_CONF_TITLE = "property";
	public static String PROPERTY_KEY_CONF_TITLE = "name";
	public static String PROPERTY_VALUE_CONF_TITLE = "value";

	//
	public void setID(String id);

	public String getID();

	public void setName(String name);

	public String getName();

	public void setDescription(String description);

	public String getDescription();

}
