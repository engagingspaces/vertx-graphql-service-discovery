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

package io.engagingspaces.graphql.discovery;

import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

/**
 * Interface for registrations managed by a particular {@link Registrar}.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public interface Registration {

    /**
     * @return the service discovery instance this registration is for
     */
    ServiceDiscovery getDiscovery();

    /**
     * Gets the service discovery options that were used to create the service discovery.
     * <p>
     * Service discovery options are required for discovery management in publishers and consumers, but may
     * be {@code null} for un-managed registrations (i.e. those created by invoking `GraphQLService` and `GraphQLClient`
     * directly).
     *
     * @return the service discovery options
     */
    ServiceDiscoveryOptions getDiscoveryOptions();
}
