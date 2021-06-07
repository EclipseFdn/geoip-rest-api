/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.service.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipsefoundation.geoip.client.model.CountryCSV;
import org.eclipsefoundation.geoip.client.model.IPVersion;
import org.eclipsefoundation.geoip.client.model.SubnetRange;
import org.eclipsefoundation.geoip.client.service.NetworkService;

import com.opencsv.bean.CsvToBeanBuilder;

/**
 * Loads Network subnet information via CSV files on the system.
 * 
 * @author Martin Lowe
 *
 */
@ApplicationScoped
public class CSVNetworkService implements NetworkService {

	@ConfigProperty(name = "eclipse.subnet.ipv4.path")
	String ipv4FilePath;
	@ConfigProperty(name = "eclipse.subnet.ipv6.path")
	String ipv6FilePath;
	@ConfigProperty(name = "eclipse.subnet.countries.path")
	String countriesFilePath;

	private Map<String, String> countryIdToIso;
	private Map<String, List<String>> ipv4Subnets;
	private Map<String, List<String>> ipv6Subnets;

	@PostConstruct
	public void init() {
		this.ipv4Subnets = new HashMap<>();
		this.ipv6Subnets = new HashMap<>();
		this.countryIdToIso = new HashMap<>();
		loadCountries(countriesFilePath, countryIdToIso);
		loadMap(ipv4FilePath, ipv4Subnets);
		loadMap(ipv6FilePath, ipv6Subnets);
	}

	@Override
	public List<String> getSubnets(String countryCode, IPVersion ipv) {
		Objects.requireNonNull(countryCode);
		// match on ipversion, and check that we have value. If found, return copy, else
		// return empty list
		if (IPVersion.IPV4.equals(ipv) && ipv4Subnets.containsKey(countryCode)) {
			return new ArrayList<>(ipv4Subnets.get(countryCode.toLowerCase()));
		} else if (IPVersion.IPV6.equals(ipv) && ipv6Subnets.containsKey(countryCode)) {
			return new ArrayList<>(ipv6Subnets.get(countryCode.toLowerCase()));
		}
		return Collections.emptyList();
	}

	private void loadCountries(String filePath, Map<String, String> container) {
		try (FileReader reader = new FileReader(filePath)) {
			// read in all of the country lines as country objects
			List<CountryCSV> countries = new CsvToBeanBuilder<CountryCSV>(reader).withType(CountryCSV.class).build().parse();
			// add each of the countries to the container map, mapping ID to the ISO code
			for (CountryCSV c : countries) {
				container.put(c.getId(), c.getCountryIsoCode().toLowerCase());
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find country CSV file at path " + filePath, e);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading in CSV file at path " + filePath, e);
		}
	}

	private void loadMap(String filePath, Map<String, List<String>> container) {
		try (FileReader reader = new FileReader(filePath)) {
			// read in all of the country lines as country objects
			List<SubnetRange> subnets = new CsvToBeanBuilder<SubnetRange>(reader).withType(SubnetRange.class).build()
					.parse();
			for (SubnetRange subnet : subnets) {
				// for each of the subnet ranges, get the best available ID
				String actualId = subnet.getGeoname() != null ? subnet.getGeoname() : subnet.getRegisteredGeoname();
				// lookup the ISO code for the current entry
				String isoCode = countryIdToIso.get(actualId);
				// if exists, add to subnet range list in map
				if (isoCode != null) {
					container.computeIfAbsent(isoCode, k -> new ArrayList<>()).add(subnet.getNetwork());
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find country CSV file at path " + filePath, e);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading in CSV file at path " + filePath, e);
		}
	}
	
}
