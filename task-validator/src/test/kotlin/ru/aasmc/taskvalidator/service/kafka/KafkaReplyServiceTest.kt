package ru.aasmc.taskvalidator.service.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.aasmc.taskvalidator.dto.ValidationRequest
import ru.aasmc.taskvalidator.dto.ValidationResponse
import ru.aasmc.taskvalidator.dto.ValidationResult
import ru.aasmc.taskvalidator.model.Range
import ru.aasmc.taskvalidator.repository.RangeRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

private const val DLT = "validate-request.DLT"
private val log = LoggerFactory.getLogger(KafkaReplyServiceTest::class.java)
private const val VALIDATE_REQUEST_TOPIC = "validate-request"
private const val VALIDATE_RESPONSE_TOPIC = "validate-response"

@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class KafkaReplyServiceTest {

    @Autowired
    lateinit var template: KafkaTemplate<String, ValidationRequest>
    @Autowired
    lateinit var objectMapper: ObjectMapper
    @Autowired
    lateinit var repo: RangeRepository

    companion object {

        @Container
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"))

        @JvmStatic
        private lateinit var kafkaConsumer: KafkaConsumer<String, String>
        private lateinit var consumerProps: MutableMap<String, Any>
        private lateinit var responseRecordsQueue: BlockingQueue<ConsumerRecord<String, String>>
        private lateinit var responseContainer: KafkaMessageListenerContainer<String, String>


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
            consumerProps = KafkaTestUtils.consumerProps(
                kafka.bootstrapServers,
                "test-consumer",
                "true"
            )
            consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            kafkaConsumer = KafkaConsumer(consumerProps)
            kafkaConsumer.subscribe(listOf(DLT))

            initResponseContainer()
        }

        private fun initResponseContainer() {
            responseRecordsQueue = LinkedBlockingQueue()
            val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProps)
            val containerProperties = ContainerProperties(VALIDATE_RESPONSE_TOPIC)
            responseContainer = KafkaMessageListenerContainer(consumerFactory, containerProperties)
            responseContainer.setupMessageListener(MessageListener<String, String> { rec ->
                log.info("Adding record to BlockingQueue: {}", rec)
                responseRecordsQueue.add(rec)
            })
            responseContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun close() {
            // Close the consumer before shutting down Testcontainers Kafka instance
            kafkaConsumer.close()
            kafka.stop()
            responseContainer.stop()
        }
    }

    @BeforeEach
    fun clearRepo() {
        repo.deleteAll()
    }

    @Test
    fun when_send_valid_request_then_it_is_sent_to_response_topic_dlt_empty() {
        val validationRequest = ValidationRequest(LocalDateTime.now(), LocalDateTime.now(), 1)
        val producerRecord = createProducerRecord(validationRequest)
        template.send(producerRecord)
            .whenCompleteAsync { response, ex ->
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
        val polledRecord = responseRecordsQueue.poll(2, TimeUnit.SECONDS)
        log.info("Record polled from blocking queue: {}", polledRecord)
        val resp = objectMapper.readValue(polledRecord.value(), ValidationResponse::class.java)
        val expected = ValidationResponse(validationRequest.taskId, ValidationResult.SUCCESS)
        assertThat(expected).isEqualTo(resp)
    }

    @Test
    fun when_send_invalid_request_then_it_is_sent_to_response_topic_dlt_empty() {
        val occupiedRange = Range(start = LocalDateTime.now(), end = LocalDateTime.now().plusDays(10))
        repo.save(occupiedRange)

        val validationRequest = ValidationRequest(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(15), 1)
        val producerRecord = createProducerRecord(validationRequest)
        template.send(producerRecord)
                .whenCompleteAsync { response, ex ->
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
        val polledRecord = responseRecordsQueue.poll(2, TimeUnit.SECONDS)
        log.info("Record polled from blocking queue: {}", polledRecord)
        val resp = objectMapper.readValue(polledRecord.value(), ValidationResponse::class.java)
        val expected = ValidationResponse(validationRequest.taskId, ValidationResult.FAILURE)
        assertThat(expected).isEqualTo(resp)
    }

    @Test
    fun should_send_to_dlt_when_message_invalid() {
        val validationRequest = ValidationRequest(LocalDateTime.now(), LocalDateTime.now(), -2)
        val producerRecord = createProducerRecord(validationRequest)

        template.send(producerRecord)
            .whenCompleteAsync { response, ex ->
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

    private fun createProducerRecord(request: ValidationRequest): ProducerRecord<String, ValidationRequest> {
        val producerRecord = ProducerRecord<String, ValidationRequest>(VALIDATE_REQUEST_TOPIC, request)
        producerRecord.headers().add(KafkaHeaders.REPLY_TOPIC, VALIDATE_RESPONSE_TOPIC.toByteArray())
        producerRecord.headers().add(KafkaHeaders.CORRELATION_ID, UUID.randomUUID().toString().toByteArray())
        return producerRecord
    }
}