
<semui:form>

    <g:if test="${groupedProperties?.size() > 0}">
        <g:each in="${groupedProperties}" var="groupedProps">
            <g:if test="${groupedProps.getValue()}">

                <h2 class="ui header">
                    ${message(code: 'subscription.properties.public')}
                    (${groupedProps.getKey().name})

                </h2>

                <table class="ui selectable celled table la-js-responsive-table la-table la-ignore-fixed">
                    <laser:render template="comparisonPropertiesTable"
                              model="[group: groupedProps.getValue().groupTree, key: groupedProps.getKey().name, propBinding: groupedProps.getValue().binding]"/>
                </table>

                <div class="ui divider"></div>
                <br />
            </g:if>
        </g:each>
    </g:if>

    <g:if test="${orphanedProperties?.size() > 0}">

        <div class="content">
            <h2 class="ui header">
                <g:if test="${groupedProperties?.size() > 0}">
                    ${message(code: 'subscription.properties.orphaned')}
                </g:if>
                <g:else>
                    ${message(code: 'subscription.properties')}
                </g:else>
            </h2>

            <table class="ui selectable celled table la-js-responsive-table la-table">
                <laser:render template="comparisonPropertiesTable"
                          model="[group: orphanedProperties, key: message(code: 'subscription.properties')]"/>
            </table>

        </div>

        <div class="ui divider"></div>
        <br />
    </g:if>
    <g:if test="${privateProperties?.size() > 0}">

        <div class="content">
            <h5 class="ui header">${message(code: 'subscription.properties.private')} ${contextOrg.name}</h5>
            <table class="ui selectable celled table la-js-responsive-table la-table">
                <laser:render template="comparisonPropertiesTable"
                          model="[group: privateProperties, key: message(code: 'subscription.properties.private') + ' ' + contextOrg.name]"/>
            </table>
        </div>

    </g:if>

    <g:if test="${!orphanedProperties && !privateProperties && !groupedProperties}">
        <strong>${message(code: 'default.compare.noProperties')}</strong>
        <br /><br />
    </g:if>

</semui:form>

