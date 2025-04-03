<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
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

    <%-- if commanded by Micha, that the notice gets moved into the form: ui.form info.msg has display:none, contact Ingrid in that case! --%>
   %{-- <g:if test="${formFields.keySet().contains('participantAccessPoints')}">
        <ui:msg class="warning" showIcon="true" message="exportClickMe.exportCSV.noAccessPoints" hideClose="true"/>
    </g:if>--}%
    <div class="ui form">

        <div class="ui top attached stackable tabular la-tab-with-js la-overflowX-auto menu">
            <g:each in="${totalFields}" var="fields" status="i">
                <g:if test="${fields.value.fields.size() > 0}">
                    <a class="${("tab-${i}" == "tab-0") ? 'active' : ''}  item"
                       data-tab="tab-${i}">${fields.value.message ? message(code: fields.value.message) : fields.value.label} (<div id="numberOfChecked-tab-${i}"></div>)</a>
                </g:if>
                <g:else>
                    <a class="disabled item"
                       data-tab="tab-${i}">${fields.value.message ? message(code: fields.value.message) : fields.value.label}</a>
                </g:else>
            </g:each>
        </div>

        <g:each in="${filterFields}" var="fields" status="i">
            <div class="ui bottom attached ${("tab-${i}" == "tab-0") ? 'active' : ''} tab segment" data-tab="tab-${i}">
                <%
                    Map<String, Object> costItemFilterFields = fields.value.fields.get('costItemsElements'),
                    otherFilterFields = fields.value.fields.findAll { Map.Entry f -> f.getKey() != 'costItemsElements' }
                %>
                <g:if test="${costItemFilterFields}">
                    <div class="ui grid">
                        <g:each in="${costItemFilterFields}" var="costItemElements" status="gc">
                            <g:if test="${gc == 0 || gc == Math.floor((costItemFilterFields.size() / 2))}">
                                <div class="wide eight field">
                            </g:if>
                            <div class="field">
                                <div class="ui checkbox">
                                    <input type="checkbox" name="ief:${costItemElements.key}"
                                           id="ief:${costItemElements.key}" ${costItemElements.value.defaultChecked ? 'checked="checked"' : ''}>
                                    <label for="ief:${costItemElements.key}">${costItemElements.value.message ? message(code: costItemElements.value.message) : costItemElements.value.label}</label>
                                </div>
                            </div>
                            <g:if test="${gc == Math.floor((costItemFields.size() / 2))-1 || gc == costItemFields.size()-1}">
                                </div><!-- .wide eight gc -->
                            </g:if>
                        </g:each>
                    </div>
                    <div class="ui divider"></div>
                </g:if>
                <div class="ui grid">
                    <g:each in="${otherFilterFields}" var="field" status="fc">
                        <g:if test="${fc == 0 || fc == Math.floor((otherFilterFields.size() / 2))}">
                            <div class="wide eight field">
                        </g:if>
                        <div class="field">
                            <div class="ui checkbox">
                                <input type="checkbox" name="ief:${field.key}"
                                       id="ief:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                                <label for="ief:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                            </div>
                        </div>
                        <g:if test="${fc == Math.floor((otherFilterFields.size() / 2))-1 || fc == otherFilterFields.size()-1}">
                            </div><!-- .wide eight fc -->
                        </g:if>
                    </g:each>
                </div>
            </div>
        </g:each>
        <%--
        <g:each in="${filterFields}" var="fields" status="i">
            <div class="ui bottom attached ${("tab-${i}" == "tab-0") ? 'active' : ''} tab segment" data-tab="tab-${i}">

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

                                <div class="ui checkbox">
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
        --%>

        <g:each in="${formFields}" var="fields" status="i">
            <div class="ui bottom attached ${("tab-${i+filterFieldsSize}" == "tab-0") ? 'active' : ''} tab segment" data-tab="tab-${i+filterFieldsSize}">
                <g:if test="${fields.value.containsKey('subTabs')}">
                    <div class="ui top attached stackable tabular la-tab-with-js menu">
                        <g:each in="${fields.value.subTabs}" var="subTab" status="st">
                            <a class="${(subTab.view == fields.value.subTabActive) ? 'active' : ''} item" data-tab="subTab-${st}">${message(code: subTab.label)}</a>
                        </g:each>
                    </div>
                    <g:each in="${fields.value.subTabs}" var="subTab" status="st">
                        <div class="ui bottom attached ${(subTab.view == fields.value.subTabActive) ? 'active' : ''} tab segment" data-tab="subTab-${st}">
                            <g:render template="/templates/export/individuallyExportFormGrid" model="[fields: fields, subTabPrefix: subTab.view]"/>
                        </div>
                    </g:each>
                </g:if>
                <g:else>
                    <g:render template="/templates/export/individuallyExportFormGrid" model="[fields: fields]"/>
                    <br/>
                </g:else>

                <g:if test="${fields.key.contains('Contacts') && contactSwitch == true}">

                    <div class="inline fields" style="border-top:1px solid lightgrey; padding-top:1em;">%{-- tmp --}%
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="public"><g:message code="org.publicContacts.label"/></label>
                                <input type="checkbox" name="contactSwitch" id="public" value="publicContact" checked="checked"/>
                            </div>
                        </div>
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="private"><g:message code="org.privateContacts.label"/></label>
                                <input type=checkbox name="contactSwitch" id="private" value="privateContact"/>
                            </div>
                        </div>
                    </div>

                </g:if>

                <g:if test="${fields.key.contains('Addresses') && contactSwitch == true}">

                    <div class="inline fields" style="border-top:1px solid lightgrey; padding-top:1em;">%{-- tmp --}%
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="publicAddress"><g:message code="org.publicAddresses.label"/></label>
                                <input type="checkbox" name="addressSwitch" id="publicAddress" value="publicAddress" checked="checked"/>
                            </div>
                        </div>
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="privateAddress"><g:message code="org.privateAddresses.label"/></label>
                                <input type=checkbox name="addressSwitch" id="privateAddress" value="privateAddress"/>
                            </div>
                        </div>
                    </div>

                </g:if>
            </div><!-- .bottom -->
        </g:each>

        <%--
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

                                <div class="ui checkbox">
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

                <g:if test="${fields.key.contains('Contacts') && contactSwitch == true}">

                    <div class="inline fields" style="border-top:1px solid lightgrey; padding-top:1em;">%{-- tmp --}%
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="public"><g:message code="org.publicContacts.label"/></label>
                                <input type="checkbox" name="contactSwitch" id="public" value="public" checked="checked"/>
                            </div>
                        </div>
                        <div class="field">
                            <div class="ui checkbox">
                                <label for="private"><g:message code="org.privateContacts.label"/></label>
                                <input type=checkbox name="contactSwitch" id="private" value="private"/>
                            </div>
                        </div>
                    </div>

                </g:if>

            </div>
        </g:each>
        --%>
