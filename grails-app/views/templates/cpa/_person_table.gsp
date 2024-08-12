<%--  model: [persons, restrictToOrg] --%>
<%@ page import="de.laser.wekb.Vendor; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyOrg; de.laser.utils.LocaleUtils; de.laser.Org; de.laser.Provider; de.laser.Vendor; de.laser.Person; de.laser.PersonRole; de.laser.I10nTranslation;" %>

<g:set var="languageSuffix" value="${LocaleUtils.getCurrentLang()}"/>

<table class="ui table sortable la-js-responsive-table la-table">
    <colgroup>
        <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
            <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                <col style="width:  15px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <col style="width: 170px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('organisation')}">
                <col style="width: 236px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('function')}">
                <col style="width: 118px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('position')}">
                <col style="width: 118px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('functionPosition')}">
                <col style="width: 236px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('showContacts') && showContacts}">
                <col style="width: 277px;">
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyInvoicingInformation')}">
                <col style="width:  118px;">
            </g:if>
            <%--<g:if test="${tmplConfigItem.equalsIgnoreCase('showAddresses') && showAddresses}">
                <col style="width: 332px;">
            </g:if>--%>
        </g:each>
        <g:if test="${showOptions}">
                <col style="width:  82px;">
        </g:if>
    </colgroup>
    <thead>
    <tr>
<g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
    <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
        <th>${message(code: 'sidewide.number')}</th>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
        <g:sortableColumn params="${params}" property="p.last_name"
                              title="${message(code: 'person.name.label')}"/>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('organisation')}">
        <g:sortableColumn params="${params}" property="sortname"
                          title="${message(code: 'person.organisation.label')}"/>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('function')}">
        <th>
            ${message(code: 'person.function.label')}
        </th>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('position')}">
        <th>
            ${message(code: 'person.position.label')}
        </th>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('functionPosition')}">
        <th>
            ${message(code: 'person.function.label')} (${message(code: 'person.position.label')})
        </th>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('showContacts') && showContacts}">
            <th>${message(code: 'person.contacts.label')}</th>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyInvoicingInformation')}">
        <th>${message(code: 'surveyOrg.person.selected')}</th>
    </g:if>
    <%--<g:if test="${tmplConfigItem.equalsIgnoreCase('showAddresses') && showAddresses}">
            <th>${message(code: 'person.addresses.label')}</th>
    </g:if>--%>
