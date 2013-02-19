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

package org.dcm4chee.xds2.src.tool.pnrsnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.audit.AuditLogger;
import org.dcm4che.net.audit.AuditRecordRepository;
import org.dcm4che.util.StringUtils;
import org.dcm4che.util.UIDUtils;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.common.audit.XDSAudit;
import org.dcm4chee.xds2.infoset.rim.RegistryError;
import org.dcm4chee.xds2.infoset.rim.RegistryErrorList;
import org.dcm4chee.xds2.infoset.rim.RegistryResponseType;
import org.dcm4chee.xds2.src.metadata.Author;
import org.dcm4chee.xds2.src.metadata.Code;
import org.dcm4chee.xds2.src.metadata.DocumentEntry;
import org.dcm4chee.xds2.src.metadata.PnRRequest;
import org.dcm4chee.xds2.src.metadata.Util;
import org.dcm4chee.xds2.src.metadata.exception.MetadataConfigurationException;
import org.dcm4chee.xds2.src.ws.XDSSourceWSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 *
 */
public class PnRSnd {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4chee.xds2.src.tool.pnrsnd.messages");
    private static final Logger log = LoggerFactory.getLogger(PnRSnd.class);
    
    private XDSSourceWSClient client = new XDSSourceWSClient();
    
    Device device;
    
