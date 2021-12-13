package dev.wickedenterprise.prototyping.blockchainlistener.actuator;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.HealthIndicatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.stereotype.Component;

@Component("externalSystemsHealthIndicator")
@Slf4j
public class ExternalSystemsHealthIndicator extends ReadinessStateHealthIndicator {

    private final HealthIndicatorUtil healthIndicatorUtil;

    public ExternalSystemsHealthIndicator(ApplicationAvailability availability, HealthIndicatorUtil healthIndicatorUtil) {
        super(availability);
        this.healthIndicatorUtil = healthIndicatorUtil;
    }

    @Override
    protected AvailabilityState getState(ApplicationAvailability applicationAvailability) {
        return checkHealthState() == Status.UP ? ReadinessState.ACCEPTING_TRAFFIC : ReadinessState.REFUSING_TRAFFIC;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.status(checkHealthState());
    }

    private Status checkHealthState() {
        log.info("Healthcheck called.");
        boolean isConnectedToWeb3 = healthIndicatorUtil.isConnectedToWeb3();
        boolean canSendToQueues = healthIndicatorUtil.canSendTransactionsToQueues();
        log.info("Status {}, {}", isConnectedToWeb3, canSendToQueues);
        if (isConnectedToWeb3 && canSendToQueues) {
            return Status.UP;
        } else {
            if (!isConnectedToWeb3) {
                log.error("No connection to Web3, Health is down.");
            }
            if (!canSendToQueues) {
                log.error("Cannot send to Queues, Health is down.");
            }
            return Status.OUT_OF_SERVICE;
        }
    }
}
