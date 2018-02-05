<semui:breadcrumbs>
    <g:if test="${params.shortcode}">
        <% def fallbackInst = com.k_int.kbplus.Org.findByShortcode(params.shortcode) %>
        <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${fallbackInst.getDesignation()}" />
        <semui:crumb controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" text="${message(code:'myinst.currentSubscriptions.label')}" />
    </g:if>
    <g:if test="${subscriptionInstance}">
        <semui:crumb class="active" id="${subscriptionInstance.id}" text="${subscriptionInstance.name}" />
    </g:if>

    <g:if test="${actionName == 'index'}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" params="${params + [format:'json']}">JSON</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" params="${params + [format:'xml']}">XML</g:link>
            </semui:exportDropdownItem>
            <g:each in="${transforms}" var="transkey,transval">
                <semui:exportDropdownItem>
                    <g:link action="index" id="${params.id}" params="${[format:'xml', transformId:transkey, mode: params.mode]}">${transval.name}</g:link>
                </semui:exportDropdownItem>
            </g:each>
        </semui:exportDropdown>
    </g:if>
    <g:if test="${actionName == 'compare'}">
        <semui:crumb class="active" message="subscription.compare.label" />

        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
    </g:if>

    <g:if test="${subscriptionInstance}">
        <li class="pull-right"><g:annotatedLabel owner="${subscriptionInstance}" property="detailsPageInfo"></g:annotatedLabel>&nbsp;</li>
    </g:if>
</semui:breadcrumbs>