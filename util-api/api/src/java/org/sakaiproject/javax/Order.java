/**
 * Order.java - 2007 Jul 20, 2007 2:32:32 PM - AZ
 */

package org.sakaiproject.javax;


/**
 * A pea which defines the order to return the results of a search
 *
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class Order {

	/**
	 * the name of the field (property) in the persisted object
	 */
	public String property;
	/**
	 * if true then the return order is ascending,
	 * if false then return order is descending
	 */
	public boolean ascending = true;

	/**
	 * a simple order for a property which is ascending
	 * @param property the name of the field (property) in the persisted object
	 */
	public Order(String property) {
		this.property = property;
		this.ascending = true;
	}

	/**
	 * define an order for a property
	 * @param property the name of the field (property) in the persisted object
	 * @param ascending if true then the return order is ascending,
	 * if false then return order is descending
	 */
	public Order(String property, boolean ascending) {
		this.property = property;
		this.ascending = ascending;
	}

	@Override
	public String toString() {
	   return "order::prop:" + property + ",asc:" + ascending;
	}

}
