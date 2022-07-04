<%@ page import="de.laser.system.SystemEvent;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.systemEvents')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.systemEvents" class="active"/>
</semui:breadcrumbs>

    <semui:headerWithIcon message="menu.admin.systemEvents" />

    <semui:filter>
        <form id="filter" class="ui form">
            <div class="five fields">
                <div class="field">
                    <label>${message(code:'default.category.label')}</label>
                    <g:select name="filter_category" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.CATEGORY.values()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_category}"
                    />
                </div>
                <div class="field">
                    <label>${message(code:'default.relevance.label')}</label>
                    <g:select name="filter_relevance" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.RELEVANCE.values()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_relevance}"
                    />
                </div>
                <div class="field">
                    <label>${message(code:'default.source.label')}</label>
                    <g:select name="filter_source" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.getAllSources( events )}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_source}"
                    />
                </div>
                <div class="field">
                    <label>Exclude <sup>!</sup></label>
                    <g:select name="filter_exclude" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.getAllSources( events )}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_exclude}"
                    />
                </div>
                <div class="field">
                    <label>Limit (Page Reload)</label>
                    <g:select name="filter_limit" class="ui selection dropdown reloadFilter la-not-clearable"
                              from="${[100, 500, 1000, 2000, 3000, 5000, 10000]}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_limit}"
                    />
                </div>
            </div>
        </form>
        <laser:script file="${this.getGroovyPageFileName()}">

            liveFilterFunction = function() {
                var fCat = $('#filter_category').val()
                var fRel = $('#filter_relevance').val()
                var fSrc = $('#filter_source').val()
                var fExc = $('#filter_exclude').val()

                var selector = ''

                if (fCat) { selector += "[data-category='" + fCat + "']" }
                if (fRel) { selector += "[data-relevance='" + fRel + "']" }
                if (fSrc) { selector += "[data-source='" + fSrc + "']" }
                if (fExc) { selector += "[data-source!='" + fExc + "']" }

                $('table tbody tr').addClass('hidden')
                $('table tbody tr' + selector).removeClass('hidden')
            }
            liveFilterFunction();

            $('.liveFilter').on('change', liveFilterFunction)

            $('.reloadFilter').on('change', function() {
                if ($('#filter_limit').val() != $('#filter_limit').dropdown('get default value')) {
                    window.location.href = '<g:createLink controller="admin" action="systemEvents" />?' + $('#filter').serialize()
                }
            })
        </laser:script>
    </semui:filter>

<div>
    <table class="ui sortable celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
        <tr>
            <th>${message(code:'default.number')}</th>
            <th>${message(code:'default.category.label')}</th>
            <th>${message(code:'default.relevance.label')}</th>
            <th>${message(code:'default.source.label')}</th>
            <th>${message(code:'default.event.label')}</th>
            <%--<th>Message</th>--%>
            <th>Payload</th>
            <th>${message(code:'default.date.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${events}" var="el" status="i">
            <%
                String tdClass = 'table-td-yoda-blue'
                switch (el.relevance?.value?.toLowerCase()) {
                    case 'info' :
                        tdClass = 'table-td-yoda-blank'; break
                    case 'ok' :
                        tdClass = 'table-td-yoda-green'; break
                    case 'warning' :
                        tdClass = 'table-td-yoda-yellow'; break
                    case 'error' :
                        tdClass = 'table-td-yoda-red'; break
                }
            %>
            <tr
                    data-category="${el.category}"
                    data-relevance="${el.relevance}"
                    data-source="${el.source}"
                    class="hidden"
            >
                <td class="${tdClass}">
                    ${i+1}.
                </td>
                <td class="${tdClass}">
                    ${el.category}
                </td>
                <td class="${tdClass}">
                    ${el.relevance}
                </td>
                <td class="${tdClass}">
                    ${el.source}
                </td>
                <td class="${tdClass}">
                    ${el.event}
                </td>
                <%--<td class="${tdClass}">
                    ${el.descr}
                </td>--%>
                <td class="${tdClass}">
                    ${el.payload?.replaceAll(',', ', ')}
                </td>
                <td class="${tdClass}">
                    <g:formatDate date="${el.created}" format="${message(code:'default.date.format.noZ')}" />
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</body>
</html>
