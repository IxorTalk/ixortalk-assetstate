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
package com.ixortalk.assetstate.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.ixortalk.assetstate.config.AssetStateAspectProperties;
import com.ixortalk.assetstate.domain.aspect.AssetState;
import com.ixortalk.assetstate.domain.asset.Asset;
import com.ixortalk.assetstate.domain.asset.AssetMgmt;
import com.ixortalk.assetstate.domain.prometheus.PrometheusQuery;
import com.ixortalk.assetstate.domain.prometheus.PrometheusRangeQueryResult;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ixortalk.assetstate.domain.aspect.Aspect.newAspect;
import static com.ixortalk.assetstate.domain.aspect.Aspect.newEmptyAspect;
import static com.ixortalk.assetstate.domain.aspect.AssetState.newAssetState;
import static com.ixortalk.assetstate.domain.prometheus.PrometheusQuery.PrometheusQueryParam.newPrometheusQueryParam;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


@RestController()
@RequestMapping("/states")
@EnableConfigurationProperties(AssetStateAspectProperties.class)
public class AssetStateController {

    @Inject
    private AssetMgmt assetMgmt;

    @Inject
    private PrometheusQuery prometheusQuery;

    @Inject
    private AssetStateAspectProperties assetStateAspectProperties;

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    public Map<String, AssetState> getAssetStates() throws Exception {

        List<Asset> assets = stream(assetMgmt.assets().spliterator(), false).collect(toList());

        Map<String, AssetState> monitoredAssets = assetStateAspectProperties.getAspects().entrySet()
                .stream()
                .flatMap(aspect ->
                        prometheusQuery.getRangeVectorFromInstantQuery(
                                newPrometheusQueryParam(
                                        aspect.getValue().getMetricName(),
                                        aspect.getValue().getRangeVectorSelector().orElse(assetStateAspectProperties.getIntervalConfig().getRangeVectorSelector()),
                                        aspect.getValue().getIncludeLabels()
                                ))
                                .data.getResult().stream().map(result -> Pair.of(aspect.getKey(), result)))
                .filter(aspectWithResult -> aspectWithResult.getRight().hasMetricProperty(assetStateAspectProperties.getMapping().getIdentifier()))
                .filter(aspectWithResult -> existingAsset(assets, aspectWithResult.getRight()))
                .collect(groupingBy(aspectWithResult -> aspectWithResult.getRight().getMetricProperty(assetStateAspectProperties.getMapping().getIdentifier())))
                .entrySet()
                .stream()
                .map(this::convertMetricResultToAssetState)
                .collect(toMap(AssetState::getId, identity()));

        Map<String, AssetState> nonMonitoredAssets = assets
                .stream()
                .filter(asset -> !monitoredAssets.containsKey(asset.getAssetProperties().getProperties().get(assetStateAspectProperties.getMapping().getIdentifier())))
                .collect(groupingBy(asset -> asset.getAssetProperties().getProperties().get(assetStateAspectProperties.getMapping().getIdentifier()).toString()))
                .entrySet()
                .stream()
                .map(AssetStateController::convertEmptyAssetToAssetState)
                .collect(toMap(AssetState::getId, identity()));

        Map<String, AssetState> combinedAssets = new HashMap<>();
        combinedAssets.putAll(monitoredAssets);
        combinedAssets.putAll(nonMonitoredAssets);

        combinedAssets.values().forEach(assetState -> postProcessEntries(assetState, assetStateAspectProperties.getAspects(), assets));

        return combinedAssets;

    }

    private boolean existingAsset(List<Asset> assets, PrometheusRangeQueryResult.Data.RangeQueryMetric prometheusMetric) {
        return findAsset(prometheusMetric.getMetricProperty(assetStateAspectProperties.getMapping().getIdentifier()), assets).isPresent();
    }

    private AssetState postProcessEntries(AssetState assetState, Map<String, AssetStateAspectProperties.Aspect> configuredAspects, List<Asset> assets) {
        configuredAspects.keySet().stream()
                .filter(configuredAspectKey -> findAsset(assetState.getId(), assets).get().matchesLabels(configuredAspects.get(configuredAspectKey).getIncludeLabels()))
                .filter(configuredAspectKey -> !assetState.getAspects().stream().anyMatch(aspect -> aspect.getName().equals(configuredAspectKey)))
                .forEach(configuredAspectKey -> assetState.addAspect(newEmptyAspect(configuredAspectKey)));
        return assetState;
    }

    private static AssetState convertEmptyAssetToAssetState(Map.Entry<String, List<Asset>> stringListEntry) {
        return newAssetState().withId(stringListEntry.getKey()).build();
    }

    private AssetState convertMetricResultToAssetState(Map.Entry<String, List<Pair<String, PrometheusRangeQueryResult.Data.RangeQueryMetric>>> entry) {
        AssetState assetState = newAssetState().withId(entry.getKey()).build();

        entry.getValue().forEach(aspectKeyResultPair -> {

            AssetStateAspectProperties.Aspect aspect = assetStateAspectProperties.getAspects().get(aspectKeyResultPair.getLeft());
            PrometheusRangeQueryResult.Data.RangeQueryMetric prometheusMetricResult = aspectKeyResultPair.getValue();

            assetState
                    .addAspect(
                            newAspect()
                                    .withName(aspectKeyResultPair.getLeft())
                                    .withLocalDateTime(prometheusMetricResult.getMetricUtcLocalDateTime())
                                    .withValue(prometheusMetricResult.getMetricValue())
                                    .withStatus(aspect.getAspectStatusResolverStrategy().resolveStatus(prometheusMetricResult.getMetricValue()))
                                    .withLabels(aspect.getFlatteningLabels().stream().map(label -> prometheusMetricResult.getMetricProperty(label)).toArray(String[]::new))
                                    .build()
                    );
        });

        return assetState;
    }

    private Optional<Asset> findAsset(String assetId, List<Asset> assets) {
        return assets
                .stream()
                .filter(asset -> assetId.equals(asset.getAssetProperties().getProperties().get(assetStateAspectProperties.getMapping().getIdentifier())))
                .findAny();
    }
}
