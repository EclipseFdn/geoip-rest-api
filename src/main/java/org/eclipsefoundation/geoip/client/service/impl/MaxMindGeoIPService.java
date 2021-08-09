/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipsefoundation.geoip.client.service.GeoIPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;

/**
 * Implementation of the Geo-IP service using MaxMind. Built in caching is
 * ignored as its a very light implementation that has no eviction policy and
 * silently stops using cache if it gets full.
 * 
 * @author Martin Lowe
 */
@Startup
@ApplicationScoped
public class MaxMindGeoIPService implements GeoIPService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MaxMindGeoIPService.class);

	@ConfigProperty(name = "maxmind.database.root")
	String dbRoot;
	@ConfigProperty(name = "maxmind.database.city.file")
	String cityFileName;
	@ConfigProperty(name = "maxmind.database.country.file")
	String countryFileName;

	private DatabaseReader countryReader;
	private DatabaseReader cityReader;

	/**
	 * Create instances of DB readers for each available DB
	 */
	@PostConstruct
	public void init() {
		File countryDb = new File(dbRoot + File.separator + countryFileName);
		File cityDb = new File(dbRoot + File.separator + cityFileName);
		// attempt to load db's, throwing up if there is an issue initializing the
		// readers
		try {
			this.countryReader = new DatabaseReader.Builder(countryDb).build();
			this.cityReader = new DatabaseReader.Builder(cityDb).build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@PreDestroy
	public void destroy() {
		try {
			this.countryReader.close();
			this.cityReader.close();
		} catch (IOException e) {
			LOGGER.error("Error while closing Geo IP databases, potential memory leak",e);
		}
	}
	
	@Override
	public City getCity(String ipAddr) {
		try {
			InetAddress addr = InetAddress.getByName(ipAddr);
			CityResponse resp = cityReader.city(addr);
			return resp.getCity();
		} catch (IOException | GeoIp2Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Country getCountry(String ipAddr) {
		try {
			InetAddress addr = InetAddress.getByName(ipAddr);
			CountryResponse resp = countryReader.country(addr);
			return resp.getCountry();
		} catch (IOException | GeoIp2Exception e) {
			throw new RuntimeException(e);
		}
	}

}
