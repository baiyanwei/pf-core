package com.secpro.platform.core.message;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import com.secpro.platform.log.utils.PlatformLogger;

/**
 * This class provides functionality that makes it easy
 * to use resource bundles and Message Formatters to 
 * create strings.  NO ONE should ever use a hard coded
 * string concatenation with in the router.
 * 
 * 
 * @author bbuffone
 *
 */
public class MessagePreparer {

	//
	//Logging Object
	//
	private static PlatformLogger theLogger = PlatformLogger.getLogger(MessagePreparer.class);

    //
    // PRIVATE FINAL STATIC INSANCE VARIABLE
    //
    
    private static final String SUFFIX = "Res.properties";

    private static HashMap<Class<?>, PropertyResourceBundle> _classBundles = 
    					new HashMap<Class<?>, PropertyResourceBundle>();

    private static HashMap<String, MessageFormat> _propertyFormatters = 
						new HashMap<String, MessageFormat>();

    //
    //PUBLIC METHODS
    //
    public static String format(Class<?> clazz, String key, Object...arguments){
    	return formatInternal(clazz, key, arguments);
    }
    
       
    //
    //PRIVATE METHODS
    //
    private static String formatInternal(Class<?> clazz, String key, Object...arguments){
    	MessageFormat messageFormatter = _propertyFormatters.get(createFormatterKey(clazz, key));
    	
    	//If the formatter doesn't exist then we want to create 
    	//a new one. First see if we loaded the bundle
    	//and then create the formatter.
    	if (messageFormatter == null){
	    	//Load the resource Bundle.
	    	PropertyResourceBundle bundle = _classBundles.get(clazz);
	    	if (bundle == null){
	    		bundle = loadResourceBundle(clazz);
	    	}
	    	if (bundle == null) return "";
	    	
	    	//Load the property from the bundle using the 
	    	//supplied key.
	    	String value = null;
	    	
	    	try{
		    	value = bundle.getString(key);
		    	if (value == null) return "";
	    	}catch (MissingResourceException e){
	    		theLogger.error("missingProperty", clazz.getName(), key);
	    		theLogger.exception(e);
	    		return "";
	    	}
	    	
	    	//Create the message formatter and format the message.
	    	messageFormatter = new MessageFormat(value);
	    	_propertyFormatters.put(createFormatterKey(clazz, key), messageFormatter);
    	}
    	
    	String test = messageFormatter.format(arguments);
    	return test;
    }
    
    /**
     * This method will use the supplied class to load the resource bundle and
     * add it to the HashMap.
     * 
     * @param clazz
     * @return
     */
    private static PropertyResourceBundle loadResourceBundle(Class<?> clazz){
    	InputStream inputStream = clazz.getResourceAsStream(clazz.getSimpleName() + SUFFIX);
    	try {
    		if (inputStream != null){
	    		PropertyResourceBundle bundle = new PropertyResourceBundle(inputStream);
	    		_classBundles.put(clazz, bundle);
	    		return bundle;
    		}else{
    			theLogger.error("loadingBundleError", clazz.getSimpleName(), SUFFIX);
    		}
		} catch (IOException e) {
			theLogger.exception(e);
		}
		return null;
    }
    
    /**
     * This method simply creates a key based on the class name
     * and the key. Used to pull and place stuff in the _propertyFormatters
     * Object.
     * 
     * @param clazz
     * @param key
     * @return
     */
    private static String createFormatterKey(Class<?> clazz, String key){
    	return clazz.getName() + "." + key;
    }
}
