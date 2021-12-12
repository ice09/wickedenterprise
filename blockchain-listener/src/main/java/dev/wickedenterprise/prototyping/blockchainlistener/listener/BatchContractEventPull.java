package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@Slf4j
public class BatchContractEventPull {

    private final ContractEventPull contractEventPull;
    private final BigInteger startBlock;
    private final BigInteger step;
    private final HealthIndicatorUtil healthIndicatorUtil;

    public BatchContractEventPull(@Value("${start.block}") BigInteger startBlock, ContractEventPull contractEventPull, HealthIndicatorUtil healthIndicatorUtil) {
        this.contractEventPull = contractEventPull;
        this.startBlock = startBlock;
        this.healthIndicatorUtil = healthIndicatorUtil;
        this.step = BigInteger.valueOf(10000);
    }

    public void waitForExternalSystems() throws InterruptedException {
        boolean rabbitAvailable;
        boolean ethereumNodeAvailable;
        for (int i=0; i<100; i++) {
            rabbitAvailable = healthIndicatorUtil.canSendTransactionsToQueues();
            ethereumNodeAvailable = healthIndicatorUtil.isConnectedToWeb3();
            if (rabbitAvailable && ethereumNodeAvailable) {
                log.info("Ready for Takeoff.");
                return;
            } else {
                log.info("Still waiting for external System startup, rabbitAvailable {}, web3Available {}, Try {}/100", rabbitAvailable, ethereumNodeAvailable, i);
                Thread.sleep(5000);
            }
        }
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
                log.error("Error while processing events: " + ex.getMessage() + ", timeout 5 secs.");
                waitFor5Seconds();
            }
        }
        return currentBlock;
    }

    private void waitFor5Seconds() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Thread exception", e);
        }
    }

}
