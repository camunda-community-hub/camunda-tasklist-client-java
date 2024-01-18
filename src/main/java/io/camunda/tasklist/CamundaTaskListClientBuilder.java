package io.camunda.tasklist;

import java.time.Duration;

import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.JwtConfig;
import io.camunda.common.auth.JwtCredential;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SaaSAuthentication;
import io.camunda.common.auth.SelfManagedAuthentication;
import io.camunda.tasklist.exception.TaskListException;

public class CamundaTaskListClientBuilder {
    private CamundaTaskListClientProperties properties = new CamundaTaskListClientProperties();

    public CamundaTaskListClientBuilder authentication(Authentication authentication) {
        properties.authentication = authentication;
        return this;
    }

    public CamundaTaskListClientBuilder taskListUrl(String taskListUrl) {
        properties.taskListUrl = formatUrl(taskListUrl);
        return this;
    }

    /**
     * Default behaviour will be to get variables along with tasks. Default value is
     * false. Can also be overwritten in the getTasks methods
     *
     * @return the builder
     */
    public CamundaTaskListClientBuilder shouldReturnVariables() {
        properties.defaultShouldReturnVariables = true;
        return this;
    }

    public CamundaTaskListClientBuilder shouldLoadTruncatedVariables() {
        properties.defaultShouldLoadTruncatedVariables = true;
        return this;
    }

    public CamundaTaskListClientBuilder alwaysReconnect() {
        properties.alwaysReconnect = true;
        return this;
    }

    /**
     * Force cookie expiration after some time (default 3mn). Only usefull with
     * SimpleAuthentication
     */
    public CamundaTaskListClientBuilder cookieExpiration(Duration cookieExpiration) {
        properties.cookieExpiration = cookieExpiration;
        return this;
    }

    public CamundaTaskListClient build() throws TaskListException {
        return new CamundaTaskListClient(properties);
    }
    
    public CamundaTaskListClientBuilder selfManagedAuthentication(String clientId, String clientSecret, String keycloakUrl) {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.addProduct(Product.TASKLIST, new JwtCredential(clientId, clientSecret, null, null));
        properties.authentication = SelfManagedAuthentication.builder().jwtConfig(jwtConfig).keycloakUrl(keycloakUrl).build();
        return this;
    }
    
    public CamundaTaskListClientBuilder saaSAuthentication(String clientId, String clientSecret) {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.addProduct(Product.TASKLIST, new JwtCredential(clientId, clientSecret, "tasklist.camunda.io", "https://login.cloud.camunda.io/oauth/token"));
        properties.authentication = SaaSAuthentication.builder().jwtConfig(jwtConfig).build();
        return this;
    }

    private String formatUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
