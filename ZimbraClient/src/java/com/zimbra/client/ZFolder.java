/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011 Zimbra, Inc.
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

package com.zimbra.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zimbra.soap.mail.type.Acl;
import org.json.JSONException;

import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.client.event.ZModifyFolderEvent;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.Grant;
import com.zimbra.soap.mail.type.Mountpoint;
import com.zimbra.soap.mail.type.RetentionPolicy;
import com.zimbra.soap.mail.type.SearchFolder;

public class ZFolder implements ZItem, Comparable<Object>, ToZJSONObject {

    public static final String ID_USER_ROOT = "1";
    public static final String ID_INBOX = "2";
    public static final String ID_TRASH = "3";
    public static final String ID_SPAM = "4";
    public static final String ID_SENT = "5";
    public static final String ID_DRAFTS = "6";
    public static final String ID_CONTACTS = "7";
    public static final String ID_TAGS = "8";
    public static final String ID_CONVERSATIONS = "9";
    public static final String ID_CALENDAR = "10";
    public static final String ID_ROOT = "11";
    public static final String ID_NOTEBOOK = "12";
    public static final String ID_AUTO_CONTACTS = "13";
    public static final String ID_CHATS = "14";
    public static final String ID_TASKS = "15";
    public static final String ID_BRIEFCASE = "16";
    public static final String ID_FIRST_USER_ID = "256";

    public static final String PERM_WRITE = "w";

    private ZFolder.Color mColor;
    private String mRgb;
    private String mId;
    private String mUuid;
    private String mName;
    private int mUnreadCount;
    private int mImapUnreadCount;
    private long mSize;
    private View mDefaultView;
    private String mFlags;
    private int mMessageCount;
    private int mImapMessageCount;
    private String mParentId;
    private int mModifiedSequence;
    private int mContentSequence;
    private int mImapUIDNEXT;
    private int mImapMODSEQ;
    private String mRemoteURL;
    private String mEffectivePerms;
    private List<ZGrant> mGrants;
    private List<ZFolder> mSubFolders;
    private ZFolder mParent;
    private boolean mIsPlaceholder;
    private ZMailbox mMailbox;
    private RetentionPolicy mRetentionPolicy = new RetentionPolicy();
    private boolean mActiveSyncDisabled;

    @Override
    public int compareTo(Object obj) {
        if (!(obj instanceof ZFolder))
            return 0;
        ZFolder other = (ZFolder) obj;
        return getName().compareToIgnoreCase(other.getName());
    }

    public enum Flag {
        checkedInUI('#'),
        doesNotInheritPermissions('i'),
        excludeFreeBusyInfo('b'),
        imapDeleted('x'),
        imapSubscribed('*'),
        syncFolder('y'),
        syncEnabled('~'),
        noInferiors('o');

        private char mFlagChar;

        public char getFlagChar() { return mFlagChar; }

        public static String toNameList(String flags) {
            if (flags == null || flags.length() == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < flags.length(); i++) {
                String v = null;
                for (Flag f : Flag.values()) {
                    if (f.getFlagChar() == flags.charAt(i)) {
                        v = f.name();
                        break;
                    }
                }
                if (sb.length() > 0) sb.append(", ");
                sb.append(v == null ? flags.substring(i, i+1) : v);
            }
            return sb.toString();
        }

        Flag(char flagChar) {
            mFlagChar = flagChar;
        }
    }

    public enum Color {

        defaultColor(0),
        blue(1),
        cyan(2),
        green(3),
        purple(4),
        red(5),
        yellow(6),
        pink(7),
        gray(8),
        orange(9),
        rgbColor;

        private long mValue;

        public long getValue() { return mValue; }

        public static Color fromString(String s) throws ServiceException {
            try {
                return fromInt(Integer.parseInt(s));
            } catch (NumberFormatException e) {
            } catch (IndexOutOfBoundsException e) {
            }

            try {
                return Color.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ZClientException.CLIENT_ERROR("invalid color: "+s+", valid values: "+Arrays.asList(Color.values()), e);
            }
        }

        Color(long value) { mValue = value; }

        Color() {}

        public Color setRgbColor(String s) {
            mValue = new com.zimbra.common.mailbox.Color(s).getValue();
            return this;
        }

