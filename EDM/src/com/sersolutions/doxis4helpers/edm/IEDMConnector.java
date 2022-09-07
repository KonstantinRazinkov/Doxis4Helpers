package com.sersolutions.doxis4helpers.edm;

import com.ser.blueline.IInformationObject;
import com.ser.blueline.ISession;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.ITask;

import java.util.List;

public interface IEDMConnector {


    Boolean Connect() throws Exception;
    String SendDocument(ISession session, String documentID, String orgID, String message, Boolean isDraft) throws Exception;
    void  ProcessMessagesFromEDM(ISession session) throws Exception;


}
