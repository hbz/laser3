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
    <table class="ui sortable celled la-table table">
        <thead>
        <tr>
            <g:sortableColumn property="category" title="Category"/>
            <g:sortableColumn property="source" title="Source"/>
            <g:sortableColumn property="event" title="Event"/>
            <g:sortableColumn property="message" title="Message"/>
            <g:sortableColumn property="payload" title="Payload"/>
            <g:sortableColumn property="relevance" title="Relevance"/>
            <g:sortableColumn property="created" title="Date"/>
        </tr>
        </thead>
        <g:each in="${events}" var="el">
            <tr>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.category}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.source}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.event}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.message}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.payload}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.relevance}
                </td>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    <g:formatDate date="${el.created}" format="yyyy-MM-dd hh:mm" />
                </td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
