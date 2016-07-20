package org.example.servicediscovery.server.droids;

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
 */
public interface DroidsData {

    Map<String, Object> threepio = immutableMapOf(
            entry("id", "2000"),
            entry("name", "C-3PO"),
            entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1002", "1003", "2001"))),
            entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            entry("primaryFunction", "Protocol")
    );

    Map<String, Object> artoo = immutableMapOf(
            entry("id", "2001"),
            entry("name", "R2-D2"),
            entry("friends", Collections.unmodifiableList(Arrays.asList("1000", "1002", "1003"))),
            entry("appearsIn", Collections.unmodifiableList(Arrays.asList(4, 5, 6))),
            entry("primaryFunction", "Astromech")
    );

    Map<String, Object> droidData = immutableMapOf(
            entry("2000", threepio),
            entry("2001", artoo)
    );

    @SuppressWarnings("unchecked")
    static Map<String, Object> getCharacter(String id) {
        if (droidData.containsKey(id)) {
            return (Map<String, Object>) droidData.get(id);
        }
        return null;
    }

    static DataFetcher getDroidDataFetcher() {
        return environment -> {
            String id = environment.getArgument("id");
            return droidData.get(id);
        };
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    static TypeResolver getCharacterTypeResolver() {
        return object -> {
            Object id = ((Map<String, Object>) object).get("id");
            if (droidData.containsKey(id)) {
                return DroidsSchema.droidType;
            }
            return null;
        };
    }

    @SuppressWarnings("unchecked")
    static DataFetcher getFriendsDataFetcher() {
        return environment -> {
            List<Object> result = Collections.emptyList();
            List<String> friends = (List<String>) ((Map<String, Object>) environment.getSource()).get("friends");
            result.addAll(friends.stream().map(DroidsData::getCharacter).collect(Collectors.toList()));
            return result;
        };
    }

    @SuppressWarnings("unused")
    static DataFetcher getHeroDataFetcher() {
        return environment -> {
            if (environment.containsArgument("episode")
                    && 5 == (int) environment.getArgument("episode")) return threepio; // It was Luke, but not here :-)
            return artoo;
        };
    }
}
