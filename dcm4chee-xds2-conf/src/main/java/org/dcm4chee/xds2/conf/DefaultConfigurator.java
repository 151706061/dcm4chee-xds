package org.dcm4chee.xds2.conf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurator {

    private static final Logger log = LoggerFactory.getLogger(DefaultConfigurator.class);

    private static final String[] affinityDomain = { "1.2.3.4.5" };
    private static final String[] MIME_TYPES2 = new String[] { "application/xml", "application/dicom", "application/pdf", "text/plain",
            "text/xml" };
    
    public static void applyDefaultConfig(DicomConfiguration config, String deviceName) {

        log.info("Initializing the default configuration for device {}", deviceName);

        try {

            // should work with agility
            String ip = System.getProperty("jboss.bind.address", "localhost");
            

            Device device = new Device(deviceName);
            Issuer issuer = new Issuer("XDS_A", "1.2.40.0.13.1.1.999.111.1111", "ISO");
            Code institutionCode = new Code("111.1111", "99DCM4CHEE", null, "Site A");
            device.setIssuerOfPatientID(issuer);
            device.setIssuerOfAccessionNumber(issuer);
            if (institutionCode != null) {
                device.setInstitutionNames(institutionCode.getCodeMeaning());
                device.setInstitutionCodes(institutionCode);
            }

            // registry
            XdsRegistry registry = new XdsRegistry();
            device.addDeviceExtension(registry);
            registry.setApplicationName("XDSREGISTRY");
            registry.setAffinityDomain(affinityDomain);
            registry.setAffinityDomainConfigDir("${jboss.server.config.dir}/xds/affinitydomain");
            registry.setAcceptedMimeTypes(MIME_TYPES2);
            registry.setSoapLogDir("../standalone/log/xds");
            registry.setCreateMissingPIDs(true);
            registry.setCreateMissingCodes(true);
            registry.setCheckAffinityDomain(true);
            registry.setCheckMimetype(true);
            registry.setPreMetadataCheck(false);
            registry.setQueryUrl("http://"+ip+":8080/xds/registry");
            registry.setRegisterUrl("http://"+ip+":8080/xds/registry");
            registry.setCheckAffinityDomain(false);
            registry.setCheckMimetype(false);

            // generic source

            XdsSource source = new XdsSource();
            device.addDeviceExtension(source);
            source.setUid("0");
            source.setRegistry(device);
            source.setRepository(device);

            // repository
            XdsRepository rep = new XdsRepository();
            device.addDeviceExtension(rep);
            rep.setApplicationName("XDS-REPOSITORY");
            rep.setRepositoryUID("9.9.9.9");
            rep.setRetrieveUrl("http://"+ip+":8080/xds/repository");
            rep.setProvideUrl("http://"+ip+":8080/xds/repository");
            rep.setAcceptedMimeTypes(MIME_TYPES2);
            rep.setSoapLogDir("../standalone/log/xds");
            rep.setCheckMimetype(false);
            rep.setAllowedCipherHostname("*");
            rep.setLogFullMessageHosts(new String[] {});

            // storage
            StorageConfiguration store = new StorageConfiguration();
            device.addDeviceExtension(store);
            store.setApplicationName("XDS-REPOSITORY-STORAGE");
            FilesystemGroup grp = new FilesystemGroup("XDS_ONLINE", "1GiB");
            Filesystem fs1 = new Filesystem("xds_fs_1", "file:///storage/xds/online/fs1", 10, Availability.ONLINE);
            Filesystem fs2 = new Filesystem("xds_fs_2", "file:///storage/xds/online/fs2", 10, Availability.ONLINE);
            fs1.setNextFilesystem(fs2);
            fs2.setNextFilesystem(fs1);
            grp.addFilesystem(fs1);
            grp.addFilesystem(fs2);
            
            // used elsewhere as well
            Map<String, Device> deviceBySrcUid = new HashMap<String, Device>();
            deviceBySrcUid.put("*", device);
            rep.setSrcDevicebySrcIdMap(deviceBySrcUid);

            try {
                if (config.findDevice(deviceName) != null)
                    config.merge(device);
            } catch (ConfigurationNotFoundException nfe) {
                config.persist(device);
            } catch (ConfigurationException ce) {
                throw ce;
            }

            config.close();

        } catch (ConfigurationException e) {
            log.error("Could not auto-initialize default XDS configuration", e);
        }

    }
}


