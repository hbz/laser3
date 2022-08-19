<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.OrgRole; de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition" %>

<laser:htmlStart message="gasco.title" />

    <br />
    <br />

    <h2 class="ui title">
        ${subscription}
        <g:if test="${tippsCount}">
            &nbsp;&nbsp;
            (${tipps?.size()} von ${tippsCount})
        </g:if>
    </h2>

    <ui:filter>
        <form class="ui form">
            <div class="fields">

                <div class="field">
                    <label>Suche nach Name</label>
                    <input type="text" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
                </div>

                <div class="field">
                    <label>Suche nach Identifikatoren</label>
                    <g:select name="idns"
                              from="${idnsPreset}" optionKey="id" optionValue="ns"
                              value="${params.idns}"
                              class="ui dropdown"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <div class="field">
                    <label>&nbsp;</label>
                    <input type="text" name="idv" placeholder="${message(code:'default.search.ph')}" value="${params.idv}" />
                </div>

                <div class="field">
                    <label>&nbsp;</label>
                    <a href="${request.forwardURI}" class="ui reset primary primary button">${message(code:'default.button.reset.label')}</a>

                    <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
                </div>

            </div>
        </form>
    </ui:filter>

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'issueEntitlement.label')}</th>
            <th>${message(code:'default.identifiers.label')}</th>
        </tr>
        </thead>
        <tbody>

            <g:each in="${tipps}" var="tipp" status="counter">
                <tr>
                    <td>${counter + 1}</td>
                    <td>
                        <ui:listIcon type="${tipp.medium?.value}"/>
                        <strong>${tipp.name}</strong>
                        <br />

                        <g:if test="${tipp.hostPlatformURL}">
                            <ui:linkWithIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
                        </g:if>
                        <br />

                        ${message(code:'tipp.platform')}:
                        <g:if test="${tipp.platform.name}">
                            ${tipp.platform.name}
                        </g:if>
                        <g:else>
                            ${message(code:'default.unknown')}
                        </g:else>
                        <br />

                        ${message(code:'package.label')}:
                        <g:if test="${tipp.pkg}"><!-- TODO: show all packages -->
                            ${tipp.pkg}
                        </g:if>
                        <g:else>
                            ${message(code:'default.unknown')}
                        </g:else>
                        <br />
                    </td>

                    <td>
                        <g:each in="${tipp.ids?.sort{it?.ns?.ns}}" var="title_id">
                            ${title_id.ns.ns}: <strong>${title_id.value}</strong>
                            <br />
                        </g:each>
                    </td>
                </tr>
            </g:each>

        </tbody>
    </table>

<style>
.ui.table thead tr:first-child>th {
    top: 48px!important;
}
</style>
<sec:ifAnyGranted roles="ROLE_USER">
    <style>
    .ui.table thead tr:first-child>th {
        top: 90px!important;
    }
    </style>
</sec:ifAnyGranted>

<laser:htmlEnd />