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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4chee.xds2.common.XDSUtil;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@ConfigClass(commonName = "XCAInitiatingGW", objectClass = "xcaInitiatingGW", nodeName = "xcaInitiatingGW")
public class XCAInitiatingGWCfg extends DeviceExtension {

	private static final long serialVersionUID = -8258532093950989486L;

	@ConfigField(name = "xdsApplicationName")
	private String applicationName;

	
	@ConfigField(name = "xdsHomeCommunityID")
	private String homeCommunityID;

	
	/**
	 * Ghost property, actual data is stored in a map
	 */
	@ConfigField(name = "xdsRespondingGatewayURL")
	private String[] respondingGWURLs;

	private Map<String, String> respondingGWUrlMapping = new HashMap<String, String>();

	
	/**
	 * Ghost property, actual data is stored in a map
	 */
	@ConfigField(name = "xdsRespondingGatewayRetrieveURL")
	private String[] respondingGWRetrieveURLs;

	private Map<String, String> respondingGWRetrieveUrlMapping = new HashMap<String, String>();

	
	/**
	 * Ghost property, actual data is stored in a map
	 */
	@ConfigField(name = "xdsAssigningAuthority")
	private String[] assigningAuthoritiesMap;

	private Map<String, String> homeIdToAssigningAuthotityMapping = new HashMap<String, String>();

	
	// AffinityDomain Option
	@ConfigField(name = "xdsRegistryURL")
	private String registryURL;

	/**
	 * Ghost property, actual data is stored in a map
	 */
	@ConfigField(name = "xdsRepositoryURL")
	private String[] repositoryURLs;

	private Map<String, String> repositoryUrlMapping = new HashMap<String, String>();


	@ConfigField(name = "xdsSoapMsgLogDir")
	private String soapLogDir;
	
	
	@ConfigField(name = "xdsAsync")
	private boolean async;

	
	@ConfigField(name = "xdsAsyncHandler")
	private boolean asyncHandler;
	
	
	@ConfigField(name = "xdsPIXManagerApplication")
	private String remotePIXManagerApplication;
	

	@ConfigField(name = "xdsPIXConsumerApplication")
	private String localPIXConsumerApplication;

	public String getApplicationName() {
		return applicationName;
	}

	public final void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getHomeCommunityID() {
		return homeCommunityID;
	}

	public void setHomeCommunityID(String homeCommunityID) {
		this.homeCommunityID = homeCommunityID;
	}

	public String getRegistryURL() {
		return registryURL;
	}

	public void setRegistryURL(String registryURL) {
		this.registryURL = registryURL;
	}

	public String getSoapLogDir() {
		return soapLogDir;
	}

	public void setSoapLogDir(String soapLogDir) {
		this.soapLogDir = soapLogDir;
	}

	public String[] getRepositoryURLs() {
		return XDSUtil.map2keyValueStrings(repositoryUrlMapping, '|');
	}

	public void setRepositoryURLs(String[] repositoryURLs) {
		XDSUtil.storeKeyValueStrings2map(repositoryURLs, '|', "*", repositoryUrlMapping);
	}

	public String getRepositoryURL(String repositoryID) {
		return XDSUtil.getValue(repositoryID, "*", repositoryUrlMapping);
	}

	public String[] getRespondingGWURLs() {
		return XDSUtil.map2keyValueStrings(respondingGWUrlMapping, '|');
	}

	public void setRespondingGWURLs(String[] urls) {
		XDSUtil.storeKeyValueStrings2map(urls, '|', "*", respondingGWUrlMapping);
	}

	public String[] getRespondingGWRetrieveURLs() {
		return XDSUtil.map2keyValueStrings(respondingGWRetrieveUrlMapping, '|');
	}

	public void setRespondingGWRetrieveURLs(String[] urls) {
		XDSUtil.storeKeyValueStrings2map(urls, '|', "*", respondingGWRetrieveUrlMapping);
	}

	public Collection<String> getCommunityIDs() {
		return respondingGWUrlMapping.keySet();
	}

	public String getRespondingGWQueryURL(String homeCommunityID) {
		return XDSUtil.getValue(homeCommunityID, "*", respondingGWUrlMapping);
	}

	public String getRespondingGWRetrieveURL(String homeCommunityID) {
		String url = XDSUtil.getValue(homeCommunityID, "*", respondingGWRetrieveUrlMapping);
		return url == null || "query".equalsIgnoreCase(url) ? XDSUtil.getValue(homeCommunityID, "*", respondingGWUrlMapping) : url;
	}

	public String[] getAssigningAuthoritiesMap() {
		return XDSUtil.map2keyValueStrings(homeIdToAssigningAuthotityMapping, '|');
	}

	public void setAssigningAuthoritiesMap(String[] sa) {
		XDSUtil.storeKeyValueStrings2map(sa, '|', "*", homeIdToAssigningAuthotityMapping);
	}

	public String[] getAssigningAuthorities() {
		int size = homeIdToAssigningAuthotityMapping.size();
		return size == 0 ? null : homeIdToAssigningAuthotityMapping.values().toArray(new String[size]);
	}

	public String getAssigningAuthority(String homeCommunityID) {
		return XDSUtil.getValue(homeCommunityID, "*", homeIdToAssigningAuthotityMapping);
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isAsyncHandler() {
		return asyncHandler;
	}

	public void setAsyncHandler(boolean asyncHandler) {
		this.asyncHandler = asyncHandler;
	}

	public String getRemotePIXManagerApplication() {
		return remotePIXManagerApplication;
	}

	public void setRemotePIXManagerApplication(String appName) {
		this.remotePIXManagerApplication = appName;
	}

	public String getLocalPIXConsumerApplication() {
		return localPIXConsumerApplication;
	}

	public void setLocalPIXConsumerApplication(String appName) {
		this.localPIXConsumerApplication = appName;
	}

	public HL7Application getPixConsumerApplication() {
		return XdsDevice.findHL7Application(localPIXConsumerApplication);
	}

	public HL7Application getPixManagerApplication() {
		return XdsDevice.findHL7Application(remotePIXManagerApplication);
	}

	@Override
	public void reconfigure(DeviceExtension from) {
		XCAInitiatingGWCfg src = (XCAInitiatingGWCfg) from;
        ReflectiveConfig.reconfigure(src, this);
	}
}
