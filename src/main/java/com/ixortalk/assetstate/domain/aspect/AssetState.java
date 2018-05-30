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
package com.ixortalk.assetstate.domain.aspect;

import java.util.List;

import com.ixortalk.util.InstanceBuilder;

import static com.google.common.collect.Lists.newArrayList;
import static com.ixortalk.assetstate.domain.aspect.Status.OK;
import static com.ixortalk.assetstate.domain.aspect.Status.UNKNOWN;

public class AssetState {

    private List<Aspect> aspects = newArrayList();

    private String id;

    private AssetState () {
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {

        if (!hasAspects() || allAspectsUnknown()) return UNKNOWN;

        return this.aspects
                .stream()
                .filter(aspect -> aspect.getStatus().isError())
                .findFirst()
                .map(aspect -> aspect.getStatus())
                .orElse(OK);
    }

    private boolean allAspectsUnknown() {
        return this.aspects.stream().allMatch(t -> t.getStatus().isUnknown());
    }


    public boolean hasAspects() {
        return !this.aspects.isEmpty();
    }

    //TOOD: this method is exposed on the object and the builder because we need it outside the builder
    // (in the stream implementation of the rest controller). Can this be done in a better way ?
    public void addAspect(Aspect aspect) {
        this.aspects.add(aspect);
    }

    public List<Aspect> getAspects() {
        return this.aspects;
    }

    public static Builder newAssetState() {
        return new Builder();
    }

    public static class Builder extends InstanceBuilder<AssetState> {

        protected AssetState createInstance () {
            return new AssetState();
        }
        public Builder withId (String id) {
            instance().id = id;
            return this;
        }
        public Builder withAspect (Aspect aspect) {
            instance().aspects.add(aspect);
            return this;
        }

    }

}
