<%--  model: [persons, restrictToOrg] --%>
<%@ page import="de.laser.Org; de.laser.Person; de.laser.PersonRole" %>

<table class="ui table la-table">
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
            <g:if test="${tmplConfigItem.equalsIgnoreCase('showAddresses') && showAddresses}">
                <col style="width: 332px;">
            </g:if>
        </g:each>
        <col style="width:  82px;">
    </colgroup>
    <thead>
    <tr>
<g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
    <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
        <th>${message(code: 'sidewide.number')}</th>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
        <g:if test="${controllerName == 'org' && actionName == 'myPublicContacts'}">
            <g:sortableColumn params="${params}" property="p.last_name"
                              title="${message(code: 'person.name.label')}"/>
        </g:if>
        <g:else>
            <th>
                ${message(code: 'person.name.label')}
            </th>
        </g:else>
    </g:if>
    <g:if test="${tmplConfigItem.equalsIgnoreCase('organisation')}">
        <th>
            ${message(code: 'person.organisation.label')}
        </th>
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
    <g:if test="${tmplConfigItem.equalsIgnoreCase('showAddresses') && showAddresses}">
            <th>${message(code: 'person.addresses.label')}</th>
    </g:if>
</g:each>
        <th class="la-action-info">${message(code: 'default.actions.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${persons}" var="person" status="c">

    <%-- filter by model.restrictToOrg --%>
        <%
            Set<PersonRole> pRoles = person.roleLinks.findAll { restrictToOrg ? (it.org == restrictToOrg) : it }?.sort { it.org.sortname }

            List<PersonRole> pRolesSorted = []
            int countFunctions = 0

            pRoles.each { item ->
                if (item.functionType) {
                    pRolesSorted.add(countFunctions++, item)
                } else {
                    pRolesSorted.push(item)
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
            <td class="la-main-object">
                ${person.first_name ? person.last_name + ', ' + person.first_name : person.last_name}
                ${person.middle_name}
            </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('organisation')}">
            <td>
                <div class="ui divided middle aligned list la-flex-list ">
                    <g:each in="${pRolesSorted.groupBy  {it.org.id}}" var="orgId">
                        <g:set var="org" value="${Org.get(orgId.key)}"/>
                        <div class="ui item ">
                                <div class="la-flexbox">
                                    <i class="icon university la-list-icon"></i>
                                    <g:link controller="organisation" action="addressbook"
                                            id="${org.id}">${org}</g:link>
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
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('position')}">
                <td>
                    <%-- filter by model.restrictToOrg --%>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${pRolesSorted.sort{it.positionType?.getI10n('value')}}" var="role">
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
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('showContacts') && showContacts}">
                <td>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${person.contacts?.toSorted()}" var="contact">
                            <g:render template="/templates/cpa/contact" model="${[
                                    contact             : contact,
                                    tmplShowDeleteButton: true,
                                    overwriteEditable   : false
                            ]}">

                            </g:render>
                        </g:each>
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('showAddresses') && showAddresses}">
                <td>
                    <div class="ui divided middle aligned list la-flex-list ">
                        <g:each in="${person.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                            <g:render template="/templates/cpa/address" model="${[
                                    address             : address,
                                    tmplShowDeleteButton: true,
                                    editable:             editable
                            ]}"/>
                        </g:each>
                    </div>
                </td>
            </g:if>
        </g:each>
            <td class="x">
                <g:if test="${editable}">
                    <button type="button" onclick="editPerson(${person.id})" class="ui icon button">
                        <i class="write icon"></i>
                    </button>

                    <g:form controller="person" action="delete" data-confirm-id="${person.id.toString() + '_form'}">
                        <g:hiddenField name="id" value="${person.id}"/>
                        <div class="ui icon negative button js-open-confirm-modal"
                             data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contact.addressbook", args: [person.toString()])}"
                             data-confirm-term-how="delete"
                             data-confirm-id="${person.id}">
                            <i class="trash alternate icon"></i>
                        </div>
                    </g:form>
                </g:if>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>

<script>
    function editPerson(id) {
        var url = '<g:createLink controller="ajaxHtml" action="editPerson" params="[showAddresses: showAddresses?:false, showContacts: showContacts?:false, org: (restrictToOrg ? restrictToOrg?.id : '')]"/>&id='+id;
        person_editModal(url)
    }
    function person_editModal(url) {
        $.ajax({
            url: url,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#personModal").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                        r2d2.initDynamicSemuiStuff('#personModal');
                        r2d2.initDynamicXEditableStuff('#personModal');
                    }
                }).modal('show');
            }
        });
    }
</script>


