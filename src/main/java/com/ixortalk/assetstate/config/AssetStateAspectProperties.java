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
package com.ixortalk.assetstate.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ixortalk.assetstate.domain.aspect.AspectStatusResolverStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.ixortalk.assetstate.domain.aspect.AspectStatusResolverStrategy.BOOLEAN_1_OK;

@ConfigurationProperties("ixortalk")
public class AssetStateAspectProperties {


    private IntervalConfig intervalConfig;

    private Mapping mapping;

    private Map<String, Aspect> aspects = newHashMap();

    public Map<String, Aspect> getAspects() {
        return this.aspects;
    }

    public IntervalConfig getIntervalConfig() {
        return this.intervalConfig;
    }

    public void setIntervalConfig(IntervalConfig intervalConfig) {
        this.intervalConfig=intervalConfig;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    public static class Aspect {

        private String metricName;
        private Map<String, String> includeLabels = newHashMap();
        private List<String> flatteningLabels = newArrayList();
        private Optional<String> rangeVectorSelector = Optional.empty();
        private AspectStatusResolverStrategy aspectStatusResolverStrategy = BOOLEAN_1_OK;

        public Map<String, String> getIncludeLabels() { return this.includeLabels; }

        public void setIncludeLabels(Map <String,String> includeLabels) { this.includeLabels = includeLabels; }

        public List<String> getFlatteningLabels() {
            return flatteningLabels;
        }

        public void setFlatteningLabels(List<String> flatteningLabels) {
            this.flatteningLabels = flatteningLabels;
        }

        public String getMetricName() { return this.metricName; }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public Optional<String> getRangeVectorSelector() {
            return rangeVectorSelector;
        }

        public void setRangeVectorSelector(Optional<String> rangeVectorSelector) {
            this.rangeVectorSelector = rangeVectorSelector;
        }

        public AspectStatusResolverStrategy getAspectStatusResolverStrategy() {
            return aspectStatusResolverStrategy;
        }

        public void setAspectStatusResolverStrategy(AspectStatusResolverStrategy aspectStatusResolverStrategy) {
            this.aspectStatusResolverStrategy = aspectStatusResolverStrategy;
        }
    }

    public static class IntervalConfig {

        private String rangeVectorSelector;

        public String getRangeVectorSelector() {
            return rangeVectorSelector;
        }

        public void setRangeVectorSelector(String rangeVectorSelector) {
            this.rangeVectorSelector = rangeVectorSelector;
        }
    }

    public static class Mapping {
        private String identifier;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }
}
