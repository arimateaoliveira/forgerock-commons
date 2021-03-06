/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2012 ForgeRock AS.
 */
package org.forgerock.json.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;

/**
 * A simple in-memory collection resource provider which uses a {@code Map} to
 * store resources. This resource provider is intended for testing purposes only
 * and there are no performance guarantees.
 */
public final class InMemoryBackend implements CollectionResourceProvider {
    // TODO: filters, sorting, paged results.

    /*
     * Throughout this map backend we take care not to invoke result handlers
     * while holding locks since result handlers may perform blocking IO
     * operations.
     */

    private final AtomicLong nextResourceId = new AtomicLong();
    private final Map<String, Resource> resources = new ConcurrentHashMap<String, Resource>();
    private final Object writeLock = new Object();

    /**
     * Creates a new in-memory collection containing no resources.
     */
    public InMemoryBackend() {
        // No implementation required.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionCollection(final ServerContext context, final ActionRequest request,
            final ResultHandler<JsonValue> handler) {
        try {
            if (request.getActionId().equals("clear")) {
                final int size;
                synchronized (writeLock) {
                    size = resources.size();
                    resources.clear();
                }
                final JsonValue result = new JsonValue(new LinkedHashMap<String, Object>(1));
                result.put("cleared", size);
                handler.handleResult(result);
            } else {
                throw new NotSupportedException("Unrecognized action ID '" + request.getActionId()
                        + "'. Supported action IDs: clear");
            }
        } catch (final ResourceException e) {
            handler.handleError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionInstance(final ServerContext context, final String id,
            final ActionRequest request, final ResultHandler<JsonValue> handler) {
        final ResourceException e = new NotSupportedException(
                "Actions are not supported for resource instances");
        handler.handleError(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createInstance(final ServerContext context, final CreateRequest request,
            final ResultHandler<Resource> handler) {
        final JsonValue value = request.getContent();
        final String id = request.getNewResourceId();
        final String rev = "0";
        try {
            final Resource resource;
            while (true) {
                final String eid = id != null ? id : String.valueOf(nextResourceId
                        .getAndIncrement());
                final Resource tmp = new Resource(eid, rev, value);
                synchronized (writeLock) {
                    final Resource existingResource = resources.put(eid, tmp);
                    if (existingResource != null) {
                        if (id != null) {
                            // Already exists - put the existing resource back.
                            resources.put(id, existingResource);
                            throw new ConflictException("The resource with ID '" + id
                                    + "' could not be created because "
                                    + "there is already another resource with the same ID");
                        } else {
                            // Retry with next available resource ID.
                        }
                    } else {
                        // Add succeeded.
                        addIdAndRevision(tmp);
                        resource = tmp;
                        break;
                    }
                }
            }
            handler.handleResult(resource);
        } catch (final ResourceException e) {
            handler.handleError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteInstance(final ServerContext context, final String id,
            final DeleteRequest request, final ResultHandler<Resource> handler) {
        final String rev = request.getRevision();
        try {
            final Resource resource;
            synchronized (writeLock) {
                resource = resources.remove(id);
                if (resource == null) {
                    throw new NotFoundException("The resource with ID '" + id
                            + "' could not be deleted because it does not exist");
                } else if (rev != null && !resource.getRevision().equals(rev)) {
                    // Mismatch - put the resource back.
                    resources.put(id, resource);
                    throw new ConflictException("The resource with ID '" + id
                            + "' could not be deleted because "
                            + "it does not have the required version");
                }
            }
            handler.handleResult(resource);
        } catch (final ResourceException e) {
            handler.handleError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void patchInstance(final ServerContext context, final String id,
            final PatchRequest request, final ResultHandler<Resource> handler) {
        final ResourceException e = new NotSupportedException("Patch operations are not supported");
        handler.handleError(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queryCollection(final ServerContext context, final QueryRequest request,
            final QueryResultHandler handler) {
        for (final Resource resource : resources.values()) {
            handler.handleResource(resource);
        }
        handler.handleResult(new QueryResult());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readInstance(final ServerContext context, final String id,
            final ReadRequest request, final ResultHandler<Resource> handler) {
        try {
            final Resource resource = resources.get(id);
            if (resource == null) {
                throw new NotFoundException("The resource with ID '" + id
                        + "' could not be read because it does not exist");
            }
            handler.handleResult(resource);
        } catch (final ResourceException e) {
            handler.handleError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInstance(final ServerContext context, final String id,
            final UpdateRequest request, final ResultHandler<Resource> handler) {
        final String rev = request.getRevision();
        try {
            final Resource resource;
            synchronized (writeLock) {
                final Resource existingResource = resources.get(id);
                if (existingResource == null) {
                    throw new NotFoundException("The resource with ID '" + id
                            + "' could not be updated because it does not exist");
                } else if (rev != null && !existingResource.getRevision().equals(rev)) {
                    throw new ConflictException("The resource with ID '" + id
                            + "' could not be updated because "
                            + "it does not have the required version");
                } else {
                    final String newRev = getNextRevision(existingResource.getRevision());
                    resource = new Resource(id, newRev, request.getNewContent());
                    addIdAndRevision(resource);
                    resources.put(id, resource);
                }
            }
            handler.handleResult(resource);
        } catch (final ResourceException e) {
            handler.handleError(e);
        }
    }

    /*
     * Add the ID and revision to the JSON content so that they are included
     * with subsequent responses. We shouldn't really update the passed in
     * content in case it is shared by other components, but we'll do it here
     * anyway for simplicity.
     */
    private void addIdAndRevision(final Resource resource) throws ResourceException {
        final JsonValue content = resource.getContent();
        try {
            content.asMap().put("_id", resource.getId());
            content.asMap().put("_rev", resource.getRevision());
        } catch (final JsonValueException e) {
            throw new BadRequestException(
                    "The request could not be processed because the provided "
                            + "content is not a JSON object");
        }
    }

    private String getNextRevision(final String rev) throws ResourceException {
        try {
            return String.valueOf(Integer.parseInt(rev) + 1);
        } catch (final NumberFormatException e) {
            throw new InternalServerErrorException("Malformed revision number '" + rev
                    + "' encountered while updating a resource");
        }
    }
}
