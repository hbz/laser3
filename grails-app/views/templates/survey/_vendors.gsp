<%@ page import="de.laser.ui.Btn" %>

<ui:filter>
    <g:form controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
            params="${params}" method="get" class="ui form">
        <g:if test="${params.viewTab}">
            <g:hiddenField name="viewTab" value="${params.viewTab}"/>
        </g:if>
        <g:if test="${participant}">
            <g:hiddenField name="participant" value="${participant.id}"/>
        </g:if>
        <g:if test="${params.subTab}">
            <g:hiddenField name="subTab" value="${params.subTab}"/>
        </g:if>
        <laser:render template="/templates/filter/vendorFilter"
                      model="[
                              tmplConfigShow: tmplConfigShowFilter,
                              tmplConfigFormFilter: true,
                              showAllIsMyXOptions: true
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
                <button name="processOption" value="unlinkVendors" type="submit" class="${Btn.NEGATIVE_CLICKCONTROL}">${message(code: 'surveyVendors.unlinkVendor.plural')}</button>
            </div>
        </g:if>
        <g:if test="${processController && processAction && tmplConfigShow.contains('linkSurveyVendor')}">
            <div class="field">
                <button name="processOption" value="linkVendors" type="submit" class="${Btn.SIMPLE_CLICKCONTROL}">${message(code: 'surveyVendors.linkVendor.plural')}</button>
            </div>
        </g:if>

    </g:form>
    <ui:paginate controller="${controllerName}" action="${actionName}"
                 id="${surveyInfo.id}"
                 params="${params}"
                 max="${max}" total="${vendorListTotal}"/>
</g:if>
<g:elseif test="${initial && actionName == 'surveyVendors'}">
    <br/>
    <strong>
        <g:message code="surveyVendors.addVendorsOverPencil"/>
    </strong>
</g:elseif>
<g:else>
    <g:if test="${filterSet}">
        <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"vendor.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br /><strong><g:message code="result.empty.object" args="${[message(code:"vendor.plural")]}"/></strong>
    </g:else>
</g:else>