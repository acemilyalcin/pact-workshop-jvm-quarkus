package org.acme.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.gradle.internal.impldep.javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "Provider")
class GreetingsServiceContractIntegrationTest {

    @Inject
    @RestClient
    GreetingService greetingService;

    @Pact(provider = "Provider", consumer = "Consumer")
    public RequestResponsePact greet(PactDslWithProvider builder) {
        return builder
                .uponReceiving("greet")
                .path("/hello")
                .method("GET")
                .willRespondWith()
                .status(200) // if this argument changed with 201 test will fail
                .body(new PactDslJsonBody()
                        .stringType("greeting")
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "greet", port = "8080")
    void testHelloEndpoint() {
        Greeting actualGreeting = greetingService.getGreeting();
        System.out.println(actualGreeting.greeting);
        Assertions.assertNotNull(actualGreeting.greeting);
    }

    @Test
    @PactTestFor(providerName = "Provider")
    void test(MockServer mockServer) throws IOException {
        HttpResponse httpResponse = Request.Get(mockServer.getUrl() + "/hello").execute().returnResponse();
        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }
}
