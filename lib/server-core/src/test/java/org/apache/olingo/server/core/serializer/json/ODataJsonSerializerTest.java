/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.serializer.json;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.core.data.EntityImpl;
import org.apache.olingo.commons.core.data.EntitySetImpl;
import org.apache.olingo.commons.core.data.PropertyImpl;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ODataJsonSerializerTest {

  private static final String ETAllPrim = "ETAllPrim";
  private static final String ETCompAllPrim = "ETCompAllPrim";
  private static final String ETCollAllPrim = "ETCollAllPrim";
  private static final String CTAllPrim = "CTAllPrim";
  private static final String CTAllPrim_Type = "com.sap.odata.test1.CTAllPrim";

  interface TecSvcProperty {
    String getName();
    String getTypeName();
    EdmPrimitiveTypeKind getType();
    boolean isCollection();
  }

  public static class TecSvcComplexProperty implements TecSvcProperty {

    final String typeName;
    final String name;
    final List<EdmProperty> properties = new ArrayList<EdmProperty>();
    boolean collection = false;

    public TecSvcComplexProperty(String typeName, String name) {
      this.typeName = typeName;
      this.name = name;
    }
    TecSvcProperty addProperties(List<EdmProperty> properties) {
      this.properties.addAll(properties);
      return this;
    }
    TecSvcProperty asCollection() {
      this.collection = true;
      return this;
    }
    @Override
    public boolean isCollection() {
      return collection;
    }
    @Override
    public String getName() {
      return name;
    }
    @Override
    public String getTypeName() {
      return typeName;
    }
    @Override
    public EdmPrimitiveTypeKind getType() {
      return null;
    }
    public List<EdmProperty> getProperties() {
      return properties;
    }
  }

  enum TecSvcSimpleProperty implements TecSvcProperty {
    Int16("PropertyInt16", EdmPrimitiveTypeKind.Int16),
    String("PropertyString", EdmPrimitiveTypeKind.String),
    Boolean("PropertyBoolean", EdmPrimitiveTypeKind.Boolean),
    Byte("PropertyByte", EdmPrimitiveTypeKind.Byte),
    SByte("PropertySByte", EdmPrimitiveTypeKind.SByte),
    Int32("PropertyInt32", EdmPrimitiveTypeKind.Int32),
    Int64("PropertyInt64", EdmPrimitiveTypeKind.Int64),
    Single("PropertySingle", EdmPrimitiveTypeKind.Single),
    Double("PropertyDouble", EdmPrimitiveTypeKind.Double),
    Decimal("PropertyDecimal", EdmPrimitiveTypeKind.Decimal),
    Binary("PropertyBinary", EdmPrimitiveTypeKind.Binary),
    Date("PropertyDate", EdmPrimitiveTypeKind.Date),
    DateTimeOffset("PropertyDateTimeOffset", EdmPrimitiveTypeKind.DateTimeOffset),
    Duration("PropertyDuration", EdmPrimitiveTypeKind.Duration),
    Guid("PropertyGuid", EdmPrimitiveTypeKind.Guid),
    TimeOfDay("PropertyTimeOfDay", EdmPrimitiveTypeKind.TimeOfDay),
    //  <NavigationProperty Name="NavPropertyETTwoPrimOne" Type="test1.ETTwoPrim" Nullable="false"/>
//    NavETTwoPrimOne = "NavPropertyETTwoPrimOne", EdmPrimitiveTypeKind.),
    //  <NavigationProperty Name="NavPropertyETTwoPrimMany" Type="Collection(test1.ETTwoPrim)" Nullable="false"/>
//    NavETTwoPrimMany("NavPropertyETTwoPrimMany", EdmCom.);
    Collection_Int16("CollPropertyInt16", EdmPrimitiveTypeKind.Int16, true),
    Collection_String("CollPropertyString", EdmPrimitiveTypeKind.String, true),
    Collection_Boolean("CollPropertyBoolean", EdmPrimitiveTypeKind.Boolean, true),
    Collection_Byte("CollPropertyByte", EdmPrimitiveTypeKind.Byte, true),
    Collection_SByte("CollPropertySByte", EdmPrimitiveTypeKind.SByte, true),
    Collection_Int32("CollPropertyInt32", EdmPrimitiveTypeKind.Int32, true),
    Collection_Int64("CollPropertyInt64", EdmPrimitiveTypeKind.Int64, true),
    Collection_Single("CollPropertySingle", EdmPrimitiveTypeKind.Single, true),
    Collection_Double("CollPropertyDouble", EdmPrimitiveTypeKind.Double, true),
    Collection_Decimal("CollPropertyDecimal", EdmPrimitiveTypeKind.Decimal, true),
    Collection_Binary("CollPropertyBinary", EdmPrimitiveTypeKind.Binary, true),
    Collection_Date("CollPropertyDate", EdmPrimitiveTypeKind.Date, true),
    Collection_DateTimeOffset("CollPropertyDateTimeOffset", EdmPrimitiveTypeKind.DateTimeOffset, true),
    Collection_Duration("CollPropertyDuration", EdmPrimitiveTypeKind.Duration, true),
    Collection_Guid("CollPropertyGuid", EdmPrimitiveTypeKind.Guid, true),
    Collection_TimeOfDay("CollPropertyTimeOfDay", EdmPrimitiveTypeKind.TimeOfDay, true);

    final String name;
    final EdmPrimitiveTypeKind type;
    final boolean isCollection;

    TecSvcSimpleProperty(String name, EdmPrimitiveTypeKind type) {
      this(name, type, false);
    }
    TecSvcSimpleProperty(String name, EdmPrimitiveTypeKind type, boolean collection) {
      this.name = name;
      this.type = type;
      this.isCollection = collection;
    }
    @Override
    public String getTypeName() {
      return type.name();
    }
    @Override
    public EdmPrimitiveTypeKind getType() {
      return type;
    }
    @Override
    public String getName() {
      return name;
    }
    @Override
    public boolean isCollection() {
      return isCollection;
    }
  }

  private EdmEntitySet edmESAllPrim;
  private EdmEntitySet edmESCompAllPrim;
  private EdmEntitySet edmESCollAllPrim;
  private EdmEntityType edmETAllPrim;
  private EdmEntityType edmETCompAllPrim;
  private EdmEntityType edmETCollAllPrim;

  private ODataJsonSerializer serializer = new ODataJsonSerializer();

  @Before
  public void prepare() throws Exception {
    // entity all primitive
    edmETAllPrim = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmETAllPrim.getName()).thenReturn(ETAllPrim);
    List<EdmProperty> properties = Arrays.asList(
        mockProperty(TecSvcSimpleProperty.Int16, false),
        mockProperty(TecSvcSimpleProperty.String),
        mockProperty(TecSvcSimpleProperty.Boolean),
        mockProperty(TecSvcSimpleProperty.Byte),
        mockProperty(TecSvcSimpleProperty.SByte),
        mockProperty(TecSvcSimpleProperty.Int32),
        mockProperty(TecSvcSimpleProperty.Int64),
        mockProperty(TecSvcSimpleProperty.Single),
        mockProperty(TecSvcSimpleProperty.Double),
        mockProperty(TecSvcSimpleProperty.Decimal),
        mockProperty(TecSvcSimpleProperty.Binary),
        mockProperty(TecSvcSimpleProperty.Date),
        mockProperty(TecSvcSimpleProperty.DateTimeOffset),
        mockProperty(TecSvcSimpleProperty.Duration),
        mockProperty(TecSvcSimpleProperty.Guid),
        mockProperty(TecSvcSimpleProperty.TimeOfDay)
//        mockProperty(NavPropertyETTwoPrimOne, false),
//        mockProperty(NavPropertyETTwoPrimMany, false)
    );
    List<String> propertyNames = new ArrayList<String>();

    for (EdmProperty property : properties) {
      propertyNames.add(property.getName());
      Mockito.when(edmETAllPrim.getProperty(property.getName())).thenReturn(property);
    }
    Mockito.when(edmETAllPrim.getPropertyNames()).thenReturn(propertyNames);

    // Entity Set All Primitive
    edmESAllPrim = Mockito.mock(EdmEntitySet.class);
    Mockito.when(edmESAllPrim.getName()).thenReturn("ESAllPrim");
    Mockito.when(edmESAllPrim.getEntityType()).thenReturn(edmETAllPrim);
    // Entity Set All Primitive
    edmESCompAllPrim = Mockito.mock(EdmEntitySet.class);
    Mockito.when(edmESCompAllPrim.getName()).thenReturn("ESCompAllPrim");
    Mockito.when(edmESCompAllPrim.getEntityType()).thenReturn(edmETCompAllPrim);
    // Entity Set All Primitive
    edmESCollAllPrim = Mockito.mock(EdmEntitySet.class);
    Mockito.when(edmESCollAllPrim.getName()).thenReturn("ESCollAllPrim");
    Mockito.when(edmESCollAllPrim.getEntityType()).thenReturn(edmETCollAllPrim);

    // Entity Type Complex All Primitive
    edmETCompAllPrim = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmETCompAllPrim.getName()).thenReturn(ETCompAllPrim);
    List<EdmProperty> capProperties = Arrays.asList(
        mockProperty(TecSvcSimpleProperty.Int16, false),
        mockProperty(new TecSvcComplexProperty(CTAllPrim_Type, CTAllPrim).addProperties(properties), false)
    );
    List<String> capPropertyNames = new ArrayList<String>();

    for (EdmProperty property : capProperties) {
      capPropertyNames.add(property.getName());
      Mockito.when(edmETCompAllPrim.getProperty(property.getName())).thenReturn(property);
    }
    Mockito.when(edmETCompAllPrim.getPropertyNames()).thenReturn(capPropertyNames);

    // entity type all primitive collections
    //
    edmETCollAllPrim = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmETCollAllPrim.getName()).thenReturn(ETCollAllPrim);
    List<EdmProperty> allCollProperties = Arrays.asList(
            mockProperty(TecSvcSimpleProperty.Int16, false),
            mockProperty(TecSvcSimpleProperty.Collection_String),
            mockProperty(TecSvcSimpleProperty.Collection_Boolean),
            mockProperty(TecSvcSimpleProperty.Collection_Byte),
            mockProperty(TecSvcSimpleProperty.Collection_SByte),
            mockProperty(TecSvcSimpleProperty.Collection_Int32),
            mockProperty(TecSvcSimpleProperty.Collection_Int64),
            mockProperty(TecSvcSimpleProperty.Collection_Single),
            mockProperty(TecSvcSimpleProperty.Collection_Double),
            mockProperty(TecSvcSimpleProperty.Collection_Decimal),
            mockProperty(TecSvcSimpleProperty.Collection_Binary),
            mockProperty(TecSvcSimpleProperty.Collection_Date),
            mockProperty(TecSvcSimpleProperty.Collection_DateTimeOffset),
            mockProperty(TecSvcSimpleProperty.Collection_Duration),
            mockProperty(TecSvcSimpleProperty.Collection_Guid),
            mockProperty(TecSvcSimpleProperty.Collection_TimeOfDay)
    );
    List<String> etCollAllPrimPropertyNames = new ArrayList<String>();

    for (EdmProperty property : allCollProperties) {
      etCollAllPrimPropertyNames.add(property.getName());
      Mockito.when(edmETCollAllPrim.getProperty(property.getName())).thenReturn(property);
    }
    Mockito.when(edmETCollAllPrim.getPropertyNames()).thenReturn(etCollAllPrimPropertyNames);

    // Entity Set all primitive collection
  }

  private EdmProperty mockProperty(TecSvcProperty name) {
    return mockProperty(name, true);
  }

  private EdmProperty mockProperty(TecSvcProperty tecProperty, boolean nullable) {
    EdmProperty edmElement = Mockito.mock(EdmProperty.class);
    Mockito.when(edmElement.getName()).thenReturn(tecProperty.getName());
    if (tecProperty instanceof TecSvcComplexProperty) {
      TecSvcComplexProperty complexProperty = (TecSvcComplexProperty) tecProperty;
      Mockito.when(edmElement.isPrimitive()).thenReturn(false);
      EdmComplexType type = Mockito.mock(EdmComplexType.class);
      Mockito.when(type.getKind()).thenReturn(EdmTypeKind.COMPLEX);
      Mockito.when(type.getName()).thenReturn(tecProperty.getTypeName());
      Mockito.when(edmElement.getType()).thenReturn(type);

      List<String> propertyNames = new ArrayList<String>();
      List<EdmProperty> properties = complexProperty.getProperties();
      for (EdmProperty property : properties) {
        propertyNames.add(property.getName());
        Mockito.when(type.getProperty(property.getName())).thenReturn(property);
      }
      Mockito.when(type.getPropertyNames()).thenReturn(propertyNames);
    } else {
      Mockito.when(edmElement.isPrimitive()).thenReturn(true);
      // TODO: set default values
      Mockito.when(edmElement.getMaxLength()).thenReturn(40);
      Mockito.when(edmElement.getPrecision()).thenReturn(10);
      Mockito.when(edmElement.getScale()).thenReturn(10);
      Mockito.when(edmElement.getType()).thenReturn(EdmPrimitiveTypeFactory.getInstance(tecProperty.getType()));
    }
    Mockito.when(edmElement.isCollection()).thenReturn(tecProperty.isCollection());
    Mockito.when(edmElement.isNullable()).thenReturn(nullable);
    return edmElement;
  }

  private PropertyImpl createProperty(TecSvcProperty property, ValueType vType, Object value) {
    return new PropertyImpl(property.getTypeName(), property.getName(), vType, value);
  }

  private PropertyImpl createProperty(String type, TecSvcSimpleProperty property, ValueType vType, Object ... value) {
    final Object propValue;
    if(value == null || value.length ==0) {
      propValue = null;
    } else if(property.isCollection()) {
      propValue = Arrays.asList(value);
    } else {
      propValue = value[0];
    }
    return new PropertyImpl(type, property.name, vType, propValue);
  }

  @Test
  public void entitySimple() throws Exception {
    Entity entity = createETAllPrim();

    InputStream result = serializer.entity(edmETAllPrim, entity, createContextURL(edmESAllPrim, true));
    String resultString = streamToString(result);
    String expectedResult = "{" +
        "\"@odata.context\":\"$metadata#ESAllPrim/$entity\"," +
        "\"PropertyInt16\":4711," +
        "\"PropertyString\":\"StringValue\"," +
        "\"PropertyBoolean\":true," +
        "\"PropertyByte\":19," +
        "\"PropertySByte\":1," +
        "\"PropertyInt32\":2147483647," +
        "\"PropertyInt64\":9223372036854775807," +
        "\"PropertySingle\":47.11," +
        "\"PropertyDouble\":4.711," +
        "\"PropertyDecimal\":4711.1174," +
        "\"PropertyBinary\":\"BAcBAQ==\"," +
        "\"PropertyDate\":\"2014-03-19\"," +
        "\"PropertyDateTimeOffset\":\"2014-03-19T10:12:00Z\"," +
        "\"PropertyDuration\":\"P16148425DT0S\"," +
        "\"PropertyGuid\":\"0000aaaa-00bb-00cc-00dd-000000ffffff\"," +
        "\"PropertyTimeOfDay\":\"10:12:00\"" +
        "}";
    Assert.assertEquals(expectedResult, resultString);
  }

  @Test
  public void entitySetETAllPrim() throws Exception {
    EdmEntitySet edmEntitySet = edmESAllPrim;
    EntitySetImpl entitySet = new EntitySetImpl();
    for (int i = 0; i < 100; i++) {
      entitySet.getEntities().add(createETAllPrim(i));
    }
    entitySet.setCount(entitySet.getEntities().size());
    ContextURL contextUrl = createContextURL(edmESAllPrim, false);
    entitySet.setNext(URI.create(contextUrl.getURI().toASCIIString() + "/next"));

    InputStream result = serializer.entitySet(edmEntitySet, entitySet, contextUrl);
    String resultString = streamToString(result);

    Assert.assertTrue(resultString.matches("\\{" +
        "\"@odata\\.context\":\"\\$metadata#ESAllPrim\"," +
        "\"@odata\\.count\":100," +
        "\"value\":\\[.*\\]," +
        "\"@odata\\.nextLink\":\"\\$metadata#ESAllPrim/next\"" +
        "\\}"));

    Matcher matcher = Pattern.compile("(\\{[a-z0-9:\\=\"\\-,\\.\\+]*\\})",
        Pattern.CASE_INSENSITIVE).matcher(resultString);
    int count = 0;
    while(matcher.find()) {
      Assert.assertTrue(matcher.group().contains("PropertyInt16\":" + count++));
    }
    Assert.assertEquals(100, count);
  }

  @Test
  public void entityCollAllPrim() throws Exception {
    Entity entity = createETCollAllPrim(4711);

    ContextURL contextUrl = createContextURL(edmESCollAllPrim, true);
    InputStream result = serializer.entity(edmETCollAllPrim, entity, contextUrl);
    String resultString = streamToString(result);
    String expectedResult = "{" +
        "\"@odata.context\":\"$metadata#ESCollAllPrim/$entity\"," +
        "\"PropertyInt16\":4711," +
        "\"CollPropertyString\":[\"StringValue_1\",\"StringValue_2\"]," +
        "\"CollPropertyBoolean\":[true,false]," +
        "\"CollPropertyByte\":[19,42]," +
        "\"CollPropertySByte\":[1,2]," +
        "\"CollPropertyInt32\":[2147483647,-2147483648]," +
        "\"CollPropertyInt64\":[9223372036854775807,-9223372036854775808]," +
        "\"CollPropertySingle\":[47.11,11.47]," +
        "\"CollPropertyDouble\":[4.711,711.4]," +
        "\"CollPropertyDecimal\":[4711.1174,1174.4711]," +
        "\"CollPropertyBinary\":[\"BAcBAQ==\",\"dGVzdA==\"]," +
        "\"CollPropertyDate\":[\"2014-03-19\",\"2014-07-02\"]," +
        "\"CollPropertyDateTimeOffset\":[\"2014-03-19T10:12:00Z\",\"2014-07-02T13:30:00Z\"]," +
        "\"CollPropertyDuration\":[\"P16148425DT0S\",\"P16253562DT12H0S\"]," +
        "\"CollPropertyGuid\":[\"0000aaaa-00bb-00cc-00dd-000000ffffff\",\"0000ffff-00dd-00cc-00bb-000000aaaaaa\"]," +
        "\"CollPropertyTimeOfDay\":[\"10:12:00\",\"13:30:00\"]" +
        "}";
    Assert.assertEquals(expectedResult, resultString);
  }

  @Test
  public void entityETCompAllPrim() throws Exception {
    Entity complexCtAllPrim = createETAllPrim();

    Entity entity = new EntityImpl();
    entity.addProperty(new PropertyImpl("Edm.Int16", TecSvcSimpleProperty.Int16.name, ValueType.PRIMITIVE, 4711));
    entity.addProperty(createProperty(
            new TecSvcComplexProperty(CTAllPrim_Type, CTAllPrim),
            ValueType.COMPLEX, complexCtAllPrim.getProperties()));

    ContextURL contextUrl = createContextURL(edmESCompAllPrim, true);
    InputStream result = serializer.entity(edmETCompAllPrim, entity, contextUrl);
    String resultString = streamToString(result);
    String expectedResult = "{" +
        "\"@odata.context\":\"$metadata#ESCompAllPrim/$entity\"," +
        "\"PropertyInt16\":4711," +
        "\"CTAllPrim\":{" +
        "\"PropertyInt16\":4711," +
        "\"PropertyString\":\"StringValue\"," +
        "\"PropertyBoolean\":true," +
        "\"PropertyByte\":19," +
        "\"PropertySByte\":1," +
        "\"PropertyInt32\":2147483647," +
        "\"PropertyInt64\":9223372036854775807," +
        "\"PropertySingle\":47.11," +
        "\"PropertyDouble\":4.711," +
        "\"PropertyDecimal\":4711.1174," +
        "\"PropertyBinary\":\"BAcBAQ==\"," +
        "\"PropertyDate\":\"2014-03-19\"," +
        "\"PropertyDateTimeOffset\":\"2014-03-19T10:12:00Z\"," +
        "\"PropertyDuration\":\"P16148425DT0S\"," +
        "\"PropertyGuid\":\"0000aaaa-00bb-00cc-00dd-000000ffffff\"," +
        "\"PropertyTimeOfDay\":\"10:12:00\"" +
        "}}";
    Assert.assertEquals(expectedResult, resultString);
  }

  private ContextURL createContextURL(EdmEntitySet entitySet, boolean isEntity) throws URISyntaxException {
    StringBuilder sb = new StringBuilder("$metadata#" + entitySet.getName());
    if(isEntity) {
      sb.append("/$entity");
    }
    return ContextURL.getInstance(new URI(sb.toString()));
  }

  private Entity createETAllPrim() {
    return createETAllPrim(4711);
  }

  private Entity createETCollAllPrim(int id) {
    Entity entity = new EntityImpl();
    Calendar date = createCalendarInstance();
    date.set(2014, Calendar.MARCH, 19, 10, 12, 0);
    date.set(Calendar.MILLISECOND, 0);
    Calendar date2 = createCalendarInstance();
    date2.set(2014, Calendar.JULY, 2, 13, 30, 0);
    date2.set(Calendar.MILLISECOND, 0);
    //
    entity.addProperty(createProperty("Edm.Int16", TecSvcSimpleProperty.Int16, ValueType.PRIMITIVE, id));
    //
    entity.addProperty(createProperty("Collection(Edm.Int16)", TecSvcSimpleProperty.Collection_Int16,
            ValueType.COLLECTION_PRIMITIVE, id));
    entity.addProperty(createProperty("Collection(Edm.String)", TecSvcSimpleProperty.Collection_String,
            ValueType.COLLECTION_PRIMITIVE, "StringValue_1", "StringValue_2"));
    entity.addProperty(createProperty("Collection(Edm.Boolean)", TecSvcSimpleProperty.Collection_Boolean,
            ValueType.COLLECTION_PRIMITIVE, Boolean.TRUE, Boolean.FALSE));
    entity.addProperty(createProperty("Collection(Edm.Byte)", TecSvcSimpleProperty.Collection_Byte,
            ValueType.COLLECTION_PRIMITIVE, Byte.valueOf("19"), Byte.valueOf("42")));
    entity.addProperty(createProperty("Collection(Edm.SByte)", TecSvcSimpleProperty.Collection_SByte,
            ValueType.COLLECTION_PRIMITIVE, Short.valueOf("1"), Short.valueOf("2")));
    entity.addProperty(createProperty("Collection(Edm.Int32)", TecSvcSimpleProperty.Collection_Int32,
            ValueType.COLLECTION_PRIMITIVE, Integer.MAX_VALUE, Integer.MIN_VALUE));
    entity.addProperty(createProperty("Collection(Edm.Int64)", TecSvcSimpleProperty.Collection_Int64,
            ValueType.COLLECTION_PRIMITIVE, Long.MAX_VALUE, Long.MIN_VALUE));
    entity.addProperty(createProperty("Collection(Edm.Single)", TecSvcSimpleProperty.Collection_Single,
            ValueType.COLLECTION_PRIMITIVE, 47.11, 11.47));
    entity.addProperty(createProperty("Collection(Edm.Double)", TecSvcSimpleProperty.Collection_Double,
            ValueType.COLLECTION_PRIMITIVE, 4.711, 711.4));
    entity.addProperty(createProperty("Collection(Edm.Decimal)", TecSvcSimpleProperty.Collection_Decimal,
            ValueType.COLLECTION_PRIMITIVE, 4711.1174, 1174.4711));
    entity.addProperty(createProperty("Collection(Edm.Binary)", TecSvcSimpleProperty.Collection_Binary,
            ValueType.COLLECTION_PRIMITIVE, new byte[]{0x04, 0x07, 0x01, 0x01}, "test".getBytes()));
    entity.addProperty(createProperty("Collection(Edm.Date)", TecSvcSimpleProperty.Collection_Date,
            ValueType.COLLECTION_PRIMITIVE, date, date2));
    entity.addProperty(createProperty("Collection(Edm.DateTimeOffset)", TecSvcSimpleProperty.Collection_DateTimeOffset,
            ValueType.COLLECTION_PRIMITIVE, date, date2));
    entity.addProperty(createProperty("Collection(Edm.Duration)", TecSvcSimpleProperty.Collection_Duration,
            ValueType.COLLECTION_PRIMITIVE, date.getTimeInMillis(), date2.getTimeInMillis()));
    entity.addProperty(createProperty("Collection(Edm.Guid)", TecSvcSimpleProperty.Collection_Guid,
            ValueType.COLLECTION_PRIMITIVE,
            UUID.fromString("AAAA-BB-CC-DD-FFFFFF"),
            UUID.fromString("FFFF-DD-CC-BB-AAAAAA")));
    entity.addProperty(createProperty("Collection(Edm.TimeOfDay)", TecSvcSimpleProperty.Collection_TimeOfDay,
            ValueType.COLLECTION_PRIMITIVE, date, date2));
    return entity;
  }

  private Entity createETAllPrim(int id) {
    Entity entity = new EntityImpl();
    Calendar date = createCalendarInstance();
    date.set(2014, Calendar.MARCH, 19, 10, 12, 0);
    date.set(Calendar.MILLISECOND, 0);
    entity.addProperty(createProperty("Edm.Int16", TecSvcSimpleProperty.Int16, ValueType.PRIMITIVE, id));
    entity.addProperty(createProperty("Edm.String", TecSvcSimpleProperty.String, ValueType.PRIMITIVE, "StringValue"));
    entity.addProperty(createProperty("Edm.Boolean", TecSvcSimpleProperty.Boolean, ValueType.PRIMITIVE, Boolean.TRUE));
    entity.addProperty(createProperty("Edm.Byte", TecSvcSimpleProperty.Byte, ValueType.PRIMITIVE, Byte.valueOf("19")));
    entity.addProperty(createProperty("Edm.SByte",
            TecSvcSimpleProperty.SByte, ValueType.PRIMITIVE, Short.valueOf("1")));
    entity.addProperty(createProperty("Edm.Int32",
            TecSvcSimpleProperty.Int32, ValueType.PRIMITIVE, Integer.MAX_VALUE));
    entity.addProperty(createProperty("Edm.Int64", TecSvcSimpleProperty.Int64, ValueType.PRIMITIVE, Long.MAX_VALUE));
    entity.addProperty(createProperty("Edm.Single", TecSvcSimpleProperty.Single, ValueType.PRIMITIVE, 47.11));
    entity.addProperty(createProperty("Edm.Double", TecSvcSimpleProperty.Double, ValueType.PRIMITIVE, 4.711));
    entity.addProperty(createProperty("Edm.Decimal", TecSvcSimpleProperty.Decimal, ValueType.PRIMITIVE, 4711.1174));
    entity.addProperty(createProperty("Edm.Binary", TecSvcSimpleProperty.Binary, ValueType.PRIMITIVE,
        new byte[]{0x04, 0x07, 0x01, 0x01}));
    entity.addProperty(createProperty("Edm.Date", TecSvcSimpleProperty.Date, ValueType.PRIMITIVE, date));
    entity.addProperty(createProperty("Edm.DateTimeOffset", TecSvcSimpleProperty.DateTimeOffset, ValueType.PRIMITIVE,
        date));
    entity.addProperty(createProperty("Edm.Duration", TecSvcSimpleProperty.Duration, ValueType.PRIMITIVE,
        date.getTimeInMillis()));
    entity.addProperty(createProperty("Edm.Guid", TecSvcSimpleProperty.Guid, ValueType.PRIMITIVE,
        UUID.fromString("AAAA-BB-CC-DD-FFFFFF")));
    entity.addProperty(createProperty("Edm.TimeOfDay", TecSvcSimpleProperty.TimeOfDay, ValueType.PRIMITIVE, date));
    return entity;
  }

  private Calendar createCalendarInstance() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
    cal.set(Calendar.ZONE_OFFSET, 0);
    return cal;
  }

  private String streamToString(InputStream result) throws IOException {
    byte[] buffer = new byte[8192];
    StringBuilder sb = new StringBuilder();

    int count = result.read(buffer);
    while (count >= 0) {
      sb.append(new String(buffer, 0, count, "UTF-8"));
      count = result.read(buffer);
    }

    return sb.toString();
  }
}