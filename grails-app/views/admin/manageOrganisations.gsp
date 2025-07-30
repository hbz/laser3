<%@ page import="de.laser.ui.CSS; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Org; de.laser.OrgSetting; de.laser.RefdataCategory; groovy.json.JsonOutput; de.laser.api.v0.ApiToolkit; de.laser.api.v0.ApiManager; de.laser.auth.Role; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.addressbook.PersonRole; de.laser.addressbook.Contact; de.laser.OrgRole; de.laser.RefdataValue" %>

<laser:htmlStart message="menu.admin.manageOrganisations" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index" />
        <ui:crumb message="menu.admin.manageOrganisations" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.manageOrganisations" total="${orgListTotal}" type="admin"/>

    <ui:filter simple="true">
        <g:form action="manageOrganisations" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'identifier', 'customerType'],
                                               ['country&region', 'libraryNetwork', 'libraryType'],
                                               ['discoverySystemsFrontend', 'discoverySystemsIndex'],
                                               ['isBetaTester', 'apiLevel', 'serverAccess', '']
                                               ],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <table class="${CSS.ADMIN_TABLE}">
        <g:set var="sqlDateToday" value="${new java.sql.Date(System.currentTimeMillis())}"/>

        <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code: 'org.sortname.label')}</th>
                <th>${message(code: 'org.fullName.label')}</th>
                <th class="center aligned">
