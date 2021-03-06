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

package org.dcm4chee.xds2.repository.ws;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;

import org.dcm4che3.net.Device;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.common.XDSUtil;
import org.dcm4chee.xds2.common.audit.AuditRequestInfo;
import org.dcm4chee.xds2.common.audit.XDSAudit;
import org.dcm4chee.xds2.common.deactivatable.DeactivateableByConfiguration;
import org.dcm4chee.xds2.common.exception.XDSException;
import org.dcm4chee.xds2.conf.XdsRepository;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType.Document;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetRequestType.DocumentRequest;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4chee.xds2.infoset.rim.ExternalIdentifierType;
import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;
import org.dcm4chee.xds2.infoset.rim.IdentifiableType;
import org.dcm4chee.xds2.infoset.rim.ObjectFactory;
import org.dcm4chee.xds2.infoset.rim.RegistryError;
import org.dcm4chee.xds2.infoset.rim.RegistryErrorList;
import org.dcm4chee.xds2.infoset.rim.RegistryPackageType;
import org.dcm4chee.xds2.infoset.rim.RegistryResponseType;
import org.dcm4chee.xds2.infoset.rim.SlotType1;
import org.dcm4chee.xds2.infoset.rim.SubmitObjectsRequest;
import org.dcm4chee.xds2.infoset.util.DocumentRegistryPortTypeFactory;
import org.dcm4chee.xds2.infoset.util.InfosetUtil;
import org.dcm4chee.xds2.infoset.ws.registry.DocumentRegistryPortType;
import org.dcm4chee.xds2.infoset.ws.repository.DocumentRepositoryPortType;
import org.dcm4chee.xds2.storage.XDSDocument;
import org.dcm4chee.xds2.storage.XDSStorage;
import org.dcm4chee.xds2.ws.handler.LogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MTOM
@BindingType(value = SOAPBinding.SOAP12HTTP_MTOM_BINDING)
@Stateless
@WebService(endpointInterface="org.dcm4chee.xds2.infoset.ws.repository.DocumentRepositoryPortType", 
    name="b",
    serviceName="XDSbRepository",
    portName="DocumentRepository_Port_Soap12",
    targetNamespace="urn:ihe:iti:xds-b:2007",
    wsdlLocation = "/META-INF/wsdl/XDS.b_DocumentRepository.wsdl"
        )

@Addressing(enabled=true, required=true)
@HandlerChain(file="handlers.xml")
@DeactivateableByConfiguration(extension = XdsRepository.class)
@PermitAll
public class XDSRepositoryBean implements DocumentRepositoryPortType {

    private ObjectFactory factory = new ObjectFactory();
    private org.dcm4chee.xds2.infoset.ihe.ObjectFactory iheFactory = new org.dcm4chee.xds2.infoset.ihe.ObjectFactory();

    private static Logger log = LoggerFactory.getLogger(XDSRepositoryBean.class);

    private XDSStorage storage;

    @Resource
    private WebServiceContext wsContext;
    @Resource
    private EJBContext ejbContext;

    @Inject
    private void setXDSStorage(XDSStorage stg) {
        storage = stg;
    }

    @Inject
    private Device device;
    private XdsRepository cfg;

    @PostConstruct
    private void init() {
        cfg = device.getDeviceExtension(XdsRepository.class);
    }

    @Override
    public RegistryResponseType documentRepositoryProvideAndRegisterDocumentSetB(
            ProvideAndRegisterDocumentSetRequestType req) {
        log.info("################ documentRepositoryProvideAndRegisterDocumentSetB called!");
        RegistryResponseType rsp;
        XDSDocument[] storedDocs = null;
        URL registryURL = null;
        String[] submUIDAndpatid = getSubmissionUIDandPatID(req.getSubmitObjectsRequest());
        try {
            if (submUIDAndpatid[1] == null)
                throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Missing patientID!", null);
            String srcID = getSourceID(req);
            registryURL = getRegistryWsdlUrl(srcID);
            String groupID = getFsGroupID(submUIDAndpatid[1]);
            logRequest(req);
            List<ExtrinsicObjectType> extrObjs = checkRequest(req);
            storedDocs = storeDocuments(req, extrObjs, groupID);
            SubmitObjectsRequest submitRequest = req.getSubmitObjectsRequest();
            rsp = dispatchSubmitObjectsRequest(submitRequest, registryURL);
        } catch (Exception x) {
            rsp = factory.createRegistryResponseType();
            if (x instanceof XDSException) {
                XDSUtil.addError(rsp, (XDSException) x);
            } else {
                XDSUtil.addError(rsp, new XDSException(XDSException.XDS_ERR_REPOSITORY_ERROR, 
                        "Unexpected error in XDS service !: "+x.getMessage(),x));
            }
        }
        boolean success = XDSConstants.XDS_B_STATUS_SUCCESS.equals(rsp.getStatus());
        AuditRequestInfo info = new AuditRequestInfo(LogHandler.getInboundSOAPHeader(), wsContext);
        if (storedDocs != null) {
            XDSAudit.logRepositoryPnRExport(submUIDAndpatid[0], submUIDAndpatid[1], info, registryURL, success);
        }
        commit(storedDocs, success);
        if (!success)
            ejbContext.setRollbackOnly();
        XDSAudit.logRepositoryImport(submUIDAndpatid[0], submUIDAndpatid[1], info, 
                XDSConstants.XDS_B_STATUS_SUCCESS.equals(rsp.getStatus()));
        log.info("################ documentRepositoryProvideAndRegisterDocumentSetB finished!");
        return rsp;
    }

