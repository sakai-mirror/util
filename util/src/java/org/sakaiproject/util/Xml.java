/**********************************************************************************
 * $URL$
 * $Id$
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * <p>
 * Xml is a DOM XML helper object with static functions to help with XML.
 * </p>
 */
public class Xml
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(Xml.class);

	/**
	 * Create a new DOM Document.
	 * 
	 * @return A new DOM document.
	 */
	public static Document createDocument()
	{
		try
		{
			DocumentBuilder builder = getDocumentBuilder();
			Document doc = builder.newDocument();

			return doc;
		}
		catch (Exception any)
		{
			M_log.warn("createDocument: " + any.toString());
			return null;
		}
	}

	/**
	 * Read a DOM Document from xml in a file.
	 * 
	 * @param name
	 *        The file name for the xml file.
	 * @return A new DOM Document with the xml contents.
	 */
	public static Document readDocument(String name)
	{
		Document doc = null;
		// first try using whatever character encoding the XML itself specifies
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputStream fis = new FileInputStream(name);
			doc = docBuilder.parse(fis);
		}
		catch (Exception e)
		{
			doc = null;
		}

		if (doc != null) return doc;

		// OK, that didn't work - the document is probably ISO-8859-1
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), "ISO-8859-1");
			InputSource inputSource = new InputSource(in);
			doc = docBuilder.parse(inputSource);
		}
		catch (Exception any)
		{
			doc = null;
		}

		if (doc != null) return doc;

		// try forcing UTF-8
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), "UTF-8");
			InputSource inputSource = new InputSource(in);
			doc = docBuilder.parse(inputSource);
		}
		catch (Exception any)
		{
			M_log.warn("readDocument failed on file: " + name + " with exception: " + any.toString());
			doc = null;
		}

		return doc;
	}

	/**
	 * Read a DOM Document from xml in a string.
	 * 
	 * @param in
	 *        The string containing the XML
	 * @return A new DOM Document with the xml contents.
	 */
	public static Document readDocumentFromString(String in)
	{
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputSource inputSource = new InputSource(new StringReader(in));
			Document doc = docBuilder.parse(inputSource);
			return doc;
		}
		catch (Exception any)
		{
			M_log.warn("readDocumentFromString: " + any.toString());
			return null;
		}
	}

	/**
	 * Read a DOM Document from xml in a stream.
	 * 
	 * @param in
	 *        The stream containing the XML
	 * @return A new DOM Document with the xml contents.
	 */
	public static Document readDocumentFromStream(InputStream in)
	{
		try
		{
			DocumentBuilder docBuilder = getDocumentBuilder();
			InputSource inputSource = new InputSource(in);
			Document doc = docBuilder.parse(inputSource);
			return doc;
		}
		catch (Exception any)
		{
			M_log.warn("readDocumentFromStream: " + any.toString());
			return null;
		}
	}

	/**
	 * Write a DOM Document to an xml file.
	 * 
	 * @param doc
	 *        The DOM Document to write.
	 * @param fileName
	 *        The complete file name path.
	 */
	public static void writeDocument(Document doc, String fileName)
	{
		try
		{
			// create a file that uses the UTF-8 encoding
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");

			// Note: using xerces %%% is there a org.w3c.dom way to do this?
			XMLSerializer s = new XMLSerializer(out, new OutputFormat("xml", "UTF-8", true));
			s.serialize(doc);
			out.close();
		}
		catch (Exception any)
		{
			M_log.warn("writeDocument: " + any.toString());
		}
	}

	/**
	 * Write a DOM Document to an output stream.
	 * 
	 * @param doc
	 *        The DOM Document to write.
	 * @param out
	 *        The output stream.
	 */
	public static String writeDocumentToString(Document doc)
	{
		try
		{
			StringWriter sw = new StringWriter();
			// Note: using xerces %%% is there a org.w3c.dom way to do this?
			XMLSerializer s = new XMLSerializer(sw, new OutputFormat("xml", "UTF-8", true /* doc */));
			s.serialize(doc);

			sw.flush();
			return sw.toString();
		}
		catch (Exception any)
		{
			M_log.warn("writeDocumentToString: " + any.toString());
			return null;
		}
	}

	/**
	 * Place a string into the attribute <tag>of the element <el>, encoded so special characters can be used.
	 * 
	 * @param el
	 *        The element.
	 * @param tag
	 *        The attribute name.
	 * @param value
	 *        The string.
	 */
	public static void encodeAttribute(Element el, String tag, String value)
	{
		// encode the message body base64, and make it an attribute
		try
		{
			String encoded = Base64.encode(value.getBytes("UTF-8"));
			el.setAttribute(tag, encoded);
		}
		catch (Exception e)
		{
			M_log.warn("encodeAttribute: " + e);
		}
	}

	/**
	 * Decode a string from the attribute <tag>of the element <el>, that was made using encodeAttribute().
	 * 
	 * @param el
	 *        The element.
	 * @param tag
	 *        The attribute name.
	 * @return The string; may be empty, won't be null.
	 */
	public static String decodeAttribute(Element el, String tag)
	{
		String charset = StringUtil.trimToNull(el.getAttribute("charset"));
		if (charset == null) charset = "UTF-8";

		String body = StringUtil.trimToNull(el.getAttribute(tag));
		if (body != null)
		{
			try
			{
				byte[] decoded = Base64.decode(body);
				body = new String(decoded, charset);
			}
			catch (Exception e)
			{
				M_log.warn("decodeAttribute: " + e);
			}
		}

		if (body == null) body = "";

		return body;
	}

	/**
	 * @return a DocumentBuilder object for XML parsing.
	 */
	protected static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		return dbf.newDocumentBuilder();
	}

	/**
	 * Serialize the properties into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param propsToSerialize
	 *        The properties to serialize.
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
	public static Element propertiesToXml(Properties propsToSerialize, Document doc, Stack stack)
	{
		Element properties = doc.createElement("properties");
		((Element) stack.peek()).appendChild(properties);
		Enumeration props = propsToSerialize.propertyNames();
		while (props.hasMoreElements())
		{
			String name = (String) props.nextElement();
			String value = propsToSerialize.getProperty(name);
			Element propElement = doc.createElement("property");
			properties.appendChild(propElement);
			propElement.setAttribute("name", name);

			// encode to allow special characters in the value
			Xml.encodeAttribute(propElement, "value", (String) value);
			propElement.setAttribute("enc", "BASE64");
		}

		return properties;
	}

	/**
	 * Fill in a properties from XML.
	 * 
	 * @param properties
	 *        The properties to fill in.
	 * @param el
	 *        The XML DOM element.
	 */
	public static void xmlToProperties(Properties properties, Element el)
	{
		// the children (property)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for property
			if (element.getTagName().equals("property"))
			{
				String name = element.getAttribute("name");
				String enc = StringUtil.trimToNull(element.getAttribute("enc"));
				String value = null;
				if ("BASE64".equalsIgnoreCase(enc))
				{
					value = decodeAttribute(element, "value");
				}
				else
				{
					value = element.getAttribute("value");
				}

				properties.put(name, value);
			}
		}
	}
}
