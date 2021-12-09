package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import dev.wickedenterprise.Hub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class ContractEventPull {

    private final Hub hub;
    private final Web3j httpWeb3j;
    private final EventPushService eventPushService;

    public ContractEventPull(Web3j httpWeb3j, Hub hub, EventPushService eventPushService) {
        this.hub = hub;
        this.httpWeb3j = httpWeb3j;
        this.eventPushService = eventPushService;
    }

    public void readEventsFromContract(BigInteger from, BigInteger to) throws IOException {
        EthFilter eventFilter = new EthFilter(DefaultBlockParameter.valueOf(from), DefaultBlockParameter.valueOf(to), hub.getContractAddress());
        String encodedEventSignature = EventEncoder.encode(Hub.TRUST_EVENT);
        eventFilter.addSingleTopic(encodedEventSignature);
        Request<?, EthLog> resReg = httpWeb3j.ethGetLogs(eventFilter);
        List<EthLog.LogResult> regLogs = resReg.send().getLogs();
        if (regLogs != null) {
            for (int i = 0; i < regLogs.size(); i++) {
                Log lastLogEntry = ((EthLog.LogObject) regLogs.get(i));
                List<String> ethLogTopics = lastLogEntry.getTopics();
                BigInteger amount = BigInteger.ZERO;
                String truster = ethLogTopics.get(1).substring(26);
                String trustee = ethLogTopics.get(2).substring(26);
                log.debug("user | canSendTo : | 0x" + trustee + " | 0x" + truster);
                try {
                    amount = new BigInteger(StringUtils.trimLeadingCharacter(lastLogEntry.getData().substring(2), '0'), 16);
                } catch (Exception ex) {
                    log.warn("Cannot convert amount, most possible 0. Setting to default value (0).");
                }
                LogEntry logEntry = new LogEntry.LogEntryBuilder()
                        .trusterAddress("0x" + truster)
                        .trusteeAddress("0x" + trustee)
                        .amount(amount)
                        .blockNumber(lastLogEntry.getBlockNumber()).build();
                eventPushService.push(logEntry);
            }
        } else {
            log.debug("No events found.");
        }
    }

    public BigInteger getCurrentBlock() {
        try {
            return httpWeb3j.ethBlockNumber().send().getBlockNumber();
        } catch (Exception e) {
            log.error("Cannot read current block number: {}", e.getMessage());
            return BigInteger.ZERO;
        }
    }

}