    private String getSourceID(ProvideAndRegisterDocumentSetRequestType req) throws XDSException {
        List<JAXBElement<? extends IdentifiableType>> objs = req.getSubmitObjectsRequest().getRegistryObjectList().getIdentifiable();
        IdentifiableType obj;
        for (int i = 0, len = objs.size() ; i < len ; i++) {
            obj = objs.get(i).getValue();
            if (obj instanceof RegistryPackageType) {
                return InfosetUtil.getExternalIdentifierValue(XDSConstants.UUID_XDSSubmissionSet_sourceId, (RegistryPackageType)obj);
            }
        }
        throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Missing XDSSubmissionSet.sourceId!", null);
    }

    private void commit(XDSDocument[] storedDocs, boolean success) {
        if (storedDocs != null) {
            try {
                storage.commit(storedDocs, success);
            } catch (Exception e) {
                log.warn("Failed to commit stored documents!");
            }
        }
    }

    @Override
    public RetrieveDocumentSetResponseType documentRepositoryRetrieveDocumentSet(RetrieveDocumentSetRequestType req) {
        log.info("################ documentRepositoryRetrieveDocumentSet called!");
        RetrieveDocumentSetResponseType rsp = iheFactory.createRetrieveDocumentSetResponseType();
        try {
            String repositoryUID = getRepositoryUniqueId();
            String docUid, reqRepoUid;
            XDSDocument doc;
            RetrieveDocumentSetResponseType.DocumentResponse docRsp;
            List<String> retrievedUIDs = new ArrayList<String>();
            int requestCount = req.getDocumentRequest().size();
            RegistryErrorList regErrors = factory.createRegistryErrorList();
            List<RegistryError> mainErrors = regErrors.getRegistryError();
            for ( DocumentRequest docReq : req.getDocumentRequest() ) {
                reqRepoUid = docReq.getRepositoryUniqueId();
                docUid = docReq.getDocumentUniqueId();
                if (reqRepoUid == null || docUid == null || reqRepoUid.trim().length() == 0 || docUid.trim().length() == 0) {
                    mainErrors.add(XDSUtil.getRegistryError(XDSException.XDS_ERR_SEVERITY_ERROR, XDSException.XDS_ERR_REPOSITORY_ERROR, 
                            "Missing required request parameter! (Repository- or Document Unique ID)", null));
                    continue;
                }
                if ( reqRepoUid.equals(repositoryUID)) {
                    doc = retrieveDocument(docUid);
                    if ( doc != null ) {
                        try {
                            docRsp = getDocumentResponse(doc, getRepositoryUniqueId());
                            rsp.getDocumentResponse().add(docRsp);
                            retrievedUIDs.add(docUid);
                        } catch (IOException e) {
                            String msg = "Error in building DocumentResponse for document:"+doc;
                            log.error(msg);
                            mainErrors.add(XDSUtil.getRegistryError(XDSException.XDS_ERR_SEVERITY_ERROR, 
                                    XDSException.XDS_ERR_REPOSITORY_ERROR, msg, docUid));
                        }
                    } else {
                        String msg = "Document not found! document UID:"+docUid;
                        log.warn(msg);
                        mainErrors.add(XDSUtil.getRegistryError(XDSException.XDS_ERR_SEVERITY_ERROR, 
                                XDSException.XDS_ERR_DOCUMENT_UNIQUE_ID_ERROR, msg, docUid));
                    }
                } else {
                    String msg = "DocumentRepositoryUID="+reqRepoUid+" is unknown! This repository unique ID:"+repositoryUID;
                    log.warn(msg);
                    mainErrors.add(XDSUtil.getRegistryError(XDSException.XDS_ERR_SEVERITY_ERROR, 
                            XDSException.XDS_ERR_UNKNOWN_REPOSITORY_ID, msg, docUid));
                }
            }
            RegistryResponseType regRsp = factory.createRegistryResponseType();

            int nrOfDocs = rsp.getDocumentResponse().size();
            if (nrOfDocs == 0) {
                if (mainErrors.size() == 0)
                    throw new XDSException(XDSException.XDS_ERR_DOCUMENT_UNIQUE_ID_ERROR, 
                        "None of the requested documents were found. This repository unique ID " + repositoryUID, null);
                regRsp.setStatus(XDSConstants.XDS_B_STATUS_FAILURE);
            } else if (nrOfDocs < requestCount) {
                regRsp.setStatus(XDSConstants.XDS_B_STATUS_PARTIAL_SUCCESS);
            } else {
                regRsp.setStatus(XDSConstants.XDS_B_STATUS_SUCCESS);
            }

            if (mainErrors.size() > 0) {
                regRsp.setRegistryErrorList(regErrors);
            }
            rsp.setRegistryResponse(regRsp);

        } catch (Exception x) {
            if (x instanceof XDSException) {
                XDSUtil.addError(rsp, (XDSException) x);
            } else {
                XDSUtil.addError(rsp, new XDSException(XDSException.XDS_ERR_REPOSITORY_ERROR, 
                        "Unexpected error in XDS service !: "+x.getMessage(),x));
            }
        }
        AuditRequestInfo info = new AuditRequestInfo(LogHandler.getInboundSOAPHeader(), wsContext);
        XDSAudit.logRepositoryRetrieveExport(req, rsp, info);
        log.info("################ documentRepositoryRetrieveDocumentSet finished!");
        return rsp;
    }

