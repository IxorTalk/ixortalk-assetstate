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

import java.time.LocalDateTime;
import java.util.List;

import com.ixortalk.util.InstanceBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import static com.google.common.collect.Lists.newArrayList;
import static com.ixortalk.assetstate.domain.aspect.Status.UNKNOWN;
import static java.util.stream.Collectors.joining;

public class Aspect {

    public static final String EMPTY_VALUE = "";
    private String name;
    private String value;
    private List<String> labels = newArrayList();
    private LocalDateTime localDateTime;
    private Status status = UNKNOWN;

    public static Aspect newEmptyAspect(String name) {
        return new Aspect(name);
    }

    private Aspect () {
    }

    private Aspect (String name) {
        this.name=name;
        this.value= EMPTY_VALUE;
    }

    public String getId() { return name + getLabels().stream().collect(joining("-")); }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<String> getLabels() {
        return labels;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public Status getStatus() {
        return this.status;
    }

    public static Builder newAspect() {
        return new Builder();
    }

    public static class Builder extends InstanceBuilder<Aspect> {

        protected Aspect createInstance () {
            return new Aspect();
        }

        public Builder withLocalDateTime(LocalDateTime localDateTime) {
            instance().localDateTime = localDateTime;
            return this;
        }
        public Builder withValue(String value) {
            instance().value = value;
            return this;
        }
        public Builder withName(String name) {
            instance().name= name;
            return this;
        }

        public Builder withLabels(String... labels) {
            instance().labels.addAll(newArrayList(labels));
            return this;
        }
        public Builder withStatus(Status status) {
            instance().status= status;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aspect aspect = (Aspect) o;

        if (name != null ? !name.equals(aspect.name) : aspect.name != null) return false;
        if (value != null ? !value.equals(aspect.value) : aspect.value != null) return false;
        return localDateTime != null ? localDateTime.equals(aspect.localDateTime) : aspect.localDateTime == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (localDateTime != null ? localDateTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).toString();
    }
}
