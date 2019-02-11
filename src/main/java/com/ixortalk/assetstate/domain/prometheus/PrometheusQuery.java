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

import java.util.Map;

import com.ixortalk.assetstate.config.feign.OAuth2ServiceFeignConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.joining;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@FeignClient(name = "prometheus", url = "${assetstate.prometheus.url}", configuration = OAuth2ServiceFeignConfiguration.class)
public interface PrometheusQuery {

    @RequestMapping(method = GET, value = "/api/v1/query")
    PrometheusRangeQueryResult getRangeVectorFromInstantQuery(@RequestParam("query") PrometheusQueryParam query);

    class PrometheusQueryParam {

        private String metric;
        private String rangeVectorSelector;
        private Map<String, String> includeLabels;

        private PrometheusQueryParam(String metric, String rangeVectorSelector, Map<String, String> includeLabels) {
            this.metric = metric;
            this.rangeVectorSelector = rangeVectorSelector;
            this.includeLabels = includeLabels;
        }

        public static PrometheusQueryParam newPrometheusQueryParam(String metric, String rangeVectorSelector, Map<String, String> includeLabels) {
            return new PrometheusQueryParam(metric, rangeVectorSelector, includeLabels);
        }

        @Override
        public String toString() {
            StringBuilder queryStringBuilder =
                    new StringBuilder()
                            .append(this.metric);

            if (!includeLabels.isEmpty()) {
                queryStringBuilder.append(this.includeLabels.entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(",", "{", "}")));
            }

            if (rangeVectorSelector != null) {
                queryStringBuilder.append("[" + rangeVectorSelector + "]");
            }

            return queryStringBuilder.toString();
        }
    }
}