        public String getRgbColor() {
            return new com.zimbra.common.mailbox.Color(mValue).toString();
        }

        public static Color fromInt(int i) throws ServiceException {
            try {
                return values()[i];
            } catch (IndexOutOfBoundsException e) {
                throw ZClientException.CLIENT_ERROR("invalid color: "+i+", must be between 0 and " + (values().length - 1), null);
            }
        }
    }

    public static final String[] RGB_COLORS = {
        // none,      blue,     cyan,      green,     purple
        "#000000",  "#5b9bf2", "#43eded", "#6acb9e", "#ba86e5",
        // red,     yellow,     pink,     gray      orange
        "#f66666", "#f8fa33", "#fe98d3", "#bebebe", "#fdbc55"
    };

    public enum View {
        appointment, chat, contact, conversation, document, message, remote,
        search, task, unknown, voice, wiki;

        public static View fromString(String src) {
            if (src == null) {
                return unknown;
            } else if ("search folder".equals(src)) {
                return search;
            } else if ("remote folder".equals(src)) {
                return remote;
            }
            try {
                return View.valueOf(src);
            } catch (IllegalArgumentException e) {
                return unknown;
            }
        }

    }

    public ZFolder(Element e, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        mMailbox = mailbox;
        mParent = parent;
        mId = e.getAttribute(MailConstants.A_ID);
        mUuid = e.getAttribute(MailConstants.A_UUID, null);
        mName = e.getAttribute(MailConstants.A_NAME, null);
        mParentId = e.getAttribute(MailConstants.A_FOLDER, null);
        mIsPlaceholder = mParentId == null;
        mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
        mRgb = e.getAttribute(MailConstants.A_RGB, null);
        if (mRgb != null) {
            mColor =  Color.rgbColor.setRgbColor(mRgb);
        } else {
            String s = e.getAttribute(MailConstants.A_COLOR, "0");
            mColor = Color.values()[(byte)Long.parseLong(s)];
        }
        mUnreadCount = (int) e.getAttributeLong(MailConstants.A_UNREAD, 0);
        mImapUnreadCount = (int) e.getAttributeLong(MailConstants.A_IMAP_UNREAD, mUnreadCount);
        mMessageCount = (int) e.getAttributeLong(MailConstants.A_NUM, 0);
        mImapMessageCount = (int) e.getAttributeLong(MailConstants.A_IMAP_NUM, mMessageCount);
        mDefaultView = View.fromString(e.getAttribute(MailConstants.A_DEFAULT_VIEW, null));
        mModifiedSequence = (int) e.getAttributeLong(MailConstants.A_MODIFIED_SEQUENCE, -1);
        mContentSequence = (int) e.getAttributeLong(MailConstants.A_REVISION, -1);
        mImapUIDNEXT = (int) e.getAttributeLong(MailConstants.A_IMAP_UIDNEXT, -1);
        mImapMODSEQ = (int) e.getAttributeLong(MailConstants.A_IMAP_MODSEQ, -1);
        mRemoteURL = e.getAttribute(MailConstants.A_URL, null);
        mEffectivePerms = e.getAttribute(MailConstants.A_RIGHTS, null);
        mSize = e.getAttributeLong(MailConstants.A_SIZE, 0);
        mActiveSyncDisabled = e.getAttributeBool(MailConstants.A_ACTIVESYNC_DISABLED, false);

        mGrants = new ArrayList<ZGrant>();
        mSubFolders = new ArrayList<ZFolder>();

        Element aclEl = e.getOptionalElement(MailConstants.E_ACL);

        if (aclEl != null) {
            for (Element grant : aclEl.listElements(MailConstants.E_GRANT)) {
                mGrants.add(new ZGrant(grant));
            }
        }

        Element rpEl = e.getOptionalElement(MailConstants.E_RETENTION_POLICY);
        if (rpEl != null) {
            mRetentionPolicy = new RetentionPolicy(rpEl);
        }

        // sub folders
        for (Element child : e.listElements(MailConstants.E_FOLDER))
            mSubFolders.add(createSubFolder(child));

        // search
        for (Element s : e.listElements(MailConstants.E_SEARCH))
            mSubFolders.add(new ZSearchFolder(s, this, mMailbox));

        // link
        for (Element l : e.listElements(MailConstants.E_MOUNT))
            mSubFolders.add(new ZMountpoint(l, this, mMailbox));
    }

