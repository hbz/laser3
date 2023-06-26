<%@ page import="de.laser.titles.BookInstance; de.laser.remote.ApiSource; de.laser.Subscription; de.laser.Package; de.laser.RefdataCategory; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyShow.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
    <ui:xEditable owner="${surveyInfo}" field="name"/>
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<div class="sixteen wide column">

    <div class="row">
        <div class="column">

            <g:if test="${titlesList && titlesList.size() > 0}">

                <g:if test="${subscription.packages.size() > 1}">
                    <a class="ui right floated button" data-href="#showPackagesModal" data-ui="modal"><g:message
                            code="subscription.details.details.package.label"/></a>
                </g:if>

                <g:if test="${subscription.packages.size() == 1}">
                    <g:link class="ui right floated button" controller="package" action="show"
                            id="${subscription.packages[0].pkg.id}"><g:message
                            code="subscription.details.details.package.label"/></g:link>
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
            <div class="ui icon positive message">
                <i class="info icon"></i>

                <div class="content">
                    <div class="header"></div>

                    <p>
                        <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                        <g:message code="showSurveyInfo.pickAndChoose.Package"/>
                    </p>
                    <br/>
                    <g:link controller="subscription" class="ui button" action="index" target="_blank"
                            id="${surveyConfig.subscription.id}">
                        ${surveyConfig.subscription.name} (${surveyConfig.subscription.status.getI10n('value')})
                    </g:link>

                    <g:link controller="subscription" class="ui button" action="linkPackage" target="_blank"
                            id="${surveyConfig.subscription.id}">
                        <g:message code="subscription.details.linkPackage.label"/>
                    </g:link>

                </div>

            </div>
        </g:if>
    </div>



            <div class="row">
                <div class="column">
                    <laser:render template="/templates/filter/tipp_ieFilter"/>
                </div>
            </div>

            <div class="row">
                <div class="eight wide column">
                    <h3 class="ui icon header la-clear-before la-noMargin-top"><span
                            class="ui circular  label">${num_tipp_rows}</span> <g:message code="title.filter.result"/></h3>
                </div>

            </div>
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
        <div class="ui form">
            <div class="three wide fields">
                <div class="field">
                    <ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
                </div>
            </div>
        </div>
        <div class="ui grid">
            <div class="row">
                <div class="column">
                    <laser:render template="/templates/tipps/table_accordion"
                                  model="[tipps: titlesList, showPackage: false, showPlattform: true]"/>
                </div>
            </div>
        </div>

        <g:if test="${titlesList}">
            <ui:paginate action="current" controller="package" params="${params}"
                         max="${max}" total="${num_tipp_rows}"/>
        </g:if>


        <div id="magicArea"></div>

<ui:modal id="showPackagesModal" message="subscription.packages.label" hideSubmitButton="true">
    <div class="ui ordered list">
        <g:each in="${subscription.packages.sort { it.pkg.name.toLowerCase() }}" var="subPkg">
            <div class="item">
                ${subPkg.pkg.name}
                <g:if test="${subPkg.pkg.contentProvider}">
                    (${subPkg.pkg.contentProvider.name})
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
