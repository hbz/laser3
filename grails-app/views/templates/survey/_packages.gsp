<%@ page import="de.laser.ui.Btn" %>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter" model="[filterConfig: [
            ['q', 'pkgStatus'],
            ['provider', 'ddc', 'curatoryGroup'],
            ['curatoryGroupType', 'automaticUpdates']
    ]]"/>
</g:if>

<g:if test="${records}">
    <g:form controller="${processController}" action="${processAction}"
            id="${surveyInfo.id}"
            params="${params}" method="post" class="ui form linkSurveyPackage">
        <laser:render template="/templates/filter/packageGokbFilterTable"
                      model="[tmplShowCheckbox: tmplShowCheckbox,
                              tmplConfigShow  : tmplConfigShow,
                      ]"/>

        <g:if test="${processController && processAction && tmplConfigShow.contains('unLinkSurveyPackage')}">
            <div class="field">
                <button name="processOption" value="unlinkPackages" type="submit" class="${Btn.NEGATIVE_CLICKCONTROL}">${message(code: 'surveyPackages.unlinkPackage.plural')}</button>
            </div>
        </g:if>
        <g:if test="${processController && processAction && tmplConfigShow.contains('linkSurveyPackage')}">
            <div class="field">
                <button name="processOption" value="linkPackages" type="submit" class="${Btn.SIMPLE_CLICKCONTROL}">${message(code: 'surveyPackages.linkPackage.plural')}</button>
            </div>
        </g:if>




    </g:form>
    <ui:paginate controller="${controllerName}" action="${actionName}"
                 id="${surveyInfo.id}"
                 params="${params}"
                 max="${max}" total="${recordsCount}"/>
</g:if>
<g:elseif test="${initial && actionName == 'surveyPackages'}">
    <br/>
    <strong>
        <g:message code="surveyPackages.addPackagesOverPencil"/>
    </strong>
</g:elseif>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:else>
</g:else>