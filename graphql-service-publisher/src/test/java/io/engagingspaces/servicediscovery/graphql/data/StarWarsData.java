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

package io.engagingspaces.servicediscovery.graphql.data;

import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import io.engagingspaces.servicediscovery.graphql.utils.MapBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test data converted from:
 * https://github.com/graphql-java/graphql-java/blob/master/src/test/groovy/graphql/StarWarsData.groovy
 */
public class StarWarsData {

    public static Map<String, Object> luke = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "100"),
            MapBuilder.entry("name", "Luke Skywalker"),
            MapBuilder.entry("friends", Collections.unmodifiableList(Arrays.asList("1002", "1003", "2000", "2001"))),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            MapBuilder.entry("homePlanet", "Tatooine")
    );

    public static Map<String, Object> vader = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "1001"),
            MapBuilder.entry("name", "Darth Vader"),
            MapBuilder.entry("friends", "1004"),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            MapBuilder.entry("homePlanet", "Tatooine")
    );

    public static Map<String, Object> han = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "1002"),
            MapBuilder.entry("name", "Han Solo"),
            MapBuilder.entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1003", "2001"))),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6)))
    );

    public static Map<String, Object> leia = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "1003"),
            MapBuilder.entry("name", "Leia Organa"),
            MapBuilder.entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1002", "2000", "2001"))),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            MapBuilder.entry("homePlanet", "Alderaan")
    );

    public static Map<String, Object> tarkin = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "1004"),
            MapBuilder.entry("name", "Wilhuff Tarkin"),
            MapBuilder.entry("friends", Collections.unmodifiableList(Collections.singletonList("1001"))),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Collections.singletonList(4)))
    );

    public static Map<String, Object> humanData = MapBuilder.immutableMapOf(
            MapBuilder.entry("1000", luke),
            MapBuilder.entry("1001", vader),
            MapBuilder.entry("1002", han),
            MapBuilder.entry("1003", leia),
            MapBuilder.entry("1004", tarkin)
    );

    public static Map<String, Object> threepio = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "2000"),
            MapBuilder.entry("name", "C-3PO"),
            MapBuilder.entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1002", "1003", "2001"))),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            MapBuilder.entry("primaryFunction", "Protocol")
    );

    public static Map<String, Object> artoo = MapBuilder.immutableMapOf(
            MapBuilder.entry("id", "2001"),
            MapBuilder.entry("name", "R2-D2"),
            MapBuilder.entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1002", "1003"))),
            MapBuilder.entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            MapBuilder.entry("primaryFunction", "Astromech")
    );

    public static Map<String, Object> droidData = MapBuilder.immutableMapOf(
            MapBuilder.entry("2000", threepio),
            MapBuilder.entry("2001", artoo)
    );

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getCharacter(String id) {
        if (humanData.containsKey(id)) return (Map<String, Object>) humanData.get(id);
        if (droidData.containsKey(id)) return (Map<String, Object>) droidData.get(id);
        return null;
    }

    public static DataFetcher getHumanDataFetcher() {
        return environment -> {
            String id = environment.getArgument("id");
            return humanData.get(id);
        };
    }

    public static DataFetcher getDroidDataFetcher() {
        return environment -> {
            String id = environment.getArgument("id");
            return droidData.get(id);
        };
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public static TypeResolver getCharacterTypeResolver() {
        return object -> {
            Object id = ((Map<String, Object>) object).get("id");
            if (humanData.containsKey(id)) {
                return StarWarsSchema.humanType;
            }
            if (droidData.containsKey(id)) {
                return StarWarsSchema.droidType;
            }
            return null;
        };
    }

    @SuppressWarnings("unchecked")
    public static DataFetcher getFriendsDataFetcher() {
        return environment -> {
            List<Object> result = Collections.emptyList();
            List<String> friends = (List<String>) ((Map<String, Object>) environment.getSource()).get("friends");
            result.addAll(friends.stream().map(StarWarsData::getCharacter).collect(Collectors.toList()));
            return result;
        };
    }

    @SuppressWarnings("unused")
    public static DataFetcher getHeroDataFetcher() {
        return environment -> {
            if (environment.containsArgument("episode")
                    && 5 == (int) environment.getArgument("episode")) return luke;
            return artoo;
        };
    }
}
