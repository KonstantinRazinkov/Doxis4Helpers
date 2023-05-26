package com.sersolutions.doxis4helpers.webcube;

import com.ser.blueline.*;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.components.Scripting;
import com.ser.evITAWeb.api.controls.*;
import com.ser.evITAWeb.api.controls.ICheckbox;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.IDBRecordSelector;
import com.ser.evITAWeb.api.controls.IDate;
import com.ser.evITAWeb.api.controls.IMultiValueEdit;
import com.ser.evITAWeb.api.controls.ISelectionBox;
import com.ser.evITAWeb.api.controls.ITextField;
import com.sersolutions.doxis4helpers.commons.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.Boolean;
import java.lang.Exception;
import java.lang.String;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for working with dialogs
 */

public class Dialogs {

    /**
     * Set control visibility
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param controlName Name of the control
     * @param state state of visibility
     */
    public  static void SetControlVisible(IDialog dlg, String controlName, Boolean state) {
        IControl searchControl = dlg.getFieldByName(controlName);
        if (searchControl != null) {
            searchControl.setVisible(state);
        }
    }

    /**
     * Set control enabling
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param controlName Name of the control
     * @param state state of enabling
     */
    public  static void SetControlEnabled(IDialog dlg, String controlName, Boolean state) {
        IControl searchControl = dlg.getFieldByName(controlName);
        if (searchControl != null) {
            searchControl.setEnabled(state);
        }
    }

    /**
     * Get control value as Text string
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param fieldName Name of the control
     * @return String text field
     */
    public static String GetTextField (IDialog dlg, String fieldName) {
        IControl searchControl = dlg.getFieldByName(fieldName);
        if (searchControl != null) {
            if (searchControl instanceof ITextField) {
                ITextField textField = (ITextField) searchControl;
                return textField.getText();

            }
            if (searchControl instanceof ISelectionBox) {
                ISelectionBox selectionBox = (ISelectionBox) searchControl;
                return selectionBox.getSelectedItem();
            }
        }

        return "";
    }

    /**
     * Get ILabel by name
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param labelName Name of the label
     * @return ILabel if found. If not - null
     *              @see ILabel
     */
    public static ILabel GetLabel (IDialog dlg, String labelName) {
        List<IControl> controls = dlg.getFields();
        for (IControl control : controls){
            if (control instanceof ILabel){
                ILabel label = (ILabel) control;
                if (label.getText().equalsIgnoreCase(labelName) || label.getName().equalsIgnoreCase(labelName)) return label;
            }
        }
        return null;
    }

    /**
     * Get IFrame by name
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param frameName Name of the label
     * @return IFrame if found. If not - null
     *              @see IFrame
     */
    public static IFrame GetFrame (IDialog dlg, String frameName) {
        List<IControl> controls = dlg.getFields();
        for (IControl control : controls){
            if (control instanceof IFrame){
                IFrame frame = (IFrame) control;
                if (frame.getText().equalsIgnoreCase(frameName) || frame.getName().equalsIgnoreCase(frameName)) return frame;
            }
        }
        return null;
    }

    /**
     * Get IFormattedLabel by name
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param formattedLabelName Name of the label
     * @return IFormattedLabel if found. If not - null
     *              @see IFormattedLabel
     */
    public static IFormattedLabel GetFormattedLabel (IDialog dlg, String formattedLabelName) {
        List<IControl> controls = dlg.getFields();
        for (IControl control : controls){
            if (control instanceof IFormattedLabel){
                IFormattedLabel formattedLabel = (IFormattedLabel) control;
                if (formattedLabel.getText().equalsIgnoreCase(formattedLabelName) || formattedLabel.getName().equalsIgnoreCase(formattedLabelName))
                    return formattedLabel;
            }
        }
        return null;
    }