    private static Properties props = new Properties();
    static {
        try {
            props.load(new FileInputStream(new File("../conf/pnrsnd.properties")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public PnRSnd() {
        initAuditLogger();
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        opts.addOption("h", "help", false, rb.getString("help"));
        opts.addOption("V", "version", false, rb.getString("version"));
        addURLOption(opts);
        addPropertyOption(opts);
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(opts, args);
        if (cl.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    rb.getString("usage"),
                    rb.getString("description"), opts,
                    rb.getString("example"));
            System.exit(0);
        }
        if (cl.hasOption("V")) {
            Package p = PnRSnd.class.getPackage();
            String s = p.getName();
            System.out.println(s.substring(s.lastIndexOf('.')+1) + ": " +
                   p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption("u")) {
            props.setProperty("URL", cl.getOptionValue("u"));
        }
        if (cl.hasOption("p")) {
            String[] codes = cl.getOptionValues("p");
            int pos;
            for (int i = 0 ; i < codes.length ; i++) {
                pos = codes[i].indexOf('=');
                props.setProperty(codes[i].substring(0, pos), codes[i].substring(++pos));
            }
        }
        return cl;
    }

    @SuppressWarnings("static-access")
    private static void addURLOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("url")
                .withDescription("Repository URL")
                .withLongOpt("url")
                .create("u"));
    }
    @SuppressWarnings("static-access")
    private static void addPropertyOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("prop")
                .withDescription("<property name>=<property value>")
                .withLongOpt("property")
                .create("p"));
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            PnRSnd main = new PnRSnd();
            PnRRequest pnrReq = main.createPnR(cl);
            main.send(pnrReq);
        } catch (ParseException e) {
            System.err.println("pnrsnd: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("pnrsnd: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private PnRRequest createPnR(CommandLine cl) throws IOException, java.text.ParseException {
        String submUID = props.getProperty("subm.UID");
        if ("new()".equals(submUID)) {
            submUID = UIDUtils.createUID();
            log.info("### submissionUID created:"+submUID);
        }
        PnRRequest pnrReq = new PnRRequest(props.getProperty("sourceID"), submUID, props.getProperty("patID"), props.getProperty("srcPatID"));
        String docUID = props.getProperty("doc.UID");
        if ("new()".equals(docUID)) {
            docUID = UIDUtils.createUID();
            log.info("### documentUID created:"+docUID);
        }
        @SuppressWarnings("unchecked")
        List<String> args = cl.getArgList();
        File docFile = new File(args.get(0));
        byte[] buffer = new byte[(int)docFile.length()];
        FileInputStream fis = new FileInputStream(docFile);
        fis.read(buffer);
        DocumentEntry doc = pnrReq.addDocumentEntry(docUID, buffer, props.getProperty("mime"));
        addAuthor(doc.addAuthor());
        doc.addConfidentialityCode(new Code(props.getProperty("confidentialityCode")));
        doc.addEventCode(new Code(props.getProperty("eventCode")));
        doc.setClassCode(new Code(props.getProperty("classCode")));
        doc.setFormatCode(new Code(props.getProperty("formatCode")));
        doc.setHealthcareFacilityTypeCode(new Code(props.getProperty("healthcareFacilityTypeCode")));
        doc.setPracticeSettingCode(new Code(props.getProperty("practiceSettingCode")));
        doc.setTypeCode(new Code(props.getProperty("typeCode")));
        doc.setCreationTime(Util.toDate(props.getProperty("creationTime")));
        doc.setServiceStartTime(Util.toDate(props.getProperty("serviceStartTime")));
        doc.setServiceStopTime(Util.toDate(props.getProperty("serviceStopTime")));
        doc.setLanguageCode("de-DE");
        
        addAuthor(pnrReq.addAuthor());
        pnrReq.setContentTypeCode(new Code(props.getProperty("contentTypeCode")));
        pnrReq.setSubmissionTime(Util.toDate(props.getProperty("submissionTime")));
        return pnrReq;
    }
    
    private void addAuthor(Author author) {
        author.setAuthorPerson(props.getProperty("authorPerson"));
        author.setAuthorInstitutions(getAuthorAttributeValues("authorInstitutions"));
        author.setAuthorRoles(getAuthorAttributeValues("authorRoles"));
        author.setAuthorSpecialities(getAuthorAttributeValues("authorSpecialities"));
        author.setAuthorTelecommunications(getAuthorAttributeValues("authorTelecommunications"));
    }
    
    private List<String> getAuthorAttributeValues(String propName) {
        String value = props.getProperty(propName);
        return value == null ? null : Arrays.asList(StringUtils.split(value, '|'));
    }

    private void send(PnRRequest pnrReq) throws MalformedURLException, MetadataConfigurationException {
        configTLS();
        URL xdsRepositoryURL = new URL(props.getProperty("URL"));
        RegistryResponseType rsp = client.sendProvideAndRegister(pnrReq, xdsRepositoryURL);
        InetAddress addr = AuditLogger.localHost();
        String localhost = addr == null ? "localhost" : addr.getHostName();//DNS!
        XDSAudit.logSourceExport(pnrReq.getSubmissionSetUID(), pnrReq.getPatientID(), 
                XDSConstants.WS_ADDRESSING_ANONYMOUS, AuditLogger.processID(), localhost, 
                xdsRepositoryURL.toExternalForm(), xdsRepositoryURL.getHost(), 
                XDSConstants.XDS_B_STATUS_SUCCESS.equals(rsp.getStatus()));
        log.info("Response:"+rsp.getStatus());
        RegistryErrorList errors = rsp.getRegistryErrorList();
        if (errors != null) {
            List<RegistryError> errList = errors.getRegistryError();
            RegistryError err;
            for (int i = 0, len = errList.size() ; i < len ;) {
                err = errList.get(i);
                log.info("Error "+(++i)+":");
                log.info("  ErrorCode  :"+err.getErrorCode());
                log.info("  CodeContext:"+err.getCodeContext());
                log.info("  Severity   :"+err.getSeverity());
                log.info("  Value      :"+err.getValue());
                log.info("  Location   :"+err.getLocation());
            }
        }
    }

    private void initAuditLogger() {
        device = new Device("test");
        Device arrDevice = createARRDevice("arr", Connection.Protocol.SYSLOG_UDP, 
                props.getProperty("audit.host"), Integer.parseInt(props.getProperty("audit.port")));
        AuditLogger logger = new AuditLogger();
        device.addDeviceExtension(logger);
        Connection auditUDP = new Connection("audit-udp", "localhost");
        auditUDP.setProtocol(Connection.Protocol.SYSLOG_UDP);
        device.addConnection(auditUDP);
        logger.addConnection(auditUDP);
        logger.setAuditSourceTypeCodes("4");
        logger.setAuditRecordRepositoryDevice(arrDevice);
        XDSAudit.setAuditLogger(logger);
    }
    private Device createARRDevice(String name, Connection.Protocol protocol, String host, int port) {
        log.info("####ARR:"+host+":"+port);
        Device arrDevice = new Device(name);
        AuditRecordRepository arr = new AuditRecordRepository();
        arrDevice.addDeviceExtension(arr);
        Connection auditUDP = new Connection("audit-udp", host, port);
        auditUDP.setProtocol(protocol);
        arrDevice.addConnection(auditUDP);
        arr.addConnection(auditUDP);
        return arrDevice ;
    }

    private void configTLS() {
        final HostnameVerifier origHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        final String allowedUrlHost = props.getProperty("allowedUrlHost");
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if ( !origHostnameVerifier.verify ( urlHostName, session)) {
                    if ( isAllowedUrlHost(urlHostName)) {
                        log.warn("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
                    } else {
                        return false;
                    }
                }
                return true;
            }

            private boolean isAllowedUrlHost(String urlHostName) {
                if (allowedUrlHost == null || "CERT".equals(allowedUrlHost)) return false;
                if ( allowedUrlHost.equals("*")) return true;
                return allowedUrlHost.equals(urlHostName);
            }

        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);


    }
}
