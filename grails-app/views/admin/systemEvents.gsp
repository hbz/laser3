<%@ page import="de.laser.system.SystemEvent;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.systemEvents')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.systemEvents" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">${message(code:'menu.admin.systemEvents')}</h1>

    <semui:filter>
        <form class="ui form">
            <div class="five fields">
                <div class="field">
                    <label>Category</label>
                    <g:select name="filter_category" class="ui dropdown"
                              from="${SystemEvent.CATEGORY.values()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value=""
                    />
                </div>
                <div class="field">
                    <label>Relevance</label>
                    <g:select name="filter_relevance" class="ui dropdown"
                              from="${SystemEvent.RELEVANCE.values()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value=""
                    />
                </div>
                <div class="field">
                    <label>Source</label>
                    <g:select name="filter_source" class="ui dropdown"
                              from="${SystemEvent.getAllSources()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value=""
                    />
                </div>
                <div class="field">
                    <label>Exclude <sup>!</sup></label>
                    <g:select name="filter_exclude" class="ui dropdown"
                              from="${SystemEvent.getAllSources()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="DataloadService"
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
        <laser:script file="${this.getGroovyPageFileName()}">
            $('#filterButton').on('click', function() {

                var fCat = $('#filter_category').val()
                var fRel = $('#filter_relevance').val()
                var fSrc = $('#filter_source').val()
                var fExc = $('#filter_exclude').val()
                var selector = ''

                if (fCat) { selector += "[data-category='" + fCat + "']" }
                if (fRel) { selector += "[data-relevance='" + fRel + "']" }
                if (fSrc) { selector += "[data-source='" + fSrc + "']" }
                if (fExc) { selector += "[data-source!='" + fExc + "']" }

                var $valid = $('table tbody tr' + selector)

                $('table tbody tr').addClass('hidden')
                $valid.removeClass('hidden')
            })

            $('#filterButton').trigger('click')
        </laser:script>
    </semui:filter>

<div>
    <table class="ui sortable celled la-table compact table">
        <thead>
        <tr>
            <g:sortableColumn property="category" title="Category"/>
            <g:sortableColumn property="relevance" title="Relevance"/>
            <th>Source</th>
            <th>Event</th>
            <%--<th>Message</th>--%>
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
                <%--<td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.descr}
                </td>--%>
                <td class="table-td-${el.relevance?.value?.toLowerCase()}">
                    ${el.payload?.replaceAll(',', ', ')}
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
