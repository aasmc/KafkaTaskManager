package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.dto.ValidationResult

private const val DLT = "validate-response.DLT"
private val log = LoggerFactory.getLogger(KafkaConfigTest::class.java)
private const val TOPIC = "validate-response"

// https://github.com/timtebeek/kafka-dead-letter-publishing
// https://github.com/quicklearninghub/kafka-error-handling
// https://www.youtube.com/watch?v=OnZ7JArSoiU
@SpringBootTest
@Testcontainers
class KafkaConfigTest {

    @Autowired
    lateinit var template: KafkaTemplate<String, ValidationResponse>

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"))

        private lateinit var kafkaConsumer: KafkaConsumer<String, ValidationResponse>

        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }

        @BeforeAll
        fun setup() {
            // Create a test consumer that handles <String, String> records, listening to orders.DLT
            // https://docs.spring.io/spring-kafka/docs/3.0.x/reference/html/#testing
            val consumerProps = KafkaTestUtils.consumerProps(
                kafka.bootstrapServers,
                "test-consumer",
                "true"
            )
            consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
            kafkaConsumer = KafkaConsumer(consumerProps)
            kafkaConsumer.subscribe(listOf(DLT))
        }

        @AfterAll
        fun close() {
            // Close the consumer before shutting down Testcontainers Kafka instance
            kafkaConsumer.close()
        }
    }

    @Test
    fun should_not_send_to_dlt_for_valid_message() {
        val validationResponse = ValidationResponse(1, ValidationResult.SUCCESS)
        template.send(TOPIC, validationResponse)
            .addCallback(
                { response -> log.info("Success: {}", response) },
                { ex -> log.error("FAILURE: {}", ex.message) }
            )

        assertThrows(
            IllegalStateException::class.java,
        ) {
            KafkaTestUtils.getSingleRecord(kafkaConsumer, DLT, 5000L)
        }
    }

    @Test
    fun should_send_to_dlt_when_message_invalid() {

    }

}




























