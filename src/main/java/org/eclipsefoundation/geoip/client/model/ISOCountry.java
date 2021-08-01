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
package org.eclipsefoundation.geoip.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ISOCountry {
    private String name;
    @JsonProperty("iso_code")
    private String isoCode;

    public ISOCountry(String name, String isoCode) {
        this.name = name;
        this.isoCode = isoCode;
    }

    public String getName() {
        return this.name;
    }

    public String getIsoCode() {
        return this.isoCode;
    }
}
