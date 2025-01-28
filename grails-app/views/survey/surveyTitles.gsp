<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Subscription; de.laser.wekb.Package; de.laser.RefdataCategory; de.laser.storage.RDStore" %>
<laser:htmlStart message="surveyShow.label" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:status object="${surveyInfo}"/>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<div class="sixteen wide column">

    <div class="row">
        <div class="column">
            <g:if test="${titlesList && titlesList.size() > 0}">
                <g:if test="${subscription.packages.size() > 1}">
                    <a class="${Btn.SIMPLE} right floated" data-href="#showPackagesModal" data-ui="modal">
                        <g:message code="subscription.details.details.package.label"/>
                    </a>
                </g:if>
                <g:if test="${subscription.packages.size() == 1}">
                    <g:link class="${Btn.SIMPLE} right floated" controller="package" action="show" id="${subscription.packages[0].pkg.id}">
                        <g:message code="subscription.details.details.package.label"/>
                    </g:link>
                </g:if>
            </g:if>
            <g:else>
                ${message(code: 'subscription.details.no_ents')}
            </g:else>
        </div>
    </div><!--.row-->

    <br>
    <br>

    <div class="la-inline-lists">
        <g:if test="${!titlesList}">
            <div class="ui icon info message">
                <i class="${Icon.UI.INFO}"></i>

                <div class="content">
                    <div class="header"></div>

                    <p>
                        <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                        <g:message code="showSurveyInfo.pickAndChoose.Package"/>
                    </p>
                    <br/>
                    <g:link controller="subscription" class="${Btn.SIMPLE}" action="index" target="_blank" id="${surveyConfig.subscription.id}">
                        ${surveyConfig.subscription.name} (${surveyConfig.subscription.status.getI10n('value')})
                    </g:link>
                    <g:link controller="subscription" class="${Btn.SIMPLE}" action="linkPackage" target="_blank" id="${surveyConfig.subscription.id}">
                        <g:message code="subscription.details.linkPackage.label"/>
                    </g:link>
                </div>

            </div>
        </g:if>
    </div>

    <laser:render template="/templates/filter/tipp_ieFilter"/>

    <h3 class="ui icon header la-clear-before la-noMargin-top">
        <ui:bubble count="${num_tipp_rows}" grey="true"/> <g:message code="title.filter.result"/>
    </h3>

        <%
            Map<String, String>
            sortFieldMap = ['tipp.sortname': message(code: 'title.label')]
            if (journalsOnly) {
                sortFieldMap['startDate'] = message(code: 'default.from')
                sortFieldMap['endDate'] = message(code: 'default.to')
            } else {
                sortFieldMap['tipp.dateFirstInPrint'] = message(code: 'tipp.dateFirstInPrint')
                sortFieldMap['tipp.dateFirstOnline'] = message(code: 'tipp.dateFirstOnline')
            }
        %>
        <g:if test="${titlesList}">
            <div class="ui form">
                <div class="two wide fields">
                    <div class="field">
                        <ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
                    </div>
                     <div class="field la-field-noLabel">
                         <ui:showMoreCloseButton />
                    </div>
                </div>
            </div>
            <div class="ui grid">
                <div class="row">
                    <div class="column">
                        <laser:render template="/templates/tipps/table_accordion" model="[tipps: titlesList, showPackage: false, showPlattform: true]"/>
                    </div>
                </div>
            </div>
            <div class="ui clearing segment la-segmentNotVisable">
                <ui:showMoreCloseButton />
            </div>

            <ui:paginate action="surveyTitles" controller="survey" params="${params}" max="${max}" total="${num_tipp_rows}"/>
        </g:if>

        <div id="magicArea"></div>

<ui:modal id="showPackagesModal" message="subscription.packages.label" hideSubmitButton="true">
    <div class="ui ordered list">
        <g:each in="${subscription.packages.sort { it.pkg.name.toLowerCase() }}" var="subPkg">
            <div class="item">
                ${subPkg.pkg.name}
                <g:if test="${subPkg.pkg.provider}">
                    (${subPkg.pkg.provider.name})
                </g:if>:
                <g:link controller="package" action="show" id="${subPkg.pkg.id}"><g:message
                        code="subscription.details.details.package.label"/></g:link>
            </div>
        </g:each>
    </div>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
</laser:script>

<laser:htmlEnd />
