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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Web is a web (html, http, etc) technlogies collection of helper methods.
 * </p>
 */
public class Web
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(Web.class);

	/** These characters are escaped when making a URL */
	protected static final String ESCAPE_URL = "$&+,:;=?@ '\"<>#%{}|\\^~[]`";

	/** These can't be encoded in URLs safely even using %nn notation, so encode them using our own custom URL encoding, which the ParameterParser decodes */
	protected static final String ESCAPE_URL_SPECIAL = "^?;";

	protected static void displayStringChars(PrintWriter out, String str)
	{
		if (str == null)
		{
			out.print("null");
		}
		else
			for (int i = 0; i < str.length(); i++)
			{
				int c = (int) str.charAt(i);
				out.print(Integer.toHexString(c) + " ");
			}
		out.println();
	}

	/**
	 * Escape a plaintext string so that it can be output as part of an HTML document. Amperstand, greater-than, less-than, newlines, etc, will be escaped so that they display (instead of being interpreted as formatting).
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value fully escaped for HTML.
	 */
	public static String escapeHtml(String value)
	{
		return FormattedText.escapeHtml(value, true);
	}

	/**
	 * Escape HTML-formatted text in preparation to include it in an HTML document.
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value escaped for HTML.
	 */
	public static String escapeHtmlFormattedText(String value)
	{
		return FormattedText.escapeHtmlFormattedText(value);
	}

	/**
	 * Return a string based on value that is safe to place into a javascript / html identifier: anything not alphanumeric change to 'x'. If the first character is not alphabetic, a letter 'i' is prepended.
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value fully escaped using javascript / html identifier rules.
	 */
	public static String escapeJavascript(String value)
	{
		if (value == null || value == "") return "";
		try
		{
			StringBuffer buf = new StringBuffer();

			// prepend 'i' if first character is not a letter
			if (!java.lang.Character.isLetter(value.charAt(0)))
			{
				buf.append("i");
			}

			// change non-alphanumeric characters to 'x'
			for (int i = 0; i < value.length(); i++)
			{
				char c = value.charAt(i);
				if (!java.lang.Character.isLetterOrDigit(c))
				{
					buf.append("x");
				}
				else
				{
					buf.append(c);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			M_log.warn("escapeJavascript: ", e);
			return value;
		}
	}

	/**
	 * Return a string based on value that is safe to place into a javascript value that is in single quiotes.
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value escaped.
	 */
	public static String escapeJsQuoted(String value)
	{
		if (value == null) return "";
		try
		{
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < value.length(); i++)
			{
				char c = value.charAt(i);

				// a single quote must be escaped with a leading backslash
				if (c == '\'')
				{
					buf.append("\\'");
				}

				// a backslash must be escaped with another backslash
				else if (c == '\\')
				{
					buf.append("\\\\");
				}

				else
				{
					buf.append(c);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			M_log.warn("escapeJsQuoted: ", e);
			return value;
		}
	}

	/**
	 * Return a string based on id that is fully escaped using URL rules, using a UTF-8 underlying encoding.
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped using URL rules.
	 */
	public static String escapeUrl(String id)
	{
		if (id == null) return "";
		try
		{
			// convert the string to bytes in UTF-8
			byte[] bytes = id.getBytes("UTF-8");

			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < bytes.length; i++)
			{
				byte b = bytes[i];
				// escape ascii control characters, ascii high bits, specials
				if (ESCAPE_URL_SPECIAL.indexOf((char) b) != -1)
				{
					buf.append("^^x"); // special funky way to encode bad URL characters - ParameterParser will decode it
					buf.append(toHex(b));
					buf.append('^');
				}
				else if ((ESCAPE_URL.indexOf((char) b) != -1) || (b <= 0x1F) || (b == 0x7F) || (b >= 0x80))
				{
					buf.append("%");
					buf.append(toHex(b));
				}
				else
				{
					buf.append((char) b);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			M_log.warn("escapeUrl: ", e);
			return id;
		}

	} // escapeUrl

	/**
	 * Returns the hex digit cooresponding to a number between 0 and 15.
	 * 
	 * @param i
	 *        The number to get the hex digit for.
	 * @return The hex digit cooresponding to that number.
	 * @exception java.lang.IllegalArgumentException
	 *            If supplied digit is not between 0 and 15 inclusive.
	 */
	protected static final char hexDigit(int i)
	{
		switch (i)
		{
			case 0:
				return '0';
			case 1:
				return '1';
			case 2:
				return '2';
			case 3:
				return '3';
			case 4:
				return '4';
			case 5:
				return '5';
			case 6:
				return '6';
			case 7:
				return '7';
			case 8:
				return '8';
			case 9:
				return '9';
			case 10:
				return 'A';
			case 11:
				return 'B';
			case 12:
				return 'C';
			case 13:
				return 'D';
			case 14:
				return 'E';
			case 15:
				return 'F';
		}

		throw new IllegalArgumentException("Invalid digit:" + i);
	}

	/**
	 * Form a path string from the parts of the array starting at index start to the end, each with a '/' in front.
	 * 
	 * @param parts
	 *        The parts strings
	 * @param start
	 *        The index of the first part to use
	 * @param end
	 *        The index past the last part to use
	 * @return a path string from the parts of the array starting at index start to the end, each with a '/' in front.
	 */
	public static String makePath(String[] parts, int start, int end)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = start; i < end; i++)
		{
			buf.append('/');
			buf.append(parts[i]);
		}

		if (buf.length() > 0) return buf.toString();

		return null;
	}

	protected static void print(PrintWriter out, String name, int value)
	{
		out.print(" " + name + ": ");
		if (value == -1)
		{
			out.println("none");
		}
		else
		{
			out.println(value);
		}
	}

	protected static void print(PrintWriter out, String name, String value)
	{
		out.print(" " + name + ": ");
		out.println(value == null ? "none" : value);
	}

	/**
	 * Compute the URL that would return to this servlet based on the current request, with the optional path and parameters
	 * 
	 * @param req
	 *        The request.
	 * @return The URL back to this servlet based on the current request.
	 */
	public static String returnUrl(HttpServletRequest req, String path)
	{
		StringBuffer url = new StringBuffer();
		url.append(serverUrl(req));
		url.append(req.getContextPath());
		url.append(req.getServletPath());

		if (path != null) url.append(path);

		// TODO: params

		return url.toString();
	}

	/**
	 * Send the HTML / Javascript to invoke an automatic update
	 * 
	 * @param out
	 * @param req
	 *        The request.
	 * @param placementId
	 *        The tool's placement id / presence location / part of the delivery address
	 * @param updateTime
	 *        The time (seconds) between courier checks
	 */
	public static void sendAutoUpdate(PrintWriter out, HttpServletRequest req, String placementId, int updateTime)
	{
		out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
		out.println("updateTime = " + updateTime + "000;");
		out.println("updateUrl = \"" + serverUrl(req) + "/courier/" + placementId + "\";");
		out.println("scheduleUpdate();");
		out.println("</script>");
	}

	/**
	 * Compute the URL that would return to this server based on the current request. Note: this method is duplicated in the kernel/request RequestFilter.java
	 * 
	 * @param req
	 *        The request.
	 * @return The URL back to this server based on the current request.
	 */
	public static String serverUrl(HttpServletRequest req)
	{
		String transport = null;
		int port = 0;
		boolean secure = false;

		// if force.url.secure is set (to a https port number), use https and this port
		String forceSecure = System.getProperty("sakai.force.url.secure");
		if (forceSecure != null)
		{
			transport = "https";
			port = Integer.parseInt(forceSecure);
			secure = true;
		}

		// otherwise use the request scheme and port
		else
		{
			transport = req.getScheme();
			port = req.getServerPort();
			secure = req.isSecure();
		}

		StringBuffer url = new StringBuffer();
		url.append(transport);
		url.append("://");
		url.append(req.getServerName());
		if (((port != 80) && (!secure)) || ((port != 443) && secure))
		{
			url.append(":");
			url.append(port);
		}

		return url.toString();
	}

	public static String snoop(PrintWriter out, boolean html, ServletConfig config, HttpServletRequest req)
	{
		// if no out, send to system out
		ByteArrayOutputStream ostream = null;
		if (out == null)
		{
			ostream = new ByteArrayOutputStream();
			out = new PrintWriter(ostream);
			html = false;
		}

		String h1 = "";
		String h1x = "";
		String pre = "";
		String prex = "";
		String b = "";
		String bx = "";
		String p = "";
		if (html)
		{
			h1 = "<h1>";
			h1x = "</h1>";
			pre = "<pre>";
			prex = "</pre>";
			b = "<b>";
			bx = "</b>";
			p = "<p>";
		}

		Enumeration e = null;

		out.println(h1 + "Snoop for request" + h1x);
		out.println(req.toString());

		if (config != null)
		{
			e = config.getInitParameterNames();
			if (e != null)
			{
				boolean first = true;
				while (e.hasMoreElements())
				{
					if (first)
					{
						out.println(h1 + "Init Parameters" + h1x);
						out.println(pre);
						first = false;
					}
					String param = (String) e.nextElement();
					out.println(" " + param + ": " + config.getInitParameter(param));
				}
				out.println(prex);
			}
		}

		out.println(h1 + "Request information:" + h1x);
		out.println(pre);

		print(out, "Request method", req.getMethod());
		String requestUri = req.getRequestURI();
		print(out, "Request URI", requestUri);
		displayStringChars(out, requestUri);
		print(out, "Request protocol", req.getProtocol());
		String servletPath = req.getServletPath();
		print(out, "Servlet path", servletPath);
		displayStringChars(out, servletPath);
		String contextPath = req.getContextPath();
		print(out, "Context path", contextPath);
		displayStringChars(out, contextPath);
		String pathInfo = req.getPathInfo();
		print(out, "Path info", pathInfo);
		displayStringChars(out, pathInfo);
		print(out, "Path translated", req.getPathTranslated());
		print(out, "Query string", req.getQueryString());
		print(out, "Content length", req.getContentLength());
		print(out, "Content type", req.getContentType());
		print(out, "Server name", req.getServerName());
		print(out, "Server port", req.getServerPort());
		print(out, "Remote user", req.getRemoteUser());
		print(out, "Remote address", req.getRemoteAddr());
		// print(out, "Remote host", req.getRemoteHost());
		print(out, "Authorization scheme", req.getAuthType());

		out.println(prex);

		e = req.getHeaderNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Request headers:" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				out.println(" " + name + ": " + req.getHeader(name));
			}
			out.println(prex);
		}

		e = req.getParameterNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Servlet parameters (Single Value style):" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				out.println(" " + name + " = " + req.getParameter(name));
			}
			out.println(prex);
		}

		e = req.getParameterNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Servlet parameters (Multiple Value style):" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				String vals[] = (String[]) req.getParameterValues(name);
				if (vals != null)
				{
					out.print(b + " " + name + " = " + bx);
					out.println(vals[0]);
					for (int i = 1; i < vals.length; i++)
						out.println("           " + vals[i]);
				}
				out.println(p);
			}
			out.println(prex);
		}

		e = req.getAttributeNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Request attributes:" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				out.println(" " + name + ": " + req.getAttribute(name));
			}
			out.println(prex);
		}

		if (ostream != null)
		{
			out.flush();
			return ostream.toString();
		}

		return "";
	}

	/**
	 * Returns a hex representation of a byte.
	 * 
	 * @param b
	 *        The byte to convert to hex.
	 * @return The 2-digit hex value of the supplied byte.
	 */
	protected static final String toHex(byte b)
	{

		char ret[] = new char[2];

		ret[0] = hexDigit((b >>> 4) & (byte) 0x0F);
		ret[1] = hexDigit((b >>> 0) & (byte) 0x0F);

		return new String(ret);
	}

}
