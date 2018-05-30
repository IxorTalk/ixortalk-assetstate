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

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.assetstate.domain.aspect.Aspect;
import com.ixortalk.assetstate.domain.aspect.AssetState;
import com.ixortalk.assetstate.domain.asset.AspectBuilderforTest;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.ixortalk.assetstate.ConfigurationTestConstants.ASPECT1;
import static com.ixortalk.assetstate.ConfigurationTestConstants.ASPECT2;
import static com.ixortalk.assetstate.domain.aspect.Aspect.newEmptyAspect;
import static com.ixortalk.assetstate.domain.aspect.Status.UNKNOWN;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.METRIC1_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.METRIC2_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.NO_METRICS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.NO_PROMETHEUS_IDENTIFIER_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric1;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric2;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.adminToken;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        assertThat(assetStates).hasSize(4);
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

        assertThat(assetStates.get("asset_only_known_in_prometheus")).isNull();
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
        setupPrometheusStubForMetric1(wireMockRule, NO_PROMETHEUS_IDENTIFIER_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, NO_PROMETHEUS_IDENTIFIER_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();
        assertThat(assetStates.get("asset1").getAspects()).extracting(Aspect::getName).containsOnly(ASPECT1, ASPECT2);
    }

    @Test
    public void assetOnlyKnownInAssetMgmtIsReturnedAsUnknown() throws Exception {

        setupAssetMgmtStubWithMetrics(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();
        assertThat(assetStates.get("asset_only_known_in_assetmgmt").getStatus().isUnknown()).isTrue();
    }

    @Test
    public void assetKnownInAssetMgmtButNotInPrometheusIsReturnedAsUnknownAssetState() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS_NOT_KNOWN_IN_PROMETHEUS);
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();

        assertThat(assetStates).hasSize(3);

        assertThat(assetStates.get("asset1_not_in_prometheus").getAspects())
                .containsOnly(
                        newEmptyAspect(ASPECT1),
                        newEmptyAspect(ASPECT2));


        assertThat(assetStates.get("asset2_not_in_prometheus").getAspects())
                .containsOnly(
                        newEmptyAspect(ASPECT1),
                        newEmptyAspect(ASPECT2));



        assertThat(assetStates.get("asset3_not_in_prometheus").getAspects())
                .containsOnly(
                        newEmptyAspect(ASPECT1),
                        newEmptyAspect(ASPECT2));


        assertThat(assetStates.get("asset1_not_in_prometheus").getStatus()).isEqualTo(UNKNOWN);
        assertThat(assetStates.get("asset2_not_in_prometheus").getStatus()).isEqualTo(UNKNOWN);
        assertThat(assetStates.get("asset3_not_in_prometheus").getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void assetStateServiceIsGeneratingCorrectJson() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, METRIC1_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, METRIC2_PROMETHEUS_RESPONSE);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/states")).andExpect(status().isOk()).andReturn();;

        assertEquals(EXPECTED_RESPONSE,  result.getResponse().getContentAsString(), false);
    }

    @Test
    public void assetStatesReturnStaleWhenPrometheusReturnsEmptyResponse() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS);
        setupPrometheusStubForMetric1(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);

        InputStream inputStream = given()
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(adminToken().getValue())
                .when()
                .get("/states")
                .then()
                .extract().asInputStream();

        Map<String,AssetState> map = objectMapper.readValue(inputStream,new TypeReference<Map<String,AssetState>>() {});

        assertThat(map).hasSize(4);
        assertThat(map.get("asset1").getAspects()).hasSize(2);

        assertThat(map.get("asset1").getAspects()).contains(newEmptyAspect(ASPECT1));
        assertThat(map.get("asset1").getAspects()).contains(newEmptyAspect(ASPECT2));

        assertThat(map.get("asset2").getAspects()).hasSize(2);
        assertThat(map.get("asset3").getAspects()).hasSize(2);
        assertThat(map.get("asset_only_known_in_assetmgmt").getAspects()).hasSize(2);
    }

    @Test
    public void freshMetricsReturnAsAspectsWithMetricsData() throws Exception {
        setupAssetMgmtStubWithMetrics(SINGLE_ASSET);
        setupPrometheusStubForMetric1(wireMockRule, EXPECTED_PROMETHEUS_SINGLE_FRESH_METRICS_RESPONSE);
        setupPrometheusStubForMetric2(wireMockRule, NO_METRICS_PROMETHEUS_RESPONSE);

        InputStream inputStream = given()
                .accept(JSON)
                .contentType(JSON)
                .auth().oauth2(adminToken().getValue())
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
