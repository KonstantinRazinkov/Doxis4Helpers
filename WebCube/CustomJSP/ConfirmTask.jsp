<%@ page language="java" pageEncoding="UTF-8" import="java.io.*, java.net.*"%>
<%@page import="com.ser.blueline.ISession"%>
<%@page import="com.ser.blueline.ISerClassFactory"%>
<%@page import="com.ser.blueline.IProperties"%>
<%@page import="com.ser.sedna.client.bluelineimpl.SEDNABluelineAdapterFactory"%>
<%@page import="com.ser.evITAWeb.api.ClientUtil"%>
<%@page import="com.ser.evITAWeb.api.IDoxisServer"%>
<%@page import="com.ser.blueline.IInformationObject"%>
<%@page import="com.ser.blueline.IDocumentServer"%>
<%@page import="com.ser.blueline.IOrgaElement"%>
<%@page import="com.ser.blueline.IUser"%>
<%@page import="com.ser.blueline.IGroup"%>
<%@page import="com.ser.blueline.IUnit"%>
<%@page import="com.ser.blueline.bpm.IBpmService"%>
<%@page import="com.ser.blueline.bpm.IDecision"%>
<%@page import="com.ser.blueline.bpm.IPossibleDecision"%>
<%@page import="com.ser.blueline.bpm.IProcessInstance"%>
<%@page import="com.ser.blueline.bpm.IReceivers"%>
<%@page import="com.ser.blueline.bpm.ITask"%>
<%@page import="com.ser.blueline.bpm.IWorkbasket"%>



<%    

	Boolean startWork = true;
	Boolean errorMessage = false;
	out.print("<h1>DOXiS4 making decisions</h1><hr />");
	
	String TaskId=request.getParameter("taskid");
	String DecisionId=request.getParameter("decisionid");
	String ResponsibleId=request.getParameter("responsibleid");
	
	
	if (TaskId == null || "".equals(TaskId)) {
		startWork = false;
		out.print("Error! No Task ID set!<br />");
	}
	if (DecisionId == null || "".equals(DecisionId)) {
		startWork = false;
		out.print("Error! No Decision ID set!<br />");
	}
	if (ResponsibleId == null || "".equals(ResponsibleId)) {
		startWork = false;
		out.print("Error! No Responsible ID set!<br />");
	}
	if (startWork == false) {
		errorMessage = true;

	}
	
	if (startWork) {
		Boolean serverConnected = false;
		ISerClassFactory _classFactory=null;
		IDocumentServer docServer=null;
		ISession session1=null;
		IBpmService bpmService=null;
		
		try {
			_classFactory = SEDNABluelineAdapterFactory.getInstance();
			IProperties properties = _classFactory.getPropertiesInstance();

			properties.setProperty("Global", "ArchivServerName", "127.0.0.1");
			properties.setProperty("Global", "SeratioServerName", "127.0.0.1");
			properties.setProperty("Global", "ArchivPort", "8080");
			properties.setProperty("Global", "SeratioPort", "8080");
			properties.setProperty("Global", "TmpDir", "/");
			//IDoxisServer doxisServer=ClientUtil.getServerObject(request);

			docServer=_classFactory.getDocumentServerInstance(properties);
			session1 = docServer.createSession( docServer.login(docServer.getSystem("Rheinwerk"), "dx4_agent_service", "dx4_agent_service".toCharArray()));		
			bpmService  = docServer.getBpmService(session1);
			serverConnected = true;
		} catch (Exception ex) {
			out.print("Error while connecting to Doxis4 server: " + ex.getMessage());
		}
		
		if (serverConnected) {
			IUser responsible=null;
			try {
				responsible = docServer.getUser(session1, ResponsibleId);
			} catch (Exception ex) {}

			if (responsible == null) {
				try {
					IGroup responsibleGroup = docServer.getGroup(session1, ResponsibleId);
					responsible = responsibleGroup.getManager();
				}
				catch (Exception ex){}
			}
			if (responsible == null) {
				try {
					IUnit responsibleUnit = docServer.getUnit(session1, ResponsibleId);
					responsible = responsibleUnit.getManager();
				}
				catch (Exception ex) {}
			}
			if (responsible == null) {
				IWorkbasket workbasket = bpmService.getWorkbasket(ResponsibleId);
				if (workbasket != null) {
					IOrgaElement orgaElement = workbasket.getAssociatedOrgaElement();
					try {
						IUser user = (IUser) orgaElement;
						responsible = user;
					}
					catch (Exception ex) {}
					if (responsible == null) {
						try {
							IGroup group = (IGroup) orgaElement;
							responsible = group.getManager();
						}
						catch (Exception ex5) {}
					}
					if (responsible == null) {
						IUnit unit = (IUnit) orgaElement;
						responsible = unit.getManager();
					}
				}

			}		
			
			if (responsible == null) {
				errorMessage = true;
				out.print("Error! Couldn't find the responsible by ID: " + ResponsibleId);
			} else {
				try {
					session1.startActingOnInstructions(responsible);
				} catch (Exception  ex) {
					errorMessage = true;
					out.print("Error! Couldn't execute task as responsible person with ID " + ResponsibleId + ", reason: " + ex.getMessage());
				}
				if (errorMessage == false {
					IInformationObject informationObject=null;
					try {informationObject = docServer.getInformationObjectByID(TaskId, session1);} catch (Exception ex){}
					if (informationObject == null) {
						errorMessage = true;
						out.print("Error! Couldn't find object in Doxis4 by ID: " + TaskId);
					} else {
						ITask task=null;
						try {task = (ITask) informationObject;} catch (Exception ex){}
						if (task == null) {
							errorMessage = true;
							out.print("Error! Object in Doxis4 with ID '" + TaskId + "' is not a Task");
						} else {
							IPossibleDecision possibleDecision=null;
							try {possibleDecision = task.findPossibleDecision(DecisionId);} catch (Exception ex){}
							if (possibleDecision == null) {
								errorMessage = true;
								out.print("Error! Couldn't find Decision for Task in Doxis4 by ID: " + DecisionId);
							} else {
								if (task.getFinishedDate() == null) {
									try {
										task.complete(possibleDecision.getDecision());
									} catch (Exception ex) {
										errorMessage = true;
										out.print("Error! Couldn't not complete task because of: " + ex.getMessage());
									}
								}
								session1.stopActingOnInstructions();

								if (errorMessage == false) {
									out.print("Task: " + task.getName() + "<br />");
									out.print("Decision: " + possibleDecision.getDecision().getName() + "<br />");
									out.print("Responsible: " + responsible.getFullName() + "<br />");
									out.print("Finished date: " + task.getFinishedDate().toString() + "<br />");
								}
							}
						}
					}
				}
			}
		}
	}
	
	if (errorMessage == true) {
		if (TaskId != null && !"".equals(TaskId)) {
			out.print("<br /> <hr />You can <a href='http://192.168.58.150:8089/webcube/?server=Presales&action=showtask&system=Rheinwerk&id=" + TaskId + "'>open the Task in Doxis4</a> for making decision or <br />");
		}
		out.print("Contact Doxis4 administrator to fix the error!");
	}
	//out.print("<script>var timer = setInterval(function() { close(); }, 1000);</script>");
%>