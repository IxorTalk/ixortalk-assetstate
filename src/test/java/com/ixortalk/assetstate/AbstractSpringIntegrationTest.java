/**
 *
 *  2016 (c) IxorTalk CVBA
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of IxorTalk CVBA
 *
 * The intellectual and technical concepts contained
 * herein are proprietary to IxorTalk CVBA
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from IxorTalk CVBA.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */
package com.ixortalk.assetstate;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ixortalk.assetstate.config.FixedClockConfiguration;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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
    protected static final String ASSETS_NOT_KNOWN_IN_PROMETHEUS = jsonFile("assetmgmt/assets_without_metrics.json");
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
    public void before() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    protected void setupAssetMgmtStubWithMetrics(String assetResponse) {
        wireMockRule.stubFor(get(urlEqualTo("/assetmgmt/assets"))
                .withHeader("Authorization", containing("Bearer"))
                .willReturn(
                        aResponse()
                                .withHeader(CONTENT_TYPE, HAL_JSON_VALUE)
                                .withBody(assetResponse)
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
