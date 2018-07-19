/*
 * Copyright (C) 2010-2014 Hamburg Sud and the contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aludratest.service.gui.integrationtest;

import org.aludratest.service.gui.component.Button;
import org.aludratest.service.gui.component.Checkbox;
import org.aludratest.service.gui.component.Dropdownbox;
import org.aludratest.service.gui.component.FileField;
import org.aludratest.service.gui.component.InputField;
import org.aludratest.service.gui.component.Label;
import org.aludratest.service.gui.component.Link;
import org.aludratest.service.gui.component.RadioButton;
import org.aludratest.service.gui.web.AludraWebGUI;
import org.aludratest.service.gui.web.uimap.UIMap;
import org.aludratest.service.locator.element.GUIElementLocator;
import org.aludratest.service.locator.element.IdLocator;
import org.aludratest.service.locator.element.XPathLocator;
import org.aludratest.service.locator.window.TitleLocator;

/**
 * UI map for the GUI componentes of this package.
 * @author YWANG
 */
@SuppressWarnings("javadoc")
public class GUITestUIMap extends UIMap {

    public static final TitleLocator TEST_PAGE_TITLE = new TitleLocator("Testwebsite");
    public static final TitleLocator LINKED_PAGE_TITLE = new TitleLocator("Beschreibung der Seite");

    public static final GUIElementLocator DROPDOWNBOX_ID = new IdLocator("DropDown:after");
    public static final GUIElementLocator DISABLED_DROPDOWNBOX_ID = new IdLocator("DisabledDropDown:after");

    public static final GUIElementLocator TEXT_FIELD_ID = new IdLocator("TextField:after");

    // contains() because highlight changes CSS class of component
    public static final GUIElementLocator NOID_TEXT_FIELD = new XPathLocator("//input[contains(@class, 'myclass')]");

    public static final GUIElementLocator DISABLED_TEXT_FIELD_ID = new IdLocator("disabled_TextField:after");
    public static final GUIElementLocator READONLY_TEXT_FIELD_ID = new IdLocator("readonly_TextField");

    public static final GUIElementLocator RADIO_BUTTON_AND = new IdLocator("RadioAnd:after");
    public static final GUIElementLocator RADIO_BUTTON_OR = new IdLocator("RadioOR:after");

    public static final GUIElementLocator FIRST_CHECKBOX_ID = new IdLocator("FirstCheckBox:after");
    public static final GUIElementLocator SECOND_CHECKBOX_ID = new IdLocator("SecondCheckBox:after");
    public static final GUIElementLocator DISABLED_CHECKBOX_ID = new IdLocator("DisabledCheckBox:after");

    public static final GUIElementLocator TEST_LINK_ID = new IdLocator("LinktoTThis:after");

    public static final GUIElementLocator FIND_BUTTON_ID = new IdLocator("FindButton:after");
    public static final GUIElementLocator DISABLED_BUTTON_ID = new IdLocator("DisabledButton:after");
    public static final GUIElementLocator HIDDEN_BUTTON_ID = new IdLocator("InvisibleButton:after");
    public static final GUIElementLocator HIDDEN_DIV_BUTTON_ID = new IdLocator("InvisibleDIVButton:after");

    public static final GUIElementLocator IMAGE_ID = new IdLocator("PictureLogo:after");
    public static final GUIElementLocator IMAGE_BUTTON_ID = new IdLocator("gF:msgimg");

    public static final GUIElementLocator LABEL_ID = new IdLocator("LabelTest:after");

    public static final GUIElementLocator FILE_FIELD_ID = new IdLocator("FileField:after");
    public static final GUIElementLocator SUBMIT_FILE_ID = new IdLocator("SubmitFile");
    public static final GUIElementLocator FILE_NAME_ID = new IdLocator("FileName");
    public static final GUIElementLocator FILE_CONTENT_ID = new IdLocator("FileContent");

    public static final GUIElementLocator SLOW_CLOSE_ID = new XPathLocator("//a[@id='slow_close']");

    // There is no element defined on test web page with this ID
    public static final GUIElementLocator NOT_EXISTING_BUTTON_ID = new IdLocator("test:test:test");

    public static final GUIElementLocator HOVER_TEXT_ID = new IdLocator("hover_text");
    public static final GUIElementLocator HOVER_VALUE_ID = new IdLocator("hover_hideshow");

    public static final GUIElementLocator TEXTAREA_ID = new IdLocator("test_textarea");
    public static final GUIElementLocator TEXTAREA_READONLY_ID = new IdLocator("test_textarea_readonly");

