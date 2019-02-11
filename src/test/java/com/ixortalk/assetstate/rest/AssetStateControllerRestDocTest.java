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

import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.assetstate.domain.auth.AuthServerUser;
import com.ixortalk.test.util.FileUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import wiremock.com.google.common.collect.Sets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.*;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.Role.ORGANIZATION_X_ADMIN;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.FLATTENING_LABELS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.METRIC1_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.METRIC2_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.NO_METRICS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric1;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric2;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetricWithIncludeLabels;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetricWithRange;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateControllerRestDocTest extends AbstractSpringIntegrationTest {

    private static final String ASSETS_WITH_METRICS = FileUtil.jsonFile("assetmgmt/assets.json");

    @Before
    public void additionalMetricsWithDifferentQueryStrings() throws Exception {
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetricWithIncludeLabels(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetricWithRange(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric(wireMockRule, FLATTENING_LABELS_PROMETHEUS_RESPONSE);

        setupAssetMgmtStubWithMetrics(ASSETS);
        AuthServerUser authServerUser = new AuthServerUser();
        setField(authServerUser, "authorities", Sets.newHashSet(ORGANIZATION_X_ADMIN.roleName()));
        setupAuthServerStub(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID, authServerUser);
    }

    @Test
    public void getAssetStates () {
        stubFor(get(urlEqualTo("/assetmgmt/assets"))
                .withHeader("Authorization", containing("Bearer"))
                .willReturn(
                        aResponse()
                                .withBody(ASSETS_WITH_METRICS)
                                .withStatus(SC_OK)
                ));

        given()
                .accept(JSON)
                .auth().preemptive().oauth2(clientInOrganizationXAdminRoleToken().getValue())
                .when()
                .get("/states")
                .then()
                .statusCode(SC_OK);
    }
}
