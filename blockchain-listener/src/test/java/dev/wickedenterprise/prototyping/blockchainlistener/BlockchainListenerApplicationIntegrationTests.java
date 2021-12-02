package dev.wickedenterprise.prototyping.blockchainlistener;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.ContractEventPull;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.RabbitMQContainer;

import java.math.BigInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {BlockchainListenerApplicationIntegrationTests.Initializer.class})
public class BlockchainListenerApplicationIntegrationTests {

	@ClassRule
	public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:management");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.rabbitmq.addresses=" + rabbitMQContainer.getAmqpUrl(),
					"spring.rabbitmq.username=" + rabbitMQContainer.getAdminUsername(),
					"spring.rabbitmq.password=" + rabbitMQContainer.getAdminPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@TestConfiguration
	public static class TestConfig {

		@Bean
		@Primary
		public ContractEventPull mockBeanContractEventPull() {
			ContractEventPull std = mock(ContractEventPull.class);
			when(std.getCurrentBlock()).thenReturn(BigInteger.ONE);
			return std;
		}

	}

	@Test
	public void contextLoads() {
	}
}