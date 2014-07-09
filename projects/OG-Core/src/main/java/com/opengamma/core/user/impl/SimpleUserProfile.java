/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZoneId;

import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.core.user.UserProfile;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * Simple implementation of {@code UserProfile}.
 * <p>
 * This is the simplest possible implementation of the {@link UserProfile} interface.
 * <p>
 * This class is mutable and not thread-safe.
 * It is intended to primarily be used via the read-only {@code UserProfile} interface.
 */
@BeanDefinition
public class SimpleUserProfile implements Bean, UserProfile, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   */
  @PropertyDefinition(validate = "notNull")
  private String _displayName = "";
  /**
   * The locale that the user prefers.
   */
  @PropertyDefinition(validate = "notNull")
  private Locale _locale = Locale.ENGLISH;
  /**
   * The time-zone used to display local times.
   */
  @PropertyDefinition(validate = "notNull")
  private ZoneId _zone = OpenGammaClock.getZone();
  /**
   * The date format style that the user prefers.
   */
  @PropertyDefinition(validate = "notNull")
  private DateStyle _dateStyle = DateStyle.TEXTUAL_MONTH;
  /**
   * The time formatter that the user prefers.
   */
  @PropertyDefinition(validate = "notNull")
  private TimeStyle _timeStyle = TimeStyle.ISO;
  /**
   * The extended map of profile data.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, String> _extensions = new TreeMap<>();

  //-------------------------------------------------------------------------
  /**
   * Creates a {@code SimpleUserProfile} from another account.
   * <p>
   * Roles and permissions are not copied.
   * 
   * @param profileToCopy  the profile to copy, not null
   * @return the new profile, not null
   */
  public static SimpleUserProfile from(UserProfile profileToCopy) {
    ArgumentChecker.notNull(profileToCopy, "profileToCopy");
    SimpleUserProfile copy = new SimpleUserProfile();
    copy.setDisplayName(profileToCopy.getDisplayName());
    copy.setLocale(profileToCopy.getLocale());
    copy.setZone(profileToCopy.getZone());
    copy.setDateStyle(profileToCopy.getDateStyle());
    copy.setTimeStyle(profileToCopy.getTimeStyle());
    copy.setExtensions(profileToCopy.getExtensions());
    return copy;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a user profile.
   */
  public SimpleUserProfile() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleUserProfile}.
   * @return the meta-bean, not null
   */
  public static SimpleUserProfile.Meta meta() {
    return SimpleUserProfile.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SimpleUserProfile.Meta.INSTANCE);
  }

  @Override
  public SimpleUserProfile.Meta metaBean() {
    return SimpleUserProfile.Meta.INSTANCE;
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
   * Gets the display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * @return the value of the property, not null
   */
  public String getDisplayName() {
    return _displayName;
  }

  /**
   * Sets the display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * @param displayName  the new value of the property, not null
   */
  public void setDisplayName(String displayName) {
    JodaBeanUtils.notNull(displayName, "displayName");
    this._displayName = displayName;
  }

  /**
   * Gets the the {@code displayName} property.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * @return the property, not null
   */
  public final Property<String> displayName() {
    return metaBean().displayName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the locale that the user prefers.
   * @return the value of the property, not null
   */
  public Locale getLocale() {
    return _locale;
  }

  /**
   * Sets the locale that the user prefers.
   * @param locale  the new value of the property, not null
   */
  public void setLocale(Locale locale) {
    JodaBeanUtils.notNull(locale, "locale");
    this._locale = locale;
  }

  /**
   * Gets the the {@code locale} property.
   * @return the property, not null
   */
  public final Property<Locale> locale() {
    return metaBean().locale().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-zone used to display local times.
   * @return the value of the property, not null
   */
  public ZoneId getZone() {
    return _zone;
  }

  /**
   * Sets the time-zone used to display local times.
   * @param zone  the new value of the property, not null
   */
  public void setZone(ZoneId zone) {
    JodaBeanUtils.notNull(zone, "zone");
    this._zone = zone;
  }

  /**
   * Gets the the {@code zone} property.
   * @return the property, not null
   */
  public final Property<ZoneId> zone() {
    return metaBean().zone().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date format style that the user prefers.
   * @return the value of the property, not null
   */
  public DateStyle getDateStyle() {
    return _dateStyle;
  }

  /**
   * Sets the date format style that the user prefers.
   * @param dateStyle  the new value of the property, not null
   */
  public void setDateStyle(DateStyle dateStyle) {
    JodaBeanUtils.notNull(dateStyle, "dateStyle");
    this._dateStyle = dateStyle;
  }

  /**
   * Gets the the {@code dateStyle} property.
   * @return the property, not null
   */
  public final Property<DateStyle> dateStyle() {
    return metaBean().dateStyle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time formatter that the user prefers.
   * @return the value of the property, not null
   */
  public TimeStyle getTimeStyle() {
    return _timeStyle;
  }

  /**
   * Sets the time formatter that the user prefers.
   * @param timeStyle  the new value of the property, not null
   */
  public void setTimeStyle(TimeStyle timeStyle) {
    JodaBeanUtils.notNull(timeStyle, "timeStyle");
    this._timeStyle = timeStyle;
  }

  /**
   * Gets the the {@code timeStyle} property.
   * @return the property, not null
   */
  public final Property<TimeStyle> timeStyle() {
    return metaBean().timeStyle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the extended map of profile data.
   * @return the value of the property, not null
   */
  public Map<String, String> getExtensions() {
    return _extensions;
  }

  /**
   * Sets the extended map of profile data.
   * @param extensions  the new value of the property, not null
   */
  public void setExtensions(Map<String, String> extensions) {
    JodaBeanUtils.notNull(extensions, "extensions");
    this._extensions.clear();
    this._extensions.putAll(extensions);
  }

  /**
   * Gets the the {@code extensions} property.
   * @return the property, not null
   */
  public final Property<Map<String, String>> extensions() {
    return metaBean().extensions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SimpleUserProfile clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleUserProfile other = (SimpleUserProfile) obj;
      return JodaBeanUtils.equal(getDisplayName(), other.getDisplayName()) &&
          JodaBeanUtils.equal(getLocale(), other.getLocale()) &&
          JodaBeanUtils.equal(getZone(), other.getZone()) &&
          JodaBeanUtils.equal(getDateStyle(), other.getDateStyle()) &&
          JodaBeanUtils.equal(getTimeStyle(), other.getTimeStyle()) &&
          JodaBeanUtils.equal(getExtensions(), other.getExtensions());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getDisplayName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLocale());
    hash += hash * 31 + JodaBeanUtils.hashCode(getZone());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDateStyle());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTimeStyle());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExtensions());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("SimpleUserProfile{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("displayName").append('=').append(JodaBeanUtils.toString(getDisplayName())).append(',').append(' ');
    buf.append("locale").append('=').append(JodaBeanUtils.toString(getLocale())).append(',').append(' ');
    buf.append("zone").append('=').append(JodaBeanUtils.toString(getZone())).append(',').append(' ');
    buf.append("dateStyle").append('=').append(JodaBeanUtils.toString(getDateStyle())).append(',').append(' ');
    buf.append("timeStyle").append('=').append(JodaBeanUtils.toString(getTimeStyle())).append(',').append(' ');
    buf.append("extensions").append('=').append(JodaBeanUtils.toString(getExtensions())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleUserProfile}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code displayName} property.
     */
    private final MetaProperty<String> _displayName = DirectMetaProperty.ofReadWrite(
        this, "displayName", SimpleUserProfile.class, String.class);
    /**
     * The meta-property for the {@code locale} property.
     */
    private final MetaProperty<Locale> _locale = DirectMetaProperty.ofReadWrite(
        this, "locale", SimpleUserProfile.class, Locale.class);
    /**
     * The meta-property for the {@code zone} property.
     */
    private final MetaProperty<ZoneId> _zone = DirectMetaProperty.ofReadWrite(
        this, "zone", SimpleUserProfile.class, ZoneId.class);
    /**
     * The meta-property for the {@code dateStyle} property.
     */
    private final MetaProperty<DateStyle> _dateStyle = DirectMetaProperty.ofReadWrite(
        this, "dateStyle", SimpleUserProfile.class, DateStyle.class);
    /**
     * The meta-property for the {@code timeStyle} property.
     */
    private final MetaProperty<TimeStyle> _timeStyle = DirectMetaProperty.ofReadWrite(
        this, "timeStyle", SimpleUserProfile.class, TimeStyle.class);
    /**
     * The meta-property for the {@code extensions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _extensions = DirectMetaProperty.ofReadWrite(
        this, "extensions", SimpleUserProfile.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "displayName",
        "locale",
        "zone",
        "dateStyle",
        "timeStyle",
        "extensions");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1714148973:  // displayName
          return _displayName;
        case -1097462182:  // locale
          return _locale;
        case 3744684:  // zone
          return _zone;
        case -259925341:  // dateStyle
          return _dateStyle;
        case 25596644:  // timeStyle
          return _timeStyle;
        case -1809421292:  // extensions
          return _extensions;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleUserProfile> builder() {
      return new DirectBeanBuilder<SimpleUserProfile>(new SimpleUserProfile());
    }

    @Override
    public Class<? extends SimpleUserProfile> beanType() {
      return SimpleUserProfile.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code displayName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> displayName() {
      return _displayName;
    }

    /**
     * The meta-property for the {@code locale} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Locale> locale() {
      return _locale;
    }

    /**
     * The meta-property for the {@code zone} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZoneId> zone() {
      return _zone;
    }

    /**
     * The meta-property for the {@code dateStyle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DateStyle> dateStyle() {
      return _dateStyle;
    }

    /**
     * The meta-property for the {@code timeStyle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<TimeStyle> timeStyle() {
      return _timeStyle;
    }

    /**
     * The meta-property for the {@code extensions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> extensions() {
      return _extensions;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1714148973:  // displayName
          return ((SimpleUserProfile) bean).getDisplayName();
        case -1097462182:  // locale
          return ((SimpleUserProfile) bean).getLocale();
        case 3744684:  // zone
          return ((SimpleUserProfile) bean).getZone();
        case -259925341:  // dateStyle
          return ((SimpleUserProfile) bean).getDateStyle();
        case 25596644:  // timeStyle
          return ((SimpleUserProfile) bean).getTimeStyle();
        case -1809421292:  // extensions
          return ((SimpleUserProfile) bean).getExtensions();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1714148973:  // displayName
          ((SimpleUserProfile) bean).setDisplayName((String) newValue);
          return;
        case -1097462182:  // locale
          ((SimpleUserProfile) bean).setLocale((Locale) newValue);
          return;
        case 3744684:  // zone
          ((SimpleUserProfile) bean).setZone((ZoneId) newValue);
          return;
        case -259925341:  // dateStyle
          ((SimpleUserProfile) bean).setDateStyle((DateStyle) newValue);
          return;
        case 25596644:  // timeStyle
          ((SimpleUserProfile) bean).setTimeStyle((TimeStyle) newValue);
          return;
        case -1809421292:  // extensions
          ((SimpleUserProfile) bean).setExtensions((Map<String, String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SimpleUserProfile) bean)._displayName, "displayName");
      JodaBeanUtils.notNull(((SimpleUserProfile) bean)._locale, "locale");
      JodaBeanUtils.notNull(((SimpleUserProfile) bean)._zone, "zone");
      JodaBeanUtils.notNull(((SimpleUserProfile) bean)._dateStyle, "dateStyle");
      JodaBeanUtils.notNull(((SimpleUserProfile) bean)._timeStyle, "timeStyle");
      JodaBeanUtils.notNull(((SimpleUserProfile) bean)._extensions, "extensions");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}