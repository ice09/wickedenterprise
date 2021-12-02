package dev.wickedenterprise.prototyping.neo4jwriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class CustomMessageListener {

    private final GraphUpdateService graphUpdateService;
    private static final Logger log = LoggerFactory.getLogger(CustomMessageListener.class);

    public CustomMessageListener(GraphUpdateService graphUpdateService) {
        this.graphUpdateService = graphUpdateService;
    }

    @RabbitListener(queues = Neo4jWriterApplication.queueName)
    public void receiveMessage(final LogEntry message) {
        log.info("Received message as a generic AMQP 'Message' wrapper: {}", message.toString());
        graphUpdateService.addTrustLine(message);
    }

}