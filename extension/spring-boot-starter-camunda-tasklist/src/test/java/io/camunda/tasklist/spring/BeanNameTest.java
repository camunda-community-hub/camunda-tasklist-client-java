package io.camunda.tasklist.spring;

import static org.assertj.core.api.Assertions.*;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.CamundaTasklistClientConfiguration;
import io.camunda.tasklist.auth.Authentication;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = "tasklist.client.profile=simple")
public class BeanNameTest {
  @Autowired ApplicationContext applicationContext;

  @TestFactory
  Stream<DynamicTest> shouldHaveBeanName() {
    return Stream.of(
            applicationContext.getBeanNamesForType(Authentication.class),
            applicationContext.getBeanNamesForType(CamundaTaskListClient.class),
            applicationContext.getBeanNamesForType(CamundaTasklistClientConfiguration.class))
        .map(s -> DynamicTest.dynamicTest(s[0], () -> testBeanName(s)));
  }

  private void testBeanName(String[] beanNames) {
    assertThat(beanNames).hasSize(1);
    assertThat(beanNames[0]).containsIgnoringCase("tasklist");
  }
}
