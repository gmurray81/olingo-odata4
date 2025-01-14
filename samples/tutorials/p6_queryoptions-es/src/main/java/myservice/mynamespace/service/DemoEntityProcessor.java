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
package myservice.mynamespace.service;

import java.util.List;
import java.util.Locale;

import myservice.mynamespace.data.Storage;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

public class DemoEntityProcessor implements EntityProcessor {

  private OData odata;
  private ServiceMetadata srvMetadata;
  private Storage storage;

  public DemoEntityProcessor(Storage storage) {
    this.storage = storage;
  }

  public void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.srvMetadata = serviceMetadata;
  }


//  /**
//   * DUMMY example implementation
//   * */
//  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
//      throws ODataApplicationException, SerializerException {
//
//		// 1. Analyze the URI
//	  	EdmEntitySet edmEntitySet  = ((UriResourceEntitySet)uriInfo.getUriResourceParts().get(0)).getEntitySet();
//	  	// get the system query option $expand
//		ExpandOption expandOption = uriInfo.getExpandOption();
//
//		// 2. get the data. 
//		
//		// Note: this is FAKE implementation
//		// used for following request: 
//		// http://localhost:8080/DemoService/DemoService.svc/Products(1)?$expand=Category
//		
//		// create hard-coded product entity and set a hard-coded category as inlineEntity for the expand
//	    Entity fakeProductEntity = new Entity().addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 11))
//	    					.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Gamer Mouse"));
//	    fakeProductEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "High end gaming mouse"));
//	    
//	    // create hard-coded category entity (the target of the $expand) 
//	    Entity fakeCategoryEntity = new Entity().addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 22))
//	    					.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Mice"));
//
//	    // create navigation link from product to category
//        Link fakeLink = new Link();
//        fakeLink.setTitle("Category");  // hard-code the name of the navigation property as declared in EdmProvider
//        fakeLink.setInlineEntity(fakeCategoryEntity); // the entity which will be expanded
//        
//        //add the link to the product entity
//        fakeProductEntity.getNavigationLinks().add(fakeLink);
//        
//        // END FAKE
//
//        
//		// 3. serialize
//		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
//		// $expand info is added to the serializer options
//		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).expand(expandOption).build();  
//		ODataSerializer serializer = this.odata.createSerializer(ODataFormat.fromContentType(responseFormat));
//		SerializerResult serializerResult = serializer.entity(srvMetadata, edmEntitySet.getEntityType(), fakeProductEntity, options);
//
//		//4. configure the response object
//		response.setContent(serializerResult.getContent());
//		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
//		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
//  }

  
  
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
	      throws ODataApplicationException, SerializerException {

			// 1. retrieve the Entity Type
			List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
			// Note: only in our example we can assume that the first segment is the EntitySet
			UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
			EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

			// 2. retrieve the data from backend
			List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
			Entity entity = storage.readEntityData(edmEntitySet, keyPredicates);

			// 3. apply system query options
			
			// handle $select
			SelectOption selectOption = uriInfo.getSelectOption();
			// in our example, we don't have performance issues, so we can rely upon the handling in the Olingo lib
			// nothing else to be done
			
			// handle $expand
			ExpandOption expandOption = uriInfo.getExpandOption();
			// in our example: http://localhost:8080/DemoService/DemoService.svc/Categories(1)/$expand=Products
			// or http://localhost:8080/DemoService/DemoService.svc/Products(1)?$expand=Category
			if(expandOption != null){

				// retrieve the EdmNavigationProperty from the expand expression
				// Note: in our example, we have only one NavigationProperty, so we can directly access it
				ExpandItem expandItem = expandOption.getExpandItems().get(0);
				// can be 'Category' or 'Products', no path supported
				UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
				// we don't need to handle error cases, as it is done in the Olingo library
				if(uriResource instanceof UriResourceNavigation){
					EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation)uriResource).getProperty();
					EdmEntityType expandEdmEntityType = edmNavigationProperty.getType();
					String navPropName = edmNavigationProperty.getName();
					
					// build the inline data
					Link link = new Link();
					link.setTitle(navPropName);
					link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);  
					
					if(edmNavigationProperty.isCollection()){ // in case of Categories(1)/$expand=Products
						// fetch the data for the $expand (to-many navigation) from backend
						// here we get the data for the expand
						EntityCollection expandEntityCollection = storage.getRelatedEntityCollection(entity, expandEdmEntityType);
						link.setInlineEntitySet(expandEntityCollection);
					}else{  // in case of Products(1)?$expand=Category
						// fetch the data for the $expand (to-one navigation) from backend
						// here we get the data for the expand
						Entity expandEntity = storage.getRelatedEntity(entity, expandEdmEntityType);
						link.setInlineEntity(expandEntity);
					}
					
					// set the link - containing the expanded data - to the current entity 
					entity.getNavigationLinks().add(link);
				}
			}
			
			
			
			// 4. serialize
			EdmEntityType edmEntityType = edmEntitySet.getEntityType();
			// we need the property names of the $select, in order to build the context URL
			String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
			ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet)
                                                .selectList(selectList)
                                                .suffix(Suffix.ENTITY).build();
		 	
			// make sure that $expand and $select are considered by the serializer
			// adding the selectOption to the serializerOpts will actually tell the lib to do the job
			EntitySerializerOptions opts = EntitySerializerOptions.with()
					.contextURL(contextUrl)
					.select(selectOption)
					.expand(expandOption)
					.build();

			ODataSerializer serializer = this.odata.createSerializer(ODataFormat.fromContentType(responseFormat));
			SerializerResult serializerResult = serializer.entity(srvMetadata, edmEntityType, entity, opts);

			//5. configure the response object
			response.setContent(serializerResult.getContent());
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	  }

  
  
  /*
   * These processor methods are not handled in this tutorial
   */

  public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType requestFormat, ContentType responseFormat)
      throws ODataApplicationException, DeserializerException, SerializerException {
    throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
  }

  public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType requestFormat, ContentType responseFormat)
      throws ODataApplicationException, DeserializerException, SerializerException {
    throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
  }

  public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException {
    throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
  }
}
