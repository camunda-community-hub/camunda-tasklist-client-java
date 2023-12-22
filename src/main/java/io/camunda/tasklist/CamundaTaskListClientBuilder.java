package io.camunda.tasklist;

import io.camunda.common.auth.Authentication;
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
     * Default behaviour will be to get variables along with tasks. Default value is false. Can also
     * be overwritten in the getTasks methods
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

    public CamundaTaskListClient build() throws TaskListException {
      return new CamundaTaskListClient(properties);
    }

    private String formatUrl(String url) {
      if (url.endsWith("/")) {
        return url.substring(0, url.length() - 1);
      }
      return url;
    }
}
