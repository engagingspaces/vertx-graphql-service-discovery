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

package io.engagingspaces.servicediscovery.graphql.service;

import org.example.servicediscovery.server.droids.DroidsSchema;
import org.example.servicediscovery.server.starwars.StarWarsSchema;
import io.engagingspaces.graphql.query.QueryResult;
import io.engagingspaces.graphql.query.Queryable;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistration;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Tests for graphql service interface static members.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class GraphQLServiceTest {

    private static final String GRAPHQL_QUERY =
            "        query CheckTypeOfR2 {\n" +
            "            droidHero {\n" +
            "                __typename\n" +
            "                name\n" +
            "            }\n" +
            "        }";

    private Vertx vertx;
    private ServiceDiscovery discovery;
    private ServiceDiscoveryOptions options;
    private String schemaName = DroidsSchema.get().schema().getQueryType().getName();

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        options = new ServiceDiscoveryOptions().setName("my-schema-discovery");
        discovery = ServiceDiscovery.create(vertx, options);
    }

    @After
    public void tearDown() {
        discovery.close();
        vertx.close();
    }

    @Test
    public void should_Publish_Queryable_Service_Proxy_From_Schema_Definition(TestContext context) {
        Async async = context.async();
        GraphQLService.publish(vertx, discovery, DroidsSchema.get(), publishResultHandler -> {
            context.assertTrue(publishResultHandler.succeeded());
            context.assertNotNull(publishResultHandler.result());

            SchemaRegistration schema = publishResultHandler.result();
            context.assertNotNull(schema.getRecord());
            context.assertEquals(schemaName, schema.getSchemaName());
            context.assertNotNull(schema.getPublisherId());
            // Discovery options not available with this type of GraphQLService calls
            context.assertNull(schema.getDiscoveryOptions());
            context.assertNotNull(schema.getServiceConsumer());
            context.assertTrue(schema.getServiceConsumer().isRegistered());
            context.assertEquals(Queryable.ADDRESS_PREFIX + "." + schemaName, schema.getServiceConsumer().address());
            context.assertNotNull(schema.getDiscovery());

            Record record = schema.getRecord();
            context.assertNotNull(record.getLocation());
            context.assertNotNull(record.getRegistration());
            context.assertEquals(Status.UP, record.getStatus());
            context.assertEquals(schemaName, record.getName());
            context.assertEquals(Queryable.ADDRESS_PREFIX + "." + schemaName,
                    record.getLocation().getString(Record.ENDPOINT));
            context.assertNotNull(record.getMetadata());
            context.assertNotNull(record.getMetadata().getJsonArray(SchemaRegistration.METADATA_QUERIES));
            context.assertNotNull(record.getMetadata().getJsonArray(SchemaRegistration.METADATA_MUTATIONS));

            ServiceDiscovery discovery = schema.getDiscovery();
            context.assertNotNull(discovery.getReference(record));
            context.assertTrue(discovery.getReference(record).get() instanceof Queryable);

            Queryable queryable = discovery.getReference(record).get();
            queryable.query(
                    GRAPHQL_QUERY,
                    queryResultHandler -> {
                        context.assertTrue(queryResultHandler.succeeded());
                        context.assertNotNull(queryResultHandler.result());
                        QueryResult queryResult = queryResultHandler.result();
                        context.assertTrue(queryResult.isSucceeded());

                        schema.unregisterServiceProxy();
                        context.assertFalse(schema.getServiceConsumer().isRegistered());

                        // Does not unpublish record or close the service discovery.
                        discovery.getRecord(published -> schemaName.equals(published.getName()), rh -> {
                            Record shouldExist = rh.result();
                            context.assertNotNull(shouldExist);
                            context.assertEquals(Status.UP, shouldExist.getStatus());
                        });
                        async.complete();
                    });
        });
    }

    @Test
    public void should_Publish_Schema_Definition_With_Metadata(TestContext context) {
        Async async = context.async();
        JsonObject metadata = new JsonObject().put("someString", "someValue").put("someObject", new JsonObject());

        GraphQLService.publish(vertx, discovery, StarWarsSchema.get(), metadata, rh -> {
            context.assertTrue(rh.succeeded());
            Record record =  rh.result().getRecord();
            context.assertEquals("someValue", record.getMetadata().getString("someString"));
            context.assertEquals(new JsonObject(), record.getMetadata().getJsonObject("someObject"));

            // Check standard metadata that is added (for future use)
            JsonArray queries = record.getMetadata().getJsonArray(SchemaRegistration.METADATA_QUERIES);
            JsonArray mutations = record.getMetadata().getJsonArray(SchemaRegistration.METADATA_MUTATIONS);
            context.assertEquals(new JsonArray("[ \"hero\", \"human\" ]"), queries);
            context.assertEquals(new JsonArray(), mutations);

            async.complete();
        });
    }

    @Test
    public void should_Unpublish_Previously_Published_Schema_Definition(TestContext context) {
        Async async = context.async();
        GraphQLService.publish(vertx, discovery, StarWarsSchema.get(), rh -> {
            context.assertTrue(rh.succeeded());
            SchemaRegistration registration = rh.result();
            GraphQLService.unpublish(registration, unpublishHandler -> {
                context.assertTrue(unpublishHandler.succeeded());
                context.assertFalse(registration.getServiceConsumer().isRegistered());

                // Did unpublish the record but did not close the service discovery.
                discovery.getRecord(published -> schemaName.equals(published.getName()), recordHandler -> {
                    Record shouldNotExist = recordHandler.result();
                    context.assertNull(shouldNotExist);
                });
                async.complete();
            });
        });
    }

    @Test
    @Ignore("Need to investigate proxy creation and desired behavior of duplicate publications")
    public void should_Have_Same_Service_Proxy_When_Published_More_Than_Once(TestContext context) {
        Async async = context.async();
        GraphQLService.publish(vertx, discovery, StarWarsSchema.get(), rh -> {
            context.assertTrue(rh.succeeded());
            SchemaRegistration registration1 = rh.result();

            GraphQLService.publish(vertx, discovery, StarWarsSchema.get(), rh2 -> {
                context.assertTrue(rh.succeeded());
                SchemaRegistration registration2 = rh.result();
                context.assertEquals(registration1, registration2);
                discovery.getRecords(record -> Queryable.SERVICE_TYPE.equals(record.getType()), rh3 -> {
                    List<Record> published = rh3.result();
                    context.assertEquals(2, published.size());
                    Queryable queryable1 = discovery.getReference(published.get(0)).<Queryable>get();
                    Queryable queryable2 = discovery.getReference(published.get(1)).<Queryable>get();

                    // Test fails here. Apparently queryable is not cached?
                    context.assertEquals(queryable1, queryable2);
                    context.assertNotNull(published.get(0).getRegistration());
                    context.assertNotNull(published.get(1).getRegistration());
                    context.assertNotEquals(published.get(0).getRegistration(), published.get(1).getRegistration());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void should_Return_Failure_Un_Publishing_Unknown_Record(TestContext context) {
        Async async = context.async();
        GraphQLService.publish(vertx, discovery, StarWarsSchema.get(), rh ->
        {
            context.assertTrue(rh.succeeded());
            SchemaRegistration registration1 = rh.result();

            GraphQLService.unpublish(SchemaRegistration.create(registration1.getDiscovery(),
                    registration1.getDiscoveryOptions(), new Record(registration1.getRecord()).setRegistration("foo"),
                    registration1.getSchemaDefinition(), registration1.getServiceConsumer()), rh2 ->
            {
                context.assertFalse(rh2.succeeded());
                async.complete();
            });
        });
    }
}
