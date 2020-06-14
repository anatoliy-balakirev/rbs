package com.rbs;

import com.atlassian.oai.validator.pact.PactProviderValidationResults;
import com.atlassian.oai.validator.pact.PactProviderValidator;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

@Slf4j
@ExtendWith(SpringExtension.class)
class SpecificationPactValidatorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationPactValidatorTest.class);
    private static final String OPEN_API_SPEC_PATH =
            SpecificationPactValidatorTest.class.getResource("/openapi/spec.yaml").toString();

    @BeforeAll
    static void setUp() {
        // To not ignore required response fields:
        // TODO: Remove as soon as https://bitbucket.org/atlassian/swagger-request-validator/issues/197/ is resolved
        System.setProperty("swagger.validation.response.body.schema.required", "ERROR");
    }

    @ParameterizedTest(name = "Validate contract #{index} from [{arguments}]")
    @MethodSource("getAllContracts")
    void validatePactContractsAgainstSpec(final Path contract) {
        final PactProviderValidator validator = PactProviderValidator
                .createFor(OPEN_API_SPEC_PATH)
                .withConsumer("ExampleConsumer", contract.toString())
                .build();

        assertNoBreakingChanges(validator.validate(), contract.getFileName().toString());
        LOGGER.info("Contract {} is valid!", contract.getFileName());
    }

    private void assertNoBreakingChanges(final PactProviderValidationResults results, final String contractName) {
        if (results.hasErrors()) {
            final StringBuilder msg =
                    new StringBuilder("Validation errors found for " + contractName + "." + System.lineSeparator());
            msg.append(results.getValidationFailureReport().replace("\n", System.lineSeparator()));
            results.getConsumerResults()
                    .forEach(consumerResult -> consumerResult.getFailedInteractions()
                            .forEach((key, failedInteractions) ->
                                    msg.append(failedInteractions).append(System.lineSeparator())));
            fail(msg.toString());
        }
    }

    private static Stream<Path> getAllContracts() throws Exception {
        return Files.walk(Paths.get(SpecificationPactValidatorTest.class.getResource("/contracts").toURI()))
                .filter(filePath -> {
                    final String fileName = filePath.toString();
                    return fileName.endsWith(".json")
                            // We are intentionally defining contracts, which are not in line with spec (e.g. missing
                            // required fields). This is done to make sure they are still handled properly (validated
                            // and proper exceptions returned).
                            // Such contracts are for sure not valid WRT spec, so we are skipping them here:
                            && !fileName.contains("BadRequest");
                });
    }
}
