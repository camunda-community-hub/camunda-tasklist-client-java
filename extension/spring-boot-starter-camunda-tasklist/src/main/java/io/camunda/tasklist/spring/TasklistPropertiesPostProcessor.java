package io.camunda.tasklist.spring;

import io.camunda.tasklist.spring.TasklistClientConfigurationProperties.Profile;
import java.io.IOException;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

public class TasklistPropertiesPostProcessor implements EnvironmentPostProcessor {
  private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    try {
      Profile profile = environment.getProperty("tasklist.client.profile", Profile.class);
      if (profile == null) {
        return;
      }
      loadProperties("tasklist-profiles/" + determinePropertiesFile(profile), environment);
      loadProperties("tasklist-profiles/defaults.yaml", environment);
    } catch (Exception e) {
      throw new IllegalStateException("Error while post processing camunda properties", e);
    }
  }

  private void loadProperties(String propertiesFile, ConfigurableEnvironment environment)
      throws IOException {
    ClassPathResource resource = new ClassPathResource(propertiesFile);
    List<PropertySource<?>> props = loader.load(propertiesFile, resource);
    for (PropertySource<?> prop : props) {
      environment.getPropertySources().addLast(prop); // lowest priority
    }
  }

  private String determinePropertiesFile(Profile clientMode) {
    switch (clientMode) {
      case oidc -> {
        return "oidc.yaml";
      }
      case saas -> {
        return "saas.yaml";
      }
      case simple -> {
        return "simple.yaml";
      }
    }
    throw new IllegalStateException("Unknown client mode " + clientMode);
  }
}
