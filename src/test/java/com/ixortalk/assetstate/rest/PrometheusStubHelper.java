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
