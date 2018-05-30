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
package com.ixortalk.assetstate.rest;

import java.io.UnsupportedEncodingException;

import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.test.util.FileUtil;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.FLATTENING_LABELS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.METRIC1_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.METRIC2_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.NO_METRICS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric1;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric2;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetricWithIncludeLabels;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetricWithRange;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.adminToken;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;


@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateControllerRestDocTest extends AbstractSpringIntegrationTest {

    private static final String ASSETS_WITH_METRICS = FileUtil.jsonFile("assetmgmt/assets.json");

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    private RequestSpecification spec;

    @Before
    public void before () {
        spec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Before
    public void additionalMetricsWithDifferentQueryStrings() throws Exception {
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetricWithIncludeLabels(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetricWithRange(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric(wireMockRule, FLATTENING_LABELS_PROMETHEUS_RESPONSE);
    }

    @Test
    public void getAssetStates () throws UnsupportedEncodingException {
        stubFor(get(urlEqualTo("/assetmgmt/assets"))
                .withHeader("Authorization", containing("Bearer"))
                .willReturn(
                        aResponse()
                                .withBody(ASSETS_WITH_METRICS)
                                .withStatus(SC_OK)
                ));

        given(this.spec)
                .accept(JSON)
                .auth().preemptive().oauth2(adminToken().getValue())
                .filter(
                        document("assetstates/get/ok",
                                preprocessResponse(prettyPrint())
                        )
                )
                .when()
                .get("/states")
                .then()
                .statusCode(SC_OK);
    }
}
