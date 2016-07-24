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

package org.example.servicediscovery.server.starwars;

import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.servicediscovery.server.utils.MapBuilder.entry;
import static org.example.servicediscovery.server.utils.MapBuilder.immutableMapOf;

/**
 * Test data converted from:
 * https://github.com/graphql-java/graphql-java/blob/master/src/test/groovy/graphql/StarWarsData.groovy
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public interface HumanData {

    Map<String, Object> luke = immutableMapOf(
            entry("id", "100"),
            entry("name", "Luke Skywalker"),
            entry("friends", Collections.unmodifiableList(Arrays.asList("1002", "1003", "2000", "2001"))),
            entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            entry("homePlanet", "Tatooine")
    );

    Map<String, Object> vader = immutableMapOf(
            entry("id", "1001"),
            entry("name", "Darth Vader"),
            entry("friends", "1004"),
            entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            entry("homePlanet", "Tatooine")
    );

    Map<String, Object> han = immutableMapOf(
            entry("id", "1002"),
            entry("name", "Han Solo"),
            entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1003", "2001"))),
            entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6)))
    );

    Map<String, Object> leia = immutableMapOf(
            entry("id", "1003"),
            entry("name", "Leia Organa"),
            entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1002", "2000", "2001"))),
            entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            entry("homePlanet", "Alderaan")
    );

    Map<String, Object> tarkin = immutableMapOf(
            entry("id", "1004"),
            entry("name", "Wilhuff Tarkin"),
            entry("friends", Collections.unmodifiableList(Collections.singletonList("1001"))),
            entry("appearsIn", Collections.unmodifiableList(Collections.singletonList(4)))
    );

    Map<String, Object> humanData = immutableMapOf(
            entry("1000", luke),
            entry("1001", vader),
            entry("1002", han),
            entry("1003", leia),
            entry("1004", tarkin)
    );

    @SuppressWarnings("unchecked")
    static Map<String, Object> getCharacter(String id) {
        if (humanData.containsKey(id)) {
            return (Map<String, Object>) humanData.get(id);
        }
        return null;
    }

    static DataFetcher getHumanDataFetcher() {
        return environment -> {
            String id = environment.getArgument("id");
            return humanData.get(id);
        };
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    static TypeResolver getCharacterTypeResolver() {
        return object -> {
            Object id = ((Map<String, Object>) object).get("id");
            if (humanData.containsKey(id)) {
                return StarWarsSchema.humanType;
            }
            return null;
        };
    }

    @SuppressWarnings("unchecked")
    static DataFetcher getFriendsDataFetcher() {
        return environment -> {
            List<Object> result = Collections.emptyList();
            List<String> friends = (List<String>) ((Map<String, Object>) environment.getSource()).get("friends");
            result.addAll(friends.stream().map(HumanData::getCharacter).collect(Collectors.toList()));
            return result;
        };
    }

    @SuppressWarnings("unused")
    static DataFetcher getHeroDataFetcher() {
        return environment -> {
            if (environment.containsArgument("episode")
                    && 5 == (int) environment.getArgument("episode")) return luke;
            return leia; // Not really, it was R2-D2, but Droids have been separated in this example.
        };
    }
}
