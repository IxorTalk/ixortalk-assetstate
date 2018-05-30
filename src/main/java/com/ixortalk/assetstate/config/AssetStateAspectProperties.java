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