</g:each>
        <g:if test="${showOptions}">
            <th class="la-action-info">${message(code: 'default.actions.label')}</th>
        </g:if>
    </tr>
    </thead>
    <tbody>
    <g:each in="${persons}" var="person" status="c">

    <%-- filter by model.restrictToOrg --%>
        <%
            Set<PersonRole> pRoles
            if(restrictToOrg)
                pRoles = person.roleLinks.findAll{ it.org == restrictToOrg }
            else if(restrictToProvider)
                pRoles = person.roleLinks.findAll{ it.provider == restrictToProvider }
            else if(restrictToVendor)
                pRoles = person.roleLinks.findAll{ it.vendor == restrictToVendor }
            else pRoles = person.roleLinks.findAll()
            pRoles.sort {
                if(it.org)
                    it.org.sortname
                else if(it.provider)
                    it.provider.sortname
                else if(it.vendor)
                    it.vendor.sortname
            }

            List<PersonRole> pRolesSorted = [], pProvRolesSorted = [], pVenRolesSorted = []
            int countFunctions = 0

            pRoles.each { item ->
                if(item.org) {
                    if (item.functionType) {
                        pRolesSorted.add(countFunctions++, item)
                    } else {
                        pRolesSorted.push(item)
                    }
                }
                else if(item.provider) {
                    if (item.functionType) {
                        pProvRolesSorted.add(countFunctions++, item)
                    } else {
                        pProvRolesSorted.push(item)
                    }
                }
                else if(item.vendor) {
                    if (item.functionType) {
                        pVenRolesSorted.add(countFunctions++, item)
                    } else {
                        pVenRolesSorted.push(item)
                    }
                }
            }
        %>

        <tr>
        <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
            <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
            <td>
                ${c + 1 + (offset ?: 0)}
            </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
            <th scope="row" class="la-th-column la-main-object">
                ${person.first_name ? person.last_name + ', ' + person.first_name : person.last_name}
                ${person.middle_name}
            </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('organisation')}">
            <td>
                <div class="ui divided middle aligned list la-flex-list ">
                    <g:each in="${pRolesSorted.groupBy  {it.org.id}}" var="orgId">
                        <g:set var="org" value="${Org.get(orgId.key)}"/>
                        <div class="ui item ">
                                <div class="la-flexbox">
                                    <i class="${Icon.ORG} la-list-icon"></i>
                                    <g:link controller="organisation" action="addressbook"
                                            id="${org.id}">${org.name} (${org.sortname})</g:link>
                                </div>
                        </div>
                    </g:each>
                    <g:each in="${pProvRolesSorted.groupBy  {it.provider.id}}" var="providerId">
                        <g:set var="provider" value="${Provider.get(providerId.key)}"/>
                        <div class="ui item ">
                                <div class="la-flexbox">
                                    <i class="${Icon.PROVIDER} la-list-icon"></i>
                                    <g:link controller="provider" action="addressbook"
                                            id="${provider.id}">${provider.name} (${provider.sortname})</g:link>
                                </div>
                        </div>
                    </g:each>
                    <g:each in="${pVenRolesSorted.groupBy  {it.vendor.id}}" var="venId">
                        <g:set var="vendor" value="${Vendor.get(venId.key)}"/>
                        <div class="ui item ">
                                <div class="la-flexbox">
                                    <i class="${Icon.VENDOR} la-list-icon"></i>
                                    <g:link controller="vendor" action="addressbook"
                                            id="${vendor.id}">${vendor.name} (${vendor.sortname})</g:link>
                                </div>
                        </div>
                    </g:each>
                </div>
            </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('function')}">
                <td>
                    <%-- filter by model.restrictToOrg --%>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${pRolesSorted.sort{it.functionType?.getI10n('value')}}" var="role">
                                <g:if test="${role.functionType}">
                                    <div class="ui item ">
                                        ${role.functionType.getI10n('value')}
                                    </div>
                                </g:if>
                        </g:each>
                        <g:each in="${pProvRolesSorted.sort{it.functionType?.getI10n('value')}}" var="role">
                                <g:if test="${role.functionType}">
                                    <div class="ui item ">
                                        ${role.functionType.getI10n('value')}
                                    </div>
                                </g:if>
                        </g:each>
                        <g:each in="${pVenRolesSorted.sort{it.functionType?.getI10n('value')}}" var="role">
                                <g:if test="${role.functionType}">
                                    <div class="ui item ">
                                        ${role.functionType.getI10n('value')}
                                    </div>
                                </g:if>
                        </g:each>
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('position')}">
                <td>
                    <%-- filter by model.restrictToOrg --%>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${pProvRolesSorted.sort{it.positionType?.getI10n('value')}}" var="role">
                                <g:if test="${role.positionType}">
                                    <div class="ui item ">
                                    ${role.positionType.getI10n('value')}
                                    </div>
                                </g:if>
                        </g:each>
                        <g:each in="${pVenRolesSorted.sort{it.positionType?.getI10n('value')}}" var="role">
                                <g:if test="${role.positionType}">
                                    <div class="ui item ">
                                    ${role.positionType.getI10n('value')}
                                    </div>
                                </g:if>
                        </g:each>
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('functionPosition')}">
                <td>
                    <%-- filter by model.restrictToOrg --%>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${pRolesSorted.sort{it.functionType ? it.functionType?.getI10n('value') : it.positionType?.getI10n('value')}}" var="role">
                            <div class="ui item ">
                                <g:if test="${role.functionType}">
                                    ${role.functionType.getI10n('value')}
                                </g:if>
                                <g:if test="${role.positionType}">
                                    (${role.positionType.getI10n('value')})
                                </g:if>
                            </div>
                        </g:each>
                        <g:each in="${pProvRolesSorted.sort{it.functionType ? it.functionType?.getI10n('value') : it.positionType?.getI10n('value')}}" var="role">
                            <div class="ui item ">
                                <g:if test="${role.functionType}">
                                    ${role.functionType.getI10n('value')}
                                </g:if>
                                <g:if test="${role.positionType}">
                                    (${role.positionType.getI10n('value')})
                                </g:if>
                            </div>
                        </g:each>
                        <g:each in="${pVenRolesSorted.sort{it.functionType ? it.functionType?.getI10n('value') : it.positionType?.getI10n('value')}}" var="role">
                            <div class="ui item ">
                                <g:if test="${role.functionType}">
                                    ${role.functionType.getI10n('value')}
                                </g:if>
                                <g:if test="${role.positionType}">
                                    (${role.positionType.getI10n('value')})
                                </g:if>
                            </div>
                        </g:each>
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('showContacts') && showContacts}">
                <td>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${person.contacts?.toSorted()}" var="contact">
                            <laser:render template="/templates/cpa/contact" model="${[
                                    contact             : contact,
                                    tmplShowDeleteButton: true,
                                    overwriteEditable   : false
                            ]}" />
                        </g:each>
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyInvoicingInformation')}">
                <td>
                        <g:if test="${editable && controllerName == 'myInstitution'}">
                            <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndPerson(participant, surveyConfig, person)}">
                                <g:link controller="myInstitution" action="surveyInfos"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setConcact: false, personId: person.id, setSurveyInvoicingInformation: true, viewTab: 'invoicingInformation']">
                                    <i class="${Icon.SYM.YES} bordered large green"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="myInstitution" action="surveyInfos"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setConcact: true, personId: person.id, setSurveyInvoicingInformation: true, viewTab: 'invoicingInformation']">
                                    <i class="${Icon.SYM.NO} bordered large red"></i>
                                </g:link>
                            </g:else>
                        </g:if>
                        <g:elseif test="${editable && controllerName == 'survey'}">
                            <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndPerson(participant, surveyConfig, person)}">
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setConcact: false, personId: person.id, setSurveyInvoicingInformation: true, viewTab: 'invoicingInformation', participant: participant.id]">
                                    <i class="${Icon.SYM.YES} bordered large green"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setConcact: true, personId: person.id, setSurveyInvoicingInformation: true, viewTab: 'invoicingInformation', participant: participant.id]">
                                    <i class="${Icon.SYM.NO} bordered large red"></i>
                                </g:link>
                            </g:else>
                        </g:elseif>
                        <g:else>
                            <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndPerson(participant, surveyConfig, person)}">
                                    <i class="${Icon.SYM.YES} bordered large green"></i>
                            </g:if>
                        </g:else>
                </td>
            </g:if>
            <%--<g:if test="${tmplConfigItem.equalsIgnoreCase('showAddresses') && showAddresses}">
                <td>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${person.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                            <laser:render template="/templates/cpa/address" model="${[
                                    address             : address,
                                    tmplShowDeleteButton: true,
                                    editable:             editable
                            ]}"/>
                        </g:each>
                    </div>
                </td>
            </g:if>--%>
        </g:each>
        <g:if test="${showOptions}">
            <td class="x">
                <g:if test="${editable}">
                    <button type="button" onclick="JSPC.app.editPerson(${person.id})" class="${Btn.MODERN.SIMPLE}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                        <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                    </button>

                    <g:form style="display:inline" controller="person" action="delete" data-confirm-id="${person.id.toString() + '_form'}">
                        <g:hiddenField id="person_id_${person.id}" name="id" value="${person.id}"/>
                        <div class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                             data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contact.addressbook", args: [person.toString()])}"
                             data-confirm-term-how="delete"
                             data-confirm-id="${person.id}"
                             role="button"
                             aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="${Icon.CMD.DELETE}"></i>
                        </div>
                    </g:form>
                </g:if>
            </td>
        </g:if>
        </tr>
    </g:each>
    </tbody>
</table>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editPerson = function (id) {
        //addresses deactivated as of ERMS-4492; the argument was showAddresses?:false
        <%
            Map<String, Object> urlParams = [showAddresses: false, showContacts: showContacts?:false]
            if(restrictToOrg)
                urlParams.org = restrictToOrg.id
            else if(restrictToProvider)
                urlParams.provider = restrictToProvider.id
            else if(restrictToVendor)
                urlParams.vendor = restrictToVendor.id
        %>
        var url = '<g:createLink controller="ajaxHtml" action="editPerson" params="${urlParams}"/>&id='+id;
        JSPC.app.person_editModal(url)
    }
    JSPC.app.person_editModal = function (url) {
        $.ajax({
            url: url,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#personModal").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                        r2d2.initDynamicXEditableStuff('#personModal');
                    }
                }).modal('show');
            }
        });
    }
</laser:script>


