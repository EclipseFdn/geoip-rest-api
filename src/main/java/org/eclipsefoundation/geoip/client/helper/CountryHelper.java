/**
 * Copyright (c) 2021 Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Author: Martin Lowe <martin.lowe@eclipse-foundation.org>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipsefoundation.geoip.client.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipsefoundation.geoip.client.model.ISOCountry;

/**
 * Helper to retrieve and cache countries and country codes in a light manner.
 */
public class CountryHelper {
    private static final Map<String, List<ISOCountry>> countryListCache = new HashMap<>();

    public static List<ISOCountry> getCountries(Locale l) {
        // Default to EN locale if not specified
        Locale actualLocale = l != null ? l : Locale.ENGLISH;
        // get the cached data if it exists
        List<ISOCountry> countries = countryListCache.get(actualLocale.getLanguage());
        if (countries == null) {
            // generate cached data
            String[] isoCountries = Locale.getISOCountries();
            countries = new ArrayList<>(isoCountries.length);
            for (String isoCountry : isoCountries) {
                Locale c = new Locale("", isoCountry);
                countries.add(new ISOCountry(c.getDisplayCountry(actualLocale), c.getCountry()));
            }
            countryListCache.put(actualLocale.getLanguage(), countries);
        }
        return new ArrayList<>(countries);
    }

    private CountryHelper() {
    }
}
