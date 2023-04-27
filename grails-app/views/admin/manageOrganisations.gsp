<%@ page import="de.laser.Org; de.laser.OrgSetting; de.laser.RefdataCategory; groovy.json.JsonOutput; de.laser.api.v0.ApiToolkit; de.laser.api.v0.ApiManager; de.laser.auth.Role; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Contact; de.laser.OrgRole; de.laser.RefdataValue" %>

<laser:htmlStart message="menu.admin.manageOrganisations" serviceInjection="true"/>

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index" />
        <ui:crumb message="menu.admin.manageOrganisations" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.manageOrganisations" total="${orgListTotal}" type="admin"/>

    <ui:filter simple="true">
        <g:form action="manageOrganisations" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'identifier', 'type', 'customerType'],
                                               ['country&region', 'libraryNetwork', 'sector', 'libraryType']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <table class="ui sortable celled la-js-responsive-table la-table table">
        <g:set var="sqlDateToday" value="${new java.sql.Date(System.currentTimeMillis())}"/>

        <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code: 'org.sortname.label')}</th>
                <th>${message(code: 'org.fullName.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code:'org.customerType.label')}</th>
                <th>
                    ${message(code:'org.apiLevel.label')}
                    <span class="la-popup-tooltip la-delay la-no-uppercase" data-position="right center" data-content="${message(code:'org.apiLevel.tooltip')}" >
                        <i class="question circle icon popup"></i>
                    </span>
                </th>
                <th>
                    ${message(code:'org.specialApiPermission.label')}
                    <span class="la-popup-tooltip la-delay la-no-uppercase" data-position="right center" data-content="${message(code:'org.specialApiPermission.tooltip')}" >
                        <i class="question circle icon popup"></i>
                    </span>
                </th>
                <th class="la-no-uppercase"><span class="la-popup-tooltip la-delay" data-position="left center"
                          data-content="${message(code:'org.legalInformation.tooltip')}" >
                    <i class="handshake outline icon"></i>
                </span></th>
                <th>${message(code:'org.hasAccessOrg')}</th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${orgList}" var="org" status="i">
                <tr>
                    <td class="center aligned">
                        ${ (params.int('offset') ?: 0)  + i + 1 }<br />
                    </td>

                    <td>
                        ${org.sortname}

                        <g:if test="${org.status?.value == 'Deleted'}">
                            <span  class="la-popup-tooltip la-delay" data-content="Diese Organisation wurde als 'gelöscht' markiert." data-position="top left">
                                <i class="icon minus circle red"></i>
                            </span>
                        </g:if>
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
                        <g:each in="${org.orgType?.sort{it?.getI10n("value")}}" var="type">
                            ${type.getI10n("value")}
                        </g:each>
                    </td>

                    <td>
                        <%
                            def customerType = OrgSetting.get(org, OrgSetting.KEYS.CUSTOMER_TYPE)
                            if (customerType != OrgSetting.SETTING_NOT_FOUND) {
                                println customerType.getRoleValue()?.getI10n('authority')
                                customerType = customerType.getRoleValue().id
                            }
                            else {
                                customerType = null
                            }

                            def gascoEntry = OrgSetting.get(org, OrgSetting.KEYS.GASCO_ENTRY)
                            if (gascoEntry != OrgSetting.SETTING_NOT_FOUND && gascoEntry.getValue()?.value == 'Yes') {
                                println '<i class="icon green globe"></i>'
                                gascoEntry = gascoEntry.getValue()
                            } else {
                                gascoEntry = RDStore.YN_NO
                            }
                        %>
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
                                println '<div><i class="ui icon lock open"></i> Statistikserver</div>'
                            }

                            def accessOA = OrgSetting.get(org, OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS)
                            if (accessOA!= OrgSetting.SETTING_NOT_FOUND && accessOA.getValue()?.value == 'Yes') {
                                println '<div><i class="ui icon lock open"></i> OAMontior</div>'
                            }

                            def accessEZB = OrgSetting.get(org, OrgSetting.KEYS.EZB_SERVER_ACCESS)
                            if (accessEZB!= OrgSetting.SETTING_NOT_FOUND && accessEZB.getValue()?.value == 'Yes') {
                                println '<div><i class="ui icon lock open"></i> EZB</div>'
                            }
                        %>
                    </td>

                    <td>
                        <g:if test="${org.createdBy && org.legallyObligedBy}">
                            <span class="la-popup-tooltip la-delay" data-position="top right"
                                  data-content="${message(code:'org.legalInformation.1.tooltip', args:[org.createdBy, org.legallyObligedBy])}" >
                                <i class="ui icon green check circle"></i>
                            </span>
                        </g:if>
                        <g:elseif test="${org.createdBy}">
                            <span class="la-popup-tooltip la-delay" data-position="top right"
                                  data-content="${message(code:'org.legalInformation.2.tooltip', args:[org.createdBy])}" >
                                <i class="ui icon grey outline circle"></i>
                            </span>
                        </g:elseif>
                        <g:elseif test="${org.legallyObligedBy}">
                            <span class="la-popup-tooltip la-delay" data-position="top right"
                                  data-content="${message(code:'org.legalInformation.3.tooltip', args:[org.legallyObligedBy])}" >
                                <i class="ui icon red question mark"></i>
                            </span>
                        </g:elseif>
                    </td>

                    <td>
                        <g:set var="accessUserList" value="${org?.hasAccessOrgListUser()}"/>

                        <g:if test="${accessUserList?.instAdms?.size() > 0}">
                            Inst_Admins: ${accessUserList?.instAdms?.size()}<br />
                        </g:if>

                        <g:if test="${accessUserList?.instEditors?.size() > 0}">
                            Inst_Editors: ${accessUserList?.instEditors?.size()}<br />
                        </g:if>

                        <g:if test="${accessUserList?.instUsers?.size() > 0}">
                            Inst_Users: ${accessUserList?.instUsers?.size()}<br />
                        </g:if>

                    </td>

                    <td class="x">
                        <g:if test="${org.isCustomerType_Consortium()}">
                            <button type="button" class="ui icon button la-modern-button la-popup-tooltip la-delay"
                                    data-gascoTarget="${Org.class.name}:${org.id}"
                                    data-gascoEntry="${gascoEntry.class.name}:${gascoEntry.id}"
                                    data-orgName="${org.name}"
                                    data-ui="modal"
                                    data-href="#gascoEntryModal"
                                    data-content="GASCO-Eintrag ändern" data-position="top left"><i class="globe icon"></i></button>
                        </g:if>

                        <g:if test="${org.isCustomerType_Inst()}">
                            <button type="button" class="ui icon button la-modern-button la-popup-tooltip la-delay"
                                    data-liTarget="${Org.class.name}:${org.id}"
                                    data-createdBy="${org.createdBy?.id}"
                                    data-legallyObligedBy="${org.legallyObligedBy?.id}"
                                    data-orgName="${org.name}"
                                    data-ui="modal"
                                    data-href="#legalInformationModal"
                                    data-content="Rechtl. Informationen ändern" data-position="top left"><i class="handshake outline icon"></i></button>
                        </g:if>

                        <button type="button" class="ui icon button la-modern-button la-popup-tooltip la-delay"
                                data-ctTarget="${Org.class.name}:${org.id}"
                                data-customerType="${customerType}"
                                data-orgName="${org.name}"
                                data-ui="modal"
                                data-href="#customerTypeModal"
                                data-content="Kundentyp ändern" data-position="top left"><i class="user icon"></i></button>

                        <button type="button" class="ui icon button la-modern-button la-popup-tooltip la-delay"
                                data-alTarget="${Org.class.name}:${org.id}"
                                data-apiLevel="${apiLevel}"
                                data-orgName="${org.name}"
                                data-ui="modal"
                                data-href="#apiLevelModal"
                                data-content="API-Zugriff ändern" data-position="top left"><i class="key icon"></i></button>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <%-- changing gasco entry --%>

    <ui:modal id="gascoEntryModal" message="org.gascoEntry.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeGascoEntry"/>
            <input type="hidden" name="target" value="" />

            <div class="field">
                <label for="orgName_gasco">${message(code:'org.label')}</label>
                <input type="text" id="orgName_gasco" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="gascoEntry">${message(code:'org.gascoEntry.label')}</label>
                <ui:select id="gascoEntry" name="gascoEntry"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="${{ RefdataValue.class.name + ':' + it.id }}"
                              optionValue="value"
                              class="ui dropdown"
                />
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.show.gascoEntryModal = function(trigger) {
                $('#gascoEntryModal #orgName_gasco').attr('value', $(trigger).attr('data-orgName'))
                $('#gascoEntryModal input[name=target]').attr('value', $(trigger).attr('data-gascoTarget'))
                $('#gascoEntryModal select[name=gascoEntry]').dropdown('set selected', $(trigger).attr('data-gascoEntry'))
            }
        </laser:script>

    </ui:modal>

    <%-- changing legal information --%>

    <ui:modal id="legalInformationModal" message="org.legalInformation.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeLegalInformation"/>
            <input type="hidden" name="target" value="" />

            <div class="field">
                <label for="orgName_li">${message(code:'org.label')}</label>
                <input type="text" id="orgName_li" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="createdBy">${message(code:'org.createdBy.label')}</label>
                <g:select id="createdBy" name="createdBy"
                              from="${allConsortia}"
                              optionKey="id"
                              optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                              class="ui dropdown search"
                />
            </div>

            <div class="field">
                <label for="legallyObligedBy">${message(code:'org.legallyObligedBy.label')}</label>
                <g:select id="legallyObligedBy" name="legallyObligedBy"
                              from="${allConsortia}"
                              optionKey="id"
                              optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                              value=""
                              class="ui dropdown search"
                />
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.show.legalInformationModal = function(trigger) {
                $('#legalInformationModal input[name=target]').attr('value', $(trigger).attr('data-liTarget'))
                $('#legalInformationModal #orgName_li').attr('value', $(trigger).attr('data-orgName'))

                var createdBy = $(trigger).attr('data-createdBy')
                if (createdBy) {
                    $('#legalInformationModal select[name=createdBy]').dropdown('set selected', createdBy)
                } else {
                    $('#legalInformationModal select[name=createdBy]').dropdown('clear')
                }
                var legallyObligedBy = $(trigger).attr('data-legallyObligedBy')
                if (legallyObligedBy) {
                    $('#legalInformationModal select[name=legallyObligedBy]').dropdown('set selected', legallyObligedBy)
                } else {
                    $('#legalInformationModal select[name=legallyObligedBy]').dropdown('clear')
                }
            }
        </laser:script>

    </ui:modal>

    <%-- changing customer type --%>

    <ui:modal id="customerTypeModal" message="org.customerType.label" isEditModal="isEditModal" formID="customerTypeChangeForm"
                 showDeleteButton="showDeleteButton" deleteFormID="customerTypeDeleteForm" msgDelete="Kundentyp löschen">

        <g:form id="customerTypeChangeForm" class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeCustomerType"/>
            <input type="hidden" name="target" value="" />

            <div class="field">
                <label for="orgName_ct">${message(code:'org.label')}</label>
                <input type="text" id="orgName_ct" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="customerType">${message(code:'org.customerType.label')}</label>
                <ui:select id="customerType" name="customerType"
                          from="${[Role.findByAuthority('FAKE')] + Role.findAllByRoleType('org')}"
                          optionKey="id"
                          optionValue="authority"
                          class="ui dropdown la-not-clearable"
                />
            </div>
        </g:form>

        <g:form id="customerTypeDeleteForm" class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="deleteCustomerType"/>
            <input type="hidden" name="target" value=""/>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.show.customerTypeModal = function(trigger) {
                $('#customerTypeModal #orgName_ct').attr('value', $(trigger).attr('data-orgName'))
                $('#customerTypeModal input[name=target]').attr('value', $(trigger).attr('data-ctTarget'))

                var customerType = $(trigger).attr('data-customerType')
                if (customerType) {
                    $('#customerTypeModal select[name=customerType]').dropdown('set selected', customerType)
                } else {
                    $('#customerTypeModal select[name=customerType]').dropdown('clear')
                }
            }
        </laser:script>

    </ui:modal>

    <%-- changing api access --%>

    <ui:modal id="apiLevelModal" message="org.apiLevel.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeApiLevel"/>
            <input type="hidden" name="target" value=""/>

            <div class="field">
                <label for="orgName_al">${message(code:'org.label')}</label>
                <input type="text" id="orgName_al" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="apiLevel">${message(code:'org.apiLevel.label')}</label>
                <g:select id="apiLevel" name="apiLevel"
                          from="${['Kein Zugriff'] + ApiToolkit.getAllApiLevels()}"
                          class="ui dropdown la-not-clearable"
                />
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.show.apiLevelModal = function(trigger) {
                $('#apiLevelModal #orgName_al').attr('value', $(trigger).attr('data-orgName'))
                $('#apiLevelModal input[name=target]').attr('value', $(trigger).attr('data-alTarget'))

                var apiLevel = $(trigger).attr('data-apiLevel')
                if (apiLevel) {
                    $('#apiLevelModal select[name=apiLevel]').dropdown('set selected', apiLevel)
                } else {
                    $('#apiLevelModal select[name=apiLevel]').dropdown('clear')
                }
            }
        </laser:script>

    </ui:modal>

<laser:htmlEnd />
