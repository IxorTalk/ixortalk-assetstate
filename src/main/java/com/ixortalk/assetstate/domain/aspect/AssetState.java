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
