package dev.wickedenterprise.prototyping.blockchainlistener;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.BatchContractEventPull;
import dev.wickedenterprise.prototyping.blockchainlistener.listener.ScheduledContractEventPull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigInteger;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class BlockchainListenerApplication implements CommandLineRunner {

	public static final String topicExchangeName = "evm-events";

	public static final String rdbmsQueueName = "rdbms-update";
	public static final String rdbmsRouteName = "evm.events.rdbms";

	public static final String graphQueueName = "graph-update";
	public static final String graphRouteName = "evm.events.neo4j";

	private final BatchContractEventPull batchContractEventPull;
	private final ScheduledContractEventPull scheduledContractEventPull;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Value("${spring.rabbitmq.addresses}")
	private String rabbitAddress;

	@Value("${ethereum.rpc.url}")
	private String ethereumNodeAddress;

	@Value("${start.block}")
	private String startBlock;


	public BlockchainListenerApplication(BatchContractEventPull batchContractEventPull, ScheduledContractEventPull scheduledContractEventPull, ApplicationEventPublisher applicationEventPublisher) {
		this.batchContractEventPull = batchContractEventPull;
		this.scheduledContractEventPull = scheduledContractEventPull;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public static void main(String[] args) {
		SpringApplication.run(BlockchainListenerApplication.class, args);
	}

	@Override
	public void run(String... args) throws InterruptedException {
		log.info("Using external systems: RabbitMQ at {}, Ethereum Node at {}", rabbitAddress, ethereumNodeAddress);
		log.info("Start pulling events at Block #{}", startBlock);
		boolean isReady = batchContractEventPull.waitForExternalSystems();
		if (!isReady) {
			log.error("Cannot start system as external systems are not available.");
			System.exit(1);
		}
		AvailabilityChangeEvent.publish(applicationEventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
		BigInteger lastProcessedBlock = batchContractEventPull.runBatch();
		if (!lastProcessedBlock.equals(BigInteger.ZERO)) {
			scheduledContractEventPull.setLastProcessedBlock(lastProcessedBlock);
		} else {
			throw new IllegalStateException("Cannot read from web3j. Aborting.");
		}
	}
}