%{--                    ${message(code:'org.customerType.label')}--}%
                    <span class="la-popup-tooltip la-no-uppercase" data-position="left center" data-content="${message(code:'org.customerType.label')}">
                        <i class="${Icon.ATTR.ORG_CUSTOMER_TYPE} popup"></i>
                    </span>
                </th>
                <th class="center aligned">
                    <span class="la-popup-tooltip" data-position="left center" data-content="${message(code:'org.isBetaTester.label')}">
                        <i class="${Icon.ATTR.ORG_IS_BETA_TESTER}"></i>
                    </span>
                </th>
                <th class="center aligned">
                    <span class="la-popup-tooltip" data-position="left center" data-content="${message(code:'org.legalInformation.tooltip')}">
                        <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION}"></i>
                    </span>
                </th>
                <th>
                    ${message(code:'org.apiLevel.label')}
                    <span class="la-popup-tooltip la-no-uppercase" data-position="right center" data-content="${message(code:'org.apiLevel.tooltip')}">
                        <i class="${Icon.TOOLTIP.HELP} popup"></i>
                    </span>
                </th>
                <th>
                    ${message(code:'org.serverAccess.label')}
                    <span class="la-popup-tooltip la-no-uppercase" data-position="right center" data-content="${message(code:'org.serverAccess.tooltip')}">
                        <i class="${Icon.TOOLTIP.HELP} popup"></i>
                    </span>
                </th>
                <th>${message(code:'org.hasAccessOrg')}</th>
                <th class="center aligned">
                    <ui:optionsIcon />
                </th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${orgList}" var="org" status="i">
                <tr class="${org.isArchived() ? 'warning' : ''}">
                    <td class="center aligned">
                        ${ (params.int('offset') ?: 0)  + i + 1 }<br />
                    </td>

                    <td>
                        <ui:archiveIcon org="${org}" /> ${org.sortname}
                    </td>

                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}">
                            ${fieldValue(bean: org, field: "name")} <br />
                            <g:if test="${org.sortname}">
                                (${fieldValue(bean: org, field: "sortname")})
                            </g:if>
                        </g:link>
                    </td>

                    <td>
                        <%
                            def customerType = OrgSetting.get(org, OrgSetting.KEYS.CUSTOMER_TYPE)
                            if (customerType != OrgSetting.SETTING_NOT_FOUND) {
                                customerType = customerType.getRoleValue().id
                            } else {
                                customerType = null
                            }
                        %>
                        <ui:customerTypeIcon org="${org}" option="tooltip" />
                    </td>

                    <td>
                        <g:if test="${org.isBetaTester}">
                            <span class="la-popup-tooltip" data-position="top right" data-content="${message(code:'org.isBetaTester.label')}" >
                                <i class="${Icon.ATTR.ORG_IS_BETA_TESTER} red"></i>
                            </span>
                        </g:if>
                    </td>

                    <td>
                        <g:if test="${org.createdBy && org.legallyObligedBy}">
                            <span class="la-popup-tooltip" data-position="top right"
                                  data-content="${message(code:'org.legalInformation.11.tooltip', args:[org.createdBy, org.legallyObligedBy])}" >
                                <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION_11}"></i>
                            </span>
                        </g:if>
                        <g:elseif test="${org.createdBy}">
                            <span class="la-popup-tooltip" data-position="top right"
                                  data-content="${message(code:'org.legalInformation.10.tooltip', args:[org.createdBy])}" >
                                <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION_10}"></i>
                            </span>
                        </g:elseif>
                        <g:elseif test="${org.legallyObligedBy}">
                            <span class="la-popup-tooltip" data-position="top right"
                                  data-content="${message(code:'org.legalInformation.01.tooltip', args:[org.legallyObligedBy])}" >
                                <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION_01}"></i>
                            </span>
                        </g:elseif>
                    </td>

                    <td>
                        <%
                            def apiLevel = OrgSetting.get(org, OrgSetting.KEYS.API_LEVEL)
                            if (apiLevel != OrgSetting.SETTING_NOT_FOUND) {
                                println '<div>' + apiLevel.getValue() + '</div>'
                                apiLevel = apiLevel.getValue()
                            }
                            else {
                                apiLevel = 'Kein Zugriff'
                            }
                        %>
                    </td>

                    <td>
                        <%
                            def accessStatistics = OrgSetting.get(org, OrgSetting.KEYS.NATSTAT_SERVER_ACCESS)
                            if (accessStatistics != OrgSetting.SETTING_NOT_FOUND && accessStatistics.getValue()?.value == 'Yes') {
                                println '<div>Statistikserver</div>'
                            }

                            def accessOA = OrgSetting.get(org, OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS)
                            if (accessOA!= OrgSetting.SETTING_NOT_FOUND && accessOA.getValue()?.value == 'Yes') {
                                println '<div>OAMonitor</div>'
                            }

                            def accessEZB = OrgSetting.get(org, OrgSetting.KEYS.EZB_SERVER_ACCESS)
                            if (accessEZB!= OrgSetting.SETTING_NOT_FOUND && accessEZB.getValue()?.value == 'Yes') {
                                println '<div>EZB</div>'
                            }
                        %>
                    </td>

                    <td>
                        <g:set var="userMap" value="${org.getUserMap()}"/>

                        <g:if test="${userMap.instAdms.size() > 0}">
                            Inst_Admins: ${userMap.instAdms.size()}<br />
                        </g:if>

                        <g:if test="${userMap.instEditors.size() > 0}">
                            Inst_Editors: ${userMap.instEditors.size()}<br />
                        </g:if>

                        <g:if test="${userMap.instUsers.size() > 0}">
                            Inst_Users: ${userMap.instUsers.size()}<br />
                        </g:if>
                    </td>

                    <td class="x">
                        <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                data-target="${org.id}"
                                data-targetName="${org.name}"
                                data-customerType="${customerType}"
                                data-ui="modal"
                                data-href="#customerTypeModal"
                                data-content="Kundentyp ändern" data-position="top left"><i class="${Icon.ATTR.ORG_CUSTOMER_TYPE}"></i></button>

                        <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                data-target="${org.id}"
                                data-targetName="${org.name}"
                                data-isBetaTester="${org.isBetaTester ? RDStore.YN_YES.id : RDStore.YN_NO.id}"
                                data-ui="modal"
                                data-href="#isBetaTesterModal"
                                data-content="${message(code:'org.isBetaTester.label')} ändern" data-position="top left"><i class="${Icon.ATTR.ORG_IS_BETA_TESTER}"></i></button>

                        <g:if test="${org.isCustomerType_Inst()}">
                            <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                    data-target="${org.id}"
                                    data-targetName="${org.name}"
                                    data-createdBy="${org.createdBy?.id}"
                                    data-legallyObligedBy="${org.legallyObligedBy?.id}"
                                    data-ui="modal"
                                    data-href="#legalInformationModal"
                                    data-content="Rechtl. Informationen ändern" data-position="top left"><i class="${Icon.ATTR.ORG_LEGAL_INFORMATION}"></i></button>
                        </g:if>
                        <g:else>
                            <div class="${Btn.MODERN.SIMPLE} disabled"><icon:placeholder/></div>
                        </g:else>

                        <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                data-target="${org.id}"
                                data-targetName="${org.name}"
                                data-apiLevel="${apiLevel}"
                                data-ui="modal"
                                data-href="#apiLevelModal"
                                data-content="API-Zugriff ändern" data-position="top left"><i class="${Icon.SYM.IS_PUBLIC}"></i></button>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <ui:paginate action="manageOrganisations" controller="admin" params="${filteredParams}" max="${max}" total="${orgListTotal}" />

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.setModalTarget = function(ctx, $trigger) {
            $(ctx + ' input[name=cmd_target]').attr('value', $trigger.attr('data-target'))
            $(ctx + ' input[name=cmd_targetName]').attr('value', $trigger.attr('data-targetName'))
        }
    </laser:script>

    <%-- changing isBetaTester--%>

    <ui:modal id="isBetaTesterModal" message="org.isBetaTester.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations', params: filteredParams]">
            <input type="hidden" name="cmd" value="changeIsBetaTester"/>
            <input type="hidden" name="cmd_target" value="" />

            <div class="field">
                <label for="cmd_targetName_isBetaTester">${message(code:'org.label')}</label>
                <input type="text" id="cmd_targetName_isBetaTester" name="cmd_targetName" value="" readonly />
            </div>

            <div class="field">
                <label for="cmd_isBetaTester">${message(code:'org.isBetaTester.label')}</label>
                <ui:select id="cmd_isBetaTester" name="cmd_isBetaTester"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              class="ui dropdown la-not-clearable"
                />
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.onShow.isBetaTesterModal = function(trigger) {
                JSPC.app.setModalTarget('#isBetaTesterModal', $(trigger))
                $('#isBetaTesterModal select[name=cmd_isBetaTester]').dropdown('set selected', $(trigger).attr('data-isBetaTester'))
            }
        </laser:script>

    </ui:modal>

    <%-- changing legal information --%>

    <ui:modal id="legalInformationModal" message="org.legalInformation.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations', params: filteredParams]">
            <input type="hidden" name="cmd" value="changeLegalInformation"/>
            <input type="hidden" name="cmd_target" value="" />

            <div class="field">
                <label for="cmd_targetName_legalInformation">${message(code:'org.label')}</label>
                <input type="text" id="cmd_targetName_legalInformation" name="cmd_targetName" value="" readonly />
            </div>

            <div class="field">
                <label for="cmd_createdBy">${message(code:'org.createdBy.label')}</label>
                <g:select id="cmd_createdBy" name="cmd_createdBy"
                              from="${allConsortia}"
                              optionKey="id"
                              optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                              class="ui dropdown clearable search"
                />
            </div>

            <div class="field">
                <label for="cmd_legallyObligedBy">${message(code:'org.legallyObligedBy.label')}</label>
                <g:select id="cmd_legallyObligedBy" name="cmd_legallyObligedBy"
                              from="${allConsortia}"
                              optionKey="id"
                              optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                              value=""
                              class="ui dropdown clearable search"
                />
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.onShow.legalInformationModal = function(trigger) {
                JSPC.app.setModalTarget('#legalInformationModal', $(trigger))

                let $cmd1 = $('#legalInformationModal select[name=cmd_createdBy]')
                let createdBy = $(trigger).attr('data-createdBy')
                if (createdBy) {
                    $cmd1.dropdown('set selected', createdBy)
                } else {
                    $cmd1.dropdown('clear')
                }

                let $cmd2 = $('#legalInformationModal select[name=cmd_legallyObligedBy]')
                let legallyObligedBy = $(trigger).attr('data-legallyObligedBy')
                if (legallyObligedBy) {
                    $cmd2.dropdown('set selected', legallyObligedBy)
                } else {
                    $cmd2.dropdown('clear')
                }
            }
        </laser:script>

    </ui:modal>

    <%-- changing customer type --%>

    <ui:modal id="customerTypeModal" message="org.customerType.label" isEditModal="isEditModal" formID="customerTypeChangeForm"
                 showDeleteButton="showDeleteButton" deleteFormID="customerTypeDeleteForm" msgDelete="Kundentyp löschen">

        <g:form id="customerTypeChangeForm" class="ui form" url="[controller: 'admin', action: 'manageOrganisations', params: filteredParams]">
            <input type="hidden" name="cmd" value="changeCustomerType"/>
            <input type="hidden" name="cmd_target" value="" />

            <div class="field">
                <label for="cmd_targetName_customerType">${message(code:'org.label')}</label>
                <input type="text" id="cmd_targetName_customerType" name="cmd_targetName" value="" readonly />
            </div>

            <div class="field">
                <label for="cmd_customerType">${message(code:'org.customerType.label')}</label>
                <ui:select id="cmd_customerType" name="cmd_customerType"
                          from="${[Role.findByAuthority('FAKE')] + Role.findAllByRoleType('org')}"
                          optionKey="id"
                          optionValue="authority"
                          class="ui dropdown la-not-clearable"
                />
            </div>
        </g:form>

        <g:form id="customerTypeDeleteForm" class="ui form" url="[controller: 'admin', action: 'manageOrganisations', params: filteredParams]">
            <input type="hidden" name="cmd" value="deleteCustomerType"/>
            <input type="hidden" name="cmd_target" value=""/>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.onShow.customerTypeModal = function(trigger) {
                JSPC.app.setModalTarget('#customerTypeModal', $(trigger))

                let $cmd = $('#customerTypeModal select[name=cmd_customerType]')
                let customerType = $(trigger).attr('data-customerType')
                if (customerType) {
                    $cmd.dropdown('set selected', customerType)
                } else {
                    $cmd.dropdown('clear')
                }
            }
        </laser:script>

    </ui:modal>

    <%-- changing api access --%>

    <ui:modal id="apiLevelModal" message="org.apiLevel.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations', params: filteredParams]">
            <input type="hidden" name="cmd" value="changeApiLevel"/>
            <input type="hidden" name="cmd_target" value=""/>

            <div class="field">
                <label for="cmd_targetName_apiLevel">${message(code:'org.label')}</label>
                <input type="text" id="cmd_targetName_apiLevel" name="cmd_targetName" value="" readonly />
            </div>

            <div class="field">
                <label for="cmd_apiLevel">${message(code:'org.apiLevel.label')}</label>
                <g:select id="cmd_apiLevel" name="cmd_apiLevel"
                          from="${['Kein Zugriff'] + ApiToolkit.getAllApiLevels()}"
                          class="ui dropdown la-not-clearable"
                />
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.onShow.apiLevelModal = function(trigger) {
                JSPC.app.setModalTarget('#apiLevelModal', $(trigger))

                let $cmd = $('#apiLevelModal select[name=cmd_apiLevel]')
                let apiLevel = $(trigger).attr('data-apiLevel')
                if (apiLevel) {
                    $cmd.dropdown('set selected', apiLevel)
                } else {
                    $cmd.dropdown('clear')
                }
            }
        </laser:script>

    </ui:modal>

<laser:htmlEnd />
