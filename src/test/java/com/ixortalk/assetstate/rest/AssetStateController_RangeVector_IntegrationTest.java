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
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.util.Map;

import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetricWithRange;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.adminToken;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test", "rangeVector"})
@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateController_RangeVector_IntegrationTest extends AbstractSpringIntegrationTest {

    @Test
    public void specifiedIncludeLabelsUsedInPrometheusQueryAndFilterOnAssets() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS);

        setupPrometheusStubForMetricWithRange(wireMockRule, EXPECTED_PROMETHEUS_SINGLE_METRIC_RESPONSE);

        InputStream inputStream =
                given()
                        .accept(JSON)
                        .auth().oauth2(adminToken().getValue())
                        .when()
                        .get("/states")
                        .then()
                        .extract().asInputStream();

        Map<String,AssetState> assetStates = objectMapper.readValue(inputStream,new TypeReference<Map<String,AssetState>>() {});

        assertThat(assetStates).hasSize(1);
        assertThat(assetStates.get("asset1").getAspects().stream().anyMatch(aspect -> aspect.getName().equals("with-range-vector-selector"))).isTrue();
    }
}
