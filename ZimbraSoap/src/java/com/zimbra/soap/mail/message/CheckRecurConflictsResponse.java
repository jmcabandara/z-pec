/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011 Zimbra, Inc.
 *
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap.mail.message;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ConflictRecurrenceInstance;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="CheckRecurConflictsResponse")
public class CheckRecurConflictsResponse {

    /**
     * @zm-api-field-description Information on conflicting instances
     */
    @XmlElement(name=MailConstants.E_INSTANCE /* inst */, required=false)
    private List<ConflictRecurrenceInstance> instances = Lists.newArrayList();

    public CheckRecurConflictsResponse() {
    }

    public void setInstances(Iterable <ConflictRecurrenceInstance> instances) {
        this.instances.clear();
        if (instances != null) {
            Iterables.addAll(this.instances,instances);
        }
    }

    public CheckRecurConflictsResponse addInstance(
                    ConflictRecurrenceInstance instance) {
        this.instances.add(instance);
        return this;
    }

    public List<ConflictRecurrenceInstance> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public Objects.ToStringHelper addToStringInfo(
                Objects.ToStringHelper helper) {
        return helper
            .add("instances", instances);
    }

    @Override
    public String toString() {
        return addToStringInfo(Objects.toStringHelper(this))
                .toString();
    }
}
