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

package io.engagingspaces.graphql.events.impl;

import io.engagingspaces.graphql.events.SchemaAnnounceHandler;
import io.engagingspaces.graphql.events.SchemaReferenceData;
import io.engagingspaces.graphql.events.SchemaUsageHandler;
import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.*;

/**
 * Internal class for managing message consumers.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class SchemaMessageConsumers {

    private final Vertx vertx;
    private final Map<String, MessageConsumer<JsonObject>> messageConsumers;
    private final List<String> consumerRegistrations;

    public SchemaMessageConsumers(Vertx vertx) {
        this.vertx = vertx;
        this.messageConsumers = new HashMap<>();
        this.consumerRegistrations = new ArrayList<>();
    }

    public void registerConsumer(String address, SchemaAnnounceHandler announceHandler) {
        registerConsumer(address, createAnnounceHandler(announceHandler));
    }

    public void registerConsumer(String address, SchemaUsageHandler usageHandler) {
        registerConsumer(address, createUsageHandler(usageHandler));
    }

    public <T extends Queryable> MessageConsumer<JsonObject> registerServiceConsumer(String address, T implementation) {
        MessageConsumer<JsonObject> serviceConsumer;
        if (!messageConsumers.containsKey(address)) {
            serviceConsumer = ProxyHelper.registerService(Queryable.class, vertx, implementation, address);
            messageConsumers.put(address, serviceConsumer);
        } else {
            serviceConsumer = messageConsumers.get(address);
        }
        consumerRegistrations.add(address);
        return serviceConsumer;
    }

    public void unregisterConsumer(String address) {
        consumerRegistrations.remove(address);
        messageConsumers.entrySet().stream()
                .filter(entry -> !consumerRegistrations.contains(address))
                .filter(entry -> entry.getKey().equals(address))
                .map(Map.Entry::getValue)
                .map(consumer -> {
                    if (consumer.isRegistered()) {
                        consumer.unregister();
                    }
                    return consumer;
                })
                .findFirst()
                .ifPresent(consumer -> messageConsumers.remove(consumer.address()));
    }

    public void close() {
        for (Iterator<Map.Entry<String,
                MessageConsumer<JsonObject>>> it = messageConsumers.entrySet().iterator(); it.hasNext();) {
            MessageConsumer consumer = it.next().getValue();
            if (consumer.isRegistered()) {
                consumer.unregister();
            }
            it.remove();
        }
        consumerRegistrations.clear();
    }

    /**
     * @return the message consumers that are registered
     */
    protected Map<String, MessageConsumer<JsonObject>> getConsumers() {
        return Collections.unmodifiableMap(messageConsumers);
    }

    private void registerConsumer(String address, Handler<Message<JsonObject>> handler) {
        if (!messageConsumers.containsKey(address) && handler != null) {
            messageConsumers.put(address, vertx.eventBus().consumer(address, handler));
        }
        consumerRegistrations.add(address);
    }

    private Handler<Message<JsonObject>> createAnnounceHandler(SchemaAnnounceHandler announceHandler) {
        return message -> {
            if (Queryable.SERVICE_TYPE.equals(message.body().getString("type"))) {
                Record record = new Record(message.body());
                announceHandler.schemaDiscoveryEvent(record);
            }
        };
    }

    private Handler<Message<JsonObject>> createUsageHandler(SchemaUsageHandler usageHandler) {
        return message -> {
            if (Queryable.SERVICE_TYPE.equals(message.body().getJsonObject("record").getString("type"))) {
                SchemaReferenceData schemaReferenceData = new SchemaReferenceData(message.body());
                usageHandler.schemaReferenceEvent(schemaReferenceData);
            }
        };
    }
}
