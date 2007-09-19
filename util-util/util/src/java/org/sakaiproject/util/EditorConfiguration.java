/**
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

import java.lang.reflect.Method;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * EditorConfiguration is a utility class that provides methods to access
 * information that is relevant to the configuration of a rich-text editor.
 */
public class EditorConfiguration 
{
	/**
	 * Access the identifier for the editor currently in use.  This value is
	 * supplied by the ServerConfigurationService and uniquely identifies a 
	 * particular editor supported by Sakai. 
	 * @return The unique identifier for the editor as specified in 
	 * "sakai.properties". 
	 */
	public static String getWysiwigEditor()
	{
		return ServerConfigurationService.getString("wysiwyg.editor");
	}
	
	/**
	 * Determine whether the CitationsService is fully configured to enable 
	 * this user to search library resources and add search results as citations 
	 * in the document in the rich-text editor. 
	 * @return true if this user may use the resource-search plug-in in the editor
	 * to search library resources and add search results as citations in the 
	 * document in the editor, false otherwise. 
	 */
	public static boolean enableResourceSearch()
	{
		Boolean showCitationsButton = Boolean.FALSE;
		
		Object component = ComponentManager.get("org.sakaiproject.citation.api.ConfigurationService");
		if(component != null)
		{
			try
			{
				Method method = component.getClass().getMethod("librarySearchEnabled", new Class[]{});
				
				if(method != null)
				{
					showCitationsButton = (Boolean) method.invoke(component, null);
				}
			}
			catch(Exception e)
			{
				// ignore -- if the service can't be found or the method 
				// 	can't be invoked, then the button should not be shown
			} 
		}
		
		return showCitationsButton.booleanValue();
	}

}
