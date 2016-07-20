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

import io.engagingspaces.servicediscovery.graphql.discovery.Registrar;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.Action;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.spi.ServiceExporter;
import io.vertx.servicediscovery.spi.ServiceImporter;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Wrapper for {@link ServiceDiscovery} that delegates calls to
 * {@link ServiceDiscovery#close()} to a {@link Registrar} and
 * forwards all other method calls to the wrapped service discovery instance.
 *
 * @author Arnold Schrijver
 */
class ManagedServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedServiceDiscovery.class);

    private final ServiceDiscovery unmanagedDiscoveryInstance;
    private final Action<Void>  closeAction;

    private ManagedServiceDiscovery(ServiceDiscovery unmanagedDiscoveryInstance, Action<Void> closeAction) {
        this.unmanagedDiscoveryInstance = unmanagedDiscoveryInstance;
        this.closeAction = closeAction;
    }

    /**
     * Creates a managed service discovery of the provided one.
     *
     * @param discovery   the service discovery that is managed
     * @param closeAction the action to perform when the discovery is closed
     * @return the managed service discovery instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends ServiceDiscovery> T of(ServiceDiscovery discovery, Action<Void>  closeAction) {
        Objects.requireNonNull(discovery, "Service discovery cannot be null");
        if (discovery instanceof ManagedServiceDiscovery) {
            return (T) discovery;
        }
        return (T) new ManagedServiceDiscovery(discovery, closeAction);
    }

    /**
     * Closes the wrapped unmanaged service discovery ({@link ServiceDiscovery#close()})) without invoking the
     * close handler of the managed instance.
     *
     * @param managedDiscovery the managed discovery
     */
    static void closeUnmanaged(ManagedServiceDiscovery managedDiscovery) {
        managedDiscovery.unmanagedDiscoveryInstance.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceReference getReference(Record record) {
        return unmanagedDiscoveryInstance.getReference(record);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceReference getReferenceWithConfiguration(Record record, JsonObject configuration) {
        return unmanagedDiscoveryInstance.getReferenceWithConfiguration(record, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean release(ServiceReference reference) {
        return unmanagedDiscoveryInstance.release(reference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDiscovery registerServiceImporter(ServiceImporter importer, JsonObject configuration) {
        return unmanagedDiscoveryInstance.registerServiceImporter(importer, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDiscovery registerServiceExporter(ServiceExporter exporter, JsonObject configuration) {
        return unmanagedDiscoveryInstance.registerServiceExporter(exporter, configuration);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This service discovery is being managed by a
     * {@link Registrar}. Upon closing any related resources will
     * be released by the registrar.
     */
    @Override
    public void close() {
        if (closeAction == null) {
            LOG.warn("Managed service discovery does not have a close handler. Closing normally...");
            unmanagedDiscoveryInstance.close();
        } else {
            closeAction.perform();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Record record, Handler<AsyncResult<Record>> resultHandler) {
        unmanagedDiscoveryInstance.publish(record, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unpublish(String id, Handler<AsyncResult<Void>> resultHandler) {
        unmanagedDiscoveryInstance.unpublish(id, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRecord(JsonObject filter, Handler<AsyncResult<Record>> resultHandler) {
        unmanagedDiscoveryInstance.getRecord(filter, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRecord(Function<Record, Boolean> filter, Handler<AsyncResult<Record>> resultHandler) {
        unmanagedDiscoveryInstance.getRecord(filter, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRecord(Function<Record, Boolean> filter, boolean includeOutOfService,
                          Handler<AsyncResult<Record>> resultHandler) {
        unmanagedDiscoveryInstance.getRecord(filter, includeOutOfService, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRecords(JsonObject filter, Handler<AsyncResult<List<Record>>> resultHandler) {
        unmanagedDiscoveryInstance.getRecords(filter, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRecords(Function<Record, Boolean> filter, Handler<AsyncResult<List<Record>>> resultHandler) {
        unmanagedDiscoveryInstance.getRecords(filter, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRecords(Function<Record, Boolean> filter, boolean includeOutOfService,
                           Handler<AsyncResult<List<Record>>> resultHandler) {
        unmanagedDiscoveryInstance.getRecords(filter, includeOutOfService, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Record record, Handler<AsyncResult<Record>> resultHandler) {
        unmanagedDiscoveryInstance.update(record, resultHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ServiceReference> bindings() {
        return unmanagedDiscoveryInstance.bindings();
    }
}
