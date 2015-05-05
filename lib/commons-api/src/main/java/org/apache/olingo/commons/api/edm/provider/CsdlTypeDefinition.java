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
package org.apache.olingo.commons.api.edm.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;

public class CsdlTypeDefinition extends CsdlAbstractEdmItem implements CsdlNamed, CsdlAnnotatable {

  private static final long serialVersionUID = 3718980071229613048L;

  private String name;

  private FullQualifiedName underlyingType;

  // Facets
  private Integer maxLength;

  private Integer precision;

  private Integer scale;

  private boolean unicode = true;

  private SRID srid;

  private final List<CsdlAnnotation> annotations = new ArrayList<CsdlAnnotation>();

  @Override
  public String getName() {
    return name;
  }

  public CsdlTypeDefinition setName(final String name) {
    this.name = name;
    return this;
  }

  public String getUnderlyingType() {
    if (underlyingType != null) {
      return underlyingType.getFullQualifiedNameAsString();
    }
    return null;
  }

  public CsdlTypeDefinition setUnderlyingType(final String underlyingType) {
    this.underlyingType = new FullQualifiedName(underlyingType);
    return this;
  }

  public CsdlTypeDefinition setUnderlyingType(final FullQualifiedName underlyingType) {
    this.underlyingType = underlyingType;
    return this;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public CsdlTypeDefinition setMaxLength(final Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  public Integer getPrecision() {
    return precision;
  }

  public CsdlTypeDefinition setPrecision(final Integer precision) {
    this.precision = precision;
    return this;
  }

  public Integer getScale() {
    return scale;
  }

  public CsdlTypeDefinition setScale(final Integer scale) {
    this.scale = scale;
    return this;
  }

  public boolean isUnicode() {
    return unicode;
  }

  public CsdlTypeDefinition setUnicode(final boolean unicode) {
    this.unicode = unicode;
    return this;
  }

  public SRID getSrid() {
    return srid;
  }

  public CsdlTypeDefinition setSrid(final SRID srid) {
    this.srid = srid;
    return this;
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return annotations;
  }
}
