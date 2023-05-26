package com.sersolutions.doxis4helpers.commons;

import com.ser.blueline.*;
import com.ser.internal.foldermanagerimpl.sedna.Folder;
import com.ser.sedna.client.bluelineimpl.bpm.ProcessInstance;
import com.ser.sedna.client.bluelineimpl.bpm.Task;
import com.ser.sedna.client.bluelineimpl.document.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class with static functions for working with descriptors
 */
public class Descriptors {

    /**
     * Get SQL-specified string for making some SQL queries
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param source Information object with descriptor
     *               @see IInformationObject
     * @param descriptorName Name or ID of the descriptor
     * @param SQLColumnName Name of column name from DB (could be not same as Descriptor name)
     * @return String with SQL-query
     * @throws Exception if there will be some problems with getting descriptor from Information object
     */
    public static String GetSQLForStringDescriptor(ISession doxis4Session, IInformationObject source, String descriptorName, String SQLColumnName) throws Exception {
        String value  = GetDescriptorValue(doxis4Session, source, descriptorName);
        if (value != null && !"".equals(value)) {
            return String.format(" AND %s = '%s'", SQLColumnName ,value);
        }
        return  "";
    }

    /**
     * Universal method to copy all descriptors from one information object to another
     * @param doxis4Session Doxis4 Session Object
     *                      @see ISession
     * @param source Information object with descriptors that must be copied to another information object
     *               @see IInformationObject
     * @param dest Information object that must take descriptors from first one
     *             @see IInformationObject
     * @param replace Boolean parameter that sets up needs of replace descriptors, that already filled to dest
     * @throws Exception if there will be some problems of getting or setting descriptors
     */
    public static void CopyAllDescriptors(ISession doxis4Session, IInformationObject source, IInformationObject dest, boolean replace) throws Exception {
        IValueDescriptor[] destDescriptors = dest.getDescriptorList();
        List<String> destAviableDescriptors = new ArrayList<String>();

        if (dest instanceof Folder) {
            destAviableDescriptors = Arrays.asList(((Folder) dest).getObjectClass().getAssignedDescriptorIDs());
        } else if (dest instanceof Document) {
            destAviableDescriptors = Arrays.asList(((Document) dest).getArchiveClass().getAssignedDescriptorIDs());
        } else if (dest instanceof Task) {
            destAviableDescriptors = Arrays.asList(((Task) dest).getProcessInstance().getProcessType().getAssignedDescriptorIDs());
        } else if (dest instanceof ProcessInstance) {
            destAviableDescriptors = Arrays.asList(((ProcessInstance) dest).getProcessType().getAssignedDescriptorIDs());
        }

        boolean transfered;
        for (IValueDescriptor sourceDescriptor : source.getDescriptorList()) {
            try {
                transfered = false;
                if (sourceDescriptor == null) continue;
                if (sourceDescriptor.getId() == null) continue;

                if (destAviableDescriptors.contains(sourceDescriptor.getId())) {
                    for (IValueDescriptor destDescriptor : destDescriptors) {
                        if (destDescriptor == null) continue;
                        if (destDescriptor.getId() == null) continue;
                        if (destDescriptor.getId().equals(sourceDescriptor.getId())) {
                            transfered = true;
                            if (replace) {
                                if (sourceDescriptor.getStringValues().length <= 1) destDescriptor.setValue(sourceDescriptor.getStringValues()[0]);
                                else{
                                    if (sourceDescriptor.getStringValues().length == 0) destDescriptor.setValue("");
                                    else destDescriptor.setValues(sourceDescriptor.getStringValues());
                                }
                            }
                            break;
                        }
                    }
                    if (!transfered) {
                        if (sourceDescriptor.getStringValues().length <= 1) {
                            AddDescriptorToInformationObject(doxis4Session, dest, sourceDescriptor.getName(), sourceDescriptor.getStringValues()[0]);
                        } else {
                            for (String add : sourceDescriptor.getStringValues()) {
                                AddStringValueToMultivalueDescriptor(doxis4Session, dest, sourceDescriptor.getName(), add);
                            }
                        }
                    }
                }

            } catch (Exception ex) {

            }

        }
    }

