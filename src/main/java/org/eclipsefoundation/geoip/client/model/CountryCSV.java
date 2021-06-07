/* Copyright (c) 2019 Eclipse Foundation and others.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-v20.html,
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.model;

import com.opencsv.bean.CsvBindByName;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Used to contain data from the countries CSV data file.
 * 
 * @author Martin Lowe
 *
 */
@RegisterForReflection
public class CountryCSV {
	@CsvBindByName(column = "geoname_id")
	private String id;
	@CsvBindByName(column = "country_iso_code")
	private String countryIsoCode;

	public CountryCSV() {
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the countryIsoCode
	 */
	public String getCountryIsoCode() {
		return countryIsoCode;
	}

	/**
	 * @param countryIsoCode the countryIsoCode to set
	 */
	public void setCountryIsoCode(String countryIsoCode) {
		this.countryIsoCode = countryIsoCode;
	}

}
