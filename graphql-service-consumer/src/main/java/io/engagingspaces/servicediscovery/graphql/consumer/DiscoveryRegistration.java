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

package io.engagingspaces.servicediscovery.graphql.consumer;

import io.engagingspaces.servicediscovery.graphql.discovery.impl.AbstractRegistration;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

/**
 * Registration for tracking service discovery events of a
 * registered {@link ServiceDiscovery}.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class DiscoveryRegistration extends AbstractRegistration {

    private DiscoveryRegistration(ServiceDiscovery discovery, ServiceDiscoveryOptions options) {
        super(discovery, options);
    }

    /**
     * Creates a new discovery registration.
     *
     * @param discovery  the service discovery instance to register to
     * @param options    the service discovery options with event addresses
     * @return the discovery registration
     */
    public static DiscoveryRegistration create(ServiceDiscovery discovery, ServiceDiscoveryOptions options) {
        return new DiscoveryRegistration(discovery, options);
    }

    /**
     * @param other the object to compare to this published schema
     * @return {@code true} when equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DiscoveryRegistration)) {
            return false;
        }
        DiscoveryRegistration test = (DiscoveryRegistration) other;
        return super.equals(test);
    }

    /**
     * @return the hash code of the discovery registration
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + super.hashCode();
        return result;
    }
}
