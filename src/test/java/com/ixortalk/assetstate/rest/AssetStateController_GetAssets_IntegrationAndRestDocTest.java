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
package com.ixortalk.assetstate.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.assetstate.domain.aspect.AssetState;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.NO_METRICS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric1;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric2;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.adminToken;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.authorizationHeader;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.userToken;
import static com.ixortalk.test.util.FileUtil.jsonFile;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ActiveProfiles({"test", "metric1And2"})
@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateController_GetAssets_IntegrationAndRestDocTest extends AbstractSpringIntegrationTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    private RequestSpecification spec;

    @Before
    public void before() throws Exception {
        spec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(this.restDocumentation))
                .build();
        setupAssetMgmtStubWithMetricsAdminAndUser(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, jsonFile("prometheus/metric1.json"));
        setupPrometheusStubForMetric2(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);

    }

    @Test
    public void getAssetStatesAsAdmin() throws IOException {

        InputStream inputStream = given(this.spec)
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(adminToken().getValue())
                .when()
                .filter(
                        document("assetstates/get/ok",
                                preprocessResponse(prettyPrint())
                        )
                )
                .get("/states")
                .then()
                .log().all()
                .statusCode(SC_OK)
                .extract().asInputStream();

        Map<String, AssetState> map = objectMapper.readValue(inputStream, new TypeReference<Map<String, AssetState>>() {
        });

        assertThat(map).hasSize(2);
        assertThat(map.get("asset1").getAspects()).hasSize(2);

        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/assetmgmt/assets"))
                .withHeader("Authorization", equalTo(authorizationHeader(adminToken()))));
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/prometheus/api/v1/query?query=" + URLEncoder.encode("metric1[5m]","UTF-8")))
                        .withHeader("Authorization", equalTo(authorizationHeader(adminToken()))));
    }


    @Test
    public void getAssetStatesAsUser() throws IOException {

        InputStream inputStream = given(this.spec)
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(userToken().getValue())
                .when()
                .filter(
                        document("assetstates/get/as-user",
                                preprocessResponse(prettyPrint())
                        )
                )
                .get("/states")
                .then()
                .statusCode(SC_OK)
                .extract().asInputStream();

        Map<String, AssetState> map = objectMapper.readValue(inputStream, new TypeReference<Map<String, AssetState>>() {
        });

        assertThat(map).hasSize(0);

        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/assetmgmt/assets"))
                        .withHeader("Authorization", equalTo(authorizationHeader(userToken()))));
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/prometheus/api/v1/query?query=" + URLEncoder.encode("metric1[5m]","UTF-8")))
                        .withHeader("Authorization", equalTo(authorizationHeader(adminToken()))));
    }
}
