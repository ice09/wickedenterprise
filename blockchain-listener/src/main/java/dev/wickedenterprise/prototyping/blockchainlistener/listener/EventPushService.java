package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import dev.wickedenterprise.prototyping.blockchainlistener.BlockchainListenerApplication;
import dev.wickedenterprise.prototyping.blockchainlistener.user.api.Data;
import dev.wickedenterprise.prototyping.blockchainlistener.user.service.EnrichmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventPushService {

    private Map<String, Data> userDataCache = new HashMap<>();

    private final EnrichmentService enrichmentService;
    private final RabbitTemplate template;

    public EventPushService(EnrichmentService enrichmentService, RabbitTemplate template) {
        this.enrichmentService = enrichmentService;
        this.template = template;
    }

    private Data createUserDto(String truster, Data trusterDto) {
        if (trusterDto == null) {
            List<Data> userData = enrichmentService.enrichUserAddress(truster).getData();
            if (!userData.isEmpty()) {
                trusterDto = userData.get(0);
            } else {
                trusterDto = Data.builder().safeAddress(truster).build();
            }
        }
        return trusterDto;
    }

    public void push(LogEntry logEntry) {
        Data trusterDto = userDataCache.get(logEntry.getTrusterAddress());
        if (trusterDto == null) {
            trusterDto = createUserDto(logEntry.getTrusterAddress(), trusterDto);
        }
        Data trusteeDto = userDataCache.get(logEntry.getTrusteeAddress());
        if (trusteeDto == null) {
            trusteeDto = createUserDto(logEntry.getTrusteeAddress(), trusteeDto);
        }
        userDataCache.put(logEntry.getTrusterAddress(), trusterDto);
        userDataCache.put(logEntry.getTrusteeAddress(), trusteeDto);
        logEntry.setTrusteeAvatar(trusteeDto.getAvatarUrl());
        logEntry.setTrusteeName(trusteeDto.getUsername());
        logEntry.setTrusterAvatar(trusterDto.getAvatarUrl());
        logEntry.setTrusterName(trusterDto.getUsername());
        template.convertAndSend(BlockchainListenerApplication.topicExchangeName, BlockchainListenerApplication.rdbmsRouteName, logEntry);
        template.convertAndSend(BlockchainListenerApplication.topicExchangeName, BlockchainListenerApplication.graphRouteName, logEntry);
        log.debug("Created {}, {}, {}", logEntry.getBlockNumber(), logEntry.getTrusterAddress(), logEntry.getTrusteeAddress());
    }
}
