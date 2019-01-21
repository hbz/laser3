<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : System Events</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="System Events" class="active"/>
</semui:breadcrumbs>

<div>
    <table class="ui sortable celled la-table la-table-small table">
        <thead>
        <tr>
            <g:sortableColumn property="category" title="Category"/>
            <g:sortableColumn property="relevance" title="Relevance"/>
            <th>Source</th>
            <th>Event</th>
            <th>Message</th>
            <th>Payload</th>
            <g:sortableColumn property="created" title="Date"/>
        </tr>
        </thead>
        <g:each in="${events}" var="el">
            <tr>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.category}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.relevance}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.source}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.event}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.descr}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.payload}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    <g:formatDate date="${el.created}" format="${message(code:'default.date.format.noZ')}" />
                </td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
