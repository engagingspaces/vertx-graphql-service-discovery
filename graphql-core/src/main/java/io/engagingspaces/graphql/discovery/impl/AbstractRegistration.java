/*
 * Copyright (c) 2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.engagingspaces.servicediscovery.graphql.discovery.impl;

import io.engagingspaces.servicediscovery.graphql.discovery.Registration;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.Objects;

/**
 * Base class for registration types.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class AbstractRegistration implements Registration {

    private final ServiceDiscovery discovery;
    private final ServiceDiscoveryOptions options;

    protected AbstractRegistration(ServiceDiscovery discovery, ServiceDiscoveryOptions options) {
        Objects.requireNonNull(discovery, "Service discovery cannot be null");
        this.discovery = discovery;
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDiscovery getDiscovery() {
        return discovery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDiscoveryOptions getDiscoveryOptions() {
        return options == null ? null : new ServiceDiscoveryOptions(options);
    }

    /**
     * @param other the object to compare to this registration
     * @return {@code true} when equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AbstractRegistration)) {
            return false;
        }
        AbstractRegistration test = (AbstractRegistration) other;
        return fieldEquals(discovery, test.discovery) && fieldEquals(options.getName(), test.options.getName());
    }

    private static boolean fieldEquals(Object value1, Object value2) {
        return value1 == null ? value2 == null : value1.equals(value2);
    }

    /**
     * @return the hash code of the registration
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (discovery == null ? 0 : discovery.hashCode());
        result = 31 * result + (options.getName() == null ? 0 : options.getName().hashCode());
        return result;
    }
}
