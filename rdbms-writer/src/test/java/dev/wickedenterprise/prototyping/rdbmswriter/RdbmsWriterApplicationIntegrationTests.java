package dev.wickedenterprise.prototyping.rdbmswriter;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RdbmsWriterApplicationIntegrationTests {

	@Autowired
	private LogEntryRepository logEntryRepository;

	@Test
	@Ignore
	public void contextLoads() {
		/* Check for unique addresses, should be equal to "Users" in neo4j DB */
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

		/* Check for unique trustlines, should be equal to "TRUSTS" in neo4j DB */
		allentries = logEntryRepository.findAll();
		Set<String> distinctTreTee = allentries.stream().map(it -> it.getTrusteeAddress() + it.getTrusterAddress()).collect(Collectors.toSet());
		log.info("Distinct (J8-Stream-Version), TRUSTS in neo4j: " + distinctTreTee.size());
		long numberFromDb = logEntryRepository.countDistinctByTrusteeAndTrusterAddress();
		log.info("Distinct (JPA-Data-Version),  TRUSTS in neo4j: " + numberFromDb);

	}
}
