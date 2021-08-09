/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipsefoundation.geoip.client.model.IPVersion;
import org.eclipsefoundation.geoip.client.service.NetworkService;

/**
 * Provides subnets for given country codes, in both ipv4 and ipv6 formats upon
 * request.
 * 
 * @author Martin Lowe
 */
@Path("/subnets")
@Produces(MediaType.APPLICATION_JSON)
public class SubnetResource {

	@Inject
	NetworkService networks;

	@GET
	@Path("/{ipv: (ipv[46])}/{isoLocale: ([a-zA-Z]{2})}")
	public Response get(@PathParam("isoLocale") String isoLocale, @PathParam("ipv") String ipVersion) {
		return Response.ok(networks.getSubnets(isoLocale, IPVersion.getByName(ipVersion))).build();
	}
}
