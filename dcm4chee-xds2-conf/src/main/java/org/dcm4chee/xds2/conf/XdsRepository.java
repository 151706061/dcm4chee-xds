/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.xds2.conf;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.core.util.ConfigIterators;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */

@LDAP(objectClasses = "xdsRepository")
@ConfigurableClass
public class XdsRepository extends XdsExtension {

    public static final Logger log = LoggerFactory.getLogger(XdsRepository.class);

    private static final String DEFAULTID = "*";

    private static final long serialVersionUID = -8258532093950989486L;

    @ConfigurableProperty(name = "xdsRepositoryUID",
            label = "XDS repository UID",
            description = "The unique identifier assigned to the repository",
            group = "General")
    private String repositoryUID;

    @ConfigurableProperty(name = "xdsAcceptedMimeTypes",
            label = "Accept MIME Types",
            description = "Which MIME types documents that are stored in this repository allowed to have.",
            group = "XDS profile strictness")
    private String[] acceptedMimeTypes = new String[]{};

    @ConfigurableProperty(name = "xdsCheckMimetype",
            label = "Check MIME Type",
            description = "Perform MIME type check for registered documents",
            defaultValue = "true",
            group = "XDS profile strictness")
    private boolean checkMimetype;

    @ConfigurableProperty(name = "xdsProvideUrl",
            label = "Provide URL",
            description = "Provide URL that should be used to store documents with this repository (Does NOT actually configure the endpoint!)",
            group = "Endpoints")
    private String provideUrl;

    @ConfigurableProperty(name = "xdsRetrieveUrl",
            label = "Retrieve URL",
            description = "Retrieve URL that should be used to retrieve documents from this repository (Does NOT actually configure the endpoint!)",
            group = "Endpoints"
    )
    private String retrieveUrl;

    @LDAP(
            distinguishingField = "xdsSourceUid",
            mapValueAttribute = "xdsSource",
            mapEntryObjectClass = "xdsSourceByUid"
    )
    @ConfigurableProperty(name = "xdsSources",
            label = "XDS Sources",
            description = "For different source ids the repository can forward to different registries. " +
                    "This mapping (Source Id -> Device) specifies which device is used to lookup the source configuration. " +
                    "The source's configuration is then used to define the registry where the data is forwarded to. Wildcard * is supported.",

            collectionOfReferences= true
    )
    private Map<String, Device> srcDevicebySrcIdMap;

    @LDAP(
            distinguishingField = "xdsAffinityDomain",
            mapValueAttribute = "xdsFileSystemGroupID",
            mapEntryObjectClass = "xdsFilesystemGroupByAffinity"
    )
    @ConfigurableProperty(name = "xdsFileSystemGroupIDs",
            label = "Filesystem groups",
            description = "Mapping (Affinity domain -> Filesystem group) that defines which filesystem groups in the storage are used for which affinity domains. Wildcard * is supported.",
            group = "Storage"
    )
    private Map<String, String> fsGroupIDbyAffinity;

    public String getRegistryURL(String sourceID) {
        try {

            XdsSource src = srcDevicebySrcIdMap.get(sourceID).getDeviceExtensionNotNull(XdsSource.class);
            return src.getRegistry().getDeviceExtensionNotNull(XdsRegistry.class).getRegisterUrl();

        } catch (Exception e) {

            try {
                XdsSource src = srcDevicebySrcIdMap.get(DEFAULTID).getDeviceExtensionNotNull(XdsSource.class);
                String url = src.getRegistry().getDeviceExtensionNotNull(XdsRegistry.class).getRegisterUrl();
                log.warn("Using default registry for source UID {}!", sourceID);
                return url;
            } catch (Exception ee) {
                throw new RuntimeException("Cannot retrieve the registry URL for source UID " + sourceID, e);
            }
        }

    }

    public String getFilesystemGroupID(String affinity) {
        String groupID = fsGroupIDbyAffinity.get(affinity);
        if (groupID == null) {
            groupID = fsGroupIDbyAffinity.get(DEFAULTID);
        }
        return groupID;
    }

    // Getters&setters

    public Map<String, Device> getSrcDevicebySrcIdMap() {
        return srcDevicebySrcIdMap;
    }

    public void setSrcDevicebySrcIdMap(Map<String, Device> srcDevicebySrcIdMap) {
        this.srcDevicebySrcIdMap = srcDevicebySrcIdMap;
    }

    public Map<String, String> getFsGroupIDbyAffinity() {
        return fsGroupIDbyAffinity;
    }

    public void setFsGroupIDbyAffinity(Map<String, String> fsGroupIDbyAffinity) {
        this.fsGroupIDbyAffinity = fsGroupIDbyAffinity;
    }

    public String getProvideUrl() {
        return provideUrl;
    }

    public void setProvideUrl(String provideUrl) {
        this.provideUrl = provideUrl;
    }

    public String getRetrieveUrl() {
        return retrieveUrl;
    }

    public void setRetrieveUrl(String retrieveUrl) {
        this.retrieveUrl = retrieveUrl;
    }

    public String getRepositoryUID() {
        return repositoryUID;
    }

    public void setRepositoryUID(String repsitoryUID) {
        this.repositoryUID = repsitoryUID;
    }

    public String[] getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }

    public void setAcceptedMimeTypes(String[] mimeTypes) {
        this.acceptedMimeTypes = mimeTypes;
    }

    public boolean isCheckMimetype() {
        return checkMimetype;
    }

    public void setCheckMimetype(boolean checkMimetype) {
        this.checkMimetype = checkMimetype;
    }

    @Override
    public void reconfigure(DeviceExtension from) {
        XdsRepository src = (XdsRepository) from;
        ConfigIterators.reconfigure(src, this, XdsRepository.class);
    }

}
