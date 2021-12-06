package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import dev.wickedenterprise.prototyping.blockchainlistener.BlockchainListenerApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@Slf4j
public class BatchContractEventPull {

    private final ContractEventPull contractEventPull;
    private final BigInteger startBlock;
    private final BigInteger step;
    private final RabbitTemplate rabbitTemplate;

    public BatchContractEventPull(@Value("${start.block}") BigInteger startBlock, EventPushService eventPushService, ContractEventPull contractEventPull, RabbitTemplate rabbitTemplate) {
        this.contractEventPull = contractEventPull;
        this.startBlock = startBlock;
        this.rabbitTemplate = rabbitTemplate;
        this.step = BigInteger.valueOf(10000);
    }

    public void waitForExternalSystems() throws InterruptedException {
        boolean rabbitAvailable;
        boolean ethereumNodeAvailable;
        for (int i=0; i<100; i++) {
            rabbitAvailable = sendZeroTransactionToAllQueues();
            BigInteger currentBlock = contractEventPull.getCurrentBlock();
            ethereumNodeAvailable = currentBlock.intValue() > 0;
            if (rabbitAvailable && ethereumNodeAvailable) {
                log.info("Ready for Takeoff.");
                return;
            } else {
                log.info("Still waiting for external System startup, rabbitAvailable {}, Current Ethereum Block {}, Try {}/100", rabbitAvailable, currentBlock, i);
                Thread.sleep(5000);
            }
        }
    }

    private boolean sendZeroTransactionToAllQueues() {
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

    public BigInteger runBatch()  {
        BigInteger index = startBlock;
        BigInteger currentBlock = contractEventPull.getCurrentBlock();
        while (currentBlock.compareTo(index) >= 0) {
            BigInteger to = index.add(step);
            if (currentBlock.compareTo(to) <= 0) {
                to = currentBlock;
            }
            log.debug("Processing from {} to {}", index, to);
            try {
                contractEventPull.readEventsFromContract(index, to);
                log.info("Processed blocks {} to {}", index, to);
                index = index.add(step);
            } catch (Exception ex) {
                log.error("Error while processing events: " + ex.getMessage());
            }
        }
        return currentBlock;
    }

}
