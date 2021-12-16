package dev.wickedenterprise.prototyping.rdbmswriter;

import lombok.extern.slf4j.Slf4j;
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
import org.testcontainers.containers.RabbitMQContainer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@ContextConfiguration(initializers = RdbmsWriterApplicationIntegrationTests.Initializer.class)
public class RdbmsWriterApplicationIntegrationTests {

	@Autowired
	private LogEntryRepository logEntryRepository;

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

	@Test
	//@Ignore
	public void contextLoads() {
		/* Check for unique addresses, should be equal to "Users" in neo4j DB */
		List<LogEntryEntity> allentries = logEntryRepository.findAll();
		log.info("Number of entries: " + allentries.size());
		Set<String> uniqueTee = allentries.stream().map(it -> it.trusteeAddress).collect(Collectors.toSet());
		Set<String> uniqueTre = allentries.stream().map(it -> it.trusterAddress).collect(Collectors.toSet());
		log.info("Unique Trustees: " + uniqueTee.size());
		log.info("Unique Trusters: " + uniqueTre.size());
		try (Writer fw = new FileWriter("users.csv")) {
			for (String user : uniqueTee) {
				fw.write(user + "\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		Set<String> allUnique = new HashSet<>();
		allUnique.addAll(uniqueTre);
		allUnique.addAll(uniqueTee);
		log.info("Unique all: " + allUnique.size());

		/* Check for unique trustlines, should be equal to "TRUSTS" in neo4j DB */
		allentries = logEntryRepository.findAll();
		Set<String> distinctTreTee = allentries.stream().map(it -> it.getTrusteeAddress() + it.getTrusterAddress()).collect(Collectors.toSet());
		log.info("Distinct (J8-Stream-Version), TRUSTS in neo4j: " + distinctTreTee.size());
		long numberFromDb = logEntryRepository.countDistinctByTrusteeAndTrusterAddress();
		log.info("Distinct (JPA-Data-Version),  TRUSTS in neo4j: " + numberFromDb);

	}
}
