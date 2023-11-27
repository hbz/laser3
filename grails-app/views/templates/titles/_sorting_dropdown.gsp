
### sorting_dropdown ###

<%
    Map<String, String> sd_sortFieldMap = [:]
%>

<g:if test="${sd_type == 1}">
    <%
        sd_sortFieldMap['tipp.sortname']    = message(code: 'title.label')

        if (sd_journalsOnly) {
            sd_sortFieldMap['startDate']    = message(code: 'default.from')
            sd_sortFieldMap['endDate']      = message(code: 'default.to')
        }
        else {
            sd_sortFieldMap['tipp.dateFirstInPrint']    = message(code: 'tipp.dateFirstInPrint')
            sd_sortFieldMap['tipp.dateFirstOnline']     = message(code: 'tipp.dateFirstOnline')
        }
        sd_sortFieldMap['tipp.accessStartDate']     = "${message(code: 'subscription.details.access_dates')} ${message(code: 'default.from')}"
        sd_sortFieldMap['tipp.accessEndDate']       = "${message(code: 'subscription.details.access_dates')} ${message(code: 'default.to')}"
    %>
</g:if>
<g:elseif test="${sd_type == 2}">
    <%
        sd_sortFieldMap['sortname']         = message(code: 'title.label')

        if (sd_journalsOnly) {
            sd_sortFieldMap['startDate']    = message(code: 'default.from')
            sd_sortFieldMap['endDate']      = message(code: 'default.to')
        }
        else {
            sd_sortFieldMap['dateFirstInPrint'] = message(code: 'tipp.dateFirstInPrint')
            sd_sortFieldMap['dateFirstOnline']  = message(code: 'tipp.dateFirstOnline')
        }
    %>
</g:elseif>

<br/> sd_type: ${sd_type}
<br/> sd_journalsOnly: ${sd_journalsOnly}
<br/> sd_sort: ${sd_sort}
<br/> sd_order: ${sd_order}
<br/> sd_sortFieldMap: ${sd_sortFieldMap}

<ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sd_sortFieldMap}" sort="${sd_sort}" order="${sd_order}"/>
