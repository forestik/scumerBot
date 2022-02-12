package com.crypto.envpostprocessor;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.springframework.boot.logging.DeferredLogFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of the {@link ExternalEnvironmentPostProcessor} that receives data from AWS secret
 */
public class AwsSecretsManagerEnvironmentPostProcessor extends ExternalEnvironmentPostProcessor {

    private static final String SECRET_PROPERTY_NAME = "aws-secret-name";

    public AwsSecretsManagerEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory, SECRET_PROPERTY_NAME);
    }

    @Override
    protected String getSecretJsonString(String secretName) {
        AWSSecretsManager client = null;
        try {
            client = AWSSecretsManagerClientBuilder.standard().build();
            GetSecretValueRequest secretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
            GetSecretValueResult secretValueResult = client.getSecretValue(secretValueRequest);
            if (secretValueResult.getSecretString() != null) {
                return secretValueResult.getSecretString();
            } else {
                return new String(secretValueResult.getSecretBinary().array(), UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to load secret json");
            throw e;
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }
}