    /**
     * Universal method to copy all descriptors from one information object to another
     * @param doxis4Session Doxis4 Session Object
     *                      @see ISession
     * @param source Information object with descriptors that must be copied to another information object
     *               @see IInformationObject
     * @param dest Information object that must take descriptors from first one
     *             @see IInformationObject
     * @param replace Boolean parameter that sets up needs of replace descriptors, that already filled to dest
     * @param addIfExistsMultivalue boolean parameter that allows adding same values several times in multivalue descriptors
     * @throws Exception if there will be some problems of getting or setting descriptors
     */
    public static void CopyAllDescriptors(ISession doxis4Session, IInformationObject source, IInformationObject dest, boolean replace, boolean addIfExistsMultivalue) throws Exception {
        IValueDescriptor[] destDescriptors = dest.getDescriptorList();
        List<String> destAviableDescriptors = new ArrayList<String>();

        if (dest instanceof Folder) {
            destAviableDescriptors = Arrays.asList(((Folder) dest).getObjectClass().getAssignedDescriptorIDs());
        } else if (dest instanceof Document) {
            destAviableDescriptors = Arrays.asList(((Document) dest).getArchiveClass().getAssignedDescriptorIDs());
        } else if (dest instanceof Task) {
            destAviableDescriptors = Arrays.asList(((Task) dest).getProcessInstance().getProcessType().getAssignedDescriptorIDs());
        } else if (dest instanceof ProcessInstance) {
            destAviableDescriptors = Arrays.asList(((ProcessInstance) dest).getProcessType().getAssignedDescriptorIDs());
        }

        boolean transfered;
        for (IValueDescriptor sourceDescriptor : source.getDescriptorList()) {
            try {
                transfered = false;
                if (sourceDescriptor == null) continue;
                if (sourceDescriptor.getId() == null) continue;

                if (destAviableDescriptors.contains(sourceDescriptor.getId())) {
                    for (IValueDescriptor destDescriptor : destDescriptors) {
                        if (destDescriptor == null) continue;
                        if (destDescriptor.getId() == null) continue;
                        if (destDescriptor.getId().equals(sourceDescriptor.getId())) {
                            transfered = true;
                            if (replace) {
                                if (sourceDescriptor.getStringValues().length <= 1) destDescriptor.setValue(sourceDescriptor.getStringValues()[0]);
                                else{
                                    if (sourceDescriptor.getStringValues().length == 0) destDescriptor.setValue("");
                                    else destDescriptor.setValues(sourceDescriptor.getStringValues());
                                }
                            }
                            break;
                        }
                    }
                    if (!transfered) {
                        if (sourceDescriptor.getStringValues().length <= 1) {
                            AddDescriptorToInformationObject(doxis4Session, dest, sourceDescriptor.getName(), sourceDescriptor.getStringValues()[0]);
                        } else {
                            for (String add : sourceDescriptor.getStringValues()) {
                                if (addIfExistsMultivalue) AddStringValueToMultivalueDescriptor(doxis4Session, dest, sourceDescriptor.getName(), add, true);
                                if (!addIfExistsMultivalue) AddStringValueToMultivalueDescriptor(doxis4Session, dest, sourceDescriptor.getName(), add);
                            }
                        }
                    }
                }

            } catch (Exception ex) {

            }

        }
    }

    /**
     * Gets all descriptors from information object to Map
     * @param source Information object with descriptors that must be taken
     *               @see IInformationObject
     * @return Map of descriptors where key will contain FQN and ID of descriptor and value - string value of descriptor (only one)
     * @throws Exception if there will be some problems with getting descriptors from information object
     */
    public static Map<String, String> GetAllDescriptors(IInformationObject source) throws Exception {
        Map<String,String> result = new ConcurrentHashMap<>();
        for (IValueDescriptor sourceDescriptor : source.getDescriptorList()) {
            try {
                if (sourceDescriptor == null) continue;
                if (sourceDescriptor.getId() == null) continue;
                result.put (String.format("%s %s", sourceDescriptor.getFullyQualifiedName(), sourceDescriptor.getId()), sourceDescriptor.getStringValues()[0]);
            } catch (Exception ex) {

            }
        }
        return result;
    }

