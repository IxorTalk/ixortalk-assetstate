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

import java.net.URLEncoder;

import javax.inject.Inject;

import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.assetstate.domain.prometheus.PrometheusQuery;
import com.ixortalk.assetstate.domain.prometheus.PrometheusRangeQueryResult;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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
        wireMockRule.stubFor(get(urlEqualTo("/prometheus/api/v1/query?query=" + URLEncoder.encode("metric1[5m]","UTF-8")))
                .willReturn(
                        aResponse()
                                .withBody(EXPECTED_PROMETHEUS_RANGE_RESPONSE)
                                .withStatus(SC_OK)
                ));


        PrometheusRangeQueryResult result = prometheusQuery.getRangeVectorFromInstantQuery(newPrometheusQueryParam("metric1","5m", newHashMap()));

        assertThat(result.status).isEqualTo("success");
    }
}
