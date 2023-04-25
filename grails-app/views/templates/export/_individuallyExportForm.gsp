<%
    int filterFieldsSize
    Map totalFields
    if(filterFields) {
        filterFieldsSize = filterFields.size()
        totalFields = filterFields+formFields
    }
    else {
        filterFieldsSize = 0
        totalFields = formFields
    }
%>

    <div class="ui form">
        <div class="field">
            <label><g:message code="exportClickMe.fieldsToExport"/></label>
        </div>

        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:each in="${totalFields}" var="fields" status="i">
                <g:if test="${fields.value.fields.size() > 0}">
                    <a class="${("tab-${i}" == "tab-0") ? 'active' : ''}  item"
                       data-tab="tab-${i}">${fields.value.message ? message(code: fields.value.message) : fields.value.label}</a>
                </g:if>
                <g:else>
                    <a class="disabled item"
                       data-tab="tab-${i}">${fields.value.message ? message(code: fields.value.message) : fields.value.label}</a>
                </g:else>
            </g:each>
        </div>

        <g:each in="${filterFields}" var="fields" status="i">
            <div class="ui bottom attached ${("tab-${i}" == "tab-0") ? 'active' : ''} tab segment"
                 data-tab="tab-${i}">
            <div class="inline fields">
                <g:each in="${fields.value.fields}" var="field" status="fc">

                    <g:if test="${field.key == 'costItemsElements'}">
                        <g:set var="newFieldsDiv" value="${false}"/>
                        </div>

                        <label for="${field.key}"><g:message
                                code="exportClickMe.costItemsElements.selectCostItems"/></label>
                        <div class="inline fields">
                        <g:each in="${field.value}" var="costItemsElements" status="gc">
                            <div class="wide eight field">

                                <div class="ui checkbox ">
                                    <input type="checkbox" name="ief:${costItemsElements.key}"
                                           id="ief:${costItemsElements.key}" ${costItemsElements.value.defaultChecked ? 'checked="checked"' : ''}>
                                    <label for="ief:${costItemsElements.key}">${costItemsElements.value.message ? message(code: costItemsElements.value.message) : costItemsElements.value.label}</label>
                                </div>

                            </div><!-- .field -->

                            <g:if test="${gc % 2 == 1}">
                                <g:set var="newFieldsDiv" value="${true}"/>
                                </div>
                                <div class="inline fields">
                            </g:if>
                            <g:else>
                                <g:set var="newFieldsDiv" value="${false}"/>
                            </g:else>
                        </g:each>
                        <g:if test="${!newFieldsDiv}">
                            </div>
                            <div class="ui divider"></div>
                            <div class="inline fields">
                        </g:if>
                        <g:else>
                            <div class="ui divider"></div>
                        </g:else>

                    </g:if>
                    <g:else>
                        <div class="wide eight field">
                            <div class="ui checkbox">
                                <input type="checkbox" name="ief:${field.key}"
                                       id="ief:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                                <label for="ief:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                            </div>
                        </div><!-- .field -->
                    </g:else>



                    <g:if test="${fc % 2 == 1}">
                        </div>
                        <div class="inline fields">
                    </g:if>

                </g:each>
            </div>
            </div>
        </g:each>

        <g:each in="${formFields}" var="fields" status="i">
            <div class="ui bottom attached ${("tab-${i+filterFieldsSize}" == "tab-0") ? 'active' : ''} tab segment"
                 data-tab="tab-${i+filterFieldsSize}">
            <div class="inline fields">
                <g:each in="${fields.value.fields}" var="field" status="fc">

                    <g:if test="${field.key == 'costItemsElements'}">
                        <g:set var="newFieldsDiv" value="${false}"/>
                        </div>

                        <label for="${field.key}"><g:message
                                code="exportClickMe.costItemsElements.selectCostItems"/></label>
                        <div class="inline fields">
                        <g:each in="${field.value}" var="costItemsElements" status="gc">
                            <div class="wide eight field">

                                <div class="ui checkbox ">
                                    <input type="checkbox" name="iex:${costItemsElements.key}"
                                           id="iex:${costItemsElements.key}" ${costItemsElements.value.defaultChecked ? 'checked="checked"' : ''}>
                                    <label for="iex:${costItemsElements.key}">${costItemsElements.value.message ? message(code: costItemsElements.value.message) : costItemsElements.value.label}</label>
                                </div>

                            </div><!-- .field -->

                            <g:if test="${gc % 2 == 1}">
                                <g:set var="newFieldsDiv" value="${true}"/>
                                </div>
                                <div class="inline fields">
                            </g:if>
                            <g:else>
                                <g:set var="newFieldsDiv" value="${false}"/>
                            </g:else>
                        </g:each>
                        <g:if test="${!newFieldsDiv}">
                            </div>
                            <div class="ui divider"></div>
                            <div class="inline fields">
                        </g:if>
                        <g:else>
                            <div class="ui divider"></div>
                        </g:else>

                    </g:if>
                    <g:else>
                        <div class="wide eight field">
                            <div class="ui checkbox">
                                <input type="checkbox" name="iex:${field.key}"
                                       id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                                <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                            </div>
                        </div><!-- .field -->
                    </g:else>



                    <g:if test="${fc % 2 == 1}">
                        </div>
                        <div class="inline fields">
                    </g:if>

                </g:each>
            </div>

                <g:if test="${fields.key.contains('Contacts') && contactSwitch == true}"><%--  --%>

                    <div class="inline fields" style="border-top:1px solid lightgrey; padding-top:1em;">%{-- tmp --}%
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="public"><g:message code="org.publicContacts.label"/></label>
                                <input type="checkbox" name="contactSwitch" id="public" value="public" checked="checked"/>
                            </div>
                        </div>
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="private"><g:message code="org.privateContacts.exports.label"/></label>
                                <input type=checkbox name="contactSwitch" id="private" value="private"/>
                            </div>
                        </div>
                    </div>

                </g:if>

            </div>
        </g:each>