    /**
     * Gets descriptor definition by its name, FQN or ID
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param descriptorName Name, FQN or ID of descriptor
     * @return Doxis4 Descriptor object
     * @see IDescriptor
     * @throws Exception if there will be some problems with getting descriptor from Doxis4
     */
    public static IDescriptor GetDescriptorDefinition(ISession doxis4Session, String descriptorName) throws Exception {
        IDescriptor descriptorDef = null;
        IDescriptor[] descriptors = doxis4Session.getDocumentServer().getDescriptorByName(descriptorName, doxis4Session);
        if (descriptors != null) {
            if (descriptors.length > 0) {
                descriptorDef = descriptors[0];
            }
        }
        if (descriptorDef == null) {
            descriptorDef = doxis4Session.getDocumentServer().getDescriptor(descriptorName, doxis4Session);
        }

        if (descriptorDef == null) {
            descriptorDef = doxis4Session.getDocumentServer().getInternalDescriptor(doxis4Session, descriptorName);
        }
        return  descriptorDef;
    }

    /**
     * Gets single string value of descriptor
     * @param doxis4Session Doxis4 session object
     *                      @see ISession
     * @param informationObject Information object with descriptor that must be taken
     *                          @see IInformationObject
     * @param descriptorName Descriptor name, FQN or ID
     * @return String value of descriptor (only first value if there is a multivalue descriptor). For getting multiple string see GetDescriptorValues.
     * Will return empty string if there will not be any value in descriptor
     */
    public static String GetDescriptorValue(ISession doxis4Session, IInformationObject informationObject, String descriptorName) {
        try {
            IDescriptor descriptorDef = GetDescriptorDefinition(doxis4Session, descriptorName);
            if (descriptorDef != null) {
                IValueDescriptor descriptor = informationObject.getDescriptor(descriptorDef);
                if (descriptor == null) {
                    return  "";
                } else {
                    return descriptor.getStringValues()[0];
                }
            }
        }
        catch ( Exception ex)
        {

        }
        return  "";
    }

    /**
     * Universal converter from String to Date
     * @param value String value that must contain date in unknown format
     * @return Recognized date. If date is not recognized - null will be returned
     */
    public static Date GetValueAsDate(String value) {
        //20181109184459000
        String formats[] = {"dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd.MM.yyyy",
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd",
                "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy",
                "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "MM/dd/yyyy",
                "yyyyMMddHHmmssSSS", "yyyyMMddHHmmss","yyyyMMddHHmm", "yyyyMMdd",
        };

        for (String format : formats) {
            try {
                return GetValueAsDate(value, format);
            } catch (Exception ex){}
        }
        return null;
    }

    /**
     * Convert string to Date with special format
     * @param value String value that must contain date with special format
     * @param format Format of date in String
     * @return Recognized date
     * @throws ParseException if String could not be parsed by provided format
     */
    public static Date GetValueAsDate(String value, String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(value);
    }


    /**
     * Gets multiple values from multivalue descriptor from Information object
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information object with descriptor that must be taken
     *                          @see IInformationObject
     * @param descriptorName Name, FQN or ID of descriptor that must be taken
     * @return Array of string with values from multivalue descriptor. Returns null if there will not be any values
     * @throws Exception if there will be some problems with getting descriptor from information object
     */
    public static String[] GetDescriptorValues(ISession doxis4Session, IInformationObject informationObject, String descriptorName) throws  Exception {
        IDescriptor descriptorDef = GetDescriptorDefinition(doxis4Session, descriptorName);
        if (descriptorDef != null) {
            IValueDescriptor descriptor = informationObject.getDescriptor(descriptorDef);
            if (descriptor == null) {
                return  null;
            } else {
                return descriptor.getStringValues();
            }
        }

        return  null;
    }

