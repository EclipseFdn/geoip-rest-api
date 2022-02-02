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
 * Test the listing resource endpoint, using fake data points to test solely the
 * responsiveness of the endpoint.
 * 
 * @author Martin Lowe
 */
@QuarkusTest
public class CountryResourceTest {

	public static final String COUNTRY_ENDPOINT_URL = "/countries/{ipAddr}";

	// Toronto IP address range
	private static final String VALID_IPV4_ADDRESS = "72.137.192.0";
	// Google IE server address
	private static final String VALID_IPV6_ADDRESS = "2a00:1450:400a:804::2004";

	@Test
	public void testCountries_success() {
		given().when().get(COUNTRY_ENDPOINT_URL, VALID_IPV4_ADDRESS).then().statusCode(200);
		given().when().get(COUNTRY_ENDPOINT_URL, VALID_IPV6_ADDRESS).then().statusCode(200);
	}

	@Test
	void testCountries_format() {
		given().when().get(COUNTRY_ENDPOINT_URL, VALID_IPV4_ADDRESS).then().assertThat()
				.body(matchesJsonSchemaInClasspath(SchemaNamespaceHelper.COUNTRY_SCHEMA_PATH));
		given().when().get(COUNTRY_ENDPOINT_URL, VALID_IPV6_ADDRESS).then().assertThat()
				.body(matchesJsonSchemaInClasspath(SchemaNamespaceHelper.COUNTRY_SCHEMA_PATH));
	}

	@Test
	public void testCountries_badIPEndpoint() {
		// IPv4 tests
		given().when().get(COUNTRY_ENDPOINT_URL, "bad.ip.add.res").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "300.0.0.0").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "1.300.0.0").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "1.0.300.0").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "1.0.0.300").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "sample").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, VALID_IPV4_ADDRESS + ":8080").then().statusCode(400);
		// seems to be an issue with Google Guava code, only gets detected by MaxMind
		given().when().get(COUNTRY_ENDPOINT_URL, "0.1.1.1").then().statusCode(500);
		// loopback + unspecified address
		given().when().get(COUNTRY_ENDPOINT_URL, "127.0.0.1").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "0.0.0.0").then().statusCode(400);

		// IPv6 tests
		given().when().get(COUNTRY_ENDPOINT_URL, "bad:ip:add::res").then().statusCode(400);
		// loopback + unspecified address
		given().when().get(COUNTRY_ENDPOINT_URL, "::").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "::1").then().statusCode(400);
	}

	@Test
	void testCountries_loopback() {
		// loopback + unspecified address
		given().when().get(COUNTRY_ENDPOINT_URL, "127.0.0.1").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "0.0.0.0").then().statusCode(400);

		// loopback + unspecified address
		given().when().get(COUNTRY_ENDPOINT_URL, "::").then().statusCode(400);
		given().when().get(COUNTRY_ENDPOINT_URL, "::1").then().statusCode(400);
	}
}
