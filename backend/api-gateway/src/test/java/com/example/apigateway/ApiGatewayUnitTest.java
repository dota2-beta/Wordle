package com.example.apigateway;

import com.example.apigateway.filters.JwtService;
import com.example.apigateway.util.TestJwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
public class ApiGatewayUnitTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret.key}")
    private String secretKey;

    private TestJwtUtil testJwtUtil;

    @BeforeEach
    void setUp() {
        testJwtUtil = new TestJwtUtil(secretKey);
    }

    @Test
    void whenPublishEndpointIsCalled_thenRequestIsProxiedWithoutAuthHeaders() throws Exception {
        //arrange
        stubFor(post(urlEqualTo("/api/auth/register"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"token\":\"fake-token-from-wiremock\"}")));
        //act && assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"fake-token-from-wiremock\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("fake-token-from-wiremock"));

        verify(postRequestedFor(urlEqualTo("/api/auth/register"))
                .withoutHeader("X-Username"));
        verify(postRequestedFor(urlEqualTo("/api/auth/register"))
                .withoutHeader("X-User-Id"));
    }

    @Test
    void whenSecureEndpointIsCalled_thenRequestIsProxiedWithAuthHeaders() throws Exception {
        //arrange
        stubFor(post(urlEqualTo("/api/games/create"))
                .willReturn(aResponse().withStatus(200)));

        String username = "fake-username";
        Long userId = 4L;
        String token = testJwtUtil.generateToken(username, userId);

        //act && assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(postRequestedFor(urlEqualTo("/api/games/create"))
            .withHeader("X-Username", equalTo(username)));
        verify(postRequestedFor(urlEqualTo("/api/games/create"))
                .withHeader("X-User-Id", equalTo(userId.toString())));
    }

    @Test
    void whenSecureEndpointIsCalledWithExpiredToken_thenReturnsUnauthorized() throws Exception {
        //arrange
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjQsInN1YiI6ImZha2UtdXNlcm5hbWUiLCJpYXQiOjE3NTg2NDcwNjYsImV4cCI6MTc1ODY0NzA2OH0.x-a9z7DjX3S6U23weDOHWbtdSsToQWWzf6vP1ZWBLW8";

        //act && assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void whenSecureEndpointIsCalledWithoutBearer_thenReturnsUnauthorized() throws Exception {
        //arrange
        String username = "fake-username";
        Long userId = 4L;
        String token = testJwtUtil.generateToken(username, userId);

        //act && assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
