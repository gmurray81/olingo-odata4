/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.odata4.client.core.edm;

import java.util.List;
import org.apache.olingo.odata4.client.api.edm.xml.v4.FunctionImport;
import org.apache.olingo.odata4.client.api.utils.EdmTypeInfo;
import org.apache.olingo.odata4.commons.api.edm.Edm;
import org.apache.olingo.odata4.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.odata4.commons.api.edm.EdmFunction;
import org.apache.olingo.odata4.commons.api.edm.EdmFunctionImport;

public class EdmFunctionImportImpl extends EdmOperationImportImpl implements EdmFunctionImport {

  private final FunctionImport functionImport;

  public EdmFunctionImportImpl(final Edm edm, final EdmEntityContainer container, final String name,
          final FunctionImport functionImport) {

    super(edm, container, name, functionImport.getEntitySet());
    this.functionImport = functionImport;
  }

  @Override
  public EdmFunction getFunction(final List<String> parameterNames) {
    return edm.getFunction(
            new EdmTypeInfo(functionImport.getFunction(), container.getNamespace()).getFullQualifiedName(),
            null, null, parameterNames);
  }

}