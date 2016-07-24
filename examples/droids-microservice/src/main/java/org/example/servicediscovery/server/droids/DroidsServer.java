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

package org.example.servicediscovery.server.droids;

import io.engagingspaces.servicediscovery.graphql.publisher.SchemaPublisher;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistrar;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

/**
 * Example code demonstrating a a Vert.x verticle that also functions as a schema publisher.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class DroidsServer extends AbstractVerticle implements SchemaPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(DroidsServer.class);
    private SchemaRegistrar registrar;

    @Override
    public void start(Future<Void> startFuture) {
        registrar = SchemaRegistrar.create(vertx);
        SchemaPublisher.publishAll(this, new ServiceDiscoveryOptions().setName("graphql-schema-discovery"), rh -> {
            if (rh.succeeded()) {
                LOG.info("Published Droids schema...");
                startFuture.complete();
            } else {
                startFuture.fail(rh.cause());
            }
        }, DroidsSchema.get());
    }

    @Override
    public void schemaPublished(SchemaRegistration registration) {
        LOG.info("Schema " + registration.getSchemaName() + " is now " + registration.getRecord().getStatus());
    }

    @Override
    public void schemaUnpublished(SchemaRegistration registration) {
        LOG.info("Schema " + registration.getSchemaName() + " was " + registration.getRecord().getStatus());
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        SchemaPublisher.close(this, rh -> {
            if (rh.succeeded()) {
                stopFuture.complete();
            } else {
                stopFuture.fail(rh.cause());
            }
        });
    }

    @Override
    public SchemaRegistrar schemaRegistrar() {
        return registrar;
    }
}
