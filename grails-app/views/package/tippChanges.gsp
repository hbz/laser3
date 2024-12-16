<%@ page import="de.laser.remote.Wekb; de.laser.wekb.TitleInstancePackagePlatform; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange; de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.RefdataValue;" %>
<laser:htmlStart message="myinst.menu.pendingChanges.label" />

<ui:breadcrumbs>
    <ui:crumb controller="package" action="index" text="${message(code: 'package.show.all')}"/>
    <ui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon>
    <ui:xEditable owner="${packageInstance}" field="name"/>
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyPkg: isMyPkg]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>

<g:set var="counter" value="${offset + 1}"/>
<table class="ui celled la-js-responsive-table la-table table sortable">
    <thead>
    <tr>
        <th>${message(code: 'sidewide.number')}</th>
        <th><g:message code="profile.dashboard.changes.object"/></th>
        <g:sortableColumn property="msgToken" title="${message(code: 'profile.dashboard.changes.event')}"/>
        <g:sortableColumn property="ts" title="${message(code: 'default.date.label')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${changes}" var="entry">
        <tr>
            <td>
                ${counter++}
            </td>
            <td>
                <g:set var="tipp" value="${entry.tipp}"/>
                <g:if test="${entry.tippCoverage}">
                    <g:set var="tipp" value="${entry.tippCoverage.tipp}"/>
                </g:if>
                <g:elseif test="${entry.priceItem}">
                    <g:set var="tipp" value="${entry.priceItem.tipp}"/>
                </g:elseif>
                <g:elseif test="${entry.oid}">
                    <g:set var="object" value="${genericOIDService.resolveOID(entry.oid)}"/>
                    <g:if test="${object instanceof TitleInstancePackagePlatform}">
                        <g:set var="tipp" value="${object}"/>
                    </g:if>
                </g:elseif>

                <g:if test="${tipp}">

                    ${tipp.name}

                    <div class="la-title">${message(code: 'default.details.label')}</div>

                    <g:link class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
                            data-content="${message(code: 'laser')}"
                            target="_blank"
                            controller="tipp" action="show"
                            id="${tipp.id}">
                        <i class="${Icon.TIPP}"></i>
                    </g:link>

                        <g:if test="${tipp.gokbId}">
                            <a role="button"
                               class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
                               data-content="${message(code: 'wekb')}"
                               href="${Wekb.getURL() + '/public/tippContent/?id=' + tipp.gokbId}"
                               target="_blank"><i class="${Icon.WEKB}"></i>
                            </a>
                        </g:if>
                </g:if>
            </td>
            <td>
                ${message(code: 'subscription.packages.' + entry.msgToken)}


                <g:if test="${entry.targetProperty in PendingChange.REFDATA_FIELDS}">
                    <g:set var="oldValue" value="${RefdataValue.get(entry.oldValue)?.getI10n('value')}"/>
                    <g:set var="newValue" value="${RefdataValue.get(entry.newValue)?.getI10n('value')}"/>
                </g:if>
                <g:else>
                    <g:set var="oldValue" value="${entry.oldValue}"/>
                    <g:set var="newValue" value="${entry.newValue}"/>
                </g:else>

                <g:if test="${oldValue != null || newValue != null}">
                    <i class="${Icon.TOOLTIP.HELP} la-popup-tooltip"
                       data-content="${(message(code: 'tipp.' + (entry.priceItem ? 'price.' : '') + entry.targetProperty) ?: '') + ': ' + message(code: 'pendingChange.change', args: [oldValue, newValue])}"></i>
                </g:if>
                <g:elseif test="${entry.targetProperty}">
                    <i class="${Icon.TOOLTIP.HELP} la-popup-tooltip"
                       data-content="${message(code: 'tipp.' + (entry.priceItem ? 'price.' : '') + entry.targetProperty)}"></i>
                </g:elseif>

            </td>
            <td>
                <g:formatDate format="${message(code: 'default.date.format.noZ')}" date="${entry.ts}"/>
            </td>
        </tr>

    </g:each>
    </tbody>
</table>

<ui:paginate offset="${offset}" max="${max}" total="${num_change_rows}" params="${params}"/>

<laser:htmlEnd />
