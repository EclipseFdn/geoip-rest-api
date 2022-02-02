/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.resources;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.eclipsefoundation.geoip.client.test.namespaces.SchemaNamespaceHelper;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Tests for the Subnets resource, which checks and promises the given endpoints
 * with some parameter checking.
 * 
 * @author Martin Lowe
 *
 */
@QuarkusTest
public class SubnetResourceTest {
	public static final String SUBNETS_ENDPOINT_URL = "/subnets/{subnet}/{locale}";

	@Test
	public void testSubnets_success() {
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv4", "ca").then().statusCode(200);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv6", "ca").then().statusCode(200);
	}

	@Test
	void testCities_format() {
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv4", "ca").then().assertThat()
				.body(matchesJsonSchemaInClasspath(SchemaNamespaceHelper.IP_ADDRESSES_SCHEMA_PATH));
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv6", "ca").then().assertThat()
				.body(matchesJsonSchemaInClasspath(SchemaNamespaceHelper.IP_ADDRESSES_SCHEMA_PATH));
	}

	@Test
	public void testSubnetsBadLocaleEndpoint() {
		// bad ipv4 calls
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv4", "").then().statusCode(400);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv4", "can").then().statusCode(400);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv4", "01").then().statusCode(400);

		// bad ipv6 calls
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv6", "").then().statusCode(400);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv6", "can").then().statusCode(400);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv6", "01").then().statusCode(400);

		// check other permutations (regex endpoint)
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv5", "ca").then().statusCode(400);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipvfour", "ca").then().statusCode(400);
		given().when().get(SUBNETS_ENDPOINT_URL, "ipv", "ca").then().statusCode(400);
	}
}
