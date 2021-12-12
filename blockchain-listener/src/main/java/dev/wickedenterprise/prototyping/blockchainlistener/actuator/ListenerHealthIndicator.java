package dev.wickedenterprise.prototyping.blockchainlistener.actuator;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.HealthIndicatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListenerHealthIndicator implements HealthIndicator {

    private final HealthIndicatorUtil healthIndicatorUtil;

    public ListenerHealthIndicator(HealthIndicatorUtil healthIndicatorUtil) {
        this.healthIndicatorUtil = healthIndicatorUtil;
    }

    @Override
    public Health health() {
        log.info("Healthcheck called.");
        boolean isConnectedToWeb3 = healthIndicatorUtil.isConnectedToWeb3();
        boolean canSendToQueues = healthIndicatorUtil.canSendTransactionsToQueues();
        log.info("Status {}, {}", isConnectedToWeb3, canSendToQueues);
        if (isConnectedToWeb3 && canSendToQueues) {
            return Health.up().build();
        } else {
            if (!isConnectedToWeb3) {
                log.error("No connection to Web3, Health is down.");
            }
            if (!canSendToQueues) {
                log.error("Cannot send to Queues, Health is down.");
            }
            return Health.down().build();
        }
    }

}
