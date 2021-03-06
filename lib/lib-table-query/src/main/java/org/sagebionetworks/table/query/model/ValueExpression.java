package org.sagebionetworks.table.query.model;

import java.util.List;

/**
 * This matches &ltvalue expression&gt in: <a href="http://savage.net.au/SQL/sql-92.bnf">SQL-92</a>
 */
public class ValueExpression extends SQLElement {

	private StringValueExpression stringValueExpression = null;
	private NumericValueExpression numericValueExpression = null;

	public ValueExpression(StringValueExpression stringValueExpression) {
		this.stringValueExpression = stringValueExpression;
	}

	public ValueExpression(NumericValueExpression numericValueExpression) {
		this.numericValueExpression = numericValueExpression;
	}

	public StringValueExpression getStringValueExpression() {
		return stringValueExpression;
	}

	public NumericValueExpression getNumericValueExpression() {
		return numericValueExpression;
	}

	@Override
	public void toSql(StringBuilder builder) {
		if (this.stringValueExpression != null) {
			stringValueExpression.toSql(builder);
		} else {
			numericValueExpression.toSql(builder);
		}
	}

	@Override
	<T extends Element> void addElements(List<T> elements, Class<T> type) {
		checkElement(elements, type, stringValueExpression);
		checkElement(elements, type, numericValueExpression);
	}

}