    private XDSDocument[] storeDocuments(ProvideAndRegisterDocumentSetRequestType req, List<ExtrinsicObjectType> extrObjs, String groupID) throws XDSException {
        Map<String, Document> docs = InfosetUtil.getDocuments(req);
        Document doc;
        ExtrinsicObjectType eo;
        String docUID;
        XDSDocument[] storedDocs = new XDSDocument[extrObjs.size()];
        String[] mimetypes = cfg.isCheckMimetype() ? cfg.getAcceptedMimeTypes() : null;
        for (int i = 0, len = extrObjs.size() ; i < len ; i++ ) {
            eo = extrObjs.get(i);
            if (eo.getId() == null || eo.getId().trim().length() == 0)
                throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Missing XDSDocumentEntry.entryUUID!", null);
            doc = docs.get(eo.getId());
            docUID = InfosetUtil.getExternalIdentifierValue(XDSConstants.UUID_XDSDocumentEntry_uniqueId, eo);
            if (docUID == null)
                throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Missing XDSDocumentEntry.uniqueId!", null);
            if (mimetypes != null) {
                boolean unsupportedMimetype = true;
                for (int j = 0 ; j < mimetypes.length ; j++) {
                    if (mimetypes[j].equals(eo.getMimeType())) {
                        unsupportedMimetype = false;
                        break;
                    }
                }
                if (unsupportedMimetype)
                    throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Mimetype not supported:"+eo.getMimeType(), null);
            }
            try {
                storedDocs[i] = storage.storeDocument(groupID, docUID, doc.getValue(), eo.getMimeType());
                if (storedDocs[i].isCommitted()) {
                    log.warn("Document already exists! docUid:"+docUID);
                } 
                Map<String, SlotType1> slots = InfosetUtil.addOrOverwriteSlot(eo, XDSConstants.SLOT_NAME_REPOSITORY_UNIQUE_ID, getRepositoryUniqueId());
                String oldValue = InfosetUtil.addOrCheckedOverwriteSlot(eo, slots, XDSConstants.SLOT_NAME_SIZE, String.valueOf(storedDocs[i].getSize()), false);
                if (oldValue != null) {
                    throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Slot 'size' already exists but has different value! old:"+oldValue+" new:"+storedDocs[i].getSize(), null);
                }
                oldValue = InfosetUtil.addOrCheckedOverwriteSlot(eo, slots, XDSConstants.SLOT_NAME_HASH, storedDocs[i].getDigest(), true);
                if (oldValue != null) {
                    throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Slot 'hash' already exists but has different value! old:"+oldValue+" new:"+storedDocs[i].getDigest(), null);
                }
            } catch (XDSException x) {
                this.commit(storedDocs, false);
                throw x;
            } catch (Exception x) {
                log.error("Storage of document failed! docUID:"+docUID, x);
                this.commit(storedDocs, false);
                throw new XDSException(XDSException.XDS_ERR_REPOSITORY_ERROR, 
                        "Storage of document "+docUID+" failed! : "+x.getMessage(),x);
            }
        }
        return storedDocs;
    }