    public ZFolder(Folder f, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        mMailbox = mailbox;
        mParent = parent;
        mId = f.getId();
        mUuid = f.getUuid();
        mName = f.getName();
        mParentId = f.getParentId();
        mIsPlaceholder = mParentId == null;
        mFlags = f.getFlags();
        mRgb = f.getRgb();
        if (mRgb != null) {
            mColor =  Color.rgbColor.setRgbColor(mRgb);
        } else {
            mColor = ZFolder.Color.fromInt(SystemUtil.coalesce(f.getColor(), 0));
        }
        mUnreadCount = SystemUtil.coalesce(f.getUnreadCount(), 0);
        mImapUnreadCount = SystemUtil.coalesce(f.getImapUnreadCount(), mUnreadCount);
        mMessageCount = SystemUtil.coalesce(f.getItemCount(), 0);
        mImapMessageCount = SystemUtil.coalesce(f.getImapItemCount(), mMessageCount);

        mDefaultView = View.conversation;
        if (f.getView() != null) {
            mDefaultView = SoapConverter.FROM_SOAP_VIEW.apply(f.getView());
        }

        mModifiedSequence = SystemUtil.coalesce(f.getModifiedSequence(), -1);
        mContentSequence = SystemUtil.coalesce(f.getRevision(), -1);
        mImapUIDNEXT = SystemUtil.coalesce(f.getImapUidNext(), -1);
        mImapMODSEQ = SystemUtil.coalesce(f.getImapModifiedSequence(), -1);
        mRemoteURL = f.getUrl();
        mEffectivePerms = f.getPerm();
        mSize = SystemUtil.coalesce(f.getTotalSize(), 0L);
        if (f.isActiveSyncDisabled() != null)
            mActiveSyncDisabled = f.isActiveSyncDisabled();

        mGrants = new ArrayList<ZGrant>();
        mSubFolders = new ArrayList<ZFolder>();

        Acl acl = f.getAcl();
        if (acl != null) {
            for (Grant g : acl.getGrants()) {
                mGrants.add(new ZGrant(g));
            }
        }

        mRetentionPolicy = f.getRetentionPolicy();

        for (Folder folder : f.getSubfolders()) {
            if (folder instanceof SearchFolder) {
                mSubFolders.add(new ZSearchFolder((SearchFolder) folder, this, mMailbox));
            } else if (folder instanceof Mountpoint) {
                mSubFolders.add(new ZMountpoint((Mountpoint) folder, this, mMailbox));
            } else {
                mSubFolders.add(new ZFolder(folder, this, getMailbox()));
            }
        }

        // TODO
        /*
        // search
        for (Element s : e.listElements(MailConstants.E_SEARCH))
            mSubFolders.add(new ZSearchFolder(s, this, mMailbox));

        // link
        for (Element l : e.listElements(MailConstants.E_MOUNT))
            mSubFolders.add(new ZMountpoint(l, this, mMailbox));
            */
    }

    protected ZFolder createSubFolder(Element element) throws ServiceException {
        return new ZFolder(element, this, getMailbox());
    }

    synchronized void addChild(ZFolder folder) {
        List<ZFolder> newSubs = new ArrayList<ZFolder>(mSubFolders);
        newSubs.add(folder);
        mSubFolders = newSubs;
    }

    synchronized void removeChild(ZFolder folder) {
        List<ZFolder> newSubs = new ArrayList<ZFolder>(mSubFolders);
        newSubs.remove(folder);
        mSubFolders = newSubs;
    }

