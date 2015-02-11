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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4chee.xds2.service.impl;

import org.dcm4che3.audit.AuditMessages.EventTypeCode;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.hl7.service.HL7Service;
import org.dcm4che3.net.hl7.service.HL7ServiceRegistry;
import org.dcm4chee.xds2.common.audit.XDSAudit;
import org.dcm4chee.xds2.common.cdi.Xds;
import org.dcm4chee.xds2.common.deactivatable.Deactivateable;
import org.dcm4chee.xds2.conf.DefaultConfigurator;
import org.dcm4chee.xds2.service.ReconfigureEvent;
import org.dcm4chee.xds2.service.XdsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 *
 */
@Singleton
@Startup
//@Typed(XdsService.class)
@Xds
public class XdsServiceImpl implements XdsService {

    private static final Logger log = LoggerFactory.getLogger(XdsServiceImpl.class);


    private static final String DEVICE_NAME_PROPERTY =
            "org.dcm4chee.xds.deviceName";
    
    private static final String DO_DEFAULT_CONFIG_PROPERTY =
    "org.dcm4chee.xds.initializeDefaultConfiguration";
    
    private static final String DEF_DEVICE_NAME =
            "dcm4chee-xds";
    private static String[] JBOSS_PROPERITIES = {
        "jboss.home",
        "jboss.modules",
        "jboss.server.base",
        "jboss.server.config",
        "jboss.server.data",
        "jboss.server.deploy",
        "jboss.server.log",
        "jboss.server.temp",
    };


    @Inject
    private DicomConfiguration conf;
    
    @Inject
    private Instance<HL7Service> hl7Services;
  
    @Inject @Named("deviceNameProperty")
    private String deviceNameProperty;

    @Inject @Named("usedDeviceExtension")
    private Instance<String[]> usedDeviceExtension;

    @Inject
    Event<ReconfigureEvent> reconfigureEvent;

    @Inject
    @Xds
    @Named("xdsServiceType")
    private String xdsServiceType;

    @Inject
    DefaultConfigurator defaultConfigurator;

    private Device device;

    private boolean running;
    
    boolean hl7serviceAvail = false;

    private final HL7ServiceRegistry hl7ServiceRegistry = new HL7ServiceRegistry();

    private void addJBossDirURLSystemProperties() {
        for (String key : JBOSS_PROPERITIES) {
            String url = new File(System.getProperty(key + ".dir"))
                .toURI().toString();
            System.setProperty(key + ".url", url.substring(0, url.length()-1));
        }
    }

    private Device findDevice() throws ConfigurationException {
    	String deviceName = System.getProperty(deviceNameProperty);
    	if (deviceName == null)
    		deviceName = System.getProperty(DEVICE_NAME_PROPERTY, DEF_DEVICE_NAME);
        try {
            conf.sync();
            return conf.findDevice(deviceName);
        } catch (ConfigurationNotFoundException e) {

            // Try to initialize the default config if configured by a system property
            if (System.getProperty(DO_DEFAULT_CONFIG_PROPERTY) != null) {
                defaultConfigurator.applyDefaultConfig(deviceName);
                return conf.findDevice(deviceName);
            } else
                throw e;
        }
    }


    @PostConstruct
    public void init() {
        log.info("Initializing XDS service for {}", xdsServiceType);
        addJBossDirURLSystemProperties();
        try {
            device = findDevice();
            
            AuditLogger logger = device.getDeviceExtension(AuditLogger.class);
            AuditLogger.setDefaultLogger(logger);
            XDSAudit.setAuditLogger(logger);
            HL7DeviceExtension hl7Extension = device.getDeviceExtension(HL7DeviceExtension.class);
            if (hl7Extension != null) {

                int hl7ServicesCount = 0;

            	for (HL7Service service : hl7Services) {
            		hl7ServiceRegistry.addHL7Service(service);
            		hl7serviceAvail = true;
                    hl7ServicesCount++;
            	}

                log.info("Registering HL7 services for {} @ {} ({} in total) ...", new Object[]{xdsServiceType, device.getDeviceName(), hl7ServicesCount});

            	if (hl7serviceAvail) {
                    ExecutorService executorService = Executors.newCachedThreadPool();
            		device.setExecutor(executorService);
            		hl7Extension.setHL7MessageListener(hl7ServiceRegistry);
            	}
            }

            start();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    @Override
    public void start() throws Exception {
        log.info("Starting XDS extension {} @ {}", xdsServiceType, device.getDeviceName());
        running = true;
        if (hl7serviceAvail) {
            log.info("Starting HL7 XDS services...");
            device.bindConnections();
            log.info("HL7 XDS services started");
    	}
        logApplicationActivity(true);
    }

    @Override
    public void stop() {
        log.info("Stopping XDS extension {} @ {}", xdsServiceType, device.getDeviceName());
        running = false;
        if (hl7serviceAvail) {
            log.info("Stopping HL7 XDS services...");
            device.unbindConnections();
            log.info("HL7 XDS services stopped");
    	}
        logApplicationActivity(false);
    }

    @Override
    public void reload() throws Exception {
        log.info("Reloading XDS extension {} @ {}", xdsServiceType, device.getDeviceName());
        device.reconfigure(findDevice());
    	if (hl7serviceAvail) {
    		device.rebindConnections();
    	}
        reconfigureEvent.fire(new ReconfigureEvent());
    }

    private void logApplicationActivity(boolean started) {
        EventTypeCode event = started ? EventTypeCode.ApplicationStart : EventTypeCode.ApplicationStop;
        if (usedDeviceExtension.isUnsatisfied()) {
            XDSAudit.logApplicationActivity(device.getDeviceName() ,event, true);
        } else {
            for (String className : usedDeviceExtension.get()) {
                try {
                    Class<? extends DeviceExtension> extClass = (Class<? extends DeviceExtension>) Class.forName(className);
                    DeviceExtension ext = device.getDeviceExtension(extClass);
                    if (ext instanceof Deactivateable) {
                        if (! ((Deactivateable) ext).isDeactivated() ) {
                            XDSAudit.logApplicationActivity(getFullApplicationName(ext) ,event, true);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    log.error("DeviceExtension class not found! className:"+className, e);;
                }
            }
        }
    }
    private String getFullApplicationName(DeviceExtension ext) {
        try {
            Method m = ext.getClass().getMethod("getApplicationName", (Class<?>[])null);
            String appName = (String) m.invoke(ext, (Object[])null);
            return appName + "@" + device.getDeviceName();
        } catch (Exception e) {
            return device.getDeviceName();
        }
    }

    @Override
    @Produces
    public Device getDevice() {
        return device;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }

}
