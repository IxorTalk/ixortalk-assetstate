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
import com.ixortalk.assetstate.domain.asset.AspectBuilderforTest;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static com.ixortalk.assetstate.ConfigurationTestConstants.WITH_FLATTENING_LABELS_ASPECT;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.FLATTENING_LABELS_PROMETHEUS_RESPONSE;
import static com.ixortalk.assetstate.rest.PrometheusStubHelper.setupPrometheusStubForMetric;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test", "flatteningLabels"})
@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AssetStateController_FlatteningLabels_IntegrationTest extends AbstractSpringIntegrationTest {

    @Inject
    private AssetStateController assetStateController;

    @Test
    public void flatteningLabels_flattensOnSpecifiedLabelValuesInAspect() throws Exception {
        setupAssetMgmtStubWithMetrics(ASSETS);

        setupPrometheusStubForMetric(wireMockRule, FLATTENING_LABELS_PROMETHEUS_RESPONSE);

        Map<String, AssetState> assetStates = assetStateController.getAssetStates();

        assertThat(assetStates).hasSize(4);
        assertThat(assetStates.get("asset1").getAspects())
                .usingElementComparatorIgnoringFields("value", "status", "localDateTime")
                .containsOnly(
                        new AspectBuilderforTest()
                                .withName(WITH_FLATTENING_LABELS_ASPECT)
                                .withLabels("labelA1", "labelB1")
                                .build(),
                        new AspectBuilderforTest()
                                .withName(WITH_FLATTENING_LABELS_ASPECT)
                                .withLabels("labelA1", "labelB2")
                                .build(),
                        new AspectBuilderforTest()
                                .withName(WITH_FLATTENING_LABELS_ASPECT)
                                .withLabels("labelA2", "labelB2")
                                .build());
    }
}
