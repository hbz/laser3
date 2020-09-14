<%@ page import="com.k_int.kbplus.GenericOIDService;" %>
<laser:serviceInjection/>

<div class="ui grid">
    <div class="sixteen wide stretched column">
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

    </div>
</div>
