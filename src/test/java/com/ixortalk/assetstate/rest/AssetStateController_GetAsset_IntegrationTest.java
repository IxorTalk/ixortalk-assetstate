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
import com.ixortalk.assetstate.domain.auth.AuthServerUser;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import wiremock.com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.*;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.Role.ORGANIZATION_X_ADMIN;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.Role.ORGANIZATION_Y_ADMIN;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.*;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_ID_USER;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.userToken;
import static com.ixortalk.test.util.FileUtil.jsonFile;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ActiveProfiles({"test", "metric1And2"})
@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateController_GetAsset_IntegrationTest extends AbstractSpringIntegrationTest {

    private AuthServerUser authServerUser;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    private RequestSpecification spec;

    private static final String RANDOM_ROLE = "RANDOM_ROLE_IN_AUTH_SERVER";

    @Before
    public void before() throws Exception {
        spec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(this.restDocumentation))
                .build();
        setupAssetMgmtStubWithMetrics(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, jsonFile("prometheus/metric1.json"));
        setupPrometheusStubForMetric2(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);

        authServerUser = new AuthServerUser();
        setupAuthServerStub(CLIENT_ID_USER, authServerUser);
        setField(authServerUser, "authorities", Sets.newHashSet(ORGANIZATION_X_ADMIN.roleName(), RANDOM_ROLE));
        setupAuthServerStub(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID, authServerUser);
    }

    @Test
    public void getAssetStatesAsOrganizationAdmin() throws IOException {

        InputStream inputStream = given(this.spec)
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(clientInOrganizationXAdminRoleToken().getValue())
                .when()
                .filter(
                        document("assetstates/get/ok",
                                preprocessResponse(prettyPrint())
                        )
                )
                .get("/states")
                .then()
                .extract().asInputStream();

        Map<String, AssetState> map = objectMapper.readValue(inputStream, new TypeReference<Map<String, AssetState>>() {
        });

        assertThat(map).hasSize(2);
        assertThat(map.get("asset1").getAspects()).hasSize(2);
    }

    @Test
    public void getAssetStatesAsDifferentOrganizationAdmin() throws IOException {

        setField(authServerUser, "authorities", Sets.newHashSet(ORGANIZATION_Y_ADMIN.roleName(), RANDOM_ROLE));
        setupAuthServerStub(CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_ID, authServerUser);
        InputStream inputStream = given(this.spec)
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(clientInOrganizationYAdminRoleToken().getValue())
                .when()
                .filter(
                        document("assetstates/get/different-admin",
                                preprocessResponse(prettyPrint())
                        )
                )
                .get("/states")
                .then()
                .extract().asInputStream();

        Map<String, AssetState> map = objectMapper.readValue(inputStream, new TypeReference<Map<String, AssetState>>() {
        });

        assertThat(map).hasSize(0);
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
                .extract().asInputStream();
        Map<String, AssetState> map = objectMapper.readValue(inputStream, new TypeReference<Map<String, AssetState>>() {
        });

        assertThat(map).hasSize(0);
    }
}
