package org.dcm4chee.xds2.conf;

import org.dcm4che3.conf.api.extensions.CommonDeviceExtension;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

/**
 * @author Roman K
 */
public class XdsExtension extends CommonDeviceExtension implements Deactivateable {

    public static final String DEVICE_NAME_PROPERTY = "org.dcm4chee.xds.deviceName";
    public static final String DEF_DEVICE_NAME = "dcm4chee-xds";

    @ConfigurableProperty(name = "xdsIsDeactivated",
            label = "Deactivated",
            description = "Controls whether the service is deactivated",
            defaultValue = "false",
            group = "General"
    )
    private boolean deactivated = false;

    @ConfigurableProperty(name = "xdsApplicationName",
            label = "Application Name",
            description = "XDS Application name",
            group = "General"
    )
    private String applicationName;

    @ConfigurableProperty(name = "xdsSoapMsgLogDir",
            label = "Path for SOAP log",
            description = "Path where to store SOAP messages log. If empty, SOAP logging is disabled.",
            group = "Logging",
            required = false)
    private String soapLogDir;

    @Override
    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public final void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getSoapLogDir() {
        return soapLogDir;
    }

    public void setSoapLogDir(String soapLogDir) {
        this.soapLogDir = soapLogDir;
    }
}
