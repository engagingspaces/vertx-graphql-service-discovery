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

package io.engagingspaces.graphql.events;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

/**
 * Immutable data object that represents the state of a {@link io.vertx.servicediscovery.ServiceReference} when a
 * service discovery `usage` event occurs that matches a published schema.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@DataObject
public class SchemaReferenceData {

    /**
     * The status of the service reference.
     */
    public enum Status {
        /**
         * Reference is bound to the GraphQL service
         */
        BOUND,

        /**
         * Reference has released the GraphQL service
         */
        RELEASED
    }

    private final String id;
    private final Record record;
    private final Status status;

    /**
     * Creates a new {@link SchemaReferenceData} from
     * its json representation.
     *
     * @param json the json object
     */
    public SchemaReferenceData(JsonObject json) {
        this.id = json.getString("id");
        this.record = new Record(json.getJsonObject("record"));
        this.status = "bind".equals(json.getValue("type")) ? Status.BOUND : Status.RELEASED;
    }

    /**
     * Creates a new {@link SchemaReferenceData} by copying
     * the values from another {@link SchemaReferenceData}.
     *
     * @param other the reference state to copy
     */
    public SchemaReferenceData(SchemaReferenceData other) {
        this.id = other.id;
        this.status = other.status;
        this.record = new Record(other.record);
    }

    /**
     * @return the JSON representation of the current
     * {@link SchemaReferenceData}.
     */
    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("record", record.toJson())
                .put("type", Status.RELEASED.equals(status) ? "release" : "bind");
    }

    /**
     * @return the service discovery name
     */
    public String getDiscoveryName() {
        return id;
    }

    /**
     * Gets the service record of the service that is referenced.
     *
     * @return the service record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Gets the status of the {@link io.vertx.servicediscovery.ServiceReference} which can be either
     * {@link Status#BOUND} or
     * {@link Status#RELEASED}
     *
     * @return the service reference status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param other the object to compare to this schema reference event
     * @return {@code true} when equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof SchemaReferenceData)) {
            return false;
        }
        SchemaReferenceData test = (SchemaReferenceData) other;

        // Record does not yet override equals() (https://github.com/vert-x3/vertx-service-discovery/pull/35)
        boolean recordEquals = record == null ? test.record == null :
                (fieldEquals(record.getName(), test.record.getName()) &&
                        fieldEquals(record.getType(), test.record.getType()));

        return recordEquals && fieldEquals(id, test.id) && fieldEquals(status, test.status);
    }

    private static boolean fieldEquals(Object value1, Object value2) {
        return value1 == null ? value2 == null : value1.equals(value2);
    }

    /**
     * @return the hash code of the service reference event
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (id == null ? 0 : id.hashCode());
        // Record does not yet override equals() (https://github.com/vert-x3/vertx-service-discovery/pull/35)
        result = 31 * result + (record == null ? 0 : (record.getName() == null ? 0 : record.getName().hashCode()));
        result = 31 * result + (status == null ? 0: status.hashCode());
        return result;
    }
}
