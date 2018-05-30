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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class PrometheusRangeQueryResult {

    public Data data;
    public String status;

    public static class Data {

        private List<RangeQueryMetric> result;
        private String resultType;

        public List<RangeQueryMetric> getResult() {
            return result;
        }

        public static class RangeQueryMetric {

            public static final String FIELD_NAME = "__name__";

            private JsonNode metric;
            private List<ArrayNode> values;

            public String getMetricName() {
                return this.metric.get(FIELD_NAME).asText();
            }

            public LocalDateTime getMetricUtcLocalDateTime() {
                return Instant.ofEpochMilli(getMostRecentArrayNode().get(0).asLong() * 1000).atZone(ZoneId.of("UTC")).toLocalDateTime();
            }

            private ArrayNode getMostRecentArrayNode() {
                return this.values.get(this.values.size()-1);
            }

            public String getMetricValue() {
                return getMostRecentArrayNode().get(1).asText();
            }

            public String getMetricType() {
                return getMostRecentArrayNode().get(1).getNodeType().toString();
            }

            public boolean hasMetricProperty(String property) {
                return this.metric.has(property);
            }

            public String getMetricProperty(String property) {
                return this.metric.get(property).asText();
            }
        }


    }

}
