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

package org.example.graphql.testdata.droids;

import graphql.Scalars;
import graphql.schema.*;

/**
 * Example code demonstrating a schema definition that exposes a GraphQL schema.
 * <p>
 * Test schema adapted from:
 * https://github.com/graphql-java/graphql-java/blob/master/src/test/groovy/graphql/StarWarsSchema.java
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class DroidsSchema {

    public static final GraphQLEnumType episodeEnum = GraphQLEnumType.newEnum()
            .name("Episode")
            .description("One of the films in the Star Wars Trilogy")
            .value("NEWHOPE", 4, "Released in 1977.")
            .value("EMPIRE", 5, "Released in 1980.")
            .value("JEDI", 6, "Released in 1983.")
            .build();

    public static final GraphQLInterfaceType characterInterface = GraphQLInterfaceType.newInterface()
            .name("Character")
            .description("A character in the Star Wars Trilogy")
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("id")
                    .description("The id of the character.")
                    .type(new GraphQLNonNull(Scalars.GraphQLString))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("name")
                    .description("The name of the character.")
                    .type(Scalars.GraphQLString)
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("friends")
                    .description("The friends of the character, or an empty list if they have none.")
                    .type(new GraphQLList(new GraphQLTypeReference("Character")))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("appearsIn")
                    .description("Which movies they appear in.")
                    .type(new GraphQLList(episodeEnum))
                    .build())
            .typeResolver(DroidsData.getCharacterTypeResolver())
            .build();

    public static final GraphQLObjectType droidType = GraphQLObjectType.newObject()
            .name("Droid")
            .description("A mechanical creature in the Star Wars universe.")
            .withInterface(characterInterface)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("id")
                    .description("The id of the droid.")
                    .type(new GraphQLNonNull(Scalars.GraphQLString))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("name")
                    .description("The name of the droid.")
                    .type(Scalars.GraphQLString)
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("friends")
                    .description("The friends of the droid, or an empty list if they have none.")
                    .type(new GraphQLList(characterInterface))
                    .dataFetcher(DroidsData.getFriendsDataFetcher())
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("appearsIn")
                    .description("Which movies they appear in.")
                    .type(new GraphQLList(episodeEnum))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("primaryFunction")
                    .description("The primary function of the droid.")
                    .type(Scalars.GraphQLString)
                    .build())
            .build();

    public static final GraphQLObjectType queryType = GraphQLObjectType.newObject()
            .name("DroidQueries")
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("droidHero")
                    .type(droidType)
                    .argument(GraphQLArgument.newArgument()
                            .name("episode")
                            .description("If omitted, returns the hero of the whole saga. If provided, " +
                                    "returns the hero of that particular episode.")
                            .type(episodeEnum)
                            .build())
                    .dataFetcher(new StaticDataFetcher(DroidsData.artoo))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("droid")
                    .type(droidType)
                    .argument(GraphQLArgument.newArgument()
                            .name("id")
                            .description("id of the droid")
                            .type(new GraphQLNonNull(Scalars.GraphQLString))
                            .build())
                    .dataFetcher(DroidsData.getDroidDataFetcher())
                    .build())
            .build();

    public static final GraphQLSchema droidsSchema = GraphQLSchema.newSchema()
            .query(queryType)
            .build();
}
