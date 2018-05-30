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
