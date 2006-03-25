/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ResourceLoader provides an alternate implementation of org.util.ResourceBundle, dynamically selecting the prefered locale from either the user's session or from the user's sakai preferences
 * 
 * @author Sugiura, Tatsuki (University of Nagoya)
 */
public class ResourceLoader extends DummyMap implements Map
{
	/** This string is used by the UserPrefsTool (imitating a service) */
	public static final String PREFS_KEY = ResourceLoader.class.getName();

	/** Preferences key for user's regional language locale */
	public static final String LOCALE_KEY = "locale";

	protected static Log M_log = LogFactory.getLog(ResourceLoader.class);

	protected String baseName = null;

	protected Hashtable bundles = new Hashtable();

	/**
	 * Default constructor (does nothing)
	 */
	public ResourceLoader()
	{
		M_log.debug("init");
	}

	/**
	 * Constructor: set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public ResourceLoader(String name)
	{
		M_log.debug("init");
		setBaseName(name);
	}

	/**
	 * * Set baseName * *
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public void setBaseName(String name)
	{
		M_log.debug("set baseName=" + name);
		this.baseName = name;
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

	/**
	 * * Return ResourceBundle for user's preferred locale * *
	 * 
	 * @return user's ResourceBundle object
	 */
	protected ResourceBundle getBundle()
	{
		Locale loc = getLocale();
		ResourceBundle bundle = (ResourceBundle) this.bundles.get(loc);
		if (bundle == null)
		{
			M_log.debug("Load bundle name=" + this.baseName + ", locale=" + getLocale().toString());
			bundle = loadBundle(loc);
		}
		return bundle;
	}

	/**
	 * * Clear bundles hashmap
	 */
	public void purgeCache()
	{
		this.bundles = new Hashtable();
		M_log.debug("purge bundle cache");
	}

	/**
	 * * Return user's prefered locale * First: return locale from Sakai user preferences, if available * Second: return locale from user session, if available * Last: return system default locale * *
	 * 
	 * @return user's Locale object
	 */
	public Locale getLocale()
	{
		M_log.debug("checking locale");
		Locale loc = null;

		// First: find locale from Sakai user preferences, if available
		/* TODO: how to do this? dependencies! -ggolden
		try
		{
			String userId = SessionManager.getCurrentSessionUserId();
			Preferences prefs = PreferencesService.getPreferences(userId);
			ResourceProperties locProps = prefs.getProperties(PREFS_KEY);

			String localeString = locProps.getProperty(LOCALE_KEY);
			if (localeString != null)
			{
				String[] locValues = localeString.split("_");
				if (locValues.length > 1)
					loc = new Locale(locValues[0], locValues[1]); // language, country
				else if (locValues.length == 1) loc = new Locale(locValues[0]); // just language
			}
		}
		catch (Exception e)
		{
		} // ignore and continue
		*/

		// Second: find locale from user session, if available
		/* TODO: how to do this? dependencies! -ggolden
		if (loc == null)
		{
			try
			{
				loc = (Locale) SessionManager.getCurrentSession().getAttribute("locale");
				M_log.debug("get locale from session: " + loc.toString());
			}
			catch (NullPointerException e)
			{
			} // ignore and continue
		}
		*/

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
			M_log.debug("tweak for en");
		}

		return loc;
	}

	/**
	 * * Return ResourceBundle for specified locale * *
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
	 * * Return string value for specified property in current locale specific ResourceBundle * *
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle * *
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
	 * * Return (generic object) value for specified property in current locale specific ResourceBundle * *
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle * *
	 * @return value for specified property key
	 */
	public Object get(Object key)
	{
		return getString(key.toString());
	}

	public Object getFormattedMessage(Object key, Object[] args)
	{
		String pattern = (String) get(key);
		return MessageFormat.format(pattern, args);
	}

	public Set keySet()
	{
		return getBundleAsMap().keySet();
	}

	public Collection values()
	{
		return getBundleAsMap().values();
	}

	public Set entrySet()
	{
		return getBundleAsMap().entrySet();
	}

	protected Map getBundleAsMap()
	{
		Map bundle = new Hashtable();

		for (Enumeration e = getBundle().getKeys(); e.hasMoreElements();)
		{
			Object key = e.nextElement();
			bundle.put(key, getBundle().getObject((String) key));
		}

		return bundle;
	}
}

abstract class DummyMap implements Map
{
	public abstract Object get(Object key);

	public boolean containsKey(Object key)
	{
		return true;
	}

	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

	public Set keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Collection values()
	{
		throw new UnsupportedOperationException();
	}

	public Set entrySet()
	{
		throw new UnsupportedOperationException();
	}

	public Object put(Object arg0, Object arg1)
	{
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public void putAll(Map arg0)
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public int size()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}
}
