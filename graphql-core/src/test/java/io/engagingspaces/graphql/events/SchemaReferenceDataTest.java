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

import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the {@link SchemaReferenceData} data object.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class SchemaReferenceDataTest {

    private static final JsonObject INPUT_DATA = new JsonObject(
            "{\n" +
            "  \"id\": \"theDiscoveryName\"," +
            "  \"record\": {\n" +
            "    \"name\": \"theGraphQLServiceName\",\n" +
            "    \"type\": \"" + Queryable.SERVICE_TYPE + "\",\n" +
            "    \"location\": {},\n" +
            "    \"metadata\": {},\n" +
            "    \"registration\": \"theUUID\",\n" +
            "    \"status\": \"UNKNOWN\"\n" +
            "  },\n" +
            "  \"status\": \"RELEASED\"\n" +
            "}");

    private static final SchemaReferenceData EXPECTED_EVENT_DATA = new SchemaReferenceData(new JsonObject()
            .put("id", "theDiscoveryName")
            .put("record", new JsonObject()
                    .put("name", "theGraphQLServiceName")
                    .put("type", Queryable.SERVICE_TYPE)
                    .put("location", new JsonObject())
                    .put("metadata", new JsonObject())
                    .put("registration", "theUUID")
                    .put("status", "UNKNOWN"))
            .put("status", "RELEASED"));

    @Test
    public void should_Create_Schema_Reference_Event_From_Valid_Json() {
        // given
        final JsonObject input = INPUT_DATA;
        // when
        SchemaReferenceData result1 = new SchemaReferenceData(input);
        SchemaReferenceData result2 = new SchemaReferenceData(result1);
        SchemaReferenceData result3 = new SchemaReferenceData(result2.toJson());

        // then
        assertEquals("theDiscoveryName", result1.getDiscoveryName());
        assertNotNull(result1.getRecord());
        assertEquals("theGraphQLServiceName", result1.getRecord().getName());
        assertEquals(SchemaReferenceData.Status.RELEASED, result1.getStatus());
        assertEquals(EXPECTED_EVENT_DATA, result1);
        assertEquals(EXPECTED_EVENT_DATA.hashCode(), result1.hashCode());
        assertEquals(result1, result1);
        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertNotEquals(result1, null);
        assertNotEquals(result1, "");
    }

    @Test
    public void should_Include_Record_Name_And_Type_In_Equality_Check() {
        final JsonObject input = INPUT_DATA;
        SchemaReferenceData result1 = new SchemaReferenceData(input);

        // Ignore record fields
        assertEquals(result1, new SchemaReferenceData(input.put("record",
                input.getJsonObject("record").put("registration", "XYZ"))));
        assertEquals(result1, new SchemaReferenceData(input.put("record",
                input.getJsonObject("record").put("status", "OUT_OF_SERVICE"))));
        assertEquals(result1, new SchemaReferenceData(input.put("record",
                input.getJsonObject("record").put("location", "theNewLocation"))));
        assertEquals(result1, new SchemaReferenceData(input.put("record",
                input.getJsonObject("record").put("metadata", new JsonObject().put("foo", "bar")))));

        // Equality is determined by record name and type.
        assertNotEquals(result1, new SchemaReferenceData(input.put("record",
                input.getJsonObject("record").put("name", "theOtherServiceName"))));
        assertNotEquals(result1, new SchemaReferenceData(input.put("record",
                input.getJsonObject("record").put("name", "theGraphQLServiceName").put("type", "some-type"))));
    }
}
