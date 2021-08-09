package org.eclipsefoundation.geoip.client.config;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.Metadata;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {Metadata.class, MaxMindDbConstructor.class})
public class MaxmindReflectionRegistrationConfig {
}
