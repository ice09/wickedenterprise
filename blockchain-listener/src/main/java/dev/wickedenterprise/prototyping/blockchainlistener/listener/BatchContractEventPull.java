package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import dev.wickedenterprise.Hub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;

@Service
@Slf4j
public class BatchContractEventPull {

    private final ContractEventPull contractEventPull;
    private final BigInteger startBlock;
    private final BigInteger step;

    public BatchContractEventPull(@Value("${start.block}") BigInteger startBlock, EventPushService eventPushService, ContractEventPull contractEventPull) {
        this.contractEventPull = contractEventPull;
        this.startBlock = startBlock;
        this.step = BigInteger.valueOf(10000);
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
