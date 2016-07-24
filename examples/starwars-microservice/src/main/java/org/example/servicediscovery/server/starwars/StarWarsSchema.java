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

import graphql.schema.*;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaDefinition;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * Example code demonstrating a schema definition that exposes a GraphQL schema.
 * <p>
 * Test schema adapted from:
 * https://github.com/graphql-java/graphql-java/blob/master/src/test/groovy/graphql/StarWarsSchema.java
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class StarWarsSchema implements SchemaDefinition {

    public static SchemaDefinition get() {
        return new StarWarsSchema();
    }

    @Override
    public GraphQLSchema schema() {
        return starWarsSchema;
    }

    static final GraphQLEnumType episodeEnum = newEnum()
            .name("Episode")
            .description("One of the films in the Star Wars Trilogy")
            .value("NEWHOPE", 4, "Released in 1977.")
            .value("EMPIRE", 5, "Released in 1980.")
            .value("JEDI", 6, "Released in 1983.")
            .build();


    static final GraphQLInterfaceType characterInterface = newInterface()
            .name("Character")
            .description("A character in the Star Wars Trilogy")
            .field(newFieldDefinition()
                    .name("id")
                    .description("The id of the character.")
                    .type(new GraphQLNonNull(GraphQLString))
                    .build())
            .field(newFieldDefinition()
                    .name("name")
                    .description("The name of the character.")
                    .type(GraphQLString)
                    .build())
            .field(newFieldDefinition()
                    .name("friends")
                    .description("The friends of the character, or an empty list if they have none.")
                    .type(new GraphQLList(new GraphQLTypeReference("Character")))
                    .build())
            .field(newFieldDefinition()
                    .name("appearsIn")
                    .description("Which movies they appear in.")
                    .type(new GraphQLList(episodeEnum))
                    .build())
            .typeResolver(HumanData.getCharacterTypeResolver())
            .build();

    static final GraphQLObjectType humanType = newObject()
            .name("Human")
            .description("A humanoid creature in the Star Wars universe.")
            .withInterface(characterInterface)
            .field(newFieldDefinition()
                    .name("id")
                    .description("The id of the human.")
                    .type(new GraphQLNonNull(GraphQLString))
                    .build())
            .field(newFieldDefinition()
                    .name("name")
                    .description("The name of the human.")
                    .type(GraphQLString)
                    .build())
            .field(newFieldDefinition()
                    .name("friends")
                    .description("The friends of the human, or an empty list if they have none.")
                    .type(new GraphQLList(characterInterface))
                    .dataFetcher(HumanData.getFriendsDataFetcher())
                    .build())
            .field(newFieldDefinition()
                    .name("appearsIn")
                    .description("Which movies they appear in.")
                    .type(new GraphQLList(episodeEnum))
                    .build())
            .field(newFieldDefinition()
                    .name("homePlanet")
                    .description("The home planet of the human, or null if unknown.")
                    .type(GraphQLString)
                    .build())
            .build();

    static final GraphQLObjectType queryType = newObject()
            .name("StarWarsQuery")
            .field(newFieldDefinition()
                    .name("hero")
                    .type(characterInterface)
                    .argument(newArgument()
                            .name("episode")
                            .description("If omitted, returns the hero of the whole saga." +
                                    "If provided, returns the hero of that particular episode.")
                            .type(episodeEnum)
                            .build())
                    .dataFetcher(new StaticDataFetcher(HumanData.vader)) // would be cool
                    .build())
            .field(newFieldDefinition()
                    .name("human")
                    .type(humanType)
                    .argument(newArgument()
                            .name("id")
                            .description("id of the human")
                            .type(new GraphQLNonNull(GraphQLString))
                            .build())
                    .dataFetcher(HumanData.getHumanDataFetcher())
                    .build())
            .build();

    static final GraphQLSchema starWarsSchema = GraphQLSchema.newSchema()
            .query(queryType)
            .build();
}