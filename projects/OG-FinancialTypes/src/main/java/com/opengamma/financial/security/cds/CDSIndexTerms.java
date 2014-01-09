/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Immutable set of tenors that represents the CreditDefaultSwapIndex security terms
 */
@BeanDefinition(builderScope = "private")
public final class CDSIndexTerms
    implements ImmutableBean, Iterable<Tenor>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton empty cdsIndex terms.
   */
  public static final CDSIndexTerms EMPTY = new CDSIndexTerms(ImmutableSortedSet.<Tenor>of());

  /**
   * The set of tenors.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSortedSet<Tenor> _tenors;

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@link CDSIndexTerms} from a tenor.
   * 
   * @param tenor  the tenor to warp in the terms, not null
   * @return the terms, not null
   */
  public static CDSIndexTerms of(Tenor tenor) {
    ArgumentChecker.notNull(tenor, "tenor");
    return new CDSIndexTerms(ImmutableSortedSet.of(tenor));
  }

  /**
   * Obtains an {@link CDSIndexTerms} from an array of tenors.
   * 
   * @param tenors  an array of tenors, no nulls, not null
   * @return the terms, not null
   */
  public static CDSIndexTerms of(Tenor... tenors) {
    ArgumentChecker.noNulls(tenors, "tenors");
    return new CDSIndexTerms(ImmutableSortedSet.copyOf(tenors));
  }

  /**
   * Obtains an {@link CDSIndexTerms} from a collection of tenors.
   * 
   * @param tenors  the collection of tenors, no nulls, not null
   * @return the terms, not null
   */
  public static CDSIndexTerms of(Iterable<Tenor> tenors) {
    ArgumentChecker.noNulls(tenors, "tenors");
    return new CDSIndexTerms(ImmutableSortedSet.copyOf(tenors));
  }

  //-------------------------------------------------------------------------
  /**
   * Returns an iterator over the tenors in the terms.
   * 
   * @return the tenors in the terms, not null
   */
  @Override
  public Iterator<Tenor> iterator() {
    return _tenors.iterator();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CDSIndexTerms}.
   * @return the meta-bean, not null
   */
  public static CDSIndexTerms.Meta meta() {
    return CDSIndexTerms.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CDSIndexTerms.Meta.INSTANCE);
  }

  private CDSIndexTerms(
      SortedSet<Tenor> tenors) {
    JodaBeanUtils.notNull(tenors, "tenors");
    this._tenors = ImmutableSortedSet.copyOf(tenors);
  }

  @Override
  public CDSIndexTerms.Meta metaBean() {
    return CDSIndexTerms.Meta.INSTANCE;
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
   * Gets the set of tenors.
   * @return the value of the property, not null
   */
  public ImmutableSortedSet<Tenor> getTenors() {
    return _tenors;
  }

  //-----------------------------------------------------------------------
  @Override
  public CDSIndexTerms clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CDSIndexTerms other = (CDSIndexTerms) obj;
      return JodaBeanUtils.equal(getTenors(), other.getTenors());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getTenors());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("CDSIndexTerms{");
    buf.append("tenors").append('=').append(JodaBeanUtils.toString(getTenors()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CDSIndexTerms}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code tenors} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSortedSet<Tenor>> _tenors = DirectMetaProperty.ofImmutable(
        this, "tenors", CDSIndexTerms.class, (Class) ImmutableSortedSet.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "tenors");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -877322829:  // tenors
          return _tenors;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CDSIndexTerms.Builder builder() {
      return new CDSIndexTerms.Builder();
    }

    @Override
    public Class<? extends CDSIndexTerms> beanType() {
      return CDSIndexTerms.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code tenors} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSortedSet<Tenor>> tenors() {
      return _tenors;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -877322829:  // tenors
          return ((CDSIndexTerms) bean).getTenors();
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
   * The bean-builder for {@code CDSIndexTerms}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<CDSIndexTerms> {

    private SortedSet<Tenor> _tenors = new TreeSet<Tenor>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -877322829:  // tenors
          this._tenors = (SortedSet<Tenor>) newValue;
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
    public CDSIndexTerms build() {
      return new CDSIndexTerms(
          _tenors);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("CDSIndexTerms.Builder{");
      buf.append("tenors").append('=').append(JodaBeanUtils.toString(_tenors));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
