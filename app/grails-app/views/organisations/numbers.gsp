<%@ page
        import="com.k_int.kbplus.Org"
        import="com.k_int.kbplus.Person"
        import="com.k_int.kbplus.PersonRole"
        import="com.k_int.kbplus.RefdataValue"
        import="com.k_int.kbplus.RefdataCategory"
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

<h1 class="ui header"><semui:headerIcon/>
${orgInstance.name}
</h1>

<g:render template="nav" contextPath="."/>

<semui:messages data="${flash}"/>


<g:if test="${editable}">
    <input class="ui button"
           value="${message(code: 'numbers.create.label')}"
           data-semui="modal"
           href="#numbersFormModal"/>
</g:if>

<g:render template="/numbers/formModal" model="['org'                   : orgInstance,
                                                'isPublic'              : RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No'),
                                                presetFunctionType      : RefdataValue.getByValueAndCategory('General contact person', 'Person Function'),
                                                tmplHideResponsibilities: true]"/>


<h5 class="ui header"><g:message code="numbers.plural" default="Numbers"/></h5>

<table class="ui table la-table">
<thead>
<tr>
    <th>${message(code: 'numbers.number.label')}-${message(code: 'numbers.type.label')}</th>
    <th>${message(code: 'numbers.number.label')}</th>
    <th>${message(code: 'numbers.startDate.label')}</th>
    <th>${message(code: 'numbers.endDate.label')}</th>
    <th></th>
</tr>
</thead>
<tbody>
<g:each in="${numbersInstanceList}" var="numbersInstance">
    <tr>
        <td><semui:xEditableRefData config="Number Type" owner="${numbersInstance}" field="type" overwriteEditable="${overwriteEditable}"/></td>
        <td><semui:xEditableRefData owner="${numbersInstance}" field="number" overwriteEditable="${overwriteEditable}"/></td>
        <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${numbersInstance?.startDate}" /></td>
        <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${numbersInstance?.endDate}" /></td>
        <td class="x">
            <g:if test="${editable}">
                <g:form controller="numbers" action="delete">
                    <g:hiddenField name="id" value="${numbersInstance?.id}"/>
                    <g:link class="ui icon button" controller="numbers" action="edit" id="${numbersInstance?.id}">
                        <i class="write icon"></i>
                    </g:link>
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