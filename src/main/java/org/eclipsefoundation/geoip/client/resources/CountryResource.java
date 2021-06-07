/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.resources;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipsefoundation.geoip.client.helper.CountryHelper;
import org.eclipsefoundation.geoip.client.helper.InetAddressHelper;
import org.eclipsefoundation.geoip.client.model.Error;
import org.eclipsefoundation.geoip.client.service.GeoIPService;

import com.maxmind.geoip2.record.Country;

/**
 * @author martin
 *
 */
@Path("/countries")
@Produces(MediaType.APPLICATION_JSON)
public class CountryResource {

	@Inject
	GeoIPService geoIp;
	@Context
	UriInfo uriInfo;

	@GET
	@Path("/{ipAddr}")
	public Response get(@PathParam("ipAddr") String ipAddr) {
		// validate IP
		if (!InetAddressHelper.validInetAddress(ipAddr)) {
			return new Error(Status.BAD_REQUEST, "Valid IP address must be passed to retrieve location data")
					.asResponse();
		}
		// retrieve cached country data
		Country c = geoIp.getCountry(ipAddr);
		if (c == null) {
			return new Error(Status.INTERNAL_SERVER_ERROR, "Error while retrieving location for IP " + ipAddr)
					.asResponse();
		}
		// return country data
		try {
			return Response.ok(c.toJson()).build();
		} catch (IOException e) {
			throw new RuntimeException("Error while converting country record to JSON", e);
		}
	}

	@GET
	@Path("/all")
	public Response getAll(@QueryParam("locale") String locale) {
		Locale l = new Locale(locale != null ? locale : "en");
		return Response.ok(CountryHelper.getCountries(isValid(l) ? l : null)).build();
	}

	private boolean isValid(Locale locale) {
		try {
			return locale.getISO3Language() != null && locale.getISO3Country() != null;
		} catch (MissingResourceException e) {
			return false;
		}
	}
}
