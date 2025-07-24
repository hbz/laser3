<%@ page import="de.laser.ui.Btn; de.laser.IssueEntitlement; de.laser.wekb.TitleInstancePackagePlatform;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition" %>

<laser:htmlStart message="menu.public.gasco_monitor" description="${message(code:'metaDescription.gasco')}"/>

    <g:render template="/public/gasco/nav" />

    <ui:h1HeaderWithIcon text="${message(code: 'menu.public.gasco_monitor')}: ${subscription}" type="gasco" total="${issueEntitlementsCount}"/>

    <ui:filter simple="true">
        <form class="ui form">
            <div class="fields">

                <div class="field">
                    <label for="q">Suche nach Titel</label>
                    <input type="text" id="q" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
                </div>

                <div class="field">
                    <label for="idns">Identifikator-Typ</label>
                    <g:select id="idns" name="idns"
                              from="${idnsPreset}" optionKey="id" optionValue="ns"
                              value="${params.idns}"
                              class="ui dropdown clearable "
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <div class="field">
                    <label for="idv">Identifikator</label>
                    <input type="text" id="idv" name="idv" placeholder="Identifikator eingeben" value="${params.idv}" />
                </div>

                <div class="field">
                    <label>&nbsp;</label>
                    <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>

                    <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}" />
                </div>

            </div>
        </form>
    </ui:filter>

    <h3 class="ui icon header la-clear-before la-noMargin-top">
        <span class="ui circular label">${issueEntitlementsFilterCount}</span> <g:message code="title.filter.result"/>
    </h3>

    <div class="ui grid">
        <div class="row">
            <div class="column">
                <laser:render template="/templates/tipps/table_accordion"
                              model="[tipps: issueEntitlements?.tipp, showPackage: false, showPlattform: true]"/>
            </div>
        </div>
    </div>

<g:if test="${issueEntitlements}">
    <ui:paginate action="gascoDetails" controller="public" params="${params}" max="${max}" total="${issueEntitlementsCount}"/>
</g:if>

<laser:htmlEnd />