    @Override
    public void modifyNotification(ZModifyEvent event) throws ServiceException {
        if (event instanceof ZModifyFolderEvent) {
            ZModifyFolderEvent fevent = (ZModifyFolderEvent) event;
            mName = fevent.getName(mName);
            mParentId = fevent.getParentId(mParentId);
            mFlags = fevent.getFlags(mFlags);
            mColor = fevent.getColor(mColor);
            mUnreadCount = fevent.getUnreadCount(mUnreadCount);
            mImapUnreadCount = fevent.getImapUnreadCount(mImapUnreadCount);
            mMessageCount = fevent.getMessageCount(mMessageCount);
            mImapMessageCount = fevent.getImapMessageCount(mImapMessageCount);
            mDefaultView = fevent.getDefaultView(mDefaultView);
            mModifiedSequence = fevent.getModifiedSequence(mModifiedSequence);
            mContentSequence = fevent.getContentSequence(mContentSequence);
            mImapUIDNEXT = fevent.getImapUIDNEXT(mImapUIDNEXT);
            mImapMODSEQ = fevent.getImapMODSEQ(mImapMODSEQ);
            mRemoteURL = fevent.getRemoteURL(mRemoteURL);
            mActiveSyncDisabled = fevent.isActiveSyncDisabled(mActiveSyncDisabled);
            mEffectivePerms = fevent.getEffectivePerm(mEffectivePerms);
            mGrants = fevent.getGrants(mGrants);
            mSize = fevent.getSize(mSize);
            mRetentionPolicy = fevent.getRetentionPolicy(mRetentionPolicy);
        }
    }

    public ZSearchContext getSearchContext() {
        ZSearchParams params = new ZSearchParams("inid:"+getId());
        if (getDefaultView() != null) params.setTypes(getDefaultView().name());
        return new ZSearchContext(params,getMailbox());
    }

    public ZMailbox getMailbox() {
        return mMailbox;
    }

    public ZFolder getParent() {
        return mParent;
    }

