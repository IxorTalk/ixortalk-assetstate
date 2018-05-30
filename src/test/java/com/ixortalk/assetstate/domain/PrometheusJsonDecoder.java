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
package com.ixortalk.assetstate.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixortalk.assetstate.domain.prometheus.PrometheusRangeQueryResult;
import com.ixortalk.assetstate.domain.prometheus.PrometheusRangeQueryResult.Data.RangeQueryMetric;
import com.ixortalk.test.util.FileUtil;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.assertj.core.api.Assertions.assertThat;

public class PrometheusJsonDecoder {


    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .setVisibility(ALL, NONE)
                .setVisibility(FIELD, ANY)
                .disable(WRITE_DATES_AS_TIMESTAMPS);
    }
    @Test
    public void verifyPrometheusDeserialization() throws IOException {
        String jsonFile = FileUtil.jsonFile("prometheus/metric1.json");

        PrometheusRangeQueryResult prometheusInstantQueryResult = objectMapper().readValue(jsonFile, PrometheusRangeQueryResult.class);

        OffsetDateTime odt = OffsetDateTime.parse("2016-07-22T07:49:08.000Z");

        assertThat(prometheusInstantQueryResult.status).isEqualTo("success");
        assertThat(prometheusInstantQueryResult.data.getResult()).hasSize(3);

        RangeQueryMetric metric_1 = prometheusInstantQueryResult.data.getResult().get(0);
        assertThat(metric_1.getMetricProperty("controllerId")).isEqualTo("asset1");
        assertThat(metric_1.getMetricName()).isEqualTo("metric1");
        assertThat(metric_1.getMetricType()).isEqualTo("STRING");
        assertThat(metric_1.getMetricValue()).isEqualTo("0");
        assertThat(metric_1.getMetricUtcLocalDateTime()).isEqualTo(odt.toLocalDateTime());

        RangeQueryMetric  metric_2 = prometheusInstantQueryResult.data.getResult().get(1);
        assertThat(metric_2.getMetricProperty("controllerId")).isEqualTo("asset2");
        assertThat(metric_2.getMetricName()).isEqualTo("metric1");
        assertThat(metric_2.getMetricType()).isEqualTo("STRING");
        assertThat(metric_2.getMetricValue()).isEqualTo("1");
        assertThat(metric_2.getMetricUtcLocalDateTime()).isEqualTo(odt.toLocalDateTime());

        RangeQueryMetric  metric_3 = prometheusInstantQueryResult.data.getResult().get(2);
        assertThat(metric_3.getMetricProperty("controllerId")).isEqualTo("asset_only_known_in_prometheus");
        assertThat(metric_3.getMetricName()).isEqualTo("metric1");
        assertThat(metric_3.getMetricType()).isEqualTo("STRING");
        assertThat(metric_3.getMetricValue()).isEqualTo("2");
        assertThat(metric_3.getMetricUtcLocalDateTime()).isEqualTo(odt.toLocalDateTime());

    }
}
