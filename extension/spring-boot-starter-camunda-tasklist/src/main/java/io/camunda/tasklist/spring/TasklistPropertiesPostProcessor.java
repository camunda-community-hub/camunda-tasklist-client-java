package io.camunda.tasklist.spring;

import io.camunda.tasklist.spring.TasklistClientConfigurationProperties.Profile;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

public class TasklistPropertiesPostProcessor implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    try {
      Profile profile = environment.getProperty("tasklist.client.profile", Profile.class);
      if (profile == null) {
        return;
      }
      YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
      String propertiesFile = "tasklist-profiles/" + determinePropertiesFile(profile);
      ClassPathResource resource = new ClassPathResource(propertiesFile);
      List<PropertySource<?>> props = loader.load(propertiesFile, resource);
      for (PropertySource<?> prop : props) {
        environment.getPropertySources().addLast(prop); // lowest priority
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error while post processing camunda properties", e);
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
