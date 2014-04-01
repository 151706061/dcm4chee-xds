package org.dcm4chee.xds2.infoset.util;

import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

public class BasePortTypeFactory {

    protected final static String URN_IHE_ITI = "urn:ihe:iti:xds-b:2007";
    protected static final String URN_IHE_RAD_XDSI_B_2009 = "urn:ihe:rad:xdsi-b:2009";
    protected static final String URN_IHE_RAD_2009 = "urn:ihe:rad:2009";

    @SuppressWarnings("rawtypes")
    public static void configurePort(BindingProvider bindingProvider, String endpointAddress, boolean mtom, boolean mustUnderstand, boolean addLogHandler) {
        SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding(); 
        binding.setMTOMEnabled(mtom);
        if (mustUnderstand || addLogHandler) {
            List<Handler> currentHandlers = binding.getHandlerChain();
            if (mustUnderstand)
                currentHandlers.add(new EnsureMustUnderstandHandler());
            if (addLogHandler)
                currentHandlers.add(new SentSOAPLogHandler());
        }
        Map<String, Object> reqCtx = bindingProvider.getRequestContext();
        reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }	
}
