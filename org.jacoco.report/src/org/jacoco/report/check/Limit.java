/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.check;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Descriptor for a limit which is given by a {@link Rule}.
 */
public class Limit {

	private static final Map<CounterValue, String> VALUE_NAMES;
	private static final Map<CounterEntity, String> ENTITY_NAMES;

	static {
		final Map<CounterValue, String> values = new HashMap<CounterValue, String>();
		values.put(CounterValue.TOTALCOUNT, "total count");
		values.put(CounterValue.MISSEDCOUNT, "missed count");
		values.put(CounterValue.COVEREDCOUNT, "covered count");
		values.put(CounterValue.MISSEDRATIO, "missed ratio");
		values.put(CounterValue.COVEREDRATIO, "covered ratio");
		VALUE_NAMES = Collections.unmodifiableMap(values);

		final Map<CounterEntity, String> entities = new HashMap<CounterEntity, String>();
		entities.put(CounterEntity.INSTRUCTION, "instructions");
		entities.put(CounterEntity.BRANCH, "branches");
		entities.put(CounterEntity.COMPLEXITY, "complexity");
		entities.put(CounterEntity.LINE, "lines");
		entities.put(CounterEntity.METHOD, "methods");
		entities.put(CounterEntity.CLASS, "classes");
		ENTITY_NAMES = Collections.unmodifiableMap(entities);
	}

	private CounterEntity entity;

	private CounterValue value;

	private BigDecimal minimum;

	private BigDecimal maximum;

	/**
	 * Creates a new instance with the following defaults:
	 * <ul>
	 * <li>counter entity: {@link CounterEntity#INSTRUCTION}
	 * <li>counter value: {@link CounterValue#COVEREDRATIO}
	 * <li>minimum: no limit
	 * <li>maximum: no limit
	 * </ul>
	 */
	public Limit() {
		this.entity = CounterEntity.INSTRUCTION;
		this.value = CounterValue.COVEREDRATIO;
	}

	/**
	 * @return the configured counter entity to check
	 */
	public CounterEntity getEntity() {
		return entity;
	}

	/**
	 * Sets the counter entity to check.
	 * 
	 * @param entity
	 *            counter entity to check
	 */
	public void setCounter(final CounterEntity entity) {
		this.entity = entity;
	}

	/**
	 * @return the configured value to check
	 */
	public CounterValue getValue() {
		return value;
	}

	/**
	 * Sets the value to check.
	 * 
	 * @param value
	 *            value to check
	 */
	public void setValue(final CounterValue value) {
		this.value = value;
	}

	/**
	 * @return configured minimum value, or <code>null</code> if no minimum is
	 *         given
	 */
	public BigDecimal getMinimum() {
		return minimum;
	}

	/**
	 * Sets allowed minimum value. Coverage ratios are given in the range from
	 * 0.0 to 1.0.
	 * 
	 * @param minimum
	 *            allowed minimum or <code>null</code>, if no minimum should be
	 *            checked
	 */
	public void setMinimum(final BigDecimal minimum) {
		this.minimum = minimum;
	}

	/**
	 * Sets allowed minimum value as String representation.
	 * 
	 * @param minimum
	 *            allowed minimum or <code>null</code>, if no minimum should be
	 *            checked
	 * @see Limit#setMinimum(BigDecimal)
	 */
	public void setMinimum(final String minimum) {
		setMinimum(minimum == null ? null : new BigDecimal(minimum));
	}

	/**
	 * @return configured maximum value, or <code>null</code> if no maximum is
	 *         given
	 */
	public BigDecimal getMaximum() {
		return maximum;
	}

	/**
	 * Sets allowed maximum value as String representation.
	 * 
	 * @param maximum
	 *            allowed maximum or <code>null</code>, if no maximum should be
	 *            checked
	 * @see #setMaximum(BigDecimal)
	 */
	public void setMaximum(final String maximum) {
		setMaximum(maximum == null ? null : new BigDecimal(maximum));
	}

	/**
	 * Sets allowed maximum value. Coverage ratios are given in the range from
	 * 0.0 to 1.0.
	 * 
	 * @param maximum
	 *            allowed maximum or <code>null</code>, if no maximum should be
	 *            checked
	 */
	public void setMaximum(final BigDecimal maximum) {
		this.maximum = maximum;
	}

	String check(final ICoverageNode node) {
		final double d = node.getCounter(entity).getValue(value);
		if (Double.isNaN(d)) {
			return null;
		}
		final BigDecimal bd = BigDecimal.valueOf(d);
		if (minimum != null && minimum.compareTo(bd) > 0) {
			return message("minimum", bd, minimum, RoundingMode.FLOOR);
		}
		if (maximum != null && maximum.compareTo(bd) < 0) {
			return message("maximum", bd, maximum, RoundingMode.CEILING);
		}
		return null;
	}

	private String message(final String minmax, final BigDecimal v,
			final BigDecimal ref, final RoundingMode mode) {
		final BigDecimal rounded = v.setScale(ref.scale(), mode);
		return String.format("%s %s is %s, but expected %s is %s",
				ENTITY_NAMES.get(entity), VALUE_NAMES.get(value),
				rounded.toPlainString(), minmax, ref.toPlainString());
	}

}
