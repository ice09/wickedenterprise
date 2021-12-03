package dev.wickedenterprise.prototyping.rdbmswriter;

import dev.wickedenterprise.prototyping.blockchainlistener.listener.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Example;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomMessageListener {

    private final LogEntryRepository logEntryRepository;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(CustomMessageListener.class);

    public CustomMessageListener(LogEntryRepository logEntryRepository, JdbcTemplate jdbcTemplate) {
        this.logEntryRepository = logEntryRepository;
        this.jdbcTemplate = jdbcTemplate;
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS log_all_attrs_idx ON log_entry_entity (amount,block_number,trustee_address,trustee_name,trustee_avatar,truster_address,truster_avatar,truster_name)");
    }

    @RabbitListener(queues = RdbmsWriterApplication.queueName)
    public void receiveMessage(final LogEntry message) {
        log.info("Received message as a generic AMQP 'Message' wrapper: {}", message.toString());
        LogEntryEntity entity = new LogEntryEntity();
        entity.setTrusteeAddress(message.getTrusteeAddress());
        entity.setTrusteeName(message.getTrusteeName());
        entity.setTrusteeAvatar(message.getTrusteeAvatar());
        entity.setTrusterAddress(message.getTrusterAddress());
        entity.setTrusterName(message.getTrusterName());
        entity.setTrusterAvatar(message.getTrusterAvatar());
        entity.setAmount(message.getAmount());
        entity.setBlockNumber(message.getBlockNumber());
        if (!logEntryRepository.exists(Example.of(entity))) {
            logEntryRepository.save(entity);
        } else {
            log.info("Will not insert double entry: " + entity);
        }
    }

}