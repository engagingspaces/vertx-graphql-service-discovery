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

package io.engagingspaces.graphql.discovery.impl;

import io.engagingspaces.graphql.discovery.Registrar;
import io.engagingspaces.graphql.discovery.Registration;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Action;
import io.vertx.core.impl.VertxInternal;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.*;

/**
 * Base class for registrars.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 * @param <T> the registration type to use
 */
public abstract class AbstractRegistrar<T extends Registration> implements Registrar {

    protected final Vertx vertx;
    private final Map<String, ManagedServiceDiscovery> serviceDiscoveries;
    private final Map<T, String> registrationMap;

    protected AbstractRegistrar(Vertx vertx) {
        this.vertx = vertx;
        this.serviceDiscoveries = new HashMap<>();
        this.registrationMap = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> serviceDiscoveryNames() {
        return Collections.unmodifiableList(new ArrayList<>(serviceDiscoveries.keySet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDiscovery getDiscovery(String discoveryName) {
        if (serviceDiscoveryNames().contains(discoveryName)) {
            return serviceDiscoveries.get(discoveryName);
        }
        return null;
    }

    /**
     * Gets an existing managed repository, or creates it.
     *
     * @param options      the service discovery options
     * @param closeHandler the action to perform when {@code discovery.close()} is called
     * @return the managed service discovery
     */
    protected ServiceDiscovery getOrCreateDiscovery(ServiceDiscoveryOptions options, Action<Void> closeHandler) {
        if (options.getName() == null) {
            options.setName(getNodeId(vertx));
        }
        String discoveryName = options.getName();
        if (serviceDiscoveries.containsKey(discoveryName)) {
            return serviceDiscoveries.get(discoveryName);
        }
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx, options);
        if (closeHandler != null) {
            discovery = ManagedServiceDiscovery.of(discovery, closeHandler);
            serviceDiscoveries.put(discoveryName, (ManagedServiceDiscovery) discovery);
        }
        return discovery;
    }

    /**
     * Adds a new registration.
     *
     * @param discoveryName the name of the service discovery
     * @param registration  the registration
     * @return the registration that was passed in
     */
    protected T register(String discoveryName, T registration) {
        registrationMap.put(registration, discoveryName);
        return registration;
    }

    /**
     * Unregisters the registration by removing it from the list of registrations.
     * <p>
     * If after un-registration the associated service discovery is no longer used, then it will also be closed.
     *
     * @param registration the schema registration
     */
    protected void unregister(T registration) {
        String discoveryName = registrationMap.get(registration);
        registrationMap.remove(registration);
        if (!registrationMap.containsValue(discoveryName)) {
            closeServiceDiscovery(discoveryName);
        }
    }

    /**
     * @return the current registrations of the registrar
     */
    protected List<T> registrations() {
        return new ArrayList<>(registrationMap.keySet());
    }

    /**
     * Closes the specified service discovery and unregisters event handlers.
     *
     * @param discoveryName the service discovery name
     */
    protected void closeServiceDiscovery(String discoveryName) {
        if (serviceDiscoveries.containsKey(discoveryName)) {
            ManagedServiceDiscovery.closeUnmanaged(serviceDiscoveries.remove(discoveryName));
        }
    }

    /**
     * Closes the registrar and release all its resources.
     */
    protected void close() {
        for (Map.Entry<String, ManagedServiceDiscovery> entry : serviceDiscoveries.entrySet()) {
            ManagedServiceDiscovery.closeUnmanaged(entry.getValue());
        }
        serviceDiscoveries.clear();
        registrationMap.clear();
    }

    private static String getNodeId(Vertx vertx) {
        if (vertx.isClustered()) {
            return ((VertxInternal) vertx).getNodeID();
        } else {
            return "localhost";
        }
    }
}
