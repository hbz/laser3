<%@ page import="de.laser.Org;de.laser.Person;de.laser.PersonRole;de.laser.RefdataValue;de.laser.RefdataCategory;de.laser.helper.RDConstants;de.laser.ReaderNumber" %>
<laser:serviceInjection />
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
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

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

        <g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}"/>

        <semui:messages data="${flash}"/>

        <g:render template="/readerNumber/formModal" model="[formId: 'newForUni',withSemester: true,title:message(code: 'readerNumber.createForUni.label')]"/>
        <g:render template="/readerNumber/formModal" model="[formId: 'newForPublic',withDueDate: true,title:message(code: 'readerNumber.createForPublic.label')]"/>
        <g:render template="/readerNumber/formModal" model="[formId: 'newForState',withDueDate: true,title:message(code: 'readerNumber.createForState.label')]"/>

        <g:if test="${numbersWithSemester || numbersWithDueDate}">
            <g:if test="${numbersWithSemester}">
                <table class="ui table celled sortable la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="semester" title="${message(code: 'readerNumber.semester.label')}" params="${[tableA:true]}"/>
                            <g:each in="${semesterCols}" var="column">
                                <th>
                                    ${column}
                                    <g:if test="${editable}">
                                        <%--<g:if test="${!(column in RefdataCategory.getAllRefdataValues(RDConstants.NUMBER_TYPE).collect {rdv->rdv.getI10n("value")})}">
                                            <g:link class="ui icon button js-open-confirm-modal" controller="readerNumber" action="delete"
                                                    data-confirm-tokenMsg="${message(code: 'readerNumber.confirm.delete')}"
                                                    data-confirm-term-how="ok" params="${[referenceGroup:column,org:params.id]}">
                                                <i class="red times icon"></i>
                                            </g:link>
                                        </g:if>--%>
                                    </g:if>
                                </th>
                            </g:each>
                            <th><g:message code="readerNumber.sum.label"/></th>
                            <th></th>
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
                                    </g:if>
                                </td>
                            </g:each>
                            <%
                                Map<String,Integer> sumRow = semesterSums.get(numbersInstance.getKey())
                                int students = sumRow.get(RefdataValue.getByValueAndCategory(ReaderNumber.READER_NUMBER_STUDENTS,RDConstants.NUMBER_TYPE).getI10n('value')) ?: 0
                                int FTEs = sumRow.get(RefdataValue.getByValueAndCategory(ReaderNumber.READER_NUMBER_FTE,RDConstants.NUMBER_TYPE).getI10n('value')) ?: 0
                                int staff = sumRow.get(RefdataValue.getByValueAndCategory(ReaderNumber.READER_NUMBER_SCIENTIFIC_STAFF,RDConstants.NUMBER_TYPE).getI10n('value')) ?: 0
                                boolean missing = students == 0 || FTEs == 0 || staff == 0
                                //int allOthers = sumRow.findAll { row -> !RefdataCategory.getAllRefdataValues(RDConstants.NUMBER_TYPE).collect { rdv -> rdv.getI10n("value") }.contains(row.key) }.collect { row -> row.value }.sum()
                            %>
                            <td>
                                <g:if test="${FTEs > 0}">
                                    <g:formatNumber number="${students+FTEs}"/>
                                </g:if>
                                <g:if test="${FTEs > 0 && staff > 0}">/</g:if>
                                <g:if test="${staff > 0}">
                                    <g:formatNumber number="${students+staff}"/>
                                </g:if>

                            </td>
                            <td class="x">
                                <g:if test="${editable}">
                                    <g:if test="${missing}">
                                        <a role="button" class="ui icon button" data-semui="modal" href="#newForSemester${numbersInstance.getKey().id}">
                                            <i class="write icon"></i>
                                        </a>
                                    </g:if>
                                    <g:link class="ui icon negative button js-open-confirm-modal" controller="readerNumber" action="delete"
                                            data-confirm-tokenMsg="${message(code: 'readerNumber.confirm.delete')}"
                                            data-confirm-term-how="ok" params="${[semester:numbersInstance.getKey().id,org:params.id]}">
                                        <i class="trash alternate icon"></i>
                                    </g:link>
                                    <g:render template="/readerNumber/formModal" model="[formId: 'newForSemester'+numbersInstance.getKey().id,semester:numbersInstance.getKey().id,withSemester: true,title:message(code: 'readerNumber.createForUni.label')]"/>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </g:if>

            <g:if test="${numbersWithDueDate}">
                <table class="ui table celled sortable la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="dueDate" title="${message(code: 'readerNumber.dueDate.label')}" params="${[tableB:true]}"/>
                            <g:each in="${dueDateCols}" var="column">
                                <th>${column}</th>
                            </g:each>
                            <th><g:message code="readerNumber.sum.label"/></th>
                            <th></th>
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
                                        </g:if>
                                    </td>
                                </g:each>
                                <td><g:formatNumber number="${dueDateSums.get(numbersInstance.getKey())}"/></td>
                                <td>
                                    <g:if test="${editable}">
                                        <g:link class="ui icon negative button js-open-confirm-modal" controller="readerNumber" action="delete"
                                                data-confirm-tokenMsg="${message(code: 'readerNumber.confirm.delete')}"
                                                data-confirm-term-how="ok" params="${[dueDate:numbersInstance.getKey(),org:params.id]}">
                                            <i class="trash alternate icon"></i>
                                        </g:link>
                                    </g:if>
                                </td>
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