package io.engagingspaces.graphql.servicediscovery.publisher;

import io.engagingspaces.graphql.query.Queryable;
import io.engagingspaces.graphql.schema.SchemaDefinition;
import io.engagingspaces.graphql.schema.SchemaMetadata;
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

import static org.example.graphql.testdata.droids.DroidsSchema.droidsSchema;
import static org.example.graphql.testdata.starwars.StarWarsSchema.starWarsSchema;
import static org.junit.Assert.*;

public class SchemaRegistrationTest {

    private Vertx vertx;
    private ServiceDiscovery discovery;
    private ServiceDiscoveryOptions options;
    private Record record;
    private SchemaDefinition definition;
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
        definition = SchemaDefinition.createInstance(droidsSchema,
                SchemaMetadata.create(new JsonObject().put("publisherId", "thePublisherId")));
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
        SchemaRegistration registration1 = SchemaRegistration.create(discovery, options, record, definition, null);
        assertEquals(registration1, registration1);
        assertNotEquals(registration1, "test");

        SchemaRegistration registration2 = SchemaRegistration.create(discovery,
                new ServiceDiscoveryOptions(options).setName("theOtherDiscovery"), record, definition, null);
        assertNotEquals(registration1, registration2);

        SchemaRegistration registration3 = SchemaRegistration.create(discovery,
                new ServiceDiscoveryOptions(options).setName("theOtherDiscovery"), record, definition, consumer);
        assertNotEquals(registration2, registration3);

        SchemaDefinition starwarsSchema = SchemaDefinition
                .createInstance(starWarsSchema, SchemaMetadata.create());
        SchemaRegistration registration4 = SchemaRegistration.create(discovery, options, record, starwarsSchema, null);
        assertNotEquals(registration1, registration4);

        SchemaRegistration registration5 = SchemaRegistration.
                create(discovery, options, new Record(record).setName("theOtherRecord"), starwarsSchema, null);
        assertNotEquals(registration4, registration5);

        SchemaRegistration registration6 = SchemaRegistration.
                create(discovery, options, new Record(record).setStatus(Status.DOWN), starwarsSchema, null);
        assertEquals(registration4, registration6);
    }

    @Test
    public void should_Unregister_Service_Proxy_When_Registered() {
        SchemaRegistration registration1 = SchemaRegistration.create(discovery, options, record, definition, null);
        SchemaRegistration registration2 = SchemaRegistration.create(discovery, options, record, definition, consumer);
        assertTrue(consumer.isRegistered());

        registration1.unregisterServiceProxy();
        assertTrue(consumer.isRegistered());
        registration2.unregisterServiceProxy();
        assertFalse(consumer.isRegistered());
    }
}
