package ru.aasmc.taskvalidator.service.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.aasmc.taskvalidator.dto.ValidationRequest
import java.time.Duration
import java.time.LocalDateTime

private const val DLT = "validate-request.DLT"
private val log = LoggerFactory.getLogger(KafkaReplyServiceTest::class.java)
private const val TOPIC = "validate-request"

@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class KafkaReplyServiceTest {

    @Autowired
    lateinit var template: KafkaTemplate<String, ValidationRequest>

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

        @BeforeAll
        @JvmStatic
        fun setup() {
            // Create a test consumer that handles <String, String> records, listening to orders.DLT
            // https://docs.spring.io/spring-kafka/docs/3.0.x/reference/html/#testing
            val consumerProps = KafkaTestUtils.consumerProps(
                kafka.bootstrapServers,
                "test-consumer",
                "true"
            )
            consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
//            consumerProps[JsonDeserializer.TRUSTED_PACKAGES] = "ru.aasmc.taskvalidator.dto"
            kafkaConsumer = KafkaConsumer(consumerProps)
            kafkaConsumer.subscribe(listOf(DLT))
        }

        @AfterAll
        @JvmStatic
        fun close() {
            // Close the consumer before shutting down Testcontainers Kafka instance
            kafkaConsumer.close()
        }
    }

    @Test
    fun should_not_send_to_dlt_for_valid_message() {
        val validationRequest = ValidationRequest(LocalDateTime.now(), LocalDateTime.now(), 1)
        val producerRecord = ProducerRecord<String, ValidationRequest>(TOPIC, validationRequest)
        producerRecord.headers().add(KafkaHeaders.REPLY_TOPIC, "validate-response".toByteArray())
        template.send(producerRecord)
            .whenCompleteAsync {response, ex ->
                if (ex == null) {
                    log.info("Success: {}", response)
                } else {
                    log.error("FAILURE: {}", ex.message)
                }
            }

        assertThrows(
            IllegalStateException::class.java,
        ) {
            KafkaTestUtils.getSingleRecord(kafkaConsumer, DLT, Duration.ofSeconds(5))
        }
    }

    @Test
    fun should_send_to_dlt_when_message_invalid() {
        val validationRequest = ValidationRequest(LocalDateTime.now(), LocalDateTime.now(), -2)
        val producerRecord = ProducerRecord<String, ValidationRequest>(TOPIC, validationRequest)
        producerRecord.headers().add(KafkaHeaders.REPLY_TOPIC, "validate-response".toByteArray())

        template.send(producerRecord)
            .whenCompleteAsync {response, ex ->
                if (ex == null) {
                    log.info("Success: {}", response)
                } else {
                    log.error("FAILURE: {}", ex.message)
                }
            }

        val record = KafkaTestUtils.getSingleRecord(kafkaConsumer, DLT, Duration.ofSeconds(10))
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
            .isEqualTo("org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException")
        assertThat(String(headers.lastHeader("kafka_dlt-exception-message").value()))
            .contains("Listener method could not be invoked with the incoming message")
    }
}