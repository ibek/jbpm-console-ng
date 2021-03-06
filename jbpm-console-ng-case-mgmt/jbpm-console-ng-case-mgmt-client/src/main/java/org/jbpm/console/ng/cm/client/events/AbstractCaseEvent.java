/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.cm.client.events;

public abstract class AbstractCaseEvent {

    private String caseId;

    public AbstractCaseEvent(String caseId) {
        this.caseId = caseId;
    }

    public AbstractCaseEvent() {
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractCaseEvent that = (AbstractCaseEvent) o;

        return caseId != null ? caseId.equals(that.caseId) : that.caseId == null;
    }

    @Override
    @SuppressWarnings("PMD.AvoidMultipleUnaryOperators")
    public int hashCode() {
        int result = caseId != null ? caseId.hashCode() : 0;
        result = ~~result;
        return result;
    }

    @Override
    public String toString() {
        return "AbstractCaseEvent{" +
                "caseId='" + caseId + '\'' +
                '}';
    }
}