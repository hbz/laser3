<%@ page import="de.laser.wekb.TitleInstancePackagePlatform" %>
<laser:serviceInjection/>
<ui:greySegment>
    <table class="ui selectable celled table la-js-responsive-table la-table la-ignore-fixed">
        <thead>
            <tr>
                <th rowspan="2">${message(code: 'default.title.label')}</th>
                <g:each in="${objects}" var="object">
                    <th colspan="4">
                        <g:if test="${object}"><g:link
                                controller="${object.getClass().getSimpleName().toLowerCase()}" action="show"
                                id="${object.id}">${object.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                </g:each>
            </tr>
            <tr>
                <g:each in="${objects}" var="object">
                    <th>${message(code: 'default.date.label')}</th>
                    <th>${message(code: 'subscription.details.access_dates')}</th>
                    <th>${message(code: 'tipp.price')}</th>
                    <th>${message(code: 'issueEntitlement.perpetualAccessBySub.label')}</th>
                </g:each>
            </tr>
        </thead>
        <tbody id="entitlements">
            <laser:render template="compareEntitlementRow" model="[showPlattform: showPlattform, showPackage: showPackage, ies: ies, objects: objects]"/>
        </tbody>
    </table>
</ui:greySegment>
<laser:script file="${this.getGroovyPageFileName()}">
    let max = ${max};
    let offset = ${offset+max};
    let lock = false;
    $(window).scroll(function() {
        if($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
            if(!lock) {
                lock = true;
                $.ajax({
                    url: '<g:createLink controller="compare" action="loadNextBatch" />',
                    data: {
                        selectedObjects: ${params.selectedObjects.toList()},
                        max: max,
                        offset: offset
                    },
                    success: function (data) {
                        offset += max;
                        $("#entitlements:last-child").append(data);
                        lock = false;
                    }
                });
            }
        }
    });
</laser:script>