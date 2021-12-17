package dev.wickedenterprise.prototyping.blockchainlistener.config;

import dev.wickedenterprise.Hub;
import dev.wickedenterprise.prototyping.blockchainlistener.BlockchainListenerApplication;
import okhttp3.OkHttpClient;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.concurrent.TimeUnit;

@Configuration
public class MonitorConfig {

    private final String ethereumRpcUrl;
    private final String hubAddress;
    private final String mnemonic;

    public MonitorConfig(@Value("${ethereum.rpc.url}") String ethereumRpcUrl, @Value("${circles.hub.address}") String hubAddress, @Value("${web3.mnemonic}") String mnemonic) {
        this.ethereumRpcUrl = ethereumRpcUrl;
        this.hubAddress = hubAddress;
        this.mnemonic = mnemonic;
    }

    @Bean
    public CredentialHolder createCredentials() {
        return new CredentialHolder(createMasterKeyPair());
    }

    public Bip32ECKeyPair createMasterKeyPair() {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");
        return Bip32ECKeyPair.generateKeyPair(seed);
    }

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(ethereumRpcUrl, createOkHttpClient()));
    }

    @Bean
    public Hub createTrustHubProxy() throws Exception {
        if (hubAddress != null) {
            return Hub.load(hubAddress, web3j(), createCredentials().deriveChildKeyPair(0), new DefaultGasProvider());
        } else {
            return Hub.deploy(web3j(), createCredentials().deriveChildKeyPair(0), new DefaultGasProvider()).send();
        }
    }

    @Bean
    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    Queue queueRdbms() {
        return new Queue(BlockchainListenerApplication.rdbmsQueueName, false);
    }

    @Bean
    Queue queueGraph() {
        return new Queue(BlockchainListenerApplication.graphQueueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(BlockchainListenerApplication.topicExchangeName);
    }

    @Bean
    Binding bindingRdbms(TopicExchange exchange) {
        return BindingBuilder.bind(queueRdbms()).to(exchange).with(BlockchainListenerApplication.rdbmsRouteName);
    }

    @Bean
    Binding bindingGraph(TopicExchange exchange) {
        return BindingBuilder.bind(queueGraph()).to(exchange).with(BlockchainListenerApplication.graphRouteName);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        configureTimeouts(builder);
        return builder.build();
    }

    private void configureTimeouts(OkHttpClient.Builder builder) {
        long tos = 800000L;
        builder.connectTimeout(tos, TimeUnit.SECONDS);
        builder.readTimeout(tos, TimeUnit.SECONDS);  // Sets the socket timeout too
        builder.writeTimeout(tos, TimeUnit.SECONDS);
    }

}
