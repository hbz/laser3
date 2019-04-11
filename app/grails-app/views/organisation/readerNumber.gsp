<%@ page
        import="com.k_int.kbplus.Org;com.k_int.kbplus.Person;com.k_int.kbplus.PersonRole;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory"
%>
<g:set var="overwriteEditable" value="${editable || accService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}" />
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

<h1 class="ui left aligned icon header"><semui:headerIcon />
${orgInstance.name}
</h1>

<g:render template="nav"/>

<semui:messages data="${flash}"/>


<g:if test="${editable}">
    <input class="ui button"
           value="${message(code: 'readerNumber.create.label')}"
           data-semui="modal"
           data-href="#numbersFormModal"/>
</g:if>

<g:render template="/readerNumber/formModal"/>


<h5 class="ui header"><g:message code="menu.institutions.readerNumbers" default="Numbers"/></h5>

<table class="ui table la-table">
<thead>
<tr>
    <th>${message(code: 'readerNumber.referenceGroup.label')}</th>
    <th>${message(code: 'readerNumber.number.label')}</th>
    <th>${message(code: 'readerNumber.dueDate.label')}</th>
    <th></th>
</tr>
</thead>
<tbody>
<g:each in="${numbersInstanceList}" var="numbersInstance">

    <tr>
        <td>${numbersInstance.type}</td>
        <td>${numbersInstance.number}</td>
        <td>${numbersInstance.dueDate}</td>
        <td>${numbersInstance.semester}</td>
        <td class="x">
            <g:if test="${editable}">
                <button type="button" class="ui icon button" data-semui="modal" href="#numbersFormModal_${docctx.id}" data-tooltip="${message(code:"readerNumber.edit.label")}"><i class="pencil icon"></i></button>
                <g:form controller="numbers" action="delete">
                    <g:hiddenField name="id" value="${numbersInstance?.id}"/>
                    <button class="ui icon negative button" type="submit" name="_action_delete">
                        <i class="trash alternate icon"></i>
                    </button>
                </g:form>
            </g:if>
        </td>
    </tr>
</g:each>
</tbody>
</table>

</body>
</html>