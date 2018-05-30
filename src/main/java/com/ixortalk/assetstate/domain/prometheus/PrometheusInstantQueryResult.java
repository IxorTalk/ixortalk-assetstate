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
package com.ixortalk.assetstate.domain.prometheus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class PrometheusInstantQueryResult {

    public Data data;
    public String status;

    public static class Data {

        private List<InstantQueryMetric> result;
        private String resultType;

        public List<InstantQueryMetric> getResult() {
            return result;
        }

        public static class InstantQueryMetric {

            public static final String FIELD_NAME = "__name__";

            private JsonNode metric;
            private ArrayNode value;

            public String getMetricName() {
                return this.metric.get(FIELD_NAME).asText();
            }

            public LocalDateTime getMetricUtcLocalDateTime() {
                return Instant.ofEpochMilli(this.value.get(0).asLong() * 1000).atZone(ZoneId.of("UTC")).toLocalDateTime();
            }

            public String getMetricValue() {
                return this.value.get(1).asText();
            }

            public String getMetricType() {
                return this.value.get(1).getNodeType().toString();
            }

            public String getMetricProperty(String property) {
                return this.metric.get(property).asText();
            }

        }

    }

}
