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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.hl7.HL7ApplicationCache;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.api.hl7.IHL7ApplicationCache;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.dicom.DicomConfigurationManager;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4chee.storage.conf.StorageConfiguration;
import org.dcm4chee.xds2.common.cdi.Xds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import java.util.Collection;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */

@ApplicationScoped
public class XdsConfigurationFactory {

    public static final Logger log = LoggerFactory.getLogger(XdsConfigurationFactory.class);


    /**
     * Allows for custom config storage backend
     */

    @Inject
    Instance<Configuration> dbConfigStorage;

    @Produces
    @ApplicationScoped
    public DicomConfigurationManager getDicomConfigurationManager() throws ConfigurationException {
        return getCommonDicomConfigurationWithHL7();
    }

    public CommonDicomConfigurationWithHL7 getCommonDicomConfigurationWithHL7() throws ConfigurationException {
        DicomConfigurationBuilder builder = DicomConfigurationBuilder
                .newConfigurationBuilder(System.getProperties());

        // register custom config storage backend if provided
        if (dbConfigStorage != null &&
                !dbConfigStorage.isUnsatisfied() &&
                !dbConfigStorage.isAmbiguous()) {
            Configuration storage = dbConfigStorage.get();
            log.info("Using custom configuration storage {}", storage.getClass());
            builder.registerCustomConfigurationStorage(storage);
        }

        builder.registerDeviceExtension(XdsRegistry.class);
        builder.registerDeviceExtension(XdsRepository.class);
        builder.registerDeviceExtension(XCARespondingGWCfg.class);
        builder.registerDeviceExtension(XCAiRespondingGWCfg.class);
        builder.registerDeviceExtension(XCAiInitiatingGWCfg.class);
        builder.registerDeviceExtension(XCAInitiatingGWCfg.class);
        builder.registerDeviceExtension(XdsSource.class);
        builder.registerDeviceExtension(StorageConfiguration.class);
        builder.registerDeviceExtension(HL7DeviceExtension.class);
        builder.registerDeviceExtension(AuditRecordRepository.class);
        builder.registerDeviceExtension(AuditLogger.class);

        return  builder.build();
    }


    public void disposeDicomConfiguration(@Disposes DicomConfiguration conf) {
        conf.close();
    }

    @Produces
    @ApplicationScoped
    public IHL7ApplicationCache getHL7ApplicationCache(DicomConfiguration conf) {
        return new HL7ApplicationCache(conf.getDicomConfigurationExtension(HL7Configuration.class));
    }

}