    private XDSDocument retrieveDocument(String docUID) throws XDSException {
        try {
            XDSDocument xdsDoc = storage.retrieveDocument(docUID);
            return xdsDoc;
        } catch (Exception x) {
            throw new XDSException(XDSException.XDS_ERR_REPOSITORY_ERROR, 
                    "Retrieve of document "+docUID+" failed! : "+x.getMessage(),x);
        }
    }

    private String getRepositoryUniqueId() {
        return cfg.getRepositoryUID();
    }

    private URL getRegistryWsdlUrl(String sourceID) throws MalformedURLException {
        String url = cfg.getRegistryURL(sourceID);
        return new URL(url);
    }
    private String getFsGroupID(String pid) throws MalformedURLException {
        int pos;
        if ( (pos = pid.indexOf('^')) == -1 || 
                (pos = pid.indexOf('^', pos+1)) == -1 || 
                (pos = pid.indexOf('^', pos+1)) == -1) {
            throw new IllegalArgumentException("Missing Authority in patID! pid:"+pid);
        }            
        String authority = pid.substring(++pos);
        pos = authority.indexOf('&');pos++;
        int pos1 = authority.indexOf('&', pos);
        String affinity = authority.substring(pos, pos1);
        log.debug("Get filesystemGroupID for affinity {}", affinity);
        return cfg.getFilesystemGroupID(affinity);
    }

    private RegistryResponseType dispatchSubmitObjectsRequest(SubmitObjectsRequest submitRequest, URL xdsRegistryURI) throws MalformedURLException,
    JAXBException, XDSException {

        DocumentRegistryPortType port = DocumentRegistryPortTypeFactory.getDocumentRegistryPortSoap12(xdsRegistryURI.toString());
        log.info("####################################################");
        log.info("####################################################");
        log.info("XDS.b: Send register document-b request to registry:"+xdsRegistryURI);
        log.info("####################################################");
        log.info("####################################################");
        log.info("org.jboss.security.ignoreHttpsHost:"+System.getProperty("org.jboss.security.ignoreHttpsHost"));
        RegistryResponseType rsp = null;
        try {
            rsp = port.documentRegistryRegisterDocumentSetB(submitRequest);
        } catch ( Exception x) {
            throw new XDSException( XDSException.XDS_ERR_REG_NOT_AVAIL, "Document Registry not available: "+xdsRegistryURI, x);
        }
        return rsp;
    }

    private List<ExtrinsicObjectType> checkRequest(ProvideAndRegisterDocumentSetRequestType req) throws XDSException {
        SubmitObjectsRequest sor = req.getSubmitObjectsRequest();
        RegistryPackageType submissionSet = InfosetUtil.getRegistryPackage(sor, XDSConstants.UUID_XDSSubmissionSet);
        if ( submissionSet == null ) {
            log.error("No RegistryPackage id=SubmissionSet found!");
            throw new XDSException( XDSException.XDS_ERR_REPOSITORY_ERROR, 
                    XDSException.XDS_ERR_MISSING_REGISTRY_PACKAGE, null);
        } else if (submissionSet.getId() == null || submissionSet.getId().trim().length() == 0) {
            throw new XDSException(XDSException.XDS_ERR_REPOSITORY_METADATA_ERROR, "Missing XDSSubmissionSet.entryUUID!", null);
        }
        List<ExtrinsicObjectType> extrObjs = InfosetUtil.getExtrinsicObjects(req.getSubmitObjectsRequest());
        checkPatientIDs(req, submissionSet, extrObjs);
        Map<String, Document> docs = InfosetUtil.getDocuments(req);
        if ( extrObjs.size() > docs.size() ) {
            log.warn("Missing Documents! Found more ExtrinsicObjects("+extrObjs.size()+") than Documents("+docs.size()+")!");
            throw new XDSException(XDSException.XDS_ERR_MISSING_DOCUMENT,
                    "", null);
        } else if ( extrObjs.size() < docs.size() ) {
            log.warn("Missing Document Metadata! Found less ExtrinsicObjects("+extrObjs.size()+") than Documents("+docs.size()+")!");
            throw new XDSException(XDSException.XDS_ERR_MISSING_DOCUMENT_METADATA,
                    "", null);

        }
        return extrObjs;
    }

