
<ui:filter>
    <g:form controller="${processController}" action="${processAction}" id="${surveyInfo.id}"
            params="${params}" method="get" class="ui form">
        <laser:render template="/templates/filter/vendorFilter"
                      model="[
                              tmplConfigShow: [['name', 'venStatus'], ['supportedLibrarySystems', 'electronicBillings', 'invoiceDispatchs'], ['curatoryGroup', 'curatoryGroupType']],
                              tmplConfigFormFilter: true
                      ]"/>
    </g:form>
</ui:filter>

<g:if test="${vendorList}">
    <g:form controller="${processController}" action="${processAction}"
            id="${surveyInfo.id}"
            params="${params}" method="post" class="ui form linkSurveyVendor">

        <laser:render template="/templates/filter/vendorFilterTable"
                      model="[orgList: vendorList,
                              tmplShowCheckbox: tmplShowCheckbox,
                              tmplConfigShow: tmplConfigShow
                      ]"/>


        <g:if test="${processController && processAction && tmplConfigShow.contains('unLinkSurveyVendor')}">
            <div class="field">
                <input type="submit" class="ui button negative js-click-control" value="${message(code: 'surveyVendors.unlinkVendor.plural')}"/>
            </div>
        </g:if>
        <g:if test="${processController && processAction && tmplConfigShow.contains('linkSurveyVendor')}">
            <div class="field">
                <input type="submit" class="ui button js-click-control" value="${message(code: 'surveyVendors.linkVendor.plural')}"/>
            </div>
        </g:if>




    </g:form>
    <ui:paginate controller="${controllerName}" action="${actionName}"
                 id="${surveyInfo.id}"
                 params="${params}"
                 max="${max}" total="${vendorListTotal}"/>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"vendor.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br /><strong><g:message code="result.empty.object" args="${[message(code:"vendor.plural")]}"/></strong>
    </g:else>
</g:else>