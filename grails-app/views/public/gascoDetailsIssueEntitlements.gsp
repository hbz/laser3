<%@ page import="de.laser.IssueEntitlement; de.laser.TitleInstancePackagePlatform;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition" %>

<laser:htmlStart message="menu.public.gasco_monitor" />

    <ui:h1HeaderWithIcon text="${message(code: 'menu.public.gasco_monitor')}: ${subscription}" type="gasco">
        <g:if test="${issueEntitlementsCount}">
            &nbsp;&nbsp;
            (${issueEntitlements?.size()} von ${issueEntitlementsCount})
        </g:if>
    </ui:h1HeaderWithIcon>

    <ui:filter simple="true">
        <form class="ui form">
            <div class="fields">

                <div class="field">
                    <label for="q">Suche nach Name</label>
                    <input type="text" id="q" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
                </div>

                <div class="field">
                    <label for="idns">Identifikator-Typ</label>
                    <g:select id="idns" name="idns"
                              from="${idnsPreset}" optionKey="id" optionValue="ns"
                              value="${params.idns}"
                              class="ui dropdown"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <div class="field">
                    <label for="idv">Identifikator</label>
                    <input type="text" id="idv" name="idv" placeholder="Identifikator eingeben" value="${params.idv}" />
                </div>

                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>

                    <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
                </div>

            </div>
        </form>
    </ui:filter>

    <div class="ui grid">
        <div class="row">
            <div class="column">
                <laser:render template="/templates/tipps/table_accordion"
                              model="[tipps: issueEntitlements?.tipp, showPackage: false, showPlattform: true]"/>
            </div>
        </div>
    </div>

<laser:htmlEnd />