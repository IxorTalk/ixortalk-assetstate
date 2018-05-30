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
import java.net.URLEncoder;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.ixortalk.assetstate.ConfigurationTestConstants.METRIC1;
import static com.ixortalk.assetstate.ConfigurationTestConstants.METRIC2;
import static com.ixortalk.assetstate.ConfigurationTestConstants.METRIC_NAME;
import static com.ixortalk.test.util.FileUtil.jsonFile;
import static org.apache.http.HttpStatus.SC_OK;

public class PrometheusStubHelper {

    static final String METRIC1_PROMETHEUS_RESPONSE = jsonFile("prometheus/metric1.json");
    static final String METRIC2_PROMETHEUS_RESPONSE = jsonFile("prometheus/metric2.json");
    static final String NO_METRICS_PROMETHEUS_RESPONSE = jsonFile("prometheus/no_metrics.json");
    static final String FLATTENING_LABELS_PROMETHEUS_RESPONSE = jsonFile("prometheus/metricFlatteningLabels.json");
    static final String NO_PROMETHEUS_IDENTIFIER_RESPONSE = jsonFile("prometheus/no_prometheus_identifier.json");

    static void setupPrometheusStubForMetric1(WireMockRule wireMockRule, String response) throws Exception {
        setupPrometheusStub(wireMockRule, response, METRIC1 + "[5m]");
    }

    static void setupPrometheusStubForMetric2(WireMockRule wireMockRule, String response) throws Exception {
        setupPrometheusStub(wireMockRule, response, METRIC2 + "[5m]");
    }

    static void setupPrometheusStubForMetric(WireMockRule wireMockRule, String response) throws Exception {
        setupPrometheusStub(wireMockRule, response, METRIC_NAME + "[5m]");
    }

    static void setupPrometheusStubForMetricWithIncludeLabels(WireMockRule wireMockRule, String response) throws Exception {
        setupPrometheusStub(wireMockRule, response, METRIC_NAME + "{labelKey1='labelValue1',labelKey2='labelValue2'}[5m]");
    }

    static void setupPrometheusStubForMetricWithRange(WireMockRule wireMockRule, String response) throws Exception {
        setupPrometheusStub(wireMockRule, response, METRIC_NAME + "[10h]");
    }

    private static void setupPrometheusStub(WireMockRule wireMockRule, String response, String queryString) throws UnsupportedEncodingException {
        wireMockRule.stubFor(get(urlEqualTo("/prometheus/api/v1/query?query=" + URLEncoder.encode(queryString,"UTF-8")))
                .willReturn(
                        aResponse()
                                .withBody(response)
                                .withStatus(SC_OK)
                ));
    }
}
