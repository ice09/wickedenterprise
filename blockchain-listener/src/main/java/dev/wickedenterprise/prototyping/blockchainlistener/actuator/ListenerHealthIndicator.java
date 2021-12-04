package dev.wickedenterprise.prototyping.blockchainlistener.actuator;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.ContractEventPull;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class ListenerHealthIndicator implements HealthIndicator {

    private final ContractEventPull contractEventPull;

    public ListenerHealthIndicator(ContractEventPull contractEventPull) {
        this.contractEventPull = contractEventPull;
    }

    @Override
    public Health health() {
        if (contractEventPull.getCurrentBlock().compareTo(BigInteger.ZERO) > 0) {
            return Health.up().build();
        } else {
            return Health.down().build();
        }
    }

}