    private String checkPatientIDs(ProvideAndRegisterDocumentSetRequestType req, RegistryPackageType submissionSet, List<ExtrinsicObjectType> extrObjs) throws XDSException {
        String submissionPatId = InfosetUtil.getExternalIdentifierValue(XDSConstants.UUID_XDSSubmissionSet_patientId, submissionSet);
        String docPatId;
        ExtrinsicObjectType eo;
        for ( int i = 0, len = extrObjs.size() ; i < len ; i++ ) {
            eo = extrObjs.get(i);
            docPatId = InfosetUtil.getExternalIdentifierValue(XDSConstants.UUID_XDSDocumentEntry_patientId, eo);
            if ( docPatId != null && !docPatId.equals(submissionPatId)) {
                String msg = "XDSDocumentEntry.patientId ("+docPatId+")and XDSSubmissionSet.patientId ("+submissionPatId+") doesn't match! ExtrinsicObject.Id:"+eo.getId();
                log.warn(msg);
                throw new XDSException(XDSException.XDS_ERR_PATID_DOESNOT_MATCH,
                        msg, null);
            }
        }
        RegistryPackageType folder = InfosetUtil.getRegistryPackage(req.getSubmitObjectsRequest(), XDSConstants.UUID_XDSFolder);
        String folderPatId = folder == null ? null : InfosetUtil.getExternalIdentifierValue(XDSConstants.UUID_XDSFolder_patientId, folder);
        if ( folderPatId != null && !folderPatId.equals(submissionPatId)) {
            String msg = "XDSFolder.patientId ("+folderPatId+")and XDSSubmissionSet.patientId ("+submissionPatId+") doesn't match!";
            log.warn(msg);
            throw new XDSException(XDSException.XDS_ERR_PATID_DOESNOT_MATCH,
                    msg, null);
        }
        return submissionPatId;
    }

    private DocumentResponse getDocumentResponse(XDSDocument doc, String repositoryUniqueId) throws IOException {
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        docRsp = iheFactory.createRetrieveDocumentSetResponseTypeDocumentResponse();
        docRsp.setDocumentUniqueId(doc.getUID());
        docRsp.setMimeType(doc.getMimeType());
        docRsp.setRepositoryUniqueId(repositoryUniqueId);
        docRsp.setDocument(doc.getContent());
        return docRsp;
    }

    private void logRequest(ProvideAndRegisterDocumentSetRequestType req) {
        log.info("###### SubmitObjectRequest:"+req.getSubmitObjectsRequest());
        List<Document> docs = req.getDocument();
        log.info("###### Documents:"+docs);
        if (docs != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("######Number of Documents:").append(docs.size());
            int dumpValLen;
            for (Document d : docs) {
                sb.append("\nDocument ID:"+d.getId())
                .append("       size:").append(d.getValue().length)
                .append("       value:");
                try {
                    dumpValLen = Math.min(d.getValue().length, 40);
                    sb.append(new String(d.getValue(), 0, dumpValLen));
                    if (dumpValLen == 40)
                        sb.append("...");
                } catch (Exception x) {
                    log.warn("Failed to convert value in String!", x);
                }
            }
            log.info(sb.toString());
        }
    }

    private String[] getSubmissionUIDandPatID(SubmitObjectsRequest req) {
        String[] result = new String[2];
        List<JAXBElement<? extends IdentifiableType>> objs = req.getRegistryObjectList().getIdentifiable();
        IdentifiableType obj;
        whole: for (int i=0,len=objs.size() ; i < len ; i++) {
            obj = objs.get(i).getValue();
            if (obj instanceof RegistryPackageType) {
                List<ExternalIdentifierType> list = ((RegistryPackageType)obj).getExternalIdentifier();
                if (list != null) {
                    for (ExternalIdentifierType eiType : list) {
                        if (XDSConstants.UUID_XDSSubmissionSet_patientId.equals(eiType.getIdentificationScheme())) {
                            result[1] = eiType.getValue();
                        } else if (XDSConstants.UUID_XDSSubmissionSet_uniqueId.equals(eiType.getIdentificationScheme())) {
                            result[0] = eiType.getValue();
                        } else {
                            continue;
                        }
                        if (result[0] != null && result[1] != null)
                            break whole;
                    }
                }

            }
        }
        return result;
    }

}
