/**********************************************************************************
 * $HeadURL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * A utility class to generate a SHA1 hash based on a full path to a resource/entity.
 *
 */
public class PathHashUtil 
{
	private static final Log log = LogFactory.getLog(PathHashUtil.class);

	
    private static char[] encode = { '0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static ThreadLocal digest = new ThreadLocal();
    
    /**
     * create a SHA1 hash of the path
     *
     * @param nodePath
     * @param encode
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hash(String nodePath)
    {
    	MessageDigest mdigest  = (MessageDigest) digest.get();
    	if ( mdigest == null ) 
    	{
    		try
    		{
    			mdigest = MessageDigest.getInstance("SHA1");
    		}
    		catch (NoSuchAlgorithmException e)
    		{
    			log.error("Cant find Hash Algorithm ",e);
    		}
    		digest.set(mdigest);
    	}
    	byte[] b = mdigest.digest(nodePath.getBytes());
    	char[] c = new char[b.length * 2];
    	for (int i = 0; i < b.length; i++)
    	{
    		c[i * 2] = encode[b[i]&0x0f];
    		c[i * 2 + 1] = encode[(b[i]>>4)&0x0f];
    	}
    	String encoded =  new String(c);
    	log.debug("Encoded "+nodePath+" as "+encoded);
    	return encoded;
    }

}