%{--        <g:if test="${contactSwitch == true}">--}%
%{--            <div class="fields">--}%
%{--                <div class="wide eight field">--}%
%{--                    <div class="ui checkbox">--}%
%{--                        <label for="public"><g:message code="org.publicContacts.label"/></label>--}%
%{--                        <input type="checkbox" name="contactSwitch" id="public" value="public" checked="checked"/>--}%
%{--                    </div>--}%
%{--                    <div class="ui checkbox">--}%
%{--                        <label for="private"><g:message code="org.privateContacts.exports.label"/></label>--}%
%{--                        <input type=checkbox name="contactSwitch" id="private" value="private"/>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--            </div>--}%
%{--        </g:if>--}%
        <g:if test="${orgSwitch == true}">
            <div class="fields">
                <div class="wide eight field">
                    <div class="ui checkbox">
                        <label for="exportOnlyContactPersonForInstitution"><g:message code="person.contactPersonForInstitution.label"/></label>
                        <input type=checkbox name="exportOnlyContactPersonForInstitution" value="true" checked="checked"/>
                    </div>
                    <div class="ui checkbox">
                        <label for="exportOnlyContactPersonForProviderAgency"><g:message code="person.contactPersonForProviderAgency.label"/></label>
                        <input type="checkbox" name="exportOnlyContactPersonForProviderAgency" value="true" checked="checked"/>
                    </div>
                </div>
            </div>
        </g:if>

        <br/>

        <div class="fields">

            <div class="wide eight field">
                <label for="filename"><g:message code="default.fileName.label"/></label>
                <input name="filename" id="filename" value="${exportFileName}"/>
            </div>

            <div class="wide eight field">
                <br>
                <button class="ui button positive right floated" id="export-as-excel" value="exportClickMeExcel">${exportExcelButtonName ?: 'Export Excel'}</button>
                <button class="ui button positive right floated" id="export-as-csv" value="exportClickMeCSV">${exportCSVButtonName ?: 'Export CSV'}</button>
            </div>

        </div><!-- .fields -->
    </div><!-- .form -->
