/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2007, 2009, 2010 Zimbra, Inc.
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

/**
 * Creates an empty list of voicemails.
 * @constructor
 * @class
 * This class represents a list of voicemails.
 *
 * @author Dave Comfort
 * 
 * @param type		type of thing in the list
 * @param search	the search that generated this list
 */
ZmVoiceList = function(type, search) {
	ZmList.call(this, type, search);
	this.folder = null;
};

ZmVoiceList.prototype = new ZmList;
ZmVoiceList.prototype.constructor = ZmVoiceList;

ZmVoiceList.prototype.toString = 
function() {
	return "ZmVoiceList";
};

/**
 * Moves items from one voice folder to another (typically Trash)
 * @param params		[hash]			hash of params:
 *        items			[array]			a list of items to move
 *        folder		[ZmFolder]		destination folder
 *        attrs			[hash]			additional attrs for SOAP command
 */
ZmVoiceList.prototype.moveItems =
function(params) {

	params = Dwt.getParams(arguments, ["items", "folder", "attrs"]);

	var params1 = AjxUtil.hashCopy(params);
	params1.items = AjxUtil.toArray(params.items);
	params1.attrs = params.attrs || {};
	params1.attrs.phone = this.folder.phone.name;
	params1.attrs.l = params.folder.id;
	params1.action = "move";
    if (params1.folder.id == ZmFolder.ID_TRASH) {
		if (params1.items.length > 1) {
	        params1.actionText = ZmMsg.actionTrash;
		}
    } else {
        params1.actionText = ZmMsg.actionMove;
        params1.actionArg = params.folder.getName(false, false, true);
    }
	params1.callback = new AjxCallback(this, this._handleResponseMoveItems, params);

	this._itemAction(params1);
};

/**
 * Does a hard (permanent) delete
 * @param params		[hash]			hash of params:
 *        items			[array]			a list of items to delete
 *        attrs			[hash]			additional attrs for SOAP command
 */
ZmVoiceList.prototype.deleteItems =
    function(params) {

        params = Dwt.getParams(arguments, ["items", "attrs"]);

        var params1 = AjxUtil.hashCopy(params);
        params1.items = AjxUtil.toArray(params.items);
        params1.attrs = params.attrs || {};
        params1.attrs.phone = this.folder.phone.name;
        //params1.attrs.l = params.folder.id;
        var plen = params1.items && params1.items.length || 0;
        if ( plen && !params.confirmDelete) {
            params.confirmDelete = true;
            var callback = ZmVoiceList.prototype.deleteItems.bind(this, params);
            this._popupDeleteWarningDialog(callback, 0, plen);
            return;
        }
        params.confirmDelete=false;
        params1.action = "delete";
        params1.callback = new AjxCallback(this, this._handleResponseMoveItems, params);    // Post processing for soft/hard delete is the same

        this._itemAction(params1);
    };

// The voice server isn't sending notifications. This callback updates
// folders and such after a move.
ZmVoiceList.prototype._handleResponseMoveItems =
function(params) {

	// Remove the items.
	for (var i = 0, count = params.items.length; i < count; i++) {
		this.remove(params.items[i]);
	}
	
	// Update the unread counts in the folders.
	var numUnheard = 0;
	for (var i = 0, count = params.items.length; i < count; i++) {
		if (params.items[i].isUnheard) {
			numUnheard++;
		}
	}
	var sourceFolder = params.items[0].getFolder();
	if (numUnheard) {
		sourceFolder.changeNumUnheardBy(-numUnheard);
        if (sourceFolder != params.folder){    // For hard delete, the source & destination folders are the same
		    params.folder.changeNumUnheardBy(numUnheard);
        }
	}
	
	// Replenish the list view.
	//
	// This is sort of a hack having the model call back to the controller, but without notifications
	// this seems like the best approach.
	var controller = AjxDispatcher.run("GetVoiceController");
	controller._handleResponseMoveItems(params);
};

ZmVoiceList.prototype._getActionNamespace =
function() {
	return "urn:zimbraVoice";
};

