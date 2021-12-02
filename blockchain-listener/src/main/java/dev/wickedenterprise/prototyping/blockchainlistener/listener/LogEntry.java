package dev.wickedenterprise.prototyping.blockchainlistener.listener;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@Builder
public class LogEntry implements Serializable {

    @JsonProperty
    String trusterAddress;
    @JsonProperty
    String trusterName;
    @JsonProperty
    String trusterAvatar;

    @JsonProperty
    String trusteeAddress;
    @JsonProperty
    String trusteeName;
    @JsonProperty
    String trusteeAvatar;

    @JsonProperty
    BigInteger amount;
    @JsonProperty
    BigInteger blockNumber;

}
