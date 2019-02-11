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
import com.ixortalk.assetstate.domain.aspect.Aspect;
import com.ixortalk.assetstate.domain.aspect.AssetState;
import com.ixortalk.assetstate.domain.asset.AspectBuilderforTest;
import com.ixortalk.assetstate.domain.auth.AuthServerUser;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import wiremock.com.google.common.collect.Sets;

import javax.inject.Inject;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

import static com.ixortalk.assetstate.ConfigurationTestConstants.ASPECT1;
import static com.ixortalk.assetstate.ConfigurationTestConstants.ASPECT2;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.Role.ORGANIZATION_X_ADMIN;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.clientInOrganizationXAdminRoleToken;
import static com.ixortalk.assetstate.domain.aspect.Aspect.newEmptyAspect;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.*;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ActiveProfiles({"test", "metric1And2"})
@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateControllerIntegrationTest extends AbstractSpringIntegrationTest {

    @Inject
    private AssetStateController assetStateController;

    @Test
    public void assetStateReturnsCorrectAssetsFromAssetMgmt() throws Exception {

        setupAssetMgmtStubWithMetrics(ASSETS);

        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);

        OffsetDateTime odt = OffsetDateTime.parse("2016-07-22T07:49:08.000Z");

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();

        assertThat(assetStates).hasSize(3);
        assertThat(findAspect(assetStates, "asset1", ASPECT1).getValue()).isEqualTo("0");
        assertThat(findAspect(assetStates, "asset1", ASPECT1).getLocalDateTime()).isEqualTo(odt.toLocalDateTime());
        assertThat(findAspect(assetStates, "asset1", ASPECT1).getStatus().isError()).isTrue();

        assertThat(findAspect(assetStates, "asset1", ASPECT2).getValue()).isEqualTo("aaa");
        assertThat(findAspect(assetStates, "asset1", ASPECT2).getLocalDateTime()).isEqualTo(odt.toLocalDateTime());
        assertThat(findAspect(assetStates, "asset1", ASPECT2).getStatus().isError()).isTrue();

        assertThat(assetStates.get("asset1").getStatus().isError()).isTrue();

        assertThat(findAspect(assetStates, "asset2", ASPECT1).getValue()).isEqualTo("1");
        assertThat(findAspect(assetStates, "asset2", ASPECT1).getLocalDateTime()).isEqualTo(odt.toLocalDateTime());
        assertThat(findAspect(assetStates, "asset2", ASPECT1).getStatus().isOK()).isTrue();

        assertThat(findAspect(assetStates, "asset2", ASPECT2).getValue()).isEqualTo("bbb");
        assertThat(findAspect(assetStates, "asset2", ASPECT2).getLocalDateTime()).isEqualTo(odt.toLocalDateTime());
        assertThat(findAspect(assetStates, "asset2", ASPECT2).getStatus().isError()).isTrue();

        assertThat(assetStates.get("asset2").getStatus().isError()).isTrue();


        assertThat(findAspect(assetStates, "asset3", ASPECT1)).isEqualTo(newEmptyAspect(ASPECT1));

        assertThat(findAspect(assetStates, "asset3", ASPECT2).getValue()).isEqualTo("1");
        assertThat(findAspect(assetStates, "asset3", ASPECT2).getLocalDateTime()).isEqualTo(odt.toLocalDateTime());
        assertThat(findAspect(assetStates, "asset3", ASPECT2).getStatus().isError()).isTrue();

        assertThat(assetStates.get("asset3").getStatus().isError()).isTrue();
    }

    @Test
    public void assetOnlyKnownInPrometheusIsNotReturnedAsAssetState() throws Exception {

        setupAssetMgmtStubWithMetrics(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();
        assertThat(assetStates.get("asset_only_known_in_prometheus")).isNull();
    }

    @Test
    public void assetOnlyKnownInPrometheusWithoutPrometheusIdentifierIgnored() throws Exception {

        setupAssetMgmtStubWithMetrics(SINGLE_ASSET);
        setupPrometheusStubForMetric1(wireMockRule, EXPECTED_PROMETHEUS_SINGLE_FRESH_METRICS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, NO_PROMETHEUS_IDENTIFIER_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();
        assertThat(assetStates.get("asset1").getAspects()).extracting(Aspect::getName).containsOnly(ASPECT1, ASPECT2);
    }

    @Test
    public void assetStateServiceIsGeneratingCorrectJson() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);

        AuthServerUser authServerUser = new AuthServerUser();
        setField(authServerUser, "authorities", Sets.newHashSet(ORGANIZATION_X_ADMIN.roleName()));
        setupAuthServerStub(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID, authServerUser);
        AssetState assetState = given()
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(clientInOrganizationXAdminRoleToken().getValue())
                .when()
                .get("/states")
                .then()
                .extract().response().as(AssetState.class);

        AssetState expectedAssetState = objectMapper.readValue(EXPECTED_RESPONSE, AssetState.class);
        assertThat(assetState.getId()).isEqualTo(expectedAssetState.getId());
    }

    @Test
    public void freshMetricsReturnAsAspectsWithMetricsData() throws Exception {
        setupAssetMgmtStubWithMetrics(SINGLE_ASSET);
        setupPrometheusStubForMetric1(wireMockRule, EXPECTED_PROMETHEUS_SINGLE_FRESH_METRICS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);

        AuthServerUser authServerUser = new AuthServerUser();
        setField(authServerUser, "authorities", Sets.newHashSet(ORGANIZATION_X_ADMIN.roleName()));
        setupAuthServerStub(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID, authServerUser);
        InputStream inputStream = given()
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(clientInOrganizationXAdminRoleToken().getValue())
                .when()
                .get("/states")
                .then()
                .extract().asInputStream();

        Map<String,AssetState> map = objectMapper.readValue(inputStream,new TypeReference<Map<String,AssetState>>() {});


        assertThat(map).hasSize(1);
        assertThat(map.get("asset1").getAspects()).hasSize(2);

        Aspect aspect1 = new AspectBuilderforTest()
                .withDefaults()
                .withName("test-aspect1")
                .withValue("0")
                .withLocalDateTime(createLocalDateTimeFromEpoch(1451609985))
                .build();

        Aspect aspect2 = newEmptyAspect(ASPECT2);

        assertThat(map.get("asset1").getAspects()).contains(aspect1, aspect2);
    }

    private static LocalDateTime createLocalDateTimeFromEpoch(long unixEpoch) {
        return ofEpochMilli(unixEpoch * 1000).atZone(ZoneId.of("UTC")).toLocalDateTime();
    }

    private static Aspect findAspect(Map<String, AssetState> assetStates, String assetId, String aspectName) {
        return assetStates.get(assetId).getAspects().stream().filter(aspect -> aspect.getName().equals(aspectName)).findFirst().get();
    }
}
