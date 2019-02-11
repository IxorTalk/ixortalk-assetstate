/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.assetstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ixortalk.assetstate.config.FixedClockConfiguration;
import com.ixortalk.assetstate.config.feign.OAuth2FeignRequestInterceptor;
import com.ixortalk.assetstate.domain.auth.AuthServerUser;
import com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer;
import com.jayway.restassured.RestAssured;
import feign.RequestInterceptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.test.OAuth2ContextSetup;
import org.springframework.security.oauth2.client.test.RestTemplateHolder;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_ID_ADMIN;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_SECRET_ADMIN;
import static com.ixortalk.test.util.FileUtil.jsonFile;
import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.security.oauth2.client.test.OAuth2ContextSetup.standard;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AssetStateApplication.class, OAuth2EmbeddedTestServer.class, FixedClockConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractSpringIntegrationTest implements RestTemplateHolder {

    @Rule
    public WireMockRule authServerWireMockRule = new WireMockRule(65301);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(65432);

    @Rule
    public OAuth2ContextSetup context = standard(this);

    @Inject
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @Inject
    private ServerProperties serverProperties;

    @Inject
    private WebApplicationContext webApplicationContext;


    protected static final String ASSETS = jsonFile("assetmgmt/assets.json");
    protected static final String SINGLE_ASSET = jsonFile("assetmgmt/single_asset.json");
    protected static final String EXPECTED_PROMETHEUS_SINGLE_METRIC_RESPONSE = jsonFile("prometheus/single_metric.json");
    protected static final String EXPECTED_PROMETHEUS_SINGLE_FRESH_METRICS_RESPONSE = jsonFile("prometheus/single_fresh_metric.json");
    protected static final String EXPECTED_RESPONSE = jsonFile("assetstate/response.json");

    @Inject
    private FeignContext feignContext;

    private RestOperations restTemplate = new RestTemplate();

    protected String getOAuth2AccessTokenUri() {
        return "http://localhost:" + port + "" + (isEmpty(serverProperties.getContextPath()) ? "" : serverProperties.getContextPath()) + "/oauth/token";
    }

    @Before
    public void restAssured() {
        RestAssured.port = this.port;
        RestAssured.config = config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper));
    }

    @Before
    public void before() throws Exception {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    protected void setupAssetMgmtStubWithMetrics(String assetResponse) {
        wireMockRule.stubFor(get(urlEqualTo("/assetmgmt/assets"))
                .willReturn(
                        aResponse()
                                .withHeader(CONTENT_TYPE, HAL_JSON_VALUE)
                                .withBody(assetResponse)
                                .withStatus(SC_OK)
                ));
    }

    protected void setupAuthServerStub(String login, AuthServerUser authServerUser) throws JsonProcessingException {
        authServerWireMockRule.stubFor(get(urlEqualTo("/authserver/api/users/" + login))
                .willReturn(
                        aResponse()
                                .withHeader(CONTENT_TYPE, HAL_JSON_VALUE)
                                .withBody(objectMapper.writeValueAsString(authServerUser))
                                .withStatus(SC_OK)
                ));
    }

    public RestOperations getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
        if (restTemplate instanceof OAuth2RestTemplate) {
            feignContext.getContextNames()
                    .stream()
                    .map(feignContextName -> feignContext.getInstance(feignContextName, RequestInterceptor.class))
                    .forEach(requestInterceptor -> setField(requestInterceptor, "oAuth2RestTemplate", restTemplate, OAuth2RestTemplate.class));
        }
    }

    public static class AdminClientCredentialsResourceDetails extends ClientCredentialsResourceDetails {
        public AdminClientCredentialsResourceDetails(Object object) {
            AbstractSpringIntegrationTest abstractSpringIntegrationTest = (AbstractSpringIntegrationTest) object;
            setAccessTokenUri(abstractSpringIntegrationTest.getOAuth2AccessTokenUri());
            setClientId(CLIENT_ID_ADMIN);
            setClientSecret(CLIENT_SECRET_ADMIN);
        }
    }
}
