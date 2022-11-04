<%@ page import="de.laser.system.SystemEvent;" %>

<laser:htmlStart message="menu.admin.systemEvents" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.systemEvents" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.systemEvents" total="${SystemEvent.count()}"/>

    <ui:filter simple="true">
        <form id="filter" class="ui form">
            <div class="five fields">
                <div class="field">
                    <label for="filter_category">${message(code:'default.category.label')}</label>
                    <g:select name="filter_category" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.CATEGORY.values()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_category}"
                    />
                </div>
                <div class="field">
                    <label for="filter_relevance">${message(code:'default.relevance.label')}</label>
                    <g:select name="filter_relevance" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.RELEVANCE.values()}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_relevance}"
                    />
                </div>
                <div class="field">
                    <label for="filter_source">${message(code:'default.source.label')}</label>
                    <g:select name="filter_source" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.getAllSources( events )}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_source}"
                    />
                </div>
                <div class="field">
                    <label for="filter_exclude">Exclude <sup>!</sup></label>
                    <g:select name="filter_exclude" class="ui selection dropdown liveFilter"
                              from="${SystemEvent.getAllSources( events )}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}" value="${filter_exclude}"
                    />
                </div>
                <div class="field">
                    <label for="filter_limit">${message(code:'default.period.label')} (Seite lädt neu)</label>
                    <g:select name="filter_limit" class="ui selection dropdown reloadFilter la-not-clearable"
                              from="${[7, 14, 30, 90, 180]}"
                              optionValue="${{it.toString() + ' ' + message(code:'default.day.plural')}}"
                              optionKey="${{it}}"
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
    </ui:filter>

    <table class="ui sortable celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
        <tr>
            <th scope="col" class="one wide">${message(code:'default.number')}</th>
            <th scope="col" class="two wide">${message(code:'default.category.label')}</th>
            <th scope="col" class="two wide">${message(code:'default.relevance.label')}</th>
            <th scope="col" class="two wide">${message(code:'default.source.label')}</th>
            <th scope="col" class="three wide">${message(code:'default.event.label')}</th>
            <th scope="col" class="four wide">Payload</th>
            <th scope="col" class="two wide">${message(code:'default.date.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${events}" var="el" status="i">
            <%
                String tdClass = 'table-td-yoda-blue'
                switch (el.relevance?.value?.toLowerCase()) {
                    case 'info'     : tdClass = ''; break
                    case 'ok'       : tdClass = 'positive'; break
                    case 'warning'  : tdClass = 'warning'; break
                    case 'error'    : tdClass = 'error'; break
                }
                if (el.hasChanged) {
                    tdClass += ' sf_underline'
                }
            %>
            <tr
                    data-category="${el.category}"
                    data-relevance="${el.relevance}"
                    data-source="${el.source}"
                    class="hidden"
            >
                <td class="${tdClass}">
                    ${i+1}
                </td>
                <td class="${tdClass}">
                    ${el.category}
                </td>
                <td class="${tdClass}">
                    ${el.relevance}
                </td>
                <td class="${tdClass}">
                    ${el.getSource()}
                </td>
                <td class="${tdClass}">
                    ${el.getEvent()}
                    <g:if test="${el.getEvent() != el.getDescr()}">
                        : ${el.getDescr()}
                    </g:if>
                </td>
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

<laser:htmlEnd />
