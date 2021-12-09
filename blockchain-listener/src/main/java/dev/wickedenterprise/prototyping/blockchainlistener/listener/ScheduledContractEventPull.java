package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@Slf4j
public class ScheduledContractEventPull {

    private final ContractEventPull contractEventPull;

    private BigInteger lastProcessedBlock = BigInteger.ZERO;

    public ScheduledContractEventPull(ContractEventPull contractEventPull) {
        this.contractEventPull = contractEventPull;
    }

    @Scheduled(fixedRate = 1000)
    public void processContractEvents() {
        if (lastProcessedBlock.intValue() > 0) {
            BigInteger currentBlock = contractEventPull.getCurrentBlock();
            if (currentBlock.intValue() > 0) {
                if (currentBlock.intValue() < lastProcessedBlock.intValue()) {
                    log.info("Processing blocks {} to {}", lastProcessedBlock, currentBlock);
                    try {
                        contractEventPull.readEventsFromContract(lastProcessedBlock, currentBlock);
                        lastProcessedBlock = currentBlock;
                    } catch (Exception ex) {
                        log.error("Error in reading events: " + ex.getMessage());
                    }
                } else {
                    log.info("Current Block is below latest processed Block, aborting this schedule.");
                }
            } else {
                log.info("Cannot retrieve current Block, aborting this schedule.");
            }
        } else {
            log.info("Still in batch processing. Skipping Scheduler.");
        }
    }

    public void setLastProcessedBlock(BigInteger lastProcessedBlock) {
        this.lastProcessedBlock = lastProcessedBlock;
    }
}
