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