%{--        <g:if test="${contactSwitch == true}">--}%
%{--            <div class="fields">--}%
%{--                <div class="wide eight field">--}%
%{--                    <div class="ui checkbox">--}%
%{--                        <label for="public"><g:message code="org.publicContacts.label"/></label>--}%
%{--                        <input type="checkbox" name="contactSwitch" id="public" value="public" checked="checked"/>--}%
%{--                    </div>--}%
%{--                    <div class="ui checkbox">--}%
%{--                        <label for="private"><g:message code="org.privateContacts.label"/></label>--}%
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
                        <input type=checkbox id="exportOnlyContactPersonForInstitution" name="exportOnlyContactPersonForInstitution" value="true" checked="checked"/>
                    </div>
                    <div class="ui checkbox">
                        <label for="exportOnlyContactPersonForProvider"><g:message code="person.contactPersonForProvider.label"/></label>
                        <input type="checkbox" id="exportOnlyContactPersonForProvider" name="exportOnlyContactPersonForProvider" value="true" checked="checked"/>
                    </div>
                    <div class="ui checkbox">
                        <label for="exportOnlyContactPersonForVendor"><g:message code="person.contactPersonForVendor.label"/></label>
                        <input type="checkbox" id="exportOnlyContactPersonForVendor" name="exportOnlyContactPersonForVendor" value="true" checked="checked"/>
                    </div>
                </div>
            </div>
        </g:if>

        <br/>
        <g:if test="${editExportConfig}">
                <div class="content">
                    <g:if test="${enableClickMeConfigSave}">
                        <div class="fields">
                    </g:if>
                    <g:else>
                        <div class="fields disabled la-popup-tooltip" data-position="left center" data-content="${message(code:'tooltip.onlyFullMembership')}">
                    </g:else>
                    <div class="wide four field">
                        <label for="clickMeConfigName">Export <g:message code="default.config.label"/> <g:message code="default.name.label"/></label>
                        <input name="clickMeConfigName" id="clickMeConfigName" value="${clickMeConfig.name}"/>
                    </div>

                    <div class="wide six field">
                        <label for="clickMeConfigNote"><g:message code="default.note.label"/></label>
                        <input name="clickMeConfigNote" id="clickMeConfigNote" value="${clickMeConfig.note}"/>
                    </div>

                    <div class="wide five field">
                        <label></label>
                        <button class="${Btn.POSITIVE} export" value="saveClickMeConfig" name="saveClickMeConfig">Export <g:message code="default.config.label"/> <g:message
                                code="default.button.save"/> </button>
                    </div>

                    </div><!-- .fields -->
                </div>
        </g:if>
        <g:else>
            <div class="fields">


                <div class="wide eight field">
                    <label for="filename"><g:message code="default.fileName.label"/></label>
                    <input name="filename" id="filename" value="${exportFileName}"/>
                </div>

                <div id="fileformat-query-csv" class="wide four field">
                    <label>${message(code: 'default.export.cfg.csv')}
                        <g:if test="${accessPointNotice}">
                            <span data-tooltip="${message(code: 'exportClickMe.exportCSV.noAccessPoints')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </g:if>
                        <g:if test="${currentTabNotice}">
                            <span data-tooltip="${message(code: 'exportClickMe.exportCSV.currentTabOnly')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </g:if>
                    </label>
                    <p>
                        ${message(code: 'default.export.cfg.csv.fieldSeparator')}: <span class="ui circular label">${csvFieldSeparator == '\t' ? "TAB" : csvFieldSeparator}</span> <br />
                    </p>
                </div>

                <div id="fileformat-query-tsv" class="wide four field">
                    <label>${message(code: 'default.export.cfg.tsv')}</label>
                    <p>
                        ${message(code: 'default.export.cfg.tsv.fieldSeparator')}: <span class="ui circular label">\t</span> <br />
                    </p>
                </div>
                <div id="fileformat-query-xlsx" class="wide four field">
                    <label>${message(code: 'default.export.cfg.xlsx')}</label>
                    <p>
                        ${message(code: 'default.export.cfg.xlsx.default')}
                    </p>
                </div>
                <div id="fileformat-query-pdf" class="wide four field">
                    <label>${message(code: 'default.export.cfg.pdf')}</label>
                    <p>
                        ${message(code: 'default.export.cfg.pdf.default')}
                    </p>
                </div>

                <div class="wide two field">
                    <label></label>
                    <g:set var="format" value="${overrideFormat ?: [xlsx: 'XLSX', csv: 'CSV', pdf: 'PDF']}"/>
                    <g:select name="fileformat" id="fileformat-query" class="ui selection dropdown la-not-clearable"
                              optionKey="key" optionValue="value"
                              from="${format}"
                    />
                </div>

                <div class="wide two field">
                    <label></label>
                    <button class="${Btn.POSITIVE} right floated export" value="exportClickMeExcel">Export</button>
                    <%-- disused
                    <br>
                    <g:hiddenField name="format" value=""/>
                    <g:hiddenField name="exportClickMeExcel" value=""/>
                    <g:if test="${multiMap}">
                        <button class="${Btn.POSITIVE} right floated exportButton" id="export-as-excel" value="exportClickMeExcel">Export</button>
                    </g:if>
                    <g:else>
                        <button class="${Btn.POSITIVE} right floated exportButton" id="export-as-excel" value="exportClickMeExcel">${exportExcelButtonName ?: 'Export Excel'}</button>
                        <button class="${Btn.POSITIVE} right floated exportButton" id="export-as-csv" value="exportClickMeCSV">${exportCSVButtonName ?: 'Export CSV'}</button>
                    </g:else>
                    --%>
                </div>

            </div><!-- .fields -->

            <g:if test="${showClickMeConfigSave}">
                <div class="ui accordion">
                  <div class="title">
                    <i class="dropdown icon"></i>
                <g:message code="clickMeConfig.save"/>?
                  </div>
                    <div class="content">
                        <g:if test="${enableClickMeConfigSave}">
                            <div class="fields">
                        </g:if>
                        <g:else>
                            <div class="fields disabled la-popup-tooltip" data-position="left center" data-content="${message(code:'tooltip.onlyFullMembership')}">
                        </g:else>
                            <div class="wide four field">
                                <label for="clickMeConfigName">Export <g:message code="default.config.label"/> <g:message code="default.name.label"/></label>
                                <input name="clickMeConfigName" id="clickMeConfigName" value=""/>
                            </div>

                                    <div class="wide six field">
                                        <label for="clickMeConfigNote"><g:message code="default.note.label"/></label>
                                        <input name="clickMeConfigNote" id="clickMeConfigNote" value=""/>
                                    </div>

                            <div class="wide five field">
                                <label></label>
                                <button class="${Btn.POSITIVE} export" value="saveClickMeConfig" name="saveClickMeConfig">Export <g:message code="default.config.label"/> <g:message
                                        code="default.button.save"/> </button>
                                <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code: 'clickMeConfig.save.info')}">
                                    <i class="${Icon.TOOLTIP.HELP} la-popup"></i>
                                </span>
                            </div>


                        </div><!-- .fields -->
                    </div>
                </div>
            </g:if>
        </g:else>
    </div><!-- .form -->

<laser:script file="${this.getGroovyPageFileName()}">

    $('#${modalID} select[name=fileformat]').on( 'change', function() {
        $('#${modalID} *[id^=fileformat-query-]').addClass('hidden')
        $('#${modalID} *[id^=fileformat-query-' + $('#${modalID} select[name=fileformat]').val() + ']').removeClass('hidden')
    }).trigger('change');

    $('#${modalID} input[type="checkbox"]').on( 'change', function() {
        <g:each in="${totalFields}" var="${fields}" status="i">
            $("#numberOfChecked-tab-${i}").html($('[data-tab=tab-${i}] input[type="checkbox"]').filter(':checked').length+'/'+$('[data-tab=tab-${i}] input[type="checkbox"]').length);
        </g:each>
    }).trigger('change');

</laser:script>