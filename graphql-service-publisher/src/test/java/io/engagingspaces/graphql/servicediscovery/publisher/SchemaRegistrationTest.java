package io.engagingspaces.graphql.servicediscovery.publisher;

import io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration;
import org.example.servicediscovery.server.droids.DroidsSchema;
import org.example.servicediscovery.server.starwars.StarWarsSchema;
import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.Status;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SchemaRegistrationTest {

    private Vertx vertx;
    private ServiceDiscovery discovery;
    private ServiceDiscoveryOptions options;
    private Record record;
    private io.engagingspaces.graphql.servicediscovery.publisher.SchemaDefinition definition;
    private MessageConsumer<JsonObject> consumer;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        options = new ServiceDiscoveryOptions().setName("theDiscovery")
                .setAnnounceAddress("theAnnounceAddress").setUsageAddress("theUsageAddress");
        discovery = ServiceDiscovery.create(vertx, options);
        record = new Record()
                .setName("theRecord")
                .setType(Queryable.SERVICE_TYPE)
                .setMetadata(new JsonObject().put("publisherId", "thePublisherId"))
                .setLocation(new JsonObject().put(Record.ENDPOINT, Queryable.ADDRESS_PREFIX + ".DroidQueries"))
                .setStatus(Status.UP);
        definition = DroidsSchema.get();
        consumer = ProxyHelper.registerService(Queryable.class,
                vertx, definition, Queryable.ADDRESS_PREFIX + ".DroidQueries");
    }

    @After
    public void tearDown() {
        discovery.close();
        consumer.unregister();
        vertx.close();
    }

    @Test
    public void should_Implement_Proper_Reference_Equality() {
        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration1 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.create(discovery, options, record, definition, null);
        assertEquals(registration1, registration1);
        assertNotEquals(registration1, "test");

        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration2 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.create(discovery,
                new ServiceDiscoveryOptions(options).setName("theOtherDiscovery"), record, definition, null);
        assertNotEquals(registration1, registration2);

        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration3 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.create(discovery,
                new ServiceDiscoveryOptions(options).setName("theOtherDiscovery"), record, definition, consumer);
        assertNotEquals(registration2, registration3);

        io.engagingspaces.graphql.servicediscovery.publisher.SchemaDefinition starwarsSchema = StarWarsSchema.get();
        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration4 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.create(discovery, options, record, starwarsSchema, null);
        assertNotEquals(registration1, registration4);

        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration5 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.
                create(discovery, options, new Record(record).setName("theOtherRecord"), starwarsSchema, null);
        assertNotEquals(registration4, registration5);

        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration6 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.
                create(discovery, options, new Record(record).setStatus(Status.DOWN), starwarsSchema, null);
        assertEquals(registration4, registration6);
    }

    @Test
    public void should_Unregister_Service_Proxy_When_Registered() {
        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration1 = io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration.create(discovery, options, record, definition, null);
        io.engagingspaces.graphql.servicediscovery.publisher.SchemaRegistration registration2 = SchemaRegistration.create(discovery, options, record, definition, consumer);
        assertTrue(consumer.isRegistered());

        registration1.unregisterServiceProxy();
        assertTrue(consumer.isRegistered());
        registration2.unregisterServiceProxy();
        assertFalse(consumer.isRegistered());
    }
}
