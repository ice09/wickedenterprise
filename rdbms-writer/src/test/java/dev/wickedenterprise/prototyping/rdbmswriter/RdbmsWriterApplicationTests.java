package dev.wickedenterprise.prototyping.rdbmswriter;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {RdbmsWriterApplicationTests.Initializer.class})
public class RdbmsWriterApplicationTests {

	@ClassRule
	public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:management");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.rabbitmq.addresses=" + rabbitMQContainer.getAmqpUrl(),
					"spring.rabbitmq.username=" + rabbitMQContainer.getAdminUsername(),
					"spring.rabbitmq.password=" + rabbitMQContainer.getAdminPassword(),
					"spring.datasource.url=" + postgresContainer.getJdbcUrl(),
					"spring.datasource.username=" + postgresContainer.getUsername(),
					"spring.datasource.password=" + postgresContainer.getPassword(),
					"spring.datasource.driver-class-name=org.postgresql.Driver"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}



	@ClassRule
	public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres").withUsername("events").withPassword("events").withDatabaseName("events");

	@Autowired
	private LogEntryRepository logEntryRepository;

	@Test
	public void contextLoads() {
		/* Check for unique addresses, should be equal to "Users" in neo4j DB
		List<LogEntryEntity> allentries = logEntryRepository.findAll();
		log.info("Number of entries: " + allentries.size());
		Set<String> uniqueTee = allentries.stream().map(it -> it.trusteeAddress).collect(Collectors.toSet());
		Set<String> uniqueTre = allentries.stream().map(it -> it.trusterAddress).collect(Collectors.toSet());
		log.info("Unique Trustees: " + uniqueTee.size());
		log.info("Unique Trusters: " + uniqueTre.size());
		Set<String> allUnique = new HashSet<>();
		allUnique.addAll(uniqueTre);
		allUnique.addAll(uniqueTee);
		log.info("Unique all: " + allUnique.size());
		 */

		/* Check for unique trustlines, should be equal to "TRUSTS" in neo4j DB
		List<LogEntryEntity> allentries = logEntryRepository.findAll();
		Set<String> distinctTreTee = allentries.stream().map(it -> it.getTrusteeAddress() + it.getTrusterAddress()).collect(Collectors.toSet());
		log.info("Distinct (J8-Stream-Version), TRUSTS in neo4j: " + distinctTreTee.size());
		long numberFromDb = logEntryRepository.countDistinctByTrusteeAndTrusterAddress();
		log.info("Distinct (JPA-Data-Version),  TRUSTS in neo4j: " + numberFromDb);
		*/
	}
}
