/**
 * Restriction.java - 2007 Jul 20, 2007 2:32:19 PM - AZ
 */

package org.sakaiproject.javax;


/**
 * Defines a restriction in a search, this is like saying:
 * where userId = '123'; OR where userId like '%aaronz%';
 *
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class Restriction {

	public final static int EQUALS = 0;
	public final static int GREATER = 1;
	public final static int LESS = 2;
	public final static int LIKE = 3;
	public final static int NULL = 4;
	public final static int NOT_NULL = 5;
	public final static int NOT_EQUALS = 6;

	/**
	 * the name of the field (property) in the persisted object
	 */
	public String property;
	/**
	 * the value of the {@link #property} (can be an array of items)
	 */
	public Object value;
	/**
	 * the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 */
	public int comparison = EQUALS;

	/**
	 * Simple restriction where the property must equal the value
	 * @param property the name of the field (property) in the persisted object
	 * @param value the value of the {@link #property} (can be an array of items)
	 */
	public Restriction(String property, Object value) {
		this.property = property;
		this.value = value;
	}

	/**
	 * Restriction which defines the type of comparison to make between a property and value
	 * @param property the name of the field (property) in the persisted object
	 * @param value the value of the {@link #property} (can be an array of items)
	 * @param comparison the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 */
	public Restriction(String property, Object value, int comparison) {
		this.property = property;
		this.value = value;
		this.comparison = comparison;
	}

	@Override
	public String toString() {
	   return "restriction::prop:" + property + ",value:" + value + ",comp:" + comparison;
	}

}
