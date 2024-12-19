<%@ page import="de.laser.ui.Btn" %>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter" model="[tmplConfigShow: [
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
                <input type="submit" class="${Btn.NEGATIVE_CLICKCONTROL}" value="${message(code: 'surveyPackages.unlinkPackage.plural')}"/>
            </div>
        </g:if>
        <g:if test="${processController && processAction && tmplConfigShow.contains('linkSurveyPackage')}">
            <div class="field">
                <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'surveyPackages.linkPackage.plural')}"/>
            </div>
        </g:if>




    </g:form>
    <ui:paginate controller="${controllerName}" action="${actionName}"
                 id="${surveyInfo.id}"
                 params="${params}"
                 max="${max}" total="${recordsCount}"/>
</g:if>
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