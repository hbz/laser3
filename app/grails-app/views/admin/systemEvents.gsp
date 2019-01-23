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


    <semui:filter>
        <form class="ui form">
            <div class="four fields">
                <div class="field">
                    <label>Category</label>
                    <g:select name="filter_category" class="ui dropdown"
                              from="${de.laser.SystemEvent.CATEGORY.values()}"
                              noSelection="['':'']" value=""
                    />
                </div>
                <div class="field">
                    <label>Relevance</label>
                    <g:select name="filter_relevance" class="ui dropdown"
                              from="${de.laser.SystemEvent.RELEVANCE.values()}"
                              noSelection="['':'']" value=""
                    />
                </div>
                <div class="field">
                    <label>Source</label>
                    <g:select name="filter_source" class="ui dropdown"
                              from="${de.laser.SystemEvent.getAllSources()}"
                              noSelection="['':'']" value=""
                    />
                </div>
                <div class="field">
                    <div class="field la-field-right-aligned">
                        <%--<a href="" class="ui reset primary button">Zurücksetzen</a>--%>
                        <input type="button" id="filterButton" class="ui secondary button" value="Filtern">
                    </div>
                </div>
            </div>
        </form>
        <r:script>
            $('#filterButton').on('click', function() {

                var fCat = $('#filter_category').val()
                var fRel = $('#filter_relevance').val()
                var fSrc = $('#filter_source').val()
                var selector = ''

                if (fCat) { selector += "[data-category='" + fCat + "']" }
                if (fRel) { selector += "[data-relevance='" + fRel + "']" }
                if (fSrc) { selector += "[data-source='" + fSrc + "']" }

                var $valid = $('table tbody tr' + selector)

                $('table tbody tr').addClass('hidden')
                $valid.removeClass('hidden')
            })
        </r:script>
    </semui:filter>

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
        <tbody>
        <g:each in="${events}" var="el">
            <tr
                    data-category="${el.category}"
                    data-relevance="${el.relevance}"
                    data-source="${el.source}"
            >
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
        </tbody>
    </table>
</div>
</body>
</html>
