package dev.wickedenterprise.prototyping.neo4jwriter;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {Neo4jWriterApplicationTests.Initializer.class})
public class Neo4jWriterApplicationTests {

	@ClassRule
	public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:management");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.rabbitmq.addresses=" + rabbitMQContainer.getAmqpUrl(),
					"spring.rabbitmq.username=" + rabbitMQContainer.getAdminUsername(),
					"spring.rabbitmq.password=" + rabbitMQContainer.getAdminPassword(),
					"spring.neo4j.uri=" + neo4jContainer.getBoltUrl(),
					"spring.neo4j.authentication.username=neo4j",
					"spring.neo4j.authentication.password=neo",
					"spring.data.neo4j.database=neo4j"

			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@ClassRule
	public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j").withAdminPassword("neo");

	@Test
	public void contextLoads() {
	}

}
