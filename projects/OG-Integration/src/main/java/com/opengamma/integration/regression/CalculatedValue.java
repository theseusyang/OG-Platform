/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;

/**
 * Value calculated in the engine and the properties of its {@code ValueSpecification}.
 */
@BeanDefinition
public final class CalculatedValue implements ImmutableBean {

  private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\d+ \\((.*)\\)");
  // TODO static method to register property names to remove (change set impl to something thread safe)
  // ugh. see AbstractTradeOrDailyPositionPnLFunction
  private static final Set<String> s_removedPropertyNames = Sets.newHashSet("CostOfCarryTimeSeries");

  /** The calculated value. */
  @PropertyDefinition(validate = "notNull")
  private final Object _value;

  /** The properties of the value's {@code ValueSpecification}. */
  @PropertyDefinition(validate = "notNull")
  private final ValueProperties _specificationProperties;

  @PropertyDefinition(validate = "notNull")
  private final String _targetType;

  @PropertyDefinition
  private final String _targetName;

  public static CalculatedValue of(Object value, ValueProperties specProps, String targetType, String targetName) {
    return new CalculatedValue(value, removeFunctionIds(removeProperties(specProps)), targetType, targetName);
  }

  /**
   * The Function property contains an arbitrary function ID which is different between runs.
   * @param properties Properties to clean up
   * @return The properties with the ID removed from the function name property
   */
  private static ValueProperties removeFunctionIds(ValueProperties properties) {
    Set<String> functions = properties.getValues(ValuePropertyNames.FUNCTION);
    Set<String> functionsNoId = Sets.newHashSet();
    for (String function : functions) {
      functionsNoId.add(removeFunctionId(function));
    }
    return properties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, functionsNoId).get();
  }

  private static ValueProperties removeProperties(ValueProperties properties) {
    ValueProperties filteredProps = properties;
    for (String propertyName : s_removedPropertyNames) {
      filteredProps = filteredProps.withoutAny(propertyName);
    }
    return filteredProps;
  }

  /**
   * Removes the ID from a function name, e.g. "123 (Function Name)" becomes "Function Name".
   * @return The function name string with the ID removed
   */
  private static String removeFunctionId(String functionString) {
    Matcher matcher = FUNCTION_PATTERN.matcher(functionString);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return functionString;
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CalculatedValue}.
   * @return the meta-bean, not null
   */
  public static CalculatedValue.Meta meta() {
    return CalculatedValue.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CalculatedValue.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CalculatedValue.Builder builder() {
    return new CalculatedValue.Builder();
  }

  private CalculatedValue(
      Object value,
      ValueProperties specificationProperties,
      String targetType,
      String targetName) {
    JodaBeanUtils.notNull(value, "value");
    JodaBeanUtils.notNull(specificationProperties, "specificationProperties");
    JodaBeanUtils.notNull(targetType, "targetType");
    this._value = value;
    this._specificationProperties = specificationProperties;
    this._targetType = targetType;
    this._targetName = targetName;
  }

  @Override
  public CalculatedValue.Meta metaBean() {
    return CalculatedValue.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the calculated value.
   * @return the value of the property, not null
   */
  public Object getValue() {
    return _value;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the properties of the value's {@code ValueSpecification}.
   * @return the value of the property, not null
   */
  public ValueProperties getSpecificationProperties() {
    return _specificationProperties;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the targetType.
   * @return the value of the property, not null
   */
  public String getTargetType() {
    return _targetType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the targetName.
   * @return the value of the property
   */
  public String getTargetName() {
    return _targetName;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public CalculatedValue clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CalculatedValue other = (CalculatedValue) obj;
      return JodaBeanUtils.equal(getValue(), other.getValue()) &&
          JodaBeanUtils.equal(getSpecificationProperties(), other.getSpecificationProperties()) &&
          JodaBeanUtils.equal(getTargetType(), other.getTargetType()) &&
          JodaBeanUtils.equal(getTargetName(), other.getTargetName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSpecificationProperties());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTargetType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTargetName());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("CalculatedValue{");
    buf.append("value").append('=').append(getValue()).append(',').append(' ');
    buf.append("specificationProperties").append('=').append(getSpecificationProperties()).append(',').append(' ');
    buf.append("targetType").append('=').append(getTargetType()).append(',').append(' ');
    buf.append("targetName").append('=').append(JodaBeanUtils.toString(getTargetName()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CalculatedValue}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code value} property.
     */
    private final MetaProperty<Object> _value = DirectMetaProperty.ofImmutable(
        this, "value", CalculatedValue.class, Object.class);
    /**
     * The meta-property for the {@code specificationProperties} property.
     */
    private final MetaProperty<ValueProperties> _specificationProperties = DirectMetaProperty.ofImmutable(
        this, "specificationProperties", CalculatedValue.class, ValueProperties.class);
    /**
     * The meta-property for the {@code targetType} property.
     */
    private final MetaProperty<String> _targetType = DirectMetaProperty.ofImmutable(
        this, "targetType", CalculatedValue.class, String.class);
    /**
     * The meta-property for the {@code targetName} property.
     */
    private final MetaProperty<String> _targetName = DirectMetaProperty.ofImmutable(
        this, "targetName", CalculatedValue.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "value",
        "specificationProperties",
        "targetType",
        "targetName");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 111972721:  // value
          return _value;
        case -2128969066:  // specificationProperties
          return _specificationProperties;
        case 486622315:  // targetType
          return _targetType;
        case 486420412:  // targetName
          return _targetName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CalculatedValue.Builder builder() {
      return new CalculatedValue.Builder();
    }

    @Override
    public Class<? extends CalculatedValue> beanType() {
      return CalculatedValue.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code value} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Object> value() {
      return _value;
    }

    /**
     * The meta-property for the {@code specificationProperties} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ValueProperties> specificationProperties() {
      return _specificationProperties;
    }

    /**
     * The meta-property for the {@code targetType} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> targetType() {
      return _targetType;
    }

    /**
     * The meta-property for the {@code targetName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> targetName() {
      return _targetName;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 111972721:  // value
          return ((CalculatedValue) bean).getValue();
        case -2128969066:  // specificationProperties
          return ((CalculatedValue) bean).getSpecificationProperties();
        case 486622315:  // targetType
          return ((CalculatedValue) bean).getTargetType();
        case 486420412:  // targetName
          return ((CalculatedValue) bean).getTargetName();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code CalculatedValue}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CalculatedValue> {

    private Object _value;
    private ValueProperties _specificationProperties;
    private String _targetType;
    private String _targetName;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CalculatedValue beanToCopy) {
      this._value = beanToCopy.getValue();
      this._specificationProperties = beanToCopy.getSpecificationProperties();
      this._targetType = beanToCopy.getTargetType();
      this._targetName = beanToCopy.getTargetName();
    }

    //-----------------------------------------------------------------------
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 111972721:  // value
          this._value = (Object) newValue;
          break;
        case -2128969066:  // specificationProperties
          this._specificationProperties = (ValueProperties) newValue;
          break;
        case 486622315:  // targetType
          this._targetType = (String) newValue;
          break;
        case 486420412:  // targetName
          this._targetName = (String) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public CalculatedValue build() {
      return new CalculatedValue(
          _value,
          _specificationProperties,
          _targetType,
          _targetName);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code value} property in the builder.
     * @param value  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder value(Object value) {
      JodaBeanUtils.notNull(value, "value");
      this._value = value;
      return this;
    }

    /**
     * Sets the {@code specificationProperties} property in the builder.
     * @param specificationProperties  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder specificationProperties(ValueProperties specificationProperties) {
      JodaBeanUtils.notNull(specificationProperties, "specificationProperties");
      this._specificationProperties = specificationProperties;
      return this;
    }

    /**
     * Sets the {@code targetType} property in the builder.
     * @param targetType  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder targetType(String targetType) {
      JodaBeanUtils.notNull(targetType, "targetType");
      this._targetType = targetType;
      return this;
    }

    /**
     * Sets the {@code targetName} property in the builder.
     * @param targetName  the new value
     * @return this, for chaining, not null
     */
    public Builder targetName(String targetName) {
      this._targetName = targetName;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("CalculatedValue.Builder{");
      buf.append("value").append('=').append(JodaBeanUtils.toString(_value)).append(',').append(' ');
      buf.append("specificationProperties").append('=').append(JodaBeanUtils.toString(_specificationProperties)).append(',').append(' ');
      buf.append("targetType").append('=').append(JodaBeanUtils.toString(_targetType)).append(',').append(' ');
      buf.append("targetName").append('=').append(JodaBeanUtils.toString(_targetName));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
