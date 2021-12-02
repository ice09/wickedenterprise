package dev.wickedenterprise.prototyping.rdbmswriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigInteger;

@Entity
@Data
public class LogEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String trusterAddress;
    String trusterName;
    String trusterAvatar;

    String trusteeAddress;
    String trusteeName;
    String trusteeAvatar;

    BigInteger amount;
    BigInteger blockNumber;
}