    void setParent(ZFolder parent) {
        mParent = parent;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getUuid() {
        return mUuid;
    }

    /** Returns the folder's name.  Note that this is the folder's
     *  name (e.g. <code>"foo"</code>), not its absolute pathname
     *  (e.g. <code>"/baz/bar/foo"</code>).
     *
     * @see #getPath()
     *
     */
    public String getName() {
        return mName;
    }

	public String getNameURLEncoded() {
		try {
			return URLEncoder.encode(mName, "utf-8").replace("+", "%20");
		}
		catch (UnsupportedEncodingException e) {
			return mName;
		}
	}

    /** Returns the folder's absolute path.  Paths are UNIX-style with
     *  <code>'/'</code> as the path delimiter.  Paths are relative to
     *  the user root folder,
     *  which has the path <code>"/"</code>.  So the Inbox's path is
     *  <code>"/Inbox"</code>, etc.
     */
    public String getPath() {
        // TODO: CACHE? compute upfront?
        if (mParent == null)
            return ZMailbox.PATH_SEPARATOR;
        else {
            String pp = mParent.getPath();
            return pp.length() == 1 ? (pp + mName) : (pp + ZMailbox.PATH_SEPARATOR + mName);
        }
    }

    /** Returns the folder's absolute path, url-encoded.  Paths are UNIX-style with
     *  <code>'/'</code> as the path delimiter.  Paths are relative to
     *  the user root folder,
     *  which has the path <code>"/"</code>.  So the Inbox's path is
     *  <code>"/Inbox"</code>, etc.
     */
    public String getPathURLEncoded() {
        // TODO: CACHE? compute upfront?
        if (mParent == null)
            return ZMailbox.PATH_SEPARATOR;
        else {
            String pp = mParent.getPathURLEncoded();
            String n = null;
            try {
                n = URLEncoder.encode(mName, "utf-8").replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                n = mName;
            }
            return pp.length() == 1 ? (pp + n) : (pp + ZMailbox.PATH_SEPARATOR + n);
        }
    }

     /** Returns the folder's  path relative to the root
     * @return path
     */
    public String getRootRelativePath() {
        String path = getPath();
        if (path.startsWith(ZMailbox.PATH_SEPARATOR)) {
            return path.substring(ZMailbox.PATH_SEPARATOR.length());
        } else {
            return path;
        }
    }

    public String getRootRelativePathURLEncoded() {
        String path = getPathURLEncoded();
        if (path.startsWith(ZMailbox.PATH_SEPARATOR)) {
            return path.substring(ZMailbox.PATH_SEPARATOR.length());
        } else {
            return path;
        }
    }

    /**
     *
     * @return parent id of folder, or null if root folder.
     */
    public String getParentId() {
        return mParentId;
    }

    /**
     * @return <tt>true</tt> if this is just a shim for setting up hierarchy namespace for subfolders,
     * <tt>false</tt> if it's a real folder visible to the user
     */
    boolean isHierarchyPlaceholder() {
        return mIsPlaceholder;
    }

    /**
     * @return number of unread items in folder
     */
    public int getUnreadCount() {
        return mUnreadCount;
    }

    /**
     * @return number of unread items in folder, including IMAP \Deleted items
     */
    public int getImapUnreadCount() {
        return mImapUnreadCount;
    }

    /**
     * Sets unread count.
     * @param count unread count.
     */
    void setUnreadCount(int count) {
        mUnreadCount = count;
    }

    /**
     * @return size of all items in folder (not including sub folders) in bytes
     */
    public long getSize() {
        return mSize;
    }

    /**
     * @return number of items in folder
     */
    public int getMessageCount() {
        return mMessageCount;
    }

    /**
     * @return number of items in folder, including IMAP \Deleted item
     */
    public int getImapMessageCount() {
        return mImapMessageCount;
    }

    /**
     * Sets message count.
     * @param count message count.
     */
    void setMessageCount(int count) {
        mMessageCount = count;
    }

    /** Returns the "hint" as to which view to use to display the folder's
     *  contents.
     */
    public View getDefaultView() {
        return mDefaultView;
    }

    /** Returns the sequence number for the last change in the folder's metadata.
     */
    public int getModifiedSequence() {
        return mModifiedSequence;
    }

    /** Returns the sequence number for the last change in the folder's location or name.
     */
    public int getContentSequence() {
        return mContentSequence;
    }

    /** Returns a counter that increments each time an item is added to the folder.
     */
    public int getImapUIDNEXT() {
        return mImapUIDNEXT;
    }

    /** Returns the sequence number for the last change that affected the folder's contents.
     */
    public int getImapMODSEQ() {
        return mImapMODSEQ;
    }

    /**
     *  checked in UI (#), exclude free/(b)usy info, IMAP subscribed (*)
     */
    public String getFlags() {
        return mFlags;
    }

    public boolean hasFlags() {
        return mFlags != null && mFlags.length() > 0;
    }

    public boolean isCheckedInUI() {
        return hasFlags() && mFlags.indexOf(Flag.checkedInUI.getFlagChar()) != -1;
    }

    public boolean isExcludedFromFreeBusy() {
        return hasFlags() && mFlags.indexOf(Flag.excludeFreeBusyInfo.getFlagChar()) != -1;
    }

    public boolean isIMAPSubscribed() {
        return hasFlags() && mFlags.indexOf(Flag.imapSubscribed.getFlagChar()) != -1;
    }

    public boolean isIMAPDeleted() {
        return hasFlags() && mFlags.indexOf(Flag.imapDeleted.getFlagChar()) != -1;
    }

    public boolean isSyncFolder() {
        return hasFlags() && mFlags.indexOf(Flag.syncFolder.getFlagChar()) != -1;
    }

    public boolean isSyncEnabled() {
        return hasFlags() && mFlags.indexOf(Flag.syncEnabled.getFlagChar()) != -1;
    }

    public boolean isNoInferiors() {
        return hasFlags() && mFlags.indexOf(Flag.noInferiors.getFlagChar()) != -1;
    }

    /**
     * range 0-127; defaults to 0 if not present; client can display only 0-7
     *
     * @return color
     */
    public Color getColor() {
        return mColor;
    }

    public String getRgb() {
        return mRgb;
    }
    /**
     * remote URL (RSS, iCal, etc) this folder syncs to
     *
     * @return
     */
    public String getRemoteURL() {
        return mRemoteURL;
    }
    
    public boolean isActiveSyncDisabled() {
        return mActiveSyncDisabled;
    }

    /**
     * for remote folders, the access rights the authenticated user has on the folder.
     *
     * @return
     */
    public String getEffectivePerms() {
        return mEffectivePerms;
    }

    /**
     * return grants or empty list if no grants
     */
    public List<ZGrant> getGrants() {
        return mGrants;
    }

    /**
     * @return sub folders, or empty list if no sub folders
     */
    public List<ZFolder> getSubFolders() {
        return mSubFolders;
    }

    /**
     * return sub folder with specified path. Path must not start with the mailbox path separator.
     * @param path
     * @return sub folder of this folder,
     */
    public ZFolder getSubFolderByPath(String path) {
        if (path.length() == 0) return this;
        int index = path.indexOf(ZMailbox.PATH_SEPARATOR);
        String name = index == -1 ? path : path.substring(0, index);
        String subpath = index == -1 ? null : path.substring(index+1);
        for (ZFolder f: getSubFolders()) {
            if (f.getName().equalsIgnoreCase(name)) {
                if (subpath != null) return f.getSubFolderByPath(subpath);
                else return (f.isHierarchyPlaceholder() ? null : f);
            }
        }
        return null;
    }

    public boolean isSystemFolder() {
        try {
            return Integer.parseInt(mId) < Integer.parseInt(ZFolder.ID_FIRST_USER_ID);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public RetentionPolicy getRetentionPolicy() {
        return mRetentionPolicy;
    }

    @Override
    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject jo = new ZJSONObject();
        jo.put("id", mId);
        jo.put("uuid", mUuid);
        jo.put("name", mName);
        jo.put("path", getPath());
        jo.put("pathURLEncoded", getPathURLEncoded());
        jo.put("parentId", mParentId);
        jo.put("flags", mFlags);
        jo.put("color", mColor.name());
        jo.put("unreadCount", mUnreadCount);
        jo.put("imapUnreadCount", mImapUnreadCount);
        jo.put("itemCount", mMessageCount);
        jo.put("imapItemCount", mImapMessageCount);
        jo.put("defaultView", mDefaultView.name());
        jo.put("imapUIDNEXT", mImapUIDNEXT);
        jo.put("imapMODSEQ", mImapMODSEQ);
        jo.put("defaultView", mDefaultView.name());
        jo.put("remoteUrl", mRemoteURL);
        jo.put("activeSyncDisabled", mActiveSyncDisabled);
        jo.put("effectivePermissions", mEffectivePerms);
        jo.put("grants", mGrants);
        jo.put("size", mSize);
        jo.put("subFolders", mSubFolders);
        jo.put("isCheckedInUI", isCheckedInUI());
        jo.put("isExcludedFromFreeBusy", isExcludedFromFreeBusy());
        jo.put("isSyncEnabled", isSyncEnabled());
        jo.put("isSyncFolder", isSyncFolder());
        jo.put("isSystemFolder", isSystemFolder());
        jo.put("contentSequence", getContentSequence());
        return jo;
    }

    @Override public String toString() {
        return String.format("[ZFolder %s]", getPath());
    }

    public String dump() {
        return ZJSONObject.toString(this);
    }

    public void delete() throws ServiceException { mMailbox.deleteFolder(mId); }

    public void deleteItem() throws ServiceException { delete(); }

    public void moveToTrash() throws ServiceException { mMailbox.trashFolder(mId); }

    public void sync() throws ServiceException { mMailbox.syncFolder(mId); }

    public void clearGrants() throws ServiceException {
        mMailbox.updateFolder(mId, null, null, null, null, null, new ArrayList<ZGrant>());
    }

    public void empty(boolean recursive) throws ServiceException { mMailbox.emptyFolder(mId, recursive); }

    public void importURL(String url) throws ServiceException { mMailbox.importURLIntoFolder(mId, url); }

    public void markRead() throws ServiceException { mMailbox.markFolderRead(mId); }

    public void checked(boolean checked) throws ServiceException { mMailbox.modifyFolderChecked(mId, checked); }

    public void modifySyncFlag(boolean checked) throws ServiceException { mMailbox.modifyFolderSyncFlag(mId, checked); }

    public void modifyExcludeFreeBusy(boolean exclude) throws ServiceException { mMailbox.modifyFolderExcludeFreeBusy(mId, exclude); }

    public void modifyColor(ZFolder.Color color) throws ServiceException { mMailbox.modifyFolderColor(mId, color); }

    public void modifyFlags(String flags) throws ServiceException { mMailbox.updateFolder(mId, null, null, null, null, flags, null); }

    public void modifyURL(String url) throws ServiceException { mMailbox.modifyFolderURL(mId, url); }

    public void rename(String newPath) throws ServiceException { mMailbox.renameFolder(mId, newPath); }

    public void updateFolder(String name, String parentId, Color newColor, String rgbColor, String flags, List<ZGrant> acl) throws ServiceException {
        mMailbox.updateFolder(mId, name, parentId, newColor, rgbColor, flags, acl);
    }
}
