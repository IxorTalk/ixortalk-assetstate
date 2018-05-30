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

import java.net.URLEncoder;

import javax.inject.Inject;

import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.assetstate.domain.prometheus.PrometheusQuery;
import com.ixortalk.assetstate.domain.prometheus.PrometheusRangeQueryResult;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.collect.Maps.newHashMap;
import static com.ixortalk.assetstate.domain.prometheus.PrometheusQuery.PrometheusQueryParam.newPrometheusQueryParam;
import static com.ixortalk.test.util.FileUtil.jsonFile;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class PrometheusInstantQueryIntegrationTest extends AbstractSpringIntegrationTest {

    private static final String EXPECTED_PROMETHEUS_RANGE_RESPONSE = jsonFile("prometheus/instant_query/range_vector_response.json");

    @Inject
    private PrometheusQuery prometheusQuery;

    @Test
    public void whenRangeVectorIsRequestedFromInstantQueryThenCorrectPrometheusEndpointIsHit() throws Exception {
        stubFor(get(urlEqualTo("/prometheus/api/v1/query?query=" + URLEncoder.encode("metric1[5m]","UTF-8")))
                .willReturn(
                        aResponse()
                                .withBody(EXPECTED_PROMETHEUS_RANGE_RESPONSE)
                                .withStatus(SC_OK)
                ));


        PrometheusRangeQueryResult result = prometheusQuery.getRangeVectorFromInstantQuery(newPrometheusQueryParam("metric1","5m", newHashMap()));

        assertThat(result.status).isEqualTo("success");
    }
}
