<%@ page
        import="com.k_int.kbplus.Org;com.k_int.kbplus.Person;com.k_int.kbplus.PersonRole;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory;java.text.SimpleDateFormat"
%>
<g:set var="overwriteEditable"
       value="${editable || accService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}"/>
<g:set var="sdf" value="${new SimpleDateFormat(message(code: 'default.date.format.notime'))}"/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : <g:message code="default.show.label"
                                                                     args="[entityName]"/></title>
</head>

<body>

<g:render template="breadcrumb" model="${[orgInstance: orgInstance, params: params]}"/>

<g:if test="${editable}">
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
</g:if>

<h1 class="ui left aligned icon header"><semui:headerIcon/>
${orgInstance.name}
</h1>

<g:render template="nav"/>

<semui:messages data="${flash}"/>


<g:if test="${editable}">
    <input class="ui button"
           value="${message(code: 'readerNumber.create.label')}"
           data-semui="modal"
           href="#create_number"/>
</g:if>

<g:render template="/readerNumber/formModal"/>


<h5 class="ui header"><g:message code="menu.institutions.readerNumbers" default="Numbers"/></h5>

<table class="ui table celled sortable la-table">
    <thead>
    <tr>
        <th></th>
        <g:sortableColumn property="referenceGroup" title="${message(code: 'readerNumber.referenceGroup.label')}"
                          params="${params}"/>
        <g:sortableColumn property="value" title="${message(code: 'readerNumber.number.label')}"
                          params="${params}"/>
        <g:sortableColumn property="dueDate" title="${message(code: 'readerNumber.dueDate.label')}"
                          params="${params}"/>
        <g:sortableColumn property="semester" title="${message(code: 'readerNumber.semester.label')}"
                          params="${params}"/>
        <th>${message(code: 'default.actions')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${numbersInstanceList}" var="numbersInstance" status="i">

        <tr>
            <td>${i + 1}</td>
            <td>${numbersInstance.referenceGroup}</td>
            <td><g:formatNumber number="${numbersInstance.value}" type="number"/></td>
            <td>${sdf.format(numbersInstance.dueDate)}</td>
            <td>${RefdataValue.findByValue(numbersInstance.semester)?.getI10n('value')}</td>
            <td class="x">
                <g:if test="${editable}">
                    <button type="button" class="ui icon button" data-semui="modal"
                            href="#numbersFormModal_${numbersInstance.id}"
                            data-tooltip="${message(code: "readerNumber.edit.label")}"><i class="pencil icon"></i>
                    </button>
                    <g:form controller="readerNumber" action="delete">
                        <g:hiddenField name="id" value="${numbersInstance?.id}"/>
                        <button class="ui icon negative button" type="submit" name="_action_delete">
                            <i class="trash alternate icon"></i>
                        </button>
                    </g:form>

                    <g:render template="/readerNumber/formModal"
                              model="[formId: 'numbersFormModal_' + numbersInstance.id, numbersInstance: numbersInstance]"/>
                </g:if>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>

</body>
</html>