    /**
     * Get control value as Date
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param fieldName Name of the control
     * @return Date value of date field
     */
    public static Date GetDateField(IDialog dlg, String fieldName) {
        IControl searchControl = dlg.getFieldByName(fieldName);
        if (searchControl != null && searchControl instanceof IDate) {
            IDate dateField = (IDate) searchControl;
            String formats[] = {"dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd.MM.yyyy",
                    "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd",
                    "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy",
                    "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "MM/dd/yyyy",
            };

            for (String format : formats) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat(format);
                    return formatter.parse(dateField.getText());
                }
                catch (Exception ex){}
            }
            return null;
        }
        return null;
    }

    /**
     * Set control value
     * @param dlg Doxis4 webCube dialog
     *            @see com.ser.evITAWeb.api.IDialog
     * @param controlName Name of the control
     * @param value value to be set
     */
    public  static void SetControlValue(IDialog dlg, String controlName, String value) {
        IControl searchControl = dlg.getFieldByName(controlName);
        if (searchControl != null) {
            SetControlValue(searchControl, value);
        }

    }

    /**
     * Set control value
     * @param control Control from dialog
     *                @see com.ser.evITAWeb.api.controls.IControl
     * @param value value to be set
     */
    public  static void SetControlValue(IControl control, String value) {
        if (control instanceof  ITextField) {
            ITextField textField = (ITextField) control;
            textField.setText(value);

            return;
        }
        if (control instanceof IMultiValueEdit) {
            IMultiValueEdit mve = (IMultiValueEdit) control;
            List<String> values = new ArrayList<String>();
            values.add(value);
            mve.setValues(values);
        }
        if (control instanceof IDate) {
            IDate dateField = (IDate) control;
            dateField.setText(value);
        }
        if (control instanceof ISelectionBox) {
            ISelectionBox selectionBox = (ISelectionBox) control;
            for (String items : selectionBox.getItems()) {
                if (items.equalsIgnoreCase(value)) {
                    selectionBox.setSelectedItemValue(value);
                    return;
                }
            }
            selectionBox.setSelectedItem(value);
        }
        if (control instanceof IMultiValueSelectionBox) {
            IMultiValueSelectionBox multiValueSelectionBox = (IMultiValueSelectionBox) control;
            List<String> selectedItems = new ArrayList<>();
            selectedItems.add(value);
            multiValueSelectionBox.setSelectedItems(selectedItems);
        }
        if (control instanceof ICheckbox) {
            ICheckbox checkbox = (ICheckbox) control;
            if ("1".equals(value) || "true".equals(value)) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }

        }
        if (control instanceof IDBRecordSelector) {
            IDBRecordSelector idbRecordSelector = (IDBRecordSelector) control;
            idbRecordSelector.setText(value);
            idbRecordSelector.setKeyField(value);
        }
        if (control instanceof ICategoryTree) {
            ICategoryTree iCategoryTree = (ICategoryTree) control;
            iCategoryTree.setText(value);
            iCategoryTree.setDirty(true);
        }
        if (control instanceof com.ser.evITAWeb.htmlControls.QuickSearchSelector) {
            com.ser.evITAWeb.htmlControls.QuickSearchSelector quickSearchSelector = (com.ser.evITAWeb.htmlControls.QuickSearchSelector) control;
            try {
                quickSearchSelector.setControlValue(value);
            } catch (EvitaWebException ewex){}
        }
    }

    /**
     * Set control value
     * @param dialog IDialog of form
     *                @see IDialog
     */
    public  static void ClearAllFields(IDialog dialog) {
        List<IControl> controls = dialog.getFields();
        for (IControl control : controls) {
            if (control instanceof ITextField) {
                ITextField textField = (ITextField) control;
                textField.setText("");

                return;
            }
            if (control instanceof IMultiValueEdit) {
                IMultiValueEdit mve = (IMultiValueEdit) control;
                List<String> values = new ArrayList<>();
                mve.setValues(values);
            }
            if (control instanceof IDate) {
                IDate dateField = (IDate) control;
                dateField.setText("");
            }
            if (control instanceof ISelectionBox) {
                ISelectionBox selectionBox = (ISelectionBox) control;
                selectionBox.setSelectedItem("");
            }
            if (control instanceof IMultiValueSelectionBox) {
                IMultiValueSelectionBox multiValueSelectionBox = (IMultiValueSelectionBox) control;
                List<String> selectedItems = new ArrayList<>();
                multiValueSelectionBox.setSelectedItems(selectedItems);
            }
            if (control instanceof ICheckbox) {
                ICheckbox checkbox = (ICheckbox) control;
                checkbox.setChecked(false);
            }
            if (control instanceof IDBRecordSelector) {
                IDBRecordSelector idbRecordSelector = (IDBRecordSelector) control;
                idbRecordSelector.setText("");
                idbRecordSelector.setKeyField("");
            }
            if (control instanceof ICategoryTree) {
                ICategoryTree iCategoryTree = (ICategoryTree) control;
                iCategoryTree.setText("");
                iCategoryTree.setDirty(true);
            }
            if (control instanceof com.ser.evITAWeb.htmlControls.QuickSearchSelector) {
                com.ser.evITAWeb.htmlControls.QuickSearchSelector quickSearchSelector = (com.ser.evITAWeb.htmlControls.QuickSearchSelector) control;
                try {
                    quickSearchSelector.setControlValue("");
                } catch (EvitaWebException ewex) {
                }
            }
        }
    }

    /**
     * Set control value
     * @param control Control from dialog
     *                @see com.ser.evITAWeb.api.controls.IControl
     * @param descriptor Doxis4 descriptor to be set
     *                   @see com.ser.blueline.IValueDescriptor
     */
    public  static void SetControlValue(IControl control, IValueDescriptor descriptor){

        if (control instanceof  ITextField) {
            ITextField textField = (ITextField) control;
            textField.setText(descriptor.getStringValues()[0]);

            return;
        }
        if (control instanceof IMultiValueEdit) {
            IMultiValueEdit mve = (IMultiValueEdit) control;
            List<String> values = new ArrayList<String>();
            for (String value : descriptor.getStringValues()) {
                values.add(value);
            }
            mve.setValues(values);
        }
        if (control instanceof IDate) {
            IDate dateField = (IDate) control;
            dateField.setText(descriptor.getValues()[0].getStringRepresentation());
        }
        if (control instanceof ISelectionBox) {
            ISelectionBox selectionBox = (ISelectionBox) control;
            for (String items : selectionBox.getItems()) {
                if (items.equalsIgnoreCase(descriptor.getStringValues()[0])) {
                    selectionBox.setSelectedItemValue(descriptor.getStringValues()[0]);
                    return;
                }
            }
            selectionBox.setSelectedItem(descriptor.getStringValues()[0]);
        }
        if (control instanceof IMultiValueSelectionBox) {
            IMultiValueSelectionBox multiValueSelectionBox = (IMultiValueSelectionBox) control;
            List<String> selectedItems = new ArrayList<>();

            for (String value : descriptor.getStringValues()) {
                selectedItems.add(value);
            }
            multiValueSelectionBox.setSelectedItems(selectedItems);
        }
        if (control instanceof ICheckbox) {
            ICheckbox checkbox = (ICheckbox) control;
            if ("1".equals(descriptor.getStringValues()[0]) || "true".equals(descriptor.getStringValues()[0])) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }

        }
        if (control instanceof IDBRecordSelector) {
            IDBRecordSelector idbRecordSelector = (IDBRecordSelector) control;
            idbRecordSelector.setText(descriptor.getStringValues()[0]);
            idbRecordSelector.setKeyField(descriptor.getStringValues()[0]);
        }
        if (control instanceof ICategoryTree) {
            ICategoryTree iCategoryTree = (ICategoryTree) control;
            iCategoryTree.setText(descriptor.getStringValues()[0]);
            iCategoryTree.setDirty(true);

        }
        if (control instanceof com.ser.evITAWeb.htmlControls.QuickSearchSelector) {
            com.ser.evITAWeb.htmlControls.QuickSearchSelector quickSearchSelector = (com.ser.evITAWeb.htmlControls.QuickSearchSelector) control;
            try {
                quickSearchSelector.setControlValue(descriptor.getStringValues()[0]);
            } catch (EvitaWebException ewex){}
        }
    }

    /**
     * Get control value
     * @param control Control from dialog
     *                @see com.ser.evITAWeb.api.controls.IControl
     * @return Control value as String
     */
    public  static String GetControlValue(IControl control) {

        if (control instanceof  ITextField) {
            ITextField textField = (ITextField) control;
            return textField.getText();
        }
        if (control instanceof IDate) {
            IDate dateField = (IDate) control;
            return dateField.getText();
        }
        if (control instanceof ISelectionBox) {
            ISelectionBox selectionBox = (ISelectionBox) control;
            return selectionBox.getSelectedItem();
        }
        if (control instanceof ICheckbox) {
            ICheckbox checkbox = (ICheckbox) control;
            return checkbox.getValue();

        }
        if (control instanceof IDBRecordSelector) {
            IDBRecordSelector idbRecordSelector = (IDBRecordSelector) control;
            try {
                return  idbRecordSelector.getSelectedValues()[0];
            } catch (Exception ex) {}
            return idbRecordSelector.getText();
        }
        if (control instanceof ICategoryTree) {
            ICategoryTree iCategoryTree = (ICategoryTree) control;
            try {
                return  iCategoryTree.getText();
            } catch (Exception ex) {}
            return iCategoryTree.getText();
        }
        if (control instanceof com.ser.evITAWeb.htmlControls.QuickSearchSelector) {
            com.ser.evITAWeb.htmlControls.QuickSearchSelector quickSearchSelector = (com.ser.evITAWeb.htmlControls.QuickSearchSelector) control;
            return quickSearchSelector.getControlValue();
        }
        return "";
    }

    /**
     * Get control value
     * @param dlg Dialog
     * @param controlName Control name from dialog
     *                @see com.ser.evITAWeb.api.controls.IControl
     * @return Control value as String
     */
    public  static String GetControlValue(IDialog dlg, String controlName) {
        IControl control = dlg.getFieldByName(controlName);
        return GetControlValue(control);
    }

    /**
     * Get Display value for control
     * @param control Control from dialog
     *                @see com.ser.evITAWeb.api.controls.IControl
     * @return String value
     */
    public  static String GetControlDisplayValue(IControl control) {
        if (control instanceof  ITextField) {
            ITextField textField = (ITextField) control;
            return textField.getText();
        }
        if (control instanceof IDate) {
            IDate dateField = (IDate) control;
            return dateField.getText();
        }
        if (control instanceof ISelectionBox) {
            ISelectionBox selectionBox = (ISelectionBox) control;
            return selectionBox.getDisplayTextForSelectedItem();
        }
        if (control instanceof ICheckbox) {
            ICheckbox checkbox = (ICheckbox) control;
            List<String> trueValues = new ArrayList<>() ;
            trueValues.add("1"); trueValues.add("true");
            return (trueValues.contains( checkbox.getValue()))? "True" : "False";
        }
        if (control instanceof com.ser.evITAWeb.htmlControls.QuickSearchSelector) {
            com.ser.evITAWeb.htmlControls.QuickSearchSelector quickSearchSelector = (com.ser.evITAWeb.htmlControls.QuickSearchSelector) control;
            return quickSearchSelector.getControlValue();
        }
        return "";
    }

    /**
     * Checks dialog for empty mandatory fields
     * @param dialog webCube Dialog with controls
     *            @see com.ser.evITAWeb.api.IDialog
     * @return boolean value
     * @throws EvitaWebException if something goes wrong
     */
    public static boolean HasNotFilledMandatoryControls(IDialog dialog) throws EvitaWebException {
        boolean hasEmptyMandatoryField = false;
        for (IControl control : dialog.getFields()){
            if (control.isMandatory()){
                if (control instanceof  ITextField) {
                    ITextField textField = (ITextField) control;
                    if (StringUtils.isBlank(textField.getText())) hasEmptyMandatoryField = true;
                }
                if (control instanceof IMultiValueEdit) {
                    IMultiValueEdit mve = (IMultiValueEdit) control;
                    List<String> values = mve.getValues();
                    if (values.size() == 0) hasEmptyMandatoryField = true;
                }
                if (control instanceof IDate) {
                    IDate dateField = (IDate) control;
                    if (StringUtils.isBlank(dateField.getText())) hasEmptyMandatoryField = true;
                }
                if (control instanceof ISelectionBox) {
                    ISelectionBox selectionBox = (ISelectionBox) control;
                    if (StringUtils.isBlank(selectionBox.getSelectedItem())) hasEmptyMandatoryField = true;
                }
                if (control instanceof IMultiValueSelectionBox) {
                    IMultiValueSelectionBox multiValueSelectionBox = (IMultiValueSelectionBox) control;
                    List<String> values = multiValueSelectionBox.getSelectedItems();
                    if (values.size() == 0) hasEmptyMandatoryField = true;
                }
                if (control instanceof ICheckbox) {
                    ICheckbox checkbox = (ICheckbox) control;
                    if (StringUtils.isBlank(checkbox.getValue())) hasEmptyMandatoryField = true;
                }
                if (control instanceof IDBRecordSelector) {
                    IDBRecordSelector idbRecordSelector = (IDBRecordSelector) control;
                    try{
                        if (idbRecordSelector.getSelectedValues().length == 0) hasEmptyMandatoryField = true;
                    } catch (Exception ex) {}
                }
                if (control instanceof ICategoryTree) {
                    ICategoryTree iCategoryTree = (ICategoryTree) control;
                    if (iCategoryTree.getSelectedValues().length == 0) hasEmptyMandatoryField = true;
                }
                if (control instanceof com.ser.evITAWeb.htmlControls.QuickSearchSelector) {
                    com.ser.evITAWeb.htmlControls.QuickSearchSelector quickSearchSelector = (com.ser.evITAWeb.htmlControls.QuickSearchSelector) control;
                    if (StringUtils.isBlank(quickSearchSelector.getControlValue())) hasEmptyMandatoryField = true;
                }
            }
        }
        return hasEmptyMandatoryField;
    }

    /**
     * Copy all descriptors from information object to dialog
     * @param scripting webCube scripting object
     *                  @see com.ser.evITAWeb.api.components.Scripting
     * @param source Information object with descriptors
     *               @see com.ser.blueline.IInformationObject
     * @param dlg webCube Dialog with controls
     *            @see com.ser.evITAWeb.api.IDialog
     * @param lockFields Lock (disable) fields after successful copy
     *
     */
    public static void CopyAllDescriptorsFromDocumentToDialog(Scripting scripting, IInformationObject source, IDialog dlg, Boolean lockFields) {
        for (IControl control : dlg.getFields()) {
            if (control == null) continue;
            if (control.getDescriptorId() == null) continue;
            for (IValueDescriptor descriptor : source.getDescriptorList()) {
                if (descriptor == null) continue;
                if (descriptor.getId() == null) continue;
                if (control.getDescriptorId().equals(descriptor.getId())) {
                    if (control instanceof IDate){
                        try {
                            String finalDate = "";
                            String initialDate = Descriptors.GetDescriptorValue(source.getSession(), source, descriptor.getName());
                            if (StringUtils.isNotBlank(initialDate)) {
                                Date date = Descriptors.GetValueAsDate(initialDate, "yyyyMMdd");
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                                finalDate = simpleDateFormat.format(date);
                                SetControlValue(control, finalDate);
                            }
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else SetControlValue(control, descriptor);
                    if (lockFields) control.setEnabled(false);
                }
            }
        }
    }


    /**
     * Copy all descriptors from dialog to information object
     * @param scripting webCube scripting object
     *                  @see com.ser.evITAWeb.api.components.Scripting
     * @param dlg webCube Dialog with controls
     *            @see com.ser.evITAWeb.api.IDialog
     * @param dest Information object with descriptors
     *               @see com.ser.blueline.IInformationObject
     * @param lockFields Lock (disable) fields after successful copy
     * @throws Exception if something goes wrong
     */
    public static void CopyAllDescriptorsFromDialogToDocument(Scripting scripting, IDialog dlg, IInformationObject dest, Boolean lockFields) throws Exception {
        for (IControl control : dlg.getFields()) {
            if (control == null) continue;
            if (control.getDescriptorId() == null) continue;
            Descriptors.SetDescriptorToInformationObject (scripting.getDoxisServer().getSession(), dest, control.getDescriptorId(), GetControlValue(control));

        }
    }
    public static IInformationObject[] FindInformationObjects(ISession session, String archiveClassID, String queryClassID, List<String> fields, IDialog dialog) {
        try {
            List<IValueDescriptor> descriptorsFields = new ArrayList<>();
            IDescriptor descriptorDef;
            IValueDescriptor descriptorValue;

            for (String field : fields) {
                String value = Dialogs.GetTextField(dialog, field);
                descriptorDef =  Descriptors.GetDescriptorDefinition(session, dialog.getFieldByName(field).getDescriptorId());
                if (descriptorDef != null) {
                    ISerClassFactory factory = session.getDocumentServer().getClassFactory();
                    IValueDescriptor valueDescriptor = factory.getValueDescriptorInstance(descriptorDef);
                    if (valueDescriptor != null) {
                        valueDescriptor.setValue(value);
                        descriptorsFields.add(valueDescriptor);
                    }
                }
            }

            return InformationObjects.FindInformationObjects(session, archiveClassID, queryClassID, descriptorsFields);
        } catch (Exception ex) {
            return null;
        }

    }
}
