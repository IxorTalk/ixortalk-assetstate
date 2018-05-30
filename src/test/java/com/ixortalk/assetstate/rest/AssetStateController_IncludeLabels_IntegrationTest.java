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
package com.ixortalk.assetstate.rest;

import java.util.Map;

import javax.inject.Inject;

import com.ixortalk.assetstate.AbstractSpringIntegrationTest;
import com.ixortalk.assetstate.domain.aspect.AssetState;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetricWithIncludeLabels;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test", "includeLabels"})
@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateController_IncludeLabels_IntegrationTest extends AbstractSpringIntegrationTest {

    @Inject
    private AssetStateController assetStateController;

    @Test
    public void specifiedIncludeLabelsUsedInPrometheusQueryAndFilterOnAssets() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS);

        setupPrometheusStubForMetricWithIncludeLabels(wireMockRule, EXPECTED_PROMETHEUS_SINGLE_METRIC_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();

        assertThat(assetStates).hasSize(4);
        assertThat(assetStates.get("asset1").getAspects().stream().anyMatch(aspect -> aspect.getName().equals("include-labels"))).isTrue();
        assertThat(assetStates.get("asset2").getAspects().stream().anyMatch(aspect -> aspect.getName().equals("include-labels"))).isFalse();
        assertThat(assetStates.get("asset3").getAspects().stream().anyMatch(aspect -> aspect.getName().equals("include-labels"))).isFalse();
        assertThat(assetStates.get("asset_only_known_in_assetmgmt").getAspects().stream().anyMatch(aspect -> aspect.getName().equals("include-labels"))).isTrue();
    }
}
