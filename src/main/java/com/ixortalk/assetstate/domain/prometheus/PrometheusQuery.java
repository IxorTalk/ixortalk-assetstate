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

import java.util.Map;

import com.ixortalk.assetstate.config.feign.FeignConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.joining;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@FeignClient(name = "prometheust", url = "${assetstate.prometheus.url}", configuration = FeignConfiguration.class)
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
