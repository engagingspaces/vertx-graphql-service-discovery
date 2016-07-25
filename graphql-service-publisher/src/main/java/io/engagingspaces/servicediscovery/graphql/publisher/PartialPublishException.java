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

import java.util.ArrayList;
import java.util.List;

/**
 * Exception that is thrown when publishing multiple schema definitions and some of them fail to get published.
 * <p>
 * This exception can be used to determine which schema definitions failed, re-publish them, or rollback by
 * un-publishing schema's that were successful in the publishing operation.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public final class PartialPublishException extends RuntimeException {

    private final List<Throwable> publicationErrors;

    /**
     * Default constructor
     */
    @SuppressWarnings("unused")
    public PartialPublishException() {
        this(null, (Throwable) null);
    }
    /**
     * @param message the error message
     */
    @SuppressWarnings("unused")
    public PartialPublishException(String message) {
        this(message, (Throwable) null);
    }

    /**
     * @param cause   the publication failure
     */
    @SuppressWarnings("unused")
    public PartialPublishException(Throwable cause) {
        this(null, cause);
    }

    /**
     * @param message the error message
     * @param cause   the publication failure
     */
    @SuppressWarnings("unused")
    public PartialPublishException(String message, Throwable cause) {
        super(message, cause);
        this.publicationErrors = new ArrayList<>();
    }

    /**
     * Creates a new partial publication exception with the provided list of schema publication errors.
     *
     * @param errors   the list of publication errors
     */
    public PartialPublishException(List<Throwable> errors) {
        this(null, errors);
    }

    /**
     * Creates a new partial publication exception with the provided list of schema publication errors.
     *
     * @param message  the error message
     * @param errors   the list of publication errors
     */
    public PartialPublishException(String message, List<Throwable> errors) {
        super(message != null ? message : (errors == null ? "Partial publication exception" : errors.stream()
                .map(Throwable::getMessage)
                .reduce("Failed to publish all schema definitions. " +
                        errors.size() + " errors occurred:", (msg, error) -> msg + "\n - " + error, String::join)));
        this.publicationErrors = errors == null ? new ArrayList<>() : errors;
    }

    /**
     * Gets the publication errors that occurred after publishing a list of schema definitions.
     *
     * @return the list of publication errors
     */
    public List<Throwable> getPublicationErrors() {
        return publicationErrors;
    }
}
