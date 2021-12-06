package dev.wickedenterprise.prototyping.blockchainlistener;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.BatchContractEventPull;
import dev.wickedenterprise.prototyping.blockchainlistener.listener.ScheduledContractEventPull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.math.BigInteger;

@SpringBootApplication
@EnableScheduling
public class BlockchainListenerApplication implements CommandLineRunner {

	public static final String topicExchangeName = "evm-events";

	public static final String rdbmsQueueName = "rdbms-update";
	public static final String rdbmsRouteName = "evm.events.rdbms";

	public static final String graphQueueName = "graph-update";
	public static final String graphRouteName = "evm.events.neo4j";

	private final BatchContractEventPull batchContractEventPull;
	private final ScheduledContractEventPull scheduledContractEventPull;

	public BlockchainListenerApplication(BatchContractEventPull batchContractEventPull, ScheduledContractEventPull scheduledContractEventPull) {
		this.batchContractEventPull = batchContractEventPull;
		this.scheduledContractEventPull = scheduledContractEventPull;
	}

	public static void main(String[] args) {
		SpringApplication.run(BlockchainListenerApplication.class, args);
	}

	@Override
	public void run(String... args) throws InterruptedException {
		batchContractEventPull.waitForExternalSystems();
		BigInteger lastProcessedBlock = batchContractEventPull.runBatch();
		if (!lastProcessedBlock.equals(BigInteger.ZERO)) {
			scheduledContractEventPull.setLastProcessedBlock(lastProcessedBlock);
		} else {
			throw new IllegalStateException("Cannot read from web3j. Aborting.");
		}
	}
}
