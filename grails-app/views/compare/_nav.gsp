<laser:serviceInjection/>

<div class="ui grid">
    <div class="sixteen wide stretched column">
        <g:if test="${actionName == 'compareSubscriptions'}">
            <div class="ui three item menu">

                <g:link class="item ${params.tab == 'compareElements' ? 'active' : ''}"
                        controller="compare" action="${actionName}"
                        params="${[tab: 'compareElements', selectedObjects: objects.collect{it.id}]}">
                    ${message(code: 'default.compare.compareElements')}
                </g:link>

                <g:link class="item ${params.tab == 'compareEntitlements' ? 'active' : ''}"
                        controller="compare" action="${actionName}"
                        params="${[tab: 'compareEntitlements', selectedObjects: objects.collect{it.id}]}">
                    ${message(code: 'default.compare', args: [message(code: 'subscription.entitlement.plural')])}

                </g:link>

                <g:link class="item ${params.tab == 'compareProperties' ? 'active' : ''}"
                        controller="compare" action="${actionName}"
                        params="${[tab: 'compareProperties', selectedObjects: objects.collect{it.id}]}">
                    ${message(code: 'default.compare', args: [message(code: 'propertyDefinition.label')])}

                </g:link>

            </div>
        </g:if><g:else>
        <div class="ui two item menu">

            <g:link class="item ${params.tab == 'compareElements' ? 'active' : ''}"
                    controller="compare" action="${actionName}"
                    params="${[tab: 'compareElements', selectedObjects: objects.collect{it.id}]}">
                ${message(code: 'default.compare.compareElements')}
            </g:link>

            <g:link class="item ${params.tab == 'compareProperties' ? 'active' : ''}"
                    controller="compare" action="${actionName}"
                    params="${[tab: 'compareProperties', selectedObjects: objects.collect{it.id}]}">
                ${message(code: 'default.compare', args: [message(code: 'propertyDefinition.label')])}

            </g:link>

        </div>
        </g:else>


    </div>
</div>
