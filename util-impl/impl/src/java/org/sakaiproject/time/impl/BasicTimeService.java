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

package org.sakaiproject.time.impl;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;

/**
 * <p>
 * BasicTimeService implements the Sakai TimeService
 * </p>
 */
public class BasicTimeService implements TimeService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(TimeService.class);

	/** The time zone for our GMT times. */
	protected TimeZone M_tz = null;

	/**
	 * a calendar to clone for GMT time construction
	 */
	protected GregorianCalendar M_GCal = null;

	/**
	 * The formatter for our special GMT format(s)
	 */
	protected SimpleDateFormat M_fmtA = null;

	protected SimpleDateFormat M_fmtB = null;

	protected SimpleDateFormat M_fmtC = null;

	protected SimpleDateFormat M_fmtD = null;

	protected SimpleDateFormat M_fmtE = null;

	protected SimpleDateFormat M_fmtG = null;

	// Map of LocalTzFormat objects
	private Hashtable M_localTzMap = new Hashtable();

	// Map of local user timezones
	private Hashtable M_userTzMap = new Hashtable();

	// Default local time zone id
	protected String M_tz_local_default = TimeZone.getDefault().getID();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		/** The time zone for our GMT times. */
		M_tz = TimeZone.getTimeZone("GMT");

		M_log.info("init()");

		/**
		 * a calendar to clone for GMT time construction
		 */
		M_GCal = getCalendar(M_tz, 0, 0, 0, 0, 0, 0, 0);

		// Note: formatting for GMT time representations
		M_fmtA = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		M_fmtB = new SimpleDateFormat("MMM d, yyyy h:mm aa");
		M_fmtC = new SimpleDateFormat("h:mm aa");
		M_fmtD = new SimpleDateFormat("MMM d, yyyy");
		M_fmtE = new SimpleDateFormat("yyyyMMddHHmmss");
		M_fmtG = new SimpleDateFormat("yyyy/DDD/HH/"); // that's year, day of year, hour

		M_fmtA.setTimeZone(M_tz);
		M_fmtB.setTimeZone(M_tz);
		M_fmtC.setTimeZone(M_tz);
		M_fmtD.setTimeZone(M_tz);
		M_fmtE.setTimeZone(M_tz);
		M_fmtG.setTimeZone(M_tz);
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	protected String getUserLocalTzId()
	{
		// First check if we already cached this user's timezone
		String userId = SessionManager.getCurrentSessionUserId();
		if (userId == null) return M_tz_local_default;

		String timeZone = (String) M_userTzMap.get(userId);
		if (timeZone != null) return timeZone;

		// Otherwise, get the user's preferred time zone
		Preferences prefs = PreferencesService.getPreferences(userId);
		ResourceProperties tzProps = prefs.getProperties(TimeService.APPLICATION_ID);

		timeZone = tzProps.getProperty(TimeService.TIMEZONE_KEY);

		if (timeZone == null || timeZone.equals("")) timeZone = M_tz_local_default;

		M_userTzMap.put(userId, timeZone);
		return timeZone;
	}

	protected LocalTzFormat getLocalTzFormat(String timeZoneId)
	{
		LocalTzFormat tzFormat = (LocalTzFormat) M_localTzMap.get(timeZoneId);

		if (tzFormat == null)
		{
			tzFormat = new LocalTzFormat(timeZoneId);
			M_localTzMap.put(timeZoneId, tzFormat);
		}

		return tzFormat;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: org.sakai.service.time.TimeService
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * no arg constructor
	 */
	public BasicTimeService()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTime()
	{
		return new MyTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeGmt(String value)
	{
		return new MyTime(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTime(long value)
	{
		return new MyTime(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTime(GregorianCalendar cal)
	{
		return new MyTime(cal.getTimeInMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeGmt(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		return new MyTime(M_tz, year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeGmt(TimeBreakdown breakdown)
	{
		return new MyTime(M_tz, breakdown);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeLocal(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		TimeZone tz_local = getLocalTzFormat(getUserLocalTzId()).M_tz_local;
		return new MyTime(tz_local, year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeLocal(TimeBreakdown breakdown)
	{
		TimeZone tz_local = getLocalTzFormat(getUserLocalTzId()).M_tz_local;
		return new MyTime(tz_local, breakdown);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeBreakdown newTimeBreakdown(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		return new MyTimeBreakdown(year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeRange newTimeRange(Time start, Time end, boolean startIncluded, boolean endIncluded)
	{
		return new MyTimeRange(start, end, startIncluded, endIncluded);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeRange newTimeRange(String value)
	{
		return new MyTimeRange(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeRange newTimeRange(Time startAndEnd)
	{
		return new MyTimeRange(startAndEnd);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeRange newTimeRange(long start, long duration)
	{
		return new MyTimeRange(start, duration);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeRange newTimeRange(Time start, Time end)
	{
		return new MyTimeRange(start, end);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeZone getLocalTimeZone()
	{
		return getLocalTzFormat(getUserLocalTzId()).M_tz_local;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean clearLocalTimeZone(String userId)
	{
		M_userTzMap.remove(userId);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public GregorianCalendar getCalendar(TimeZone zone, int year, int month, int day, int hour, int min, int sec, int ms)
	{
		GregorianCalendar rv = new GregorianCalendar(year, month, day, hour, min, sec);
		rv.setTimeZone(zone);
		rv.set(GregorianCalendar.MILLISECOND, ms);

		return rv;
	}

	/**
	 * Compare two Time for differences, either may be null
	 * 
	 * @param a
	 *        One Time.
	 * @param b
	 *        The other Time.
	 * @return true if the Times are different, false if they are the same.
	 */
	public boolean different(Time a, Time b)
	{
		// if both null, they are the same
		if ((a == null) && (b == null)) return false;

		// if either are null (they both are not), they are different
		if ((a == null) || (b == null)) return true;

		// now we know neither are null, so compare
		return (!a.equals(b));
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * LocalTzFormat -- maintains a local timezone and SimpleDateFormats
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class LocalTzFormat
	{
		// The time zone for our local times
		public TimeZone M_tz_local = null;

		// a calendar to clone for GMT time construction
		public GregorianCalendar M_GCall = null;

		// The formatter for our special local timezone format(s)
		public SimpleDateFormat M_fmtAl = null;

		public SimpleDateFormat M_fmtBl = null;

		public SimpleDateFormat M_fmtBlz = null;

		public SimpleDateFormat M_fmtCl = null;

		public SimpleDateFormat M_fmtClz = null;

		public SimpleDateFormat M_fmtDl = null;

		public SimpleDateFormat M_fmtD2 = null;

		public SimpleDateFormat M_fmtFl = null;

		private LocalTzFormat()
		{
		}; // disable default constructor

		public LocalTzFormat(String timeZoneId)
		{
			M_tz_local = TimeZone.getTimeZone(timeZoneId);

			M_fmtAl = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			M_fmtBl = new SimpleDateFormat("MMM d, yyyy h:mm aa");
			M_fmtBlz = new SimpleDateFormat("MMM d, yyyy h:mm aa z");
			M_fmtCl = new SimpleDateFormat("h:mm aa");
			M_fmtClz = new SimpleDateFormat("h:mm aa z");
			M_fmtDl = new SimpleDateFormat("MMM d, yyyy");
			M_fmtD2 = new SimpleDateFormat("MM/dd/yy");
			M_fmtFl = new SimpleDateFormat("HH:mm:ss");

			M_fmtAl.setTimeZone(M_tz_local);
			M_fmtBl.setTimeZone(M_tz_local);
			M_fmtBlz.setTimeZone(M_tz_local);
			M_fmtCl.setTimeZone(M_tz_local);
			M_fmtClz.setTimeZone(M_tz_local);
			M_fmtDl.setTimeZone(M_tz_local);
			M_fmtD2.setTimeZone(M_tz_local);
			M_fmtFl.setTimeZone(M_tz_local);

			M_GCall = getCalendar(M_tz_local, 0, 0, 0, 0, 0, 0, 0);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * TimeRange implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class MyTimeRange implements TimeRange
	{
		// ends included?
		protected boolean m_startIncluded = true;

		protected boolean m_endIncluded = true;

		// start and end times
		protected Time m_startTime = null;

		protected Time m_endTime = null;

		public Object clone()
		{
			TimeRange obj = newTimeRange((Time) m_startTime.clone(), (Time) m_endTime.clone(), m_startIncluded, m_endIncluded);

			return obj;

		} // clone

		/**
		 * construct from a two times, and start and end inclusion booleans
		 * 
		 * @param start:
		 *        start time
		 * @param end:
		 *        end time
		 * @param startIncluded:
		 *        true if start is part of the range
		 * @param endIncluded:
		 *        true of end is part of the range
		 */
		public MyTimeRange(Time start, Time end, boolean startIncluded, boolean endIncluded)
		{
			m_startTime = start;
			m_endTime = end;
			m_startIncluded = startIncluded;
			m_endIncluded = endIncluded;

			// start time must be <= end time
			if (m_startTime.getTime() > m_endTime.getTime())
			{
				// reverse them to fix
				Time t = m_startTime;
				m_startTime = m_endTime;
				m_endTime = t;
			}

		} // TimeRange

		/**
		 * construct from a string, in our format
		 * 
		 * @param str
		 *        the time range string
		 */
		public MyTimeRange(String str)
		{
			parse(str, null, null);

		} // TimeRange

		/**
		 * construct from a single time
		 * 
		 * @param startAndEnd:
		 *        the single time value for the range
		 */
		public MyTimeRange(Time startAndEnd)
		{
			this(startAndEnd, startAndEnd, true, true);

		} // TimeRange

		/**
		 * construct from a time long and a duration long in ms
		 * 
		 * @param start
		 *        time value
		 * @param duration
		 *        ms duration
		 */
		public MyTimeRange(long start, long duration)
		{
			m_startTime = newTime(start);
			m_endTime = newTime(start + duration);
			m_startIncluded = true;
			m_endIncluded = true;

			// start time must be <= end time
			if (m_startTime.getTime() > m_endTime.getTime())
			{
				// reverse them to fix
				Time t = m_startTime;
				m_startTime = m_endTime;
				m_endTime = t;
			}

		} // TimeRange

		/**
		 * construct from a two times - inclusive
		 * 
		 * @param start:
		 *        the start time
		 * @param end:
		 *        the end time
		 */
		public MyTimeRange(Time start, Time end)
		{
			this(start, end, true, true);

		} // TimeRange

		/**
		 * is this time in my range?
		 * 
		 * @param time:
		 *        the time to check for inclusion
		 * @return true if the time is in the range, false if not
		 */
		public boolean contains(Time time)
		{
			// assume in range, unless proven otherwise
			boolean inRange = true;

			// if out of the range...
			if (time.before(m_startTime) || time.after(m_endTime))
			{
				inRange = false;
			}

			// if at begin and begin not in range
			else if ((!m_startIncluded) && time.equals(m_startTime))
			{
				inRange = false;
			}

			// if at the end and end not in range
			else if ((!m_endIncluded) && time.equals(m_endTime))
			{
				inRange = false;
			}

			return inRange;

		} // contains

		/**
		 * do I overlap this other range at all?
		 * 
		 * @param range:
		 *        the time range to check for overlap
		 * @return true if any time in range is in my range is in the other range, false if not
		 */
		public boolean overlaps(TimeRange range)
		{
			boolean overlaps = false;

			// null range?

			// if my start is in the other range
			if (range.contains(firstTime()))
			{
				overlaps = true;
			}

			// if my end is in the other range
			else if (range.contains(lastTime()))
			{
				overlaps = true;
			}

			// if I contain the other range
			else if (contains(range))
			{
				overlaps = true;
			}

			return overlaps;

		} // overlaps

		/**
		 * do I completely contain this other range?
		 * 
		 * @param range:
		 *        the time range to check for containment
		 * @return true if range is within my time ramge
		 */
		public boolean contains(TimeRange range)
		{
			// I must contain both is start and end
			return (contains(range.firstTime()) && contains(range.lastTime()));

		} // contains

		/**
		 * what is the first time range included?
		 * 
		 * @return the first time actually in the range
		 */
		public Time firstTime()
		{
			return firstTime(1);

		} // firstTime

		/**
		 * what is the last time range included?
		 * 
		 * @return the last time actually in the range
		 */
		public Time lastTime()
		{
			return lastTime(1);

		} // lastTime

		/**
		 * what is the first time range included?
		 * 
		 * @param fudge
		 *        How many ms to advance if the first is not included.
		 * @return the first time actually in the range
		 */
		public Time firstTime(long fudge)
		{
			// if the start is included, return this
			if (m_startIncluded)
			{
				return m_startTime;
			}

			// if not, return a time one ms after start
			Time fudgeStartTime = (Time) m_startTime.clone();
			fudgeStartTime.setTime(m_startTime.getTime() + fudge);
			return fudgeStartTime;

		} // firstTime

		/**
		 * what is the last time range included?
		 * 
		 * @param fudge
		 *        How many ms to decrease if the first is not included.
		 * @return the last time actually in the range
		 */
		public Time lastTime(long fudge)
		{
			// if the end is included, return this
			if (m_endIncluded)
			{
				return m_endTime;
			}

			// if not, return a time one ms before end
			Time fudgeEndTime = (Time) m_endTime.clone();
			fudgeEndTime.setTime(m_endTime.getTime() - fudge);
			return fudgeEndTime;

		} // lastTime

		/**
		 * format the range
		 * 
		 * @return a string representation of the time range
		 */
		public String toString()
		{
			// a place to build the string (slightly larger)
			StringBuffer buf = new StringBuffer(64);

			// start with the start value, always used
			buf.append(m_startTime);

			// more that single value?
			if (!m_startTime.equals(m_endTime))
			{
				// what separator to use?
				if (m_startIncluded && m_endIncluded)
				{
					buf.append('-');
				}
				else if ((!m_startIncluded) && (!m_endIncluded))
				{
					buf.append('~');
				}
				else if (!m_startIncluded)
				{
					buf.append('[');
				}
				else
				{
					buf.append(']');
				}

				// add the end
				buf.append(m_endTime);
			}

			// return the answer as a string
			return buf.toString();

		} // toString

		/**
		 * format the range - human readable
		 * 
		 * @return a string representation of the time range, human readable
		 */
		public String toStringHR()
		{
			// a place to build the string (slightly larger)
			StringBuffer buf = new StringBuffer(64);

			// start with the start value, always used
			buf.append(m_startTime.toStringGmtFull());

			// more that single value?
			if (!m_startTime.equals(m_endTime))
			{
				// what separator to use?
				if (m_startIncluded && m_endIncluded)
				{
					buf.append(" - ");
				}
				else if ((!m_startIncluded) && (!m_endIncluded))
				{
					buf.append(" ~ ");
				}
				else if (!m_startIncluded)
				{
					buf.append(" [ ");
				}
				else
				{
					buf.append(" ] ");
				}

				// add the end
				buf.append(m_endTime.toStringGmtFull());
			}

			// return the answer as a string
			return buf.toString();

		} // toStringHR

		/**
		 * equals to another time range
		 */
		public boolean equals(Object obj)
		{
			boolean equals = false;

			if (obj instanceof MyTimeRange)
			{
				equals = ((((MyTimeRange) obj).m_startIncluded == m_startIncluded)
						&& (((MyTimeRange) obj).m_endIncluded == m_endIncluded)
						&& (((MyTimeRange) obj).m_startTime.equals(m_startTime)) && (((MyTimeRange) obj).m_endTime
						.equals(m_endTime)));
			}

			return equals;

		} // equals

		/**
		 * compute the duration, in ms, of the time range
		 */
		public long duration()
		{
			return (lastTime().getTime() - firstTime().getTime());

		} // duration

		/**
		 * parse from a string - resolve fully earliest ('!') and latest ('*') and durations ('=')
		 * 
		 * @param str
		 *        the string to parse
		 * @param earliest
		 *        Time to substitute for any 'earliest' values
		 * @param latest
		 *        the Time to use for 'latest'
		 */
		protected void parse(String str, Time earliest, Time latest)
		{
			try
			{
				// separate the string by '[]~-'
				// (we do want the delimiters as tokens, thus the true param)
				StringTokenizer tokenizer = new StringTokenizer(str, "[]~-", true);

				int tokenCount = 0;
				long startMs = -1;
				long endMs = -1;
				m_startTime = null;
				m_endTime = null;

				while (tokenizer.hasMoreTokens())
				{
					tokenCount++;
					String next = tokenizer.nextToken();

					switch (tokenCount)
					{
						case 1:
						{
							if (next.charAt(0) == '=')
							{
								// use the rest as a duration in ms
								startMs = Long.parseLong(next.substring(1));
							}

							else
							{
								m_startTime = newTimeGmt(next);
							}

						}
							break;

						case 2:
						{
							// set the inclusions
							switch (next.charAt(0))
							{
								// start not included
								case '[':
								{
									m_startIncluded = false;
									m_endIncluded = true;

								}
									break;

								// end not included
								case ']':
								{
									m_startIncluded = true;
									m_endIncluded = false;

								}
									break;

								// neither included
								case '~':
								{
									m_startIncluded = false;
									m_endIncluded = false;

								}
									break;

								// both included
								case '-':
								{
									m_startIncluded = true;
									m_endIncluded = true;

								}
									break;

								// trouble!
								default:
								{
									throw new Exception(next.charAt(0) + " invalid");
								}
							} // switch (next[0])

						}
							break;

						case 3:
						{
							if (next.charAt(0) == '=')
							{
								// use the rest as a duration in ms
								endMs = Long.parseLong(next.substring(1));
							}

							else
							{
								m_endTime = newTimeGmt(next);
							}

						}
							break;

						// trouble!
						default:
						{
							throw new Exception(">3 tokens");
						}
					} // switch (tokenCount)

				} // while (tokenizer.hasMoreTokens())

				// if either start or end was in duration, adjust (but not both!)
				if ((startMs != -1) && (endMs != -1))
				{
					throw new Exception("==");
				}

				if (startMs != -1)
				{
					if (m_endTime == null)
					{
						throw new Exception("=, * null");
					}
					m_startTime = newTime(m_endTime.getTime() - startMs);
				}
				else if (endMs != -1)
				{
					if (m_startTime == null)
					{
						throw new Exception("=, ! null");
					}
					m_endTime = newTime(m_startTime.getTime() + endMs);
				}

				// if there is only one token
				if (tokenCount == 1)
				{
					// end is start, both included
					m_endTime = m_startTime;
					m_startIncluded = true;
					m_endIncluded = true;
				}

				// start time must be <= end time
				if (m_startTime.getTime() > m_endTime.getTime())
				{
					// reverse them to fix
					Time t = m_startTime;
					m_startTime = m_endTime;
					m_endTime = t;
				}
			}
			catch (Exception e)
			{
				M_log.warn("parse: exception parsing: " + str + " : " + e.toString());

				// set a now range, just to have something
				m_startTime = newTime();
				m_endTime = m_startTime;
				m_startIncluded = true;
				m_endIncluded = true;
			}
		}

		/**
		 * Shift the time range back an intervel
		 * 
		 * @param i
		 *        time intervel in ms
		 */
		public void shiftBackward(long i)
		{
			m_startTime.setTime(m_startTime.getTime() - i);
			m_endTime.setTime(m_endTime.getTime() - i);
		}

		/**
		 * Shift the time range forward an intervel
		 * 
		 * @param i
		 *        time intervel in ms
		 */
		public void shiftForward(long i)
		{
			m_startTime.setTime(m_startTime.getTime() + i);
			m_endTime.setTime(m_endTime.getTime() + i);
		}

		/**
		 * Enlarge or shrink the time range by multiplying a zooming factor
		 * 
		 * @param f
		 *        zooming factor
		 */
		public void zoom(double f)
		{
			long oldRange = m_endTime.getTime() - m_startTime.getTime();
			long center = m_startTime.getTime() + oldRange / 2;
			long newRange = (long) ((double) oldRange * f);

			m_startTime.setTime(center - newRange / 2);
			m_endTime.setTime(center + newRange / 2);
		}

		/**
		 * Adjust this time range based on the difference between the origRange and the modRange, if any
		 * 
		 * @param original
		 *        the original time range.
		 * @param modified
		 *        the modified time range.
		 */
		public void adjust(TimeRange original, TimeRange modified)
		{
			if (original.equals(modified)) return;

			// adjust for the change in the start time
			m_startTime.setTime(m_startTime.getTime()
					+ (((MyTimeRange) modified).m_startTime.getTime() - ((MyTimeRange) original).m_startTime.getTime()));

			// adjust for the change in the end time
			m_endTime.setTime(m_endTime.getTime()
					+ (((MyTimeRange) modified).m_endTime.getTime() - ((MyTimeRange) original).m_endTime.getTime()));

		} // adjust

		/**
		 * check if the time range is really just a single time
		 * 
		 * @return true if the time range is a single time, false if it is not
		 */
		public boolean isSingleTime()
		{
			return (m_startTime.equals(m_endTime) && m_startIncluded && m_endIncluded);

		} // isSingleTime

	} // class TimeRange

	/**********************************************************************************************************************************************************************************************************************************************************
	 * TimeBreakdown implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class MyTimeBreakdown implements TimeBreakdown
	{
		/** The parts. */
		protected int year;

		protected int month;

		protected int day;

		protected int hour;

		protected int min;

		protected int sec;

		protected int ms;

		public MyTimeBreakdown(int y, int m, int d, int h, int minutes, int s, int milliseconds)
		{
			year = y;
			month = m;
			day = d;
			hour = h;
			min = minutes;
			sec = s;
			ms = milliseconds;
		}

		public MyTimeBreakdown(TimeBreakdown other)
		{
			year = ((MyTimeBreakdown) other).year;
			month = ((MyTimeBreakdown) other).month;
			day = ((MyTimeBreakdown) other).day;
			hour = ((MyTimeBreakdown) other).hour;
			min = ((MyTimeBreakdown) other).min;
			sec = ((MyTimeBreakdown) other).sec;
			ms = ((MyTimeBreakdown) other).ms;
		}

		public String toString()
		{
			return "year: " + year + " month: " + month + " day: " + day + " hour: " + hour + " min: " + min + " sec: " + sec
					+ " ms: " + ms;
		}

		public int getYear()
		{
			return year;
		}

		public int getMonth()
		{
			return month;
		}

		public int getDay()
		{
			return day;
		}

		public int getHour()
		{
			return hour;
		}

		public int getMin()
		{
			return min;
		}

		public int getSec()
		{
			return sec;
		}

		public int getMs()
		{
			return ms;
		}

		/**
		 * @param i
		 */
		public void setDay(int i)
		{
			day = i;
		}

		/**
		 * @param i
		 */
		public void setHour(int i)
		{
			hour = i;
		}

		/**
		 * @param i
		 */
		public void setMin(int i)
		{
			min = i;
		}

		/**
		 * @param i
		 */
		public void setMonth(int i)
		{
			month = i;
		}

		/**
		 * @param i
		 */
		public void setMs(int i)
		{
			ms = i;
		}

		/**
		 * @param i
		 */
		public void setSec(int i)
		{
			sec = i;
		}

		/**
		 * @param i
		 */
		public void setYear(int i)
		{
			year = i;
		}
	}
}
