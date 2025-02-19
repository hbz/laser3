<%@ page import="de.laser.ui.Btn" %>
<g:set var="entityName" value="${message(code: 'menu.yoda.mailAysnc.list')}" />
<laser:htmlStart text="${message(code:"default.list.label", args:[entityName])}" />
<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.mailAysnc.list" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.mailAysnc.list" type="yoda" total="${resultCount}"/>

    <g:render template="flashMessage"/>

<ui:filter>
    <g:form action="${actionName}" params="${params}" method="get" class="ui form">
        <input type="hidden" name="sort" value="${params.sort}">
        <input type="hidden" name="order" value="${params.order}">

        <div class="field">
            <label for="filter">Search in Subject</label>
            <input id="filter" name="filter" value="${params.filter}"/>
        </div>

        <div class="field">
            <label for="filterBody">Search in Mail-Text</label>
            <input id="filterBody" name="filterBody" value="${params.filterBody}"/>
        </div>

       %{-- <div class="two fields">
            <div class="field">
                <label for="createDate">Create Date</label>
                <ui:datepicker id="createDate" name="createDate" value="${params.createDate}"/>
            </div>

            <div class="field">
                <label for="sentDate">Sent Date</label>
                <ui:datepicker id="sentDate" name="sentDate" value="${params.sentDate}"/>
            </div>
        </div>--}%

        <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}" class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
            <input type="submit" class="ui primary button" value="${message(code: 'package.compare.filter.submit.label')}"/>
        </div>

    </g:form>
</ui:filter>

<table class="ui sortable celled la-js-responsive-table la-hover-table la-table compact table">
            <thead>
            <tr>
                <g:sortableColumn property="id" title="Id" params="${params}" />
                <g:sortableColumn property="subject" title="Subject"  params="${params}" />
                <g:sortableColumn property="to" title="To"  params="${params}" />
                <g:sortableColumn property="createDate" title="Create Date"  params="${params}" />
                <g:sortableColumn property="status" title="Status"  params="${params}" />
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${resultList}" status="i" var="message">
                <tr>
                    <td>${message.id}</td>
                    <td><g:link action="show" id="${message.id}">${fieldValue(bean: message, field: 'subject')}</g:link></td>
                    <td><g:render template="listAddr" bean="${message.to}"/></td>
                    <td><g:formatDate date="${message.createDate}" format="yyyy-MM-dd HH:mm:ss"/></td>
                    <td>${fieldValue(bean: message, field: 'status')}</td>
                    <td>
                        <a class="${Btn.SIMPLE}" href="mailto:?body=${message.text?.encodeAsHTML()}"><g:message code="default.show.label" args="['Mail']"/></a>
                        <g:if test="${message.abortable}">
                            <g:link class="${Btn.SIMPLE}" action="abort" id="${message.id}"
                                    onclick="return confirm('Are you sure?');">abort</g:link>
                        </g:if>
                        <br>
                        <br>
                        <g:link class="${Btn.SIMPLE}" action="delete" id="${message.id}"
                                onclick="return confirm('Are you sure?');">Delete</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

<g:if test="${resultList}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${resultCount}" />
</g:if>

<laser:htmlEnd/>