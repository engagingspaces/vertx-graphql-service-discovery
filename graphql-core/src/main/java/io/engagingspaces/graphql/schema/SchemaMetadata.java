package io.engagingspaces.graphql.schema;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

@DataObject
public class SchemaMetadata {

    /**
     * Key to the list of root query field names stored in schema metadata.
     */
    public static final String METADATA_QUERIES = "queries";

    /**
     * Key to the list of mutation field names stored in schema metadata.
     */
    public static final String METADATA_MUTATIONS = "mutations";

    private boolean async;
    private String schemaName;
    private String serviceAddress;
    private DeliveryOptions deliveryOptions;
    private JsonObject metadata;

    protected SchemaMetadata(JsonObject metadata, boolean async) {
        this.metadata = metadata;
        this.async = async;
    }

    public SchemaMetadata(JsonObject json) {
        Objects.requireNonNull(json, "Json metadata cannot be null");
        this.async = json.getBoolean("async");
        this.schemaName = json.getString("schemaName");
        if (json.containsKey("deliveryOptions")) {
            this.deliveryOptions = new DeliveryOptions(json.getJsonObject("deliveryOptions"));
        }
        this.metadata = json.copy();
        this.metadata.remove("async");
        this.metadata.remove("schemaName");
        this.metadata.remove("deliveryOptions");
    }

    public static SchemaMetadata create() {
        return new SchemaMetadata(new JsonObject(), false);
    }

    /**
     * Creates a new schema metadata instance.
     *
     * @param metadata   the additional custom metadata to pass to the service proxy
     * @return the schema metadata instance
     */
    public static SchemaMetadata create(JsonObject metadata) {
        return new SchemaMetadata(metadata, false);
    }

    public JsonObject toJson() {
        JsonObject result = metadata.copy()
                .put("async", async)
                .put("schemaName", schemaName);
        if (deliveryOptions != null) {
            result.put("deliveryOptions", new JsonObject()
                    .put("codecName", deliveryOptions.getCodecName())
                    .put("headers", deliveryOptions.getHeaders())
                    .put("sendTimeout", deliveryOptions.getSendTimeout()));
        }
        return result;
    }

    public boolean isAsync() {
        return async;
    }

    public SchemaMetadata setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public SchemaMetadata setSchemaName(String schemaName) {
        Objects.requireNonNull(schemaName, "Schema name cannot be null");
        this.schemaName = schemaName;
        return this;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public SchemaMetadata setServiceAddress(String address) {
        Objects.requireNonNull(address, "Service address cannot be null");
        this.serviceAddress = address;
        return this;
    }

    public DeliveryOptions getDeliveryOptions() {
        return deliveryOptions;
    }

    public SchemaMetadata setDeliveryOptions(DeliveryOptions deliveryOptions) {
        this.deliveryOptions = deliveryOptions;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) metadata.getValue(key);
    }

    public <T> SchemaMetadata put(String key, T value) {
        Objects.requireNonNull(key, "Metadata key cannot be null");
        Objects.requireNonNull(value, "Metadata value cannot be null");
        metadata.put(key, value);
        return this;
    }
}
