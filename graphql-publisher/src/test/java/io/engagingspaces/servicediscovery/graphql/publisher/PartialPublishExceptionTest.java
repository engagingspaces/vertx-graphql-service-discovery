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

package io.engagingspaces.servicediscovery.graphql.publisher;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for partial publish exception.
 *
 * @author Arnold Schrijver
 */
public class PartialPublishExceptionTest {

    PartialPublishException ex;

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void should_Report_And_Return_Publication_Errors() {
        List<Throwable> errors = Arrays.asList(new RuntimeException("Error1"), new RuntimeException("Error2"));
        ex = new PartialPublishException("Publication error test", errors);
        assertEquals("Publication error test", ex.getMessage());
        assertEquals(2, ex.getPublicationErrors().size());
        ex = new PartialPublishException(errors);
        assertEquals("Failed to publish all schema definitions. 2 errors occurred:\n - Error1\n - Error2",
                ex.getMessage());
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void should_Have_Regular_Exception_Constructors() {
        ex = new PartialPublishException();
        ex = new PartialPublishException("msg");
        ex = new PartialPublishException(new RuntimeException());
        ex = new PartialPublishException("msg", new RuntimeException());
        assertNotNull(ex.getPublicationErrors());
        assertEquals(0, ex.getPublicationErrors().size());
    }
}
