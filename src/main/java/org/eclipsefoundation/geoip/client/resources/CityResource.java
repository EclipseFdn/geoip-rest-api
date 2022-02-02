/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.resources;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.maxmind.geoip2.record.City;

import org.eclipsefoundation.core.model.Error;
import org.eclipsefoundation.geoip.client.helper.InetAddressHelper;
import org.eclipsefoundation.geoip.client.service.GeoIPService;

/**
 * @author martin
 *
 */
@Path("/cities")
@Produces(MediaType.APPLICATION_JSON)
public class CityResource {

	@Inject
	GeoIPService geoIp;

	@GET
	@Path("/{ipAddr}")
	public Response get(@PathParam("ipAddr") String ipAddr) {
		// validate IP
		if (ipAddr == null || !InetAddressHelper.validInetAddress(ipAddr)) {
			return new Error(Status.BAD_REQUEST, "Valid IP address must be passed to retrieve location data")
					.asResponse();
		}
		// retrieve cached city data
		City c = geoIp.getCity(ipAddr);
		if (c == null) {
			return new Error(Status.INTERNAL_SERVER_ERROR, "Error while retrieving location for IP " + ipAddr)
					.asResponse();
		}
		// return city data
		try {
			return Response.ok(c.toJson()).build();
		} catch (IOException e) {
			throw new RuntimeException("Error while converting city record to JSON", e);
		}
	}
}
