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

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class HostStats {

    /**
     * @zm-api-field-tag hostname
     * @zm-api-field-description Hostname
     */
    @XmlAttribute(name=AdminConstants.A_HOSTNAME, required=true)
    private final String hostName;

    /**
     * @zm-api-field-description Stats information
     */
    @XmlElement(name=AdminConstants.E_STATS, required=false)
    private StatsInfo stats;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private HostStats() {
        this((String) null);
    }

    public HostStats(String hostName) {
        this.hostName = hostName;
    }

    public void setStats(StatsInfo stats) { this.stats = stats; }
    public String getHostName() { return hostName; }
    public StatsInfo getStats() { return stats; }
}