    /**
     * Sets value to descriptor of information object
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information object that will be filled with descriptor value
     *                          @see IInformationObject
     * @param descriptorName Name, FQN or ID of descriptor
     * @param value Value of descriptor
     * @throws Exception if there will be some problems with setting value to descriptor of Information object
     */
    public static void SetDescriptorToInformationObject(ISession doxis4Session, IInformationObject informationObject, String descriptorName, String value) throws Exception {
        SetDescriptorToInformationObject(doxis4Session, informationObject, descriptorName, value, false);
    }

    /**
     *
     * Sets value to descriptor of information object
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information object that will be filled with descriptor value
     *                          @see IInformationObject
     * @param descriptorName Name, FQN or ID of descriptor
     * @param value Value of descriptor
     * @param multiField if you need to add value to multivalue descriptor - this param must be set to true
     * @throws Exception if there will be some problems with setting value to descriptor of Information object
     */
    public static void SetDescriptorToInformationObject(ISession doxis4Session, IInformationObject informationObject, String descriptorName, String value, Boolean multiField) throws Exception {
        try {
            IDescriptor[] descriptors = doxis4Session.getDocumentServer().getDescriptorByName(descriptorName, doxis4Session);
            if (descriptors != null) {
                IDescriptor descriptorDef = descriptors[0];
                IValueDescriptor descriptor = informationObject.getDescriptor(descriptorDef);
                if (descriptor == null) {
                    AddDescriptorToInformationObject(doxis4Session, informationObject, descriptorName, value);
                } else {
                    if (multiField) {
                        descriptor.addValue(value);
                    } else {
                        descriptor.setValue(value);
                    }
                }
            }
        }
        catch ( Exception ex)
        {

        }
    }

    /**
     * Add new descriptor to information object (only if this descriptor was not filled yet before).
     * This method is also used by SetDescriptorToInformationObject if it sees that descriptor was not added to information object, so it's better to use that method instead of this.
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information Object that must have new descriptor
     *                          @see IInformationObject
     * @param descriptorName Name, FQN or ID of descriptor
     * @param value Value that must be added to information object
     * @throws Exception if there will be some error with adding descriptor to information object. If descriptor was added before - there will be Exception.
     */
    public static void AddDescriptorToInformationObject(ISession doxis4Session, IInformationObject informationObject, String descriptorName, String value) throws Exception {
        IDescriptor[] descriptors = doxis4Session.getDocumentServer().getDescriptorByName(descriptorName, doxis4Session);
        if (descriptors != null) {
            IDescriptor descriptorDef = descriptors[0];
            ISerClassFactory factory =doxis4Session.getDocumentServer().getClassFactory();
            IValueDescriptor valueDescriptor = factory.getValueDescriptorInstance(descriptorDef);
            if (valueDescriptor != null) {
                valueDescriptor.setValue(value);
                informationObject.addDescriptor(valueDescriptor);
            }
        }
    }

    /**
     * Add new descriptor to information object (only if this descriptor was not filled yet before).
     * This method is also used by SetDescriptorToInformationObject if it sees that descriptor was not added to information object, so it's better to use that method instead of this.
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information Object that must have new descriptor
     *                          @see IInformationObject
     * @param descriptorName Name, FQN or ID of descriptor
     * @param newValue Value that must be added to information object
     * @throws Exception if there will be some error with adding descriptor to information object. If descriptor was added before - there will be Exception.
     */
    public static void AddStringValueToMultivalueDescriptor(ISession doxis4Session, IInformationObject informationObject, String descriptorName, String newValue) throws Exception {
        if (newValue == null) return;;
        if (descriptorName == null) return;
        String[] partiesExists = GetDescriptorValues(doxis4Session, informationObject, descriptorName);
        Boolean isExist = false;
        if (partiesExists != null) {
            for (String exist : partiesExists) {
                if (exist.toUpperCase().equals(newValue.toUpperCase())) {
                    isExist = true;
                    break;
                }
            }
        }
        if (!isExist) {
            SetDescriptorToInformationObject(doxis4Session, informationObject, descriptorName, newValue, true);
        }
    }