    public GUITestUIMap(AludraWebGUI aludraGUI) {
        super(aludraGUI);
    }

    public Dropdownbox dropDownBox() {
        return aludraGUI.getComponentFactory().createDropdownbox(DROPDOWNBOX_ID);
    }

    public Dropdownbox disabledDropDownBox() {
        return aludraGUI.getComponentFactory().createDropdownbox(DISABLED_DROPDOWNBOX_ID);
    }

    public InputField textField() {
        return aludraGUI.getComponentFactory().createInputField(TEXT_FIELD_ID);
    }

    public InputField noidTextField() {
        return aludraGUI.getComponentFactory().createInputField(NOID_TEXT_FIELD);
    }

    public InputField disabledTextField() {
        return aludraGUI.getComponentFactory().createInputField(DISABLED_TEXT_FIELD_ID);
    }

    public InputField readonlyTextField() {
        return aludraGUI.getComponentFactory().createInputField(READONLY_TEXT_FIELD_ID);
    }

    public Button findButton() {
        return aludraGUI.getComponentFactory().createButton(FIND_BUTTON_ID);
    }

    public Button findButtonWithTimeout() {
        return aludraGUI.getComponentFactory().createButton(FIND_BUTTON_ID).waitingUntilTaskCompletion();
    }

    public Button disabledButton() {
        return aludraGUI.getComponentFactory().createButton(DISABLED_BUTTON_ID);
    }

    public Button hiddenButton() {
        return aludraGUI.getComponentFactory().createButton(HIDDEN_BUTTON_ID);
    }

    public Button hiddenDivButton() {
        return aludraGUI.getComponentFactory().createButton(HIDDEN_DIV_BUTTON_ID);
    }

    public Button notExistingButton() {
        return aludraGUI.getComponentFactory().createButton(NOT_EXISTING_BUTTON_ID);
    }

    public Checkbox firstCheckBox() {
        return aludraGUI.getComponentFactory().createCheckbox(FIRST_CHECKBOX_ID);
    }

    public Checkbox secondCheckBox() {
        return aludraGUI.getComponentFactory().createCheckbox(SECOND_CHECKBOX_ID);
    }

    public Checkbox disabledCheckBox() {
        return aludraGUI.getComponentFactory().createCheckbox(DISABLED_CHECKBOX_ID);
    }

    public RadioButton andRadioButton() {
        return aludraGUI.getComponentFactory().createRadioButton(RADIO_BUTTON_AND);
    }

    public RadioButton orRadioButton() {
        return aludraGUI.getComponentFactory().createRadioButton(RADIO_BUTTON_OR);
    }

    public Link testLink() {
        return aludraGUI.getComponentFactory().createLink(TEST_LINK_ID);
    }

    public Label image() {
        return aludraGUI.getComponentFactory().createLabel(IMAGE_ID);
    }

    public Button imageButton() {
        return aludraGUI.getComponentFactory().createButton(IMAGE_BUTTON_ID);
    }

    public Label label() {
        return aludraGUI.getComponentFactory().createLabel(LABEL_ID);
    }

    public FileField fileField() {
        return aludraGUI.getComponentFactory().createFileField(FILE_FIELD_ID);
    }

    public Button fileSubmitButton() {
        return aludraGUI.getComponentFactory().createButton(SUBMIT_FILE_ID);
    }

    public Label fileNameLabel() {
        return aludraGUI.getComponentFactory().createLabel(FILE_NAME_ID);
    }

    public Label fileContentLabel() {
        return aludraGUI.getComponentFactory().createLabel(FILE_CONTENT_ID);
    }

    public Link slowCloseLink() {
        return aludraGUI.getComponentFactory().createLink(SLOW_CLOSE_ID);
    }

    public Label hoverText() {
        return aludraGUI.getComponentFactory().createLabel(HOVER_TEXT_ID);
    }

    public Label hoverValue() {
        return aludraGUI.getComponentFactory().createLabel(HOVER_VALUE_ID);
    }

    public InputField textArea() {
        return aludraGUI.getComponentFactory().createInputField(TEXTAREA_ID);
    }

    public InputField textAreaReadOnly() {
        return aludraGUI.getComponentFactory().createInputField(TEXTAREA_READONLY_ID);
    }
    
    public void pressKeys(CharSequence... keysToPress) {
        aludraGUI.perform().keysPress(keysToPress);
    }

    public void addCookie(String name, String value, String domain, String path, int expiry) {
        aludraGUI.perform().addCookie(name, value, domain, path, expiry);
    }
    
    public void deleteCookieNamed(String name) {
        aludraGUI.perform().deleteCookieNamed(name);
    }
}
