/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.magento.automation.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;

import com.magento.api.CatalogProductLinkEntity;

public class DeleteProductLinkTestCases extends MagentoTestParent {

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		try {
			testObjects = (HashMap<String, Object>) context.getBean("deleteProductLink");

			MessageProcessor createProductFlow = lookupFlowConstruct("create-product");
			MuleEvent res = createProductFlow.process(getTestEvent(testObjects));
			Integer productId = (Integer) res.getMessage().getPayload();
			testObjects.put("productId", productId);
			
			// change the sku for the second product
			testObjects.put("sku", ((String) testObjects.get("sku")) + "abc");
			res = createProductFlow.process(getTestEvent(testObjects));
			Integer linkedProductId = (Integer) res.getMessage().getPayload();
			testObjects.put("linkedProductIdOrSku", linkedProductId);
			
			String linkType = "related";
			testObjects.put("type", linkType);
			MessageProcessor addProductLinkFlow = lookupFlowConstruct("add-product-link");
			addProductLinkFlow.process(getTestEvent(testObjects));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	@Category({RegressionTests.class })
	@Test
	public void testDeleteProductLink() {
		try {
			MessageProcessor deleteProductLinkFlow = lookupFlowConstruct("delete-product-link");
			MuleEvent res = deleteProductLinkFlow.process(getTestEvent(testObjects));
			
			assertTrue((Boolean) res.getMessage().getPayload());
			
			MessageProcessor listProductLinkFlow = lookupFlowConstruct("list-product-link");
			res = listProductLinkFlow.process(getTestEvent(testObjects));
			List<CatalogProductLinkEntity> catalogProductLinkEntities = (List<CatalogProductLinkEntity>) res.getMessage().getPayload();
			
			assertEquals("There should be no linked products after deleting the product link", 0, catalogProductLinkEntities.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@After
	public void tearDown() {
		try {
			int productId = (Integer) testObjects.get("productId");
			int linkedProductId = (Integer) testObjects.get("linkedProductIdOrSku");
			deleteProductById(productId);
			deleteProductById(linkedProductId);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
