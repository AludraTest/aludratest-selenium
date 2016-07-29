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
package org.aludratest.service.gui.web.selenium.selenium2.condition;

/** The DOJO (pre 1.7) implementation for checking current AJAX status. <br>
 * Notice that this is a more complex piece of JavaScript. It overrides the standard DOJO functions <code>xhrGet</code> and
 * <code>xhrPost</code> to detect AJAX start and hook into the request to detect AJAX end. <br>
 * For working as expected, this script must be initialized BEFORE the AJAX call to check begins. Recommended is a simple way:
 * 
 * <pre>
 * // inits the IceFaces monitoring JavaScript
 * gui.perform().waitForAjaxOperationEnd("dojo", 10000);
 * myAjaxButton.click();
 * // does the real wait
 * gui.perform().waitForAjaxOperationEnd("dojo", 10000);
 * </pre>
 * 
 * @author falbrech */
public class DojoPre17AjaxIdleCondition extends AbstractAjaxIdleCondition {
    
    // @formatter:off
    private static final String DOJO_AJAX_CHECK_SCRIPT = "function createDojoHandler(oldMethod) {" +
            "   return function(args) {" +
            "      window.aludraTestDojoAjaxCounter++;" +
            "      " +
            "      var oldHandle = (typeof(args.handle) === 'function') ? args.handle : function(a, b) { };" +
            "      args.handle = function(a, b) {" +
            "          window.aludraTestDojoAjaxCounter--;" +
            "          oldHandle(a, b);" +
            "      };" +
            "      " +
            "      return oldMethod(args);" +
            "   };" +
            "}" +
            "function attachToDojo() {" +
            "   var methodsToOverride = [ 'Get', 'Post', 'Put', 'Delete' ];" +
            "   var oldMethods = { };" +
            "   for (var i = 0; i < methodsToOverride.length; i++) {" +
            "       oldMethods[methodsToOverride[i]] = dojo['xhr' + methodsToOverride[i]];" +
            "       if (typeof(oldMethod) === 'function') {" +
            "           dojo['xhr' + methodsToOverride[i]] = createDojoHandler(oldMethod);" +
            "       }" +
            "   }" +
            "   window.aludraTestDojoAjax = true;" +
            "   window.aludraTestDojoAjaxCounter = 0;" +
            "}" +
            "if (!window.aludraTestDojoAjax) {" +
            "   attachToDojo();" +
            "   return true;" +
            "}" +
            "return window.aludraTestDojoAjaxCounter == 0;";
    // @formatter:on

    @Override
    protected String getBooleanAjaxIdleScript() {
        return DOJO_AJAX_CHECK_SCRIPT;
    }

}
