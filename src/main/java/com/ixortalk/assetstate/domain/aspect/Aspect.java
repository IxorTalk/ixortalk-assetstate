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
