package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import dev.wickedenterprise.prototyping.blockchainlistener.BlockchainListenerApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

@Slf4j
@Component
public class HealthIndicatorUtil {

    private final RabbitTemplate rabbitTemplate;
    private final Web3j httpWeb3j;

    public HealthIndicatorUtil(RabbitTemplate rabbitTemplate, Web3j httpWeb3j) {
        this.rabbitTemplate = rabbitTemplate;
        this.httpWeb3j = httpWeb3j;
    }

    public boolean canSendTransactionsToQueues() {
        try {
            LogEntry logEntry = new LogEntry.LogEntryBuilder().trusterAddress("0x0").trusteeAddress("0x0").amount(BigInteger.ZERO).blockNumber(BigInteger.ZERO).build();
            rabbitTemplate.convertAndSend(BlockchainListenerApplication.topicExchangeName, BlockchainListenerApplication.rdbmsRouteName, logEntry);
            rabbitTemplate.convertAndSend(BlockchainListenerApplication.topicExchangeName, BlockchainListenerApplication.graphRouteName, logEntry);
        } catch (Exception ex) {
            log.error("Cannot send Zero Transaction Object to Queues: " + ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public boolean isConnectedToWeb3() {
        try {
            return httpWeb3j.ethBlockNumber().send().getBlockNumber().intValue() > 0;
        } catch (Exception e) {
            log.error("Cannot read current block number: {}", e.getMessage());
            return false;
        }
    }
}
