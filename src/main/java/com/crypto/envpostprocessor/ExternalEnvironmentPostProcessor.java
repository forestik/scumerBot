package com.crypto.envpostprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNullElse;

/**
 * This processor gets list of key-value pairs from external source and add into environment {@link ConfigurableEnvironment}
 */
public abstract class ExternalEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * Constant means to which position will be added property source from secret
     */
    public static final String PROPERTY_SOURCE_POSITION = "property-source-position";

    /**
     * Property source name in relation to which will be added property source from secret
     */
    public static final String PROPERTY_SOURCE_REFERENCE = "property-source-reference";

    /**
     * Constant means that property source from secret could be added before provided property source
     */
    public static final String POSITION_BEFORE = "BEFORE";

    /**
     * Constant means that property source from secret could be added after provided property source
     */
    public static final String POSITION_AFTER = "AFTER";

    /**
     * Constant means that property source from secret could be added last in environment property source
     */
    public static final String POSITION_LAST = "LAST";

    /**
     * Constant means that property source from secret could be added first in environment property source
     */
    public static final String POSITION_FIRST = "FIRST";

    private static final TypeReference<Map<String, Object>> TYPE_REF_MAP_STRING_OBJECT = new TypeReference<>() {
    };

    protected final Log log;
    private final String secretPropertyName;

    /**
     * Creates new instance
     *
     * @param secretPropertyName name of secret
     */
    protected ExternalEnvironmentPostProcessor(DeferredLogFactory logFactory, String secretPropertyName) {
        this.log = logFactory.getLog(getClass());
        this.secretPropertyName = secretPropertyName;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final var secretName = getSecretName(environment);
        if (secretName != null) {
            final var secretPropertySource = getSecretPropertySource(secretName);
            applySecretPropertySource(environment, secretPropertySource);
        }
    }

    /**
     * Gets secret JSON string from external source
     *
     * @param secretName name of the secret external source
     * @return JSON string, containing a list of key-value pairs.
     */
    protected abstract String getSecretJsonString(String secretName);

    /**
     * Gets name of the secret from available environment
     *
     * @param environment environment
     * @return secret name or null if not provided
     */
    private String getSecretName(ConfigurableEnvironment environment) {
        final var secretName = environment.getProperty(secretPropertyName);
        if (StringUtils.hasText(secretName)) {
            log.info(String.format("Secret name: %s", secretName));
            return secretName;
        } else {
            log.info("Secret name is not provided");
            return null;
        }
    }

    /**
     * Loads and parses secret property source from external source
     *
     * @param secretName secret name
     * @return secret property source
     */
    private MapPropertySource getSecretPropertySource(String secretName) {
        final var secretJsonString = getSecretJsonString(secretName);
        final Map<String, Object> secretProperties;
        try {
            secretProperties = new ObjectMapper().readValue(secretJsonString, TYPE_REF_MAP_STRING_OBJECT);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Secret value cannot be parsed as JSON", e);
        }
        final var secretPropertySource = new MapPropertySource(secretPropertyName + ":" + secretName, secretProperties);
        log.debug(String.format("Loaded %d secret properties: %s",
                secretProperties.size(), String.join(", ", secretPropertySource.getPropertyNames())));
        return secretPropertySource;
    }

    /**
     * Adds secret property source into environment with desired priority (position)
     *
     * @param environment           environment
     * @param secretsPropertySource secretsPropertySource
     */
    private void applySecretPropertySource(ConfigurableEnvironment environment, MapPropertySource secretsPropertySource) {

        // get position or use default position
        var position = (String) secretsPropertySource.getProperty(PROPERTY_SOURCE_POSITION);
        position = requireNonNullElse(position, POSITION_BEFORE).toUpperCase(Locale.ROOT);

        // get reference and validate if needed
        var reference = (String) secretsPropertySource.getProperty(PROPERTY_SOURCE_REFERENCE);
        if (reference == null && Set.of(POSITION_BEFORE, POSITION_AFTER).contains(position)) {
            log.error("Property source reference should be set");
            throw new IllegalArgumentException("Property source reference should be set");
        }

        // add secret property source at desired position
        switch (position) {
            case POSITION_BEFORE:
                assert reference != null;
                environment.getPropertySources().addBefore(reference, secretsPropertySource);
                break;
            case POSITION_AFTER:
                assert reference != null;
                environment.getPropertySources().addAfter(reference, secretsPropertySource);
                break;
            case POSITION_LAST:
                environment.getPropertySources().addLast(secretsPropertySource);
                break;
            case POSITION_FIRST:
                environment.getPropertySources().addFirst(secretsPropertySource);
                break;
            default:
                log.error("Invalid property source position: " + position);
                throw new IllegalArgumentException("Invalid property source position: " + position);
        }

    }
}
