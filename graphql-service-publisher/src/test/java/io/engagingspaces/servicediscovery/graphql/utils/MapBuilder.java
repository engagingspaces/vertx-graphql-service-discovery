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

package io.engagingspaces.servicediscovery.graphql.utils;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapted from: http://minborgsjavapot.blogspot.nl/2014/12/java-8-initializing-maps-in-smartest-way.html
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class MapBuilder {

    private MapBuilder() {}

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Map.Entry<K, V> ...entries) {
        try (Stream<Map.Entry<K, V>> stream = Stream.of(entries)) {
            return stream.collect(entriesToMap());
        }
    }

    @SafeVarargs
    public static <K, V> Map<K, V> immutableMapOf(Map.Entry<K, V> ...entries) {
        return Collections.unmodifiableMap(mapOf(entries));
    }

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}