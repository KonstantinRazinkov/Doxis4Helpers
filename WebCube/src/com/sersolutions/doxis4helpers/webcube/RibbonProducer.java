package com.sersolutions.doxis4helpers.webcube;

import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.navigation.IRibbonElement;
import com.ser.evITAWeb.api.toolbar.Button;
import com.ser.evITAWeb.api.toolbar.ICustomGroup;
import com.ser.evITAWeb.api.toolbar.Toolbar;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.bpmservice.task.TaskScripting;
import com.ser.evITAWeb.scripting.bpmservice.workbasket.WorkbasketScripting;
import com.ser.evITAWeb.scripting.hitlist.HitListScripting;
import com.ser.evITAWeb.scripting.record.RecordScripting;
import com.ser.evITAWeb.scripting.search.SearchScripting;

import java.util.List;

/**
 * Class for producing ribbon elements
 */
public class RibbonProducer {

    private Toolbar ribbon;
    private SearchScripting searchScripting;
    private RecordScripting recordScripting;
    private TaskScripting taskScripting;
    private WorkbasketScripting workBasketScripting;
    private HitListScripting hitListScripting;

    public RibbonProducer(SearchScripting searchScripting)
    {
        this.searchScripting = searchScripting;
    }

    public RibbonProducer(RecordScripting recordScripting)
    {
        this.recordScripting = recordScripting;
    }

    public RibbonProducer(TaskScripting taskScripting)
    {
        this.taskScripting = taskScripting;
    }

    public RibbonProducer(WorkbasketScripting workBasketScripting)
    {
        this.workBasketScripting = workBasketScripting;
    }

    public RibbonProducer(HitListScripting hitListScripting) { this.hitListScripting = hitListScripting;}


    /**
     * Get ribbon
     * @return ribbon
     * @throws EvitaWebException
     */
    public Toolbar getRibbon() throws EvitaWebException {
        if (searchScripting != null) {
            ribbon = searchScripting.getRibbon();
            return ribbon;
        }
        if (recordScripting != null) {
            ribbon = recordScripting.getRibbon();
            return ribbon;
        }
        if (taskScripting != null) {
            ribbon = taskScripting.getRibbon();
            return ribbon;
        }
        if (workBasketScripting != null) {
            ribbon = workBasketScripting.getRibbon();
            return ribbon;
        }
        if (hitListScripting != null){
            ribbon = getRibbon();
            return ribbon;
        }
        return  null;
    }

    /**
     * Create new button for ribbon
     * @param title Title for button
     * @param tooltip Tooltip for button
     * @param imagePath path to image of ribbon button
     * @param executeclassname Class for execution of clicking
     *                         @see com.ser.evITAWeb.scripting.toolbar.hitlist.HitlistToolbarButtonAction
     * @param redirectURL HTTP address to redirect after button was clicked (works only if executeclassname is null
     * @return
     */
    public Button getButton(String title, String tooltip, String imagePath, String executeclassname, String redirectURL) {
        Button btn = Doxis4ClassFactory.getButton();
        btn.setTitle(title);
        btn.setImagePath(imagePath); //"images/icons2015/folder_16.png"
        btn.setToolTip(tooltip);
        btn.setMessageBeforeAction(null);
        btn.setType(Button.ButtonType.TYPE32);
        if (executeclassname != null && !"".equals(executeclassname)) {
            btn.setClassToExecute(executeclassname);
        }
        if (redirectURL != null && !"".equals(redirectURL)) {
            btn.setRedirectionUrl(redirectURL);
        }
        return btn;
    }

    /**
     * Get or create button at ribbon
     * @param groupName name of the group of Ribbon
     * @param groupDescription group description
     * @param title Title for button
     * @param tooltip Tooltip for button
     * @param imagePath path to image of ribbon button
     * @param executeclassname Class for execution of clicking
     *                         @see com.ser.evITAWeb.scripting.toolbar.hitlist.HitlistToolbarButtonAction
     * @param redirectURL HTTP address to redirect after button was clicked (works only if ececuteclassname is null
     * @throws EvitaWebException
     */
    public void addButton(String groupName, String groupDescription, String title, String tooltip, String imagePath, String executeclassname, String redirectURL) throws EvitaWebException {


        Toolbar ribbon = getRibbon();
        if (ribbon == null) {
            return;
        }

        IRibbonElement ribbonElement;

        ICustomGroup group = null;

        List<ICustomGroup> groups = ribbon.getCustomGroups();
        for (int index = 0; index < groups.size(); index++) {
            if (groups.get(index).getDisplayName().contains(groupDescription)) {
                group = groups.get(index);
                break;
            }
        }
        if (group == null) {
            group = ribbon.addCustomGroup(groupName, groupDescription);
        }

        List<Button> buttonsList = group.getButtons();
        for (Button button : buttonsList) {
            if (button.getTitle().equals(title)) {
                return;
            }
        }

        group.addButton(getButton(title, tooltip, imagePath , executeclassname, redirectURL));
    }
}
