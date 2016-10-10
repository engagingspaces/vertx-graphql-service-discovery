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

package io.engagingspaces.graphql.servicediscovery.publisher;

import io.engagingspaces.graphql.discovery.impl.AbstractRegistration;
import io.engagingspaces.graphql.query.Queryable;
import io.engagingspaces.graphql.servicediscovery.service.GraphQLService;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Objects;
import java.util.Optional;

/**
 * Registration tracking publication state of a registered {@link SchemaDefinition}.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class SchemaRegistration extends AbstractRegistration {

    /**
     * Key to the list of root query field names stored in {@link Record} metadata.
     */
    public static final String METADATA_QUERIES = "queries";

    /**
     * Key to the list of mutation field names stored in {@link Record} metadata.
     */
    public static final String METADATA_MUTATIONS = "mutations";

    private final Record record;
    private final MessageConsumer<JsonObject> serviceConsumer;
    private final SchemaDefinition schemaDefinition;

    private SchemaRegistration(ServiceDiscovery discovery, ServiceDiscoveryOptions options, Record record,
                       SchemaDefinition schemaDefinition, MessageConsumer<JsonObject> serviceConsumer) {
        super(discovery, options);
        Objects.requireNonNull(record, "Service record cannot be null");
        Objects.requireNonNull(schemaDefinition, "Schema definition cannot be null");

        this.record = record;
        this.schemaDefinition = schemaDefinition;
        this.serviceConsumer = serviceConsumer;
    }

    /**
     * Registration that contains state and resources of a published graphql schema.
     * <p>
     * Besides the service record it also stores service discovery instance, discovery options, schema definition
     * and the message consumer of the {@link Queryable} service
     * proxy that exposes the schema.
     *
     * @param discovery        the service discovery
     * @param options          the service discovery options
     * @param record           the service record to register
     * @param schemaDefinition the schema definition
     * @param serviceConsumer  the consumer of the schema (implements the graphql service)
     * @return the published graphql schema
     */
    public static SchemaRegistration create(
            ServiceDiscovery discovery, ServiceDiscoveryOptions options, Record record,
            SchemaDefinition schemaDefinition, MessageConsumer<JsonObject> serviceConsumer) {
        return new SchemaRegistration(discovery, options, record, schemaDefinition, serviceConsumer);
    }

    /**
     * Gets the name of the publisher that registered the schema, {@code null} if published independently.
     *
     * @return the publisher name, or null
     */
    public Optional<String> getPublisherId() {
        return Optional.ofNullable(record.getMetadata().getString("publisherId"));
    }

    /**
     * Gets the name of the schema that is registered (the graphql service name).
     *
     * @return the schema name
     */
    public String getSchemaName() {
        return record.getName();
    }

    /**
     * Gets the schema definition that this registration was created for.
     *
     * @return the schema definition
     */
    public SchemaDefinition getSchemaDefinition() {
        return schemaDefinition;
    }

    /**
     * @return the service discovery record that refers to the published schema.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Gets the message consumer that is registered to handle schema events.
     *
     * @return the message consumer
     */
    public MessageConsumer<JsonObject> getServiceConsumer() {
        return serviceConsumer;
    }

    /**
     * Unregisters the message consumer of the {@link Queryable}
     * service proxy implementation.
     * <p>
     * You do not have to invoke it yourself, if you are using the
     * {@link GraphQLService}, or the schema publisher
     * and consumer interfaces.
     */
    public void unregisterServiceProxy() {
        if (serviceConsumer != null) {
            ProxyHelper.unregisterService(serviceConsumer);
        }
    }

    /**
     * @param other the object to compare to this schema registration
     * @return {@code true} when equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SchemaRegistration)) {
            return false;
        }
        SchemaRegistration test = (SchemaRegistration) other;
        return super.equals(test) && thisEquals(test);
    }

    private boolean thisEquals(SchemaRegistration test) {
        // Record does not yet override equals() (https://github.com/vert-x3/vertx-service-discovery/pull/35)
        return (schemaEquals(schemaDefinition, test.schemaDefinition)) &&
                fieldEquals(serviceConsumer, test.serviceConsumer) &&
                (record == null ? test.record == null : fieldEquals(record.getName(), test.record.getName()));
    }

    private static boolean schemaEquals(SchemaDefinition schema1, SchemaDefinition schema2) {
        if (schema1 == null) {
            return schema2 == null;
        }
        if (schema1.schema() == null) {
            return schema2.schema() == null;
        }
        if (schema2 != null && schema2.schema() != null) {
            return schema1.schema().getQueryType().getName().equals(schema2.schema().getQueryType().getName());
        }
        return false;
    }

    private static boolean fieldEquals(Object value1, Object value2) {
        return value1 == null ? value2 == null : value1.equals(value2);
    }

    /**
     * @return the hash code of this schema registration
     */
    @Override
    public int hashCode() {
        int result = 17;

        // Record does not yet override equals() (https://github.com/vert-x3/vertx-service-discovery/pull/35)
        result = 31 * result + (record == null ? 0 : (record.getName() == null ? 0 : record.getName().hashCode()));
        result = 31 * result + (schemaDefinition == null ? 0: schemaDefinition.hashCode());
        result = 31 * result + (serviceConsumer == null ? 0: serviceConsumer.hashCode());
        result = 31 * result + (super.hashCode());
        return result;
    }
}
