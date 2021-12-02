package dev.wickedenterprise.prototyping.neo4jwriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
