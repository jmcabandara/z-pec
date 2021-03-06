/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Zimlets
 * Copyright (C) 2006, 2007, 2008, 2009, 2010 Zimbra, Inc.
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

function Com_Zimbra_Search_Google(zimlet) {
	this.zimlet = zimlet;
	this.icon = "Google-Icon";
	this.label = this.zimlet.getMessage("com_zimbra_search_google");
};

Com_Zimbra_Search_Google.prototype.getSearchFormHTML =
function(query) {
	var zimlet = this.zimlet;
	var props = {
		acct  : zimlet.getConfig("google-account"),
		query : query
	};
	var code = zimlet.getConfig("google-search-code");
	code = zimlet.xmlObj().replaceObj(ZmZimletContext.RE_SCAN_PROP, code, props);
	return code;
};

Com_Zimbra_Search.registerHandler(Com_Zimbra_Search_Google);
