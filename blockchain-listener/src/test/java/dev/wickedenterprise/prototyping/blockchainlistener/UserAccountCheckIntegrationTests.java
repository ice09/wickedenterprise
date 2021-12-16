package dev.wickedenterprise.prototyping.blockchainlistener;

import dev.wickedenterprise.Hub;
import dev.wickedenterprise.prototyping.blockchainlistener.listener.BatchContractEventPull;
import dev.wickedenterprise.prototyping.blockchainlistener.listener.ContractEventPull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "blockchain.listener.schedule=-" })
@ContextConfiguration(initializers = {UserAccountCheckIntegrationTests.Initializer.class})
@Slf4j
public class UserAccountCheckIntegrationTests {

	@Autowired
	private Hub hub;

	@Autowired
	private Web3j httpWeb3j;

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

		@Bean
		@Primary
		public BatchContractEventPull mockBeanBatchContractEventPull() throws InterruptedException {
			BatchContractEventPull bcep = mock(BatchContractEventPull.class);
			when(bcep.runBatch()).thenReturn(BigInteger.ONE);
			when(bcep.waitForExternalSystems()).thenReturn(true);
			return bcep;
		}

	}

	@Test
	public void contextLoads() throws Exception {
		String allUsersAsString = FileUtils.readFileToString(new File("src/test/resources/users.csv"), StandardCharsets.UTF_8);
		Set<String> allUsers = Set.of(allUsersAsString.split("\n"));
		Set<String> usersMintedLast3Months = new HashSet<>();
		BigInteger currentBN = httpWeb3j.ethBlockNumber().send().getBlockNumber();
		for (int i = currentBN.subtract(BigInteger.valueOf(1555200)).intValue(); i<currentBN.intValue(); i++) {
			EthBlock block = httpWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(BigInteger.valueOf(i)), true).send();
			String hash = block.getBlock().getHash();
			//EthFilter eventFilter = new EthFilter(DefaultBlockParameter.valueOf(currentBN.subtract(BigInteger.valueOf(10000))), DefaultBlockParameter.valueOf(httpWeb3j.ethBlockNumber().send().getBlockNumber()), token);
			EthFilter eventFilter = new EthFilter(hash);
			String encodedEventSignature = EventEncoder.encode(ERC20.TRANSFER_EVENT);
			eventFilter.addSingleTopic(encodedEventSignature);
			Request<?, EthLog> resReg = httpWeb3j.ethGetLogs(eventFilter);
			List<EthLog.LogResult> regLogs = resReg.send().getLogs();
			String blockTimestamp = formatTimestamp(block);
			log.info("Scanning block #{}/{}, hash {}, got {} Transfer events, timestamp is {}", i, currentBN.intValue(), hash, regLogs.size(), blockTimestamp);
			if (regLogs != null) {
				for (EthLog.LogResult logObject : regLogs) {
					Log lastLogEntry = (EthLog.LogObject) logObject;
					List<String> ethLogTopics = lastLogEntry.getTopics();
					String sender = ethLogTopics.get(1).substring(26);
					String receiver = ethLogTopics.get(2).substring(26);
					if (allUsers.contains("0x" + receiver) && sender.startsWith("00000000000000000000")) {
						String printedDate = formatTimestamp(block);
						String token = hub.userToToken("0x" + receiver).send();
						String eventContractAddress = lastLogEntry.getAddress();
						if (token.equals(eventContractAddress)) {
							log.info("{} minted at block {} at {}", "0x" + receiver, lastLogEntry.getBlockNumber(), printedDate);
							usersMintedLast3Months.add("0x" + receiver);
						}
					}
				}
			}
		}
		System.out.println("Users which minted last 3 Months");
		for (String user : usersMintedLast3Months) {
			System.out.println(user);
		}

	}

	@NotNull
	private String formatTimestamp(EthBlock block) {
		BigInteger timestamp = block.getBlock().getTimestamp();
		String printedDate = new DateFormatter("dd.MM.yyyy").print(new Date(timestamp.longValue() * 1000), Locale.GERMAN);
		return printedDate;
	}

}