    /**
     * Add new descriptor to information object (only if this descriptor was not filled yet before).
     * This method is also used by SetDescriptorToInformationObject if it sees that descriptor was not added to information object, so it's better to use that method instead of this.
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information Object that must have new descriptor
     *                          @see IInformationObject
     * @param descriptorName Name, FQN or ID of descriptor
     * @param newValue Value that must be added to information object
     * @param addIfExists boolean value which determines whether to add same value to descriptor or not
     * @throws Exception if there will be some error with adding descriptor to information object. If descriptor was added before - there will be Exception.
     */
    public static void AddStringValueToMultivalueDescriptor(ISession doxis4Session, IInformationObject informationObject, String descriptorName, String newValue, boolean addIfExists) throws Exception {
        if (newValue == null) return;;
        if (descriptorName == null) return;
        String[] partiesExists = GetDescriptorValues(doxis4Session, informationObject, descriptorName);
        Boolean isExist = false;
        if (partiesExists != null) {
            for (String exist : partiesExists) {
                if (exist.toUpperCase().equals(newValue.toUpperCase())) {
                    isExist = true;
                    break;
                }
            }
        }
        if (addIfExists && isExist) isExist = false;
        if (!isExist) {
            SetDescriptorToInformationObject(doxis4Session, informationObject, descriptorName, newValue, true);
        }
    }

    /**
     * Adds some value to descriptor value of information object
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param informationObject Information Object that must have new value of descriptor
     * @param descriptorName Name, FQN or ID of descriptor
     * @param newValue Value that must be added to descriptor value
     * @param splitter Separator between old value and added part
     * @throws Exception if there will be some problems with getting or setting values of descriptor
     */
    public static void ConcatStringValueToDescriptor(ISession doxis4Session, IInformationObject informationObject, String descriptorName, String newValue, String splitter) throws Exception {
        String oldValue = GetDescriptorValue(doxis4Session, informationObject, descriptorName);
        if (oldValue.contains(newValue)) return;
        String addedValue = String.format("%s%s%s", GetDescriptorValue(doxis4Session, informationObject, descriptorName), splitter, newValue);
        IDescriptor descriptorDef = GetDescriptorDefinition(doxis4Session, descriptorName);
        if (addedValue.length() > descriptorDef.getLength() - 4) {
            addedValue = addedValue.substring(0, descriptorDef.getLength() - 4) + "...";
        }
        SetDescriptorToInformationObject(doxis4Session, informationObject, descriptorName, addedValue);
    }

    /*
    public static Map<String, String> GetDescriptorsListForSystem(ISession doxis4Session, String sourceSystemName, String destSystemName, int direction) throws Exception
    {
        HashMap<String, String> result = new HashMap<String, String>();

        int column;
        int row;
        int columnDestSystem=0;
        int columnSourceSystem=0;
        String key;
        String val;
        IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix("ESBDescriptors", doxis4Session);

        for (column=0; column<doxis4Matrix.getColumnCount(); column++)
        {
            if (doxis4Matrix.getColumnName(column).toUpperCase().equals(destSystemName.toUpperCase()))
            {
                columnDestSystem = column;
            }
            if (doxis4Matrix.getColumnName(column).toUpperCase().equals(sourceSystemName.toUpperCase()))
            {
                columnSourceSystem = column;
            }
        }
        for (row =0 ;row<doxis4Matrix.getRowCount(); row++)
        {
            try
            {
                key = doxis4Matrix.getValue(row, columnDestSystem);
                val = doxis4Matrix.getValue(row, columnSourceSystem);
                if (key != null && !key.equals("") && val != null && !val.equals(""))
                {
                    if (direction == 0)
                    {
                        result.put(key, val);
                    }
                    if (direction == 1)
                    {
                        result.put(val, key);
                    }

                }
            }
            catch (Exception ex)
            {}
        }


        return  result;
    }*/

}
