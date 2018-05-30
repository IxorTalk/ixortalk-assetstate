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
