<%@ page
        import="com.k_int.kbplus.Org;com.k_int.kbplus.Person;com.k_int.kbplus.PersonRole;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory;java.text.SimpleDateFormat"
%>
<laser:serviceInjection />
<g:set var="overwriteEditable"
       value="${editable || accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}"/>
<g:set var="sdf" value="${de.laser.helper.DateUtil.getSDF_NoTime()}"/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label')}"/>
    <title>${message(code: 'laser')} : ${message(code:'menu.institutions.readerNumbers')}</title>
</head>

<body>

<semui:breadcrumbs>
    <g:if test="${!inContextOrg}">
        <semui:crumb text="${orgInstance.getDesignation()}"/>
    </g:if>
    <semui:crumb text="${message(code:"menu.institutions.readerNumbers")}" class="active"/>
</semui:breadcrumbs>

<g:if test="${editable}">
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
</g:if>
<g:else>
    <br />
</g:else>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

<g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}"/>

<semui:messages data="${flash}"/>

<g:render template="/readerNumber/formModal" model="[formId: 'newForUni',withSemester: true,title:message(code: 'readerNumber.createForUni.label')]"/>
<g:render template="/readerNumber/formModal" model="[formId: 'newForPublic',withDueDate: true,title:message(code: 'readerNumber.createForPublic.label')]"/>
<%--<g:render template="/readerNumber/formModal" model="[formId: 'newForState',withDueDate: true,title:message(code: 'readerNumber.createForState.label')]"/>--%>

<table class="ui table celled sortable la-table">
    <thead>
        <tr>
            <g:sortableColumn property="referenceGroup" title="${message(code: 'readerNumber.referenceGroup.label')}"
                              params="[sort:params.sortA,order:params.orderA,table:'tableA']"/>
            <g:sortableColumn property="value" title="${message(code: 'readerNumber.number.label')}"
                              params="[sort:params.sortA,order:params.orderA,table:'tableA']"/>
            <g:sortableColumn property="semester" title="${message(code: 'readerNumber.semester.label')}"
                              params="[sort:params.sortA,order:params.orderA,table:'tableA']"/>
            <th>${message(code: 'default.actions.label')}</th>
        </tr>
    </thead>
    <tbody>
    <g:each in="${numbersWithSemester}" var="numbersInstance">
        <tr>
            <td class="la-main-object">${numbersInstance.referenceGroup}</td>
            <td><g:formatNumber number="${numbersInstance.value}" type="number"/></td>
            <td>${numbersInstance.semester.getI10n('value')}</td>
            <td class="x">
                <g:if test="${editable}">

                    <g:form controller="readerNumber" action="delete">

                        <button type="button" class="ui icon button la-popup-tooltip la-delay" data-semui="modal"
                                href="#numbersFormModal_${numbersInstance.id}"
                                data-content="${message(code: "readerNumber.edit.label")}"><i class="pencil icon"></i>
                        </button>
                        <g:hiddenField name="id" value="${numbersInstance?.id}"/>
                        <button class="ui icon negative button" type="submit" name="_action_delete">
                            <i class="trash alternate icon"></i>
                        </button>
                    </g:form>

                    <g:render template="/readerNumber/formModal" model="[formId: 'numbersFormModal_' + numbersInstance.id, withSemester: true, numbersInstance: numbersInstance]"/>
                </g:if>
            </td>
        </tr>


    </g:each>
    </tbody>
</table>

<table class="ui table celled sortable la-table">
    <thead>
        <tr>
            <g:sortableColumn property="referenceGroup" title="${message(code: 'readerNumber.referenceGroup.label')}"
                              params="[sort:params.sortB,order:params.orderB,table:'tableB']"/>
            <g:sortableColumn property="value" title="${message(code: 'readerNumber.number.label')}"
                              params="[sort:params.sortB,order:params.orderB,table:'tableB']"/>
            <g:sortableColumn property="dueDate" title="${message(code: 'readerNumber.dueDate.label')}"
                              params="[sort:params.sortB,order:params.orderB,table:'tableB']"/>
            <th>${message(code: 'default.actions.label')}</th>
        </tr>
    </thead>
    <tbody>
    <g:each in="${numbersWithDueDate}" var="numbersInstance">
        <tr>
            <td class="la-main-object">${numbersInstance.referenceGroup}</td>
            <td><g:formatNumber number="${numbersInstance.value}" type="number"/></td>
            <td>${sdf.format(numbersInstance.dueDate)}</td>
            <td class="x">
                <g:if test="${editable}">

                    <g:form controller="readerNumber" action="delete">

                        <button type="button" class="ui icon button la-popup-tooltip la-delay" data-semui="modal"
                                href="#numbersFormModal_${numbersInstance.id}"
                                data-content="${message(code: "readerNumber.edit.label")}"><i class="pencil icon"></i>
                        </button>
                        <g:hiddenField name="id" value="${numbersInstance.id}"/>
                        <button class="ui icon negative button" type="submit" name="_action_delete">
                            <i class="trash alternate icon"></i>
                        </button>
                    </g:form>

                    <g:render template="/readerNumber/formModal" model="[formId: 'numbersFormModal_' + numbersInstance.id, withDueDate: true, numbersInstance: numbersInstance]"/>
                </g:if>
            </td>
        </tr>


    </g:each>
    </tbody>
</table>

</body>
</html>