/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ResourceLoader provides an alternate implementation of org.util.ResourceBundle, dynamically selecting the prefered locale from either the user's session or from the user's sakai preferences
 * 
 * @author Sugiura, Tatsuki (University of Nagoya)
 */
public class ResourceLoader
{
	/** The type string for this "application": should not change over time as it may be stored in various parts of persistent entities. */
	public static final String APPLICATION_ID = "sakai:resourceloader";

	/** Preferences key for user's regional language locale */
	public static final String LOCALE_KEY = "locale";

	/** My Logger. */
	protected static Log M_log = LogFactory.getLog(ResourceLoader.class);

	/** The base file name for the bundles. */
	protected String baseName = null;

	/** The collection of bundles, keyed by locale. */
	protected Hashtable bundles = new Hashtable();

	/**
	 * Default constructor (does nothing)
	 */
	public ResourceLoader()
	{
	}

	/**
	 * Constructor: set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public ResourceLoader(String name)
	{
		setBaseName(name);
	}

	/**
	 * Access a message from the preferred bundle formatted.
	 * 
	 * @param key
	 *        The message key
	 * @param args
	 *        Parameters for the formatting pattern.
	 * @return The formatted message.
	 */
	public Object getFormattedMessage(String key, Object[] args)
	{
		String pattern = getString(key);
		return MessageFormat.format(pattern, args);
	}

	/**
	 * * Return user's prefered locale * First: return locale from Sakai user preferences, if available * Second: return locale from user session, if available * Last: return system default locale * *
	 * 
	 * @return user's Locale object
	 */
	public Locale getLocale()
	{
		Locale loc = null;

		// First: find locale from Sakai user preferences, if available
//		try
//		{
//			String userId = SessionManager.getCurrentSessionUserId();
//			Preferences prefs = PreferencesService.getPreferences(userId);
//			ResourceProperties locProps = prefs.getProperties(APPLICATION_ID);
//
//			String localeString = locProps.getProperty(LOCALE_KEY);
//			if (localeString != null)
//			{
//				String[] locValues = localeString.split("_");
//				if (locValues.length > 1)
//					loc = new Locale(locValues[0], locValues[1]); // language, country
//				else if (locValues.length == 1) loc = new Locale(locValues[0]); // just language
//			}
//		}
//		catch (Exception e)
//		{
//		} // ignore and continue

//		// Second: find locale from user session, if available
//		if (loc == null)
//		{
//			try
//			{
//				loc = (Locale) SessionManager.getCurrentSession().getAttribute("locale");
//			}
//			catch (NullPointerException e)
//			{
//			} // ignore and continue
//		}

		// Last: find system default locale
		if (loc == null)
		{
			// fallback to default.
			loc = Locale.getDefault();
		}
		else if (!Locale.getDefault().getLanguage().equals("en") && loc.getLanguage().equals("en"))
		{
			// Tweak for English: en is default locale. It has no suffix in filename.
			loc = new Locale("");
		}

		return loc;
	}

	/**
	 * Return string value for specified property in current locale specific ResourceBundle.
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @return String value for specified property key
	 */
	public String getString(String key)
	{
		try
		{
			return getBundle().getString(key);
		}
		catch (MissingResourceException e)
		{
			M_log.warn("Missing key: " + key);
			return "[missing key: " + key + "]";
		}
	}

	/**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @param dflt
	 *        the default value to be returned in case the property is missing
	 * @return String value for specified property key
	 */
	public String getString(String key, String dflt)
	{
		try
		{
			return getBundle().getString(key);
		}
		catch (MissingResourceException e)
		{
			return dflt;
		}
	}

	/**
	 * Set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public void setBaseName(String name)
	{
		this.baseName = name;
	}

	/**
	 * Return ResourceBundle for user's preferred locale
	 * 
	 * @return user's ResourceBundle object
	 */
	protected ResourceBundle getBundle()
	{
		Locale loc = getLocale();
		ResourceBundle bundle = (ResourceBundle) this.bundles.get(loc);
		if (bundle == null)
		{
			if (M_log.isDebugEnabled()) M_log.debug("Load bundle name=" + this.baseName + ", locale=" + getLocale().toString());
			bundle = loadBundle(loc);
		}
		return bundle;
	}

	/**
	 * Return ResourceBundle for specified locale
	 * 
	 * @param bundle
	 *        properties bundle * *
	 * @return locale specific ResourceBundle
	 */
	protected ResourceBundle loadBundle(Locale loc)
	{
		ResourceBundle newBundle = null;
		try
		{
			newBundle = ResourceBundle.getBundle(this.baseName, loc);
		}
		catch (NullPointerException e)
		{
		} // ignore

		setBundle(loc, newBundle);
		return newBundle;
	}

	/**
	 * * Add loc (key) and bundle (value) to this.bundles hash * *
	 * 
	 * @param loc
	 *        Language/Region Locale *
	 * @param bundle
	 *        properties bundle
	 */
	protected void setBundle(Locale loc, ResourceBundle bundle)
	{
		if (bundle == null) throw new NullPointerException();
		this.bundles.put(loc, bundle);
	}
}
