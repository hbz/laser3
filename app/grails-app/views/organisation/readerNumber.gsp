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

        <g:if test="${numbersWithSemester || numbersWithDueDate}">
            <g:if test="${numbersWithSemester}">
                <table class="ui table celled sortable la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="semester" title="${message(code: 'readerNumber.semester.label')}"/>
                            <g:each in="${semesterCols}" var="column">
                                <th>${column}</th>
                            </g:each>
                            <th><g:message code="readerNumber.sum.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${numbersWithSemester}" var="numbersInstance">
                        <tr>
                            <td>${numbersInstance.getKey().getI10n('value')}</td>
                            <g:each in="${semesterCols}" var="column">
                                <td>
                                    <g:set var="number" value="${numbersInstance.getValue().get(column)}"/>
                                    <g:if test="${number}">
                                        <semui:xEditable owner="${number}" field="value" format="number"/>
                                        <g:if test="${editable}">
                                            <g:form controller="readerNumber" action="delete">
                                                <g:hiddenField name="id" value="${number.id}"/>
                                                <button class="ui icon negative button" type="submit" name="_action_delete">
                                                    <i class="trash alternate icon"></i>
                                                </button>
                                            </g:form>
                                        </g:if>
                                    </g:if>
                                </td>
                            </g:each>
                            <td><g:formatNumber number="${semesterSums.get(numbersInstance.getKey())}"/></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </g:if>

            <g:if test="${numbersWithDueDate}">
                <table class="ui table celled sortable la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="dueDate" title="${message(code: 'readerNumber.dueDate.label')}"/>
                            <g:each in="${dueDateCols}" var="column">
                                <th>${column}</th>
                            </g:each>
                            <th><g:message code="readerNumber.sum.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${numbersWithDueDate}" var="numbersInstance">
                            <tr>
                                <td><g:formatDate date="${numbersInstance.getKey()}" format="${message(code:'default.date.format.notime')}"/></td>
                                <g:each in="${dueDateCols}" var="column">
                                    <td>
                                        <g:set var="number" value="${numbersInstance.getValue().get(column)}"/>
                                        <g:if test="${number}">
                                            <semui:xEditable owner="${number}" field="value" type="number"/>
                                            <g:if test="${editable}">
                                                <g:form controller="readerNumber" action="delete">
                                                    <g:hiddenField name="id" value="${number.id}"/>
                                                    <button class="ui icon negative button" type="submit" name="_action_delete">
                                                        <i class="trash alternate icon"></i>
                                                    </button>
                                                </g:form>
                                            </g:if>
                                        </g:if>
                                    </td>
                                </g:each>
                                <td><g:formatNumber number="${dueDateSums.get(numbersInstance.getKey())}"/></td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:if>
        </g:if>
        <g:else>
            <g:message code="readerNumber.noNumbersEntered"/>
        </g:else>
    </body>
</html>