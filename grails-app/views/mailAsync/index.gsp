<g:set var="entityName" value="${message(code: 'menu.yoda.mailAysnc.list')}" />
<laser:htmlStart text="${message(code:"default.list.label", args:[entityName])}" />
<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.mailAysnc.list" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.mailAysnc.list" type="yoda" />

    <g:render template="flashMessage"/>

<table class="ui sortable celled la-js-responsive-table la-hover-table la-table compact table">
            <thead>
            <tr>
                <g:sortableColumn property="id" title="Id"/>
                <g:sortableColumn property="subject" title="Subject"/>
                <g:sortableColumn property="to" title="To"/>
                <g:sortableColumn property="createDate" title="Create Date"/>
                <g:sortableColumn property="status" title="Status"/>
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
                        <g:link class="ui button" action="show" id="${message.id}"><g:message code="default.show.label" args="['Mail']"/></g:link>
                        <g:if test="${message.abortable}">
                            <g:link class="ui button" action="abort" id="${message.id}"
                                    onclick="return confirm('Are you sure?');">abort</g:link>
                        </g:if>
                        <g:link class="ui button" action="delete" id="${message.id}"
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