package ru.aasmc.historymanager.events.service

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.aasmc.historymanager.loadJsonFromFile

private const val DLT = "events.public.crud_events.DLT"
private val log = LoggerFactory.getLogger(KafkaEventConsumerTest::class.java)
private const val TOPIC = "events.public.crud_events"
private const val VALID_EVENT = "events/valid.json"
private const val INVALID_EVENT = "events/invalid.json"

@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class KafkaEventConsumerTest {

    @Autowired
    private lateinit var template: KafkaTemplate<String, String>

    companion object {
        @Container
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"))

        @JvmStatic
        private lateinit var kafkaConsumer: KafkaConsumer<String, String>

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("kafkaprops.bootstrapServers", kafka::getBootstrapServers)
        }

        @JvmStatic
        @BeforeAll
        fun setup() {
            val consumerProps = KafkaTestUtils.consumerProps(
                kafka.bootstrapServers,
                "test-consumer",
                "true"
            )
            consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            kafkaConsumer = KafkaConsumer(consumerProps)
            kafkaConsumer.subscribe(listOf(DLT))
        }

        @JvmStatic
        @AfterAll
        fun close() {
            kafkaConsumer.close()
        }
    }

    @Test
    fun should_not_send_to_dlt_for_valid_message() {
        val eventString = loadJsonFromFile(VALID_EVENT)
        template.send(TOPIC, eventString)
            .addCallback(
                { res -> log.info("Success: {}", res) },
                { ex -> log.info("Failure: {}", ex.message) }
            )
        assertThrows(
            IllegalStateException::class.java
        ) {
            KafkaTestUtils.getSingleRecord(kafkaConsumer, DLT, 5000L)
        }
    }

    @Test
    fun should_send_to_dlt_when_message_invalid() {
        val eventString = loadJsonFromFile(INVALID_EVENT)
        template.send(TOPIC, eventString)
            .addCallback(
                { res -> log.info("Success: {}", res) },
                { ex -> log.info("Failure: {}", ex.message) }
            )

        val record = KafkaTestUtils.getSingleRecord(kafkaConsumer, DLT, 2000)
        val headers = record.headers()
        assertThat(headers).map<String, RuntimeException> { obj: Header -> obj.key() }
            .containsAll(
                listOf(
                    "kafka_dlt-exception-fqcn",
                    "kafka_dlt-exception-cause-fqcn",
                    "kafka_dlt-exception-message",
                    "kafka_dlt-exception-stacktrace",
                    "kafka_dlt-original-topic",
                    "kafka_dlt-original-partition",
                    "kafka_dlt-original-offset",
                    "kafka_dlt-original-timestamp",
                    "kafka_dlt-original-timestamp-type",
                    "kafka_dlt-original-consumer-group"
                )
            )
        assertThat(String(headers.lastHeader("kafka_dlt-exception-fqcn").value()))
            .isEqualTo("org.springframework.kafka.listener.ListenerExecutionFailedException")
        assertThat(String(headers.lastHeader("kafka_dlt-exception-cause-fqcn").value()))
            .isEqualTo("com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException")
        assertThat(record.value()).isEqualTo(eventString)
    }

}

























