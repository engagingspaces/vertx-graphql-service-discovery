package org.example.servicediscovery.server.starwars;

import io.engagingspaces.servicediscovery.graphql.publisher.SchemaPublisher;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistrar;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

public class StarWarsServer extends AbstractVerticle implements SchemaPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(StarWarsServer.class);
    private SchemaRegistrar registrar;

    @Override
    public void start(Future<Void> startFuture) {
        registrar = SchemaRegistrar.create(vertx);
        SchemaPublisher.publishAll(this, new ServiceDiscoveryOptions().setName("graphql-schema-discovery"), rh -> {
            if (rh.succeeded()) {
                LOG.info("Published StarWars schema...");
                startFuture.complete();
            } else {
                startFuture.fail(rh.cause());
            }
        }, StarWarsSchema.get());
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
