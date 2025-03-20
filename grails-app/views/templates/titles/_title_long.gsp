<%@ page import="de.laser.remote.Wekb; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>
<div class="la-icon-list">
<ui:listIcon type="${tipp.titleType}"/>
<g:if test="${ie}">
    <g:if test="${controllerName == 'issueEntitlement' && actionName == 'show'}">
        <strong>${ie.tipp.name}</strong>
    </g:if>
    <g:else>
        <g:link controller="issueEntitlement" id="${ie.id}"
                action="show"><strong>${ie.tipp.name}</strong>
        </g:link>
    </g:else>
</g:if>
<g:else>
    <g:if test="${controllerName == 'tipp' && actionName == 'show'}">
        <strong>${tipp.name}</strong>
    </g:if>
    <g:else>
        <g:link controller="tipp" id="${tipp.id}" action="show" params="${[sub: sub]}"></g:link>
    </g:else>
</g:else>

<g:if test="${tipp.hostPlatformURL}">
    <ui:linkWithIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
</g:if>
<br/>

<g:if test="${!showCompact}">
    <br/>
</g:if>

<laser:render template="/templates/identifier" model="${[tipp: tipp]}"/>

<g:if test="${!showCompact}">
    <br/>
</g:if>

<div class="item">
    <ui:listIcon type="${tipp.titleType}"/>

    <div class="content">
        ${showCompact ? '' : message(code: 'tipp.titleType') + ':'} ${tipp.titleType}
    </div>
</div>


    <g:if test="${ie && (ie.tipp.medium || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_MEDIUM} la-popup-tooltip" data-content="${message(code: 'tipp.medium')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.medium') + ':'} ${ie.tipp.medium?.getI10n('value')}
            </div>
        </div>
    </g:if>
    <g:else>
        <g:if test="${(tipp.medium || showEmptyFields)}">
            <div class="item">
                <i class="${Icon.ATTR.TIPP_MEDIUM} la-popup-tooltip" data-content="${message(code: 'tipp.medium')}"></i>

                <div class="content">
                    ${showCompact ? '' : message(code: 'tipp.medium') + ':'} ${tipp.medium?.getI10n('value')}
                </div>
            </div>
        </g:if>
    </g:else>


    <g:if test="${ie}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_STATUS} la-popup-tooltip" data-content="${message(code: 'default.status.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'default.status.label') + ':'} ${ie.status.getI10n('value')}
            </div>
        </div>
    </g:if>
    <g:else>
        <%--<g:if test="${(tipp.status || showEmptyFields)}">--%>
            <div class="item">
                <i class="${Icon.ATTR.TIPP_STATUS} la-popup-tooltip" data-content="${message(code: 'default.status.label')}"></i>

                <div class="content">
                    ${showCompact ? '' : message(code: 'default.status.label') + ':'} ${tipp.status.getI10n('value')}
                </div>
            </div>
        <%--</g:if>--%>
    </g:else>

    <g:if test="${ie}">
        <div class="item">
            <i class="grey save icon la-popup-tooltip"
               data-content="${message(code: 'issueEntitlement.perpetualAccessBySub.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}
                <%
                    if (contextService.getOrg() || surveyService.hasParticipantPerpetualAccessToTitle3(contextService.getOrg(), tipp)){
                        if (ie.perpetualAccessBySub) {
                            println g.link([action: 'index', controller: 'subscription', id: ie.perpetualAccessBySub.id], "${RDStore.YN_YES.getI10n('value')}: ${ie.perpetualAccessBySub.dropdownNamingConvention()}")
                        }else {
                            println RDStore.YN_YES.getI10n('value')
                        }
                    }
                    else {
                        println RDStore.YN_NO.getI10n('value')
                    }
                %>
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'monograph') && (tipp.volume || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_COVERAGE} la-popup-tooltip" data-content="${message(code: 'tipp.volume')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.volume') + ':'} ${tipp.volume}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'monograph') && (tipp.firstAuthor || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_FIRST_AUTHOR} la-popup-tooltip" data-content="${message(code: 'tipp.firstAuthor')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.firstAuthor') + ':'} ${tipp.firstAuthor}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'monograph') && (tipp.firstEditor || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_FIRST_EDITOR} la-popup-tooltip" data-content="${message(code: 'tipp.firstEditor')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.firstEditor') + ':'} ${tipp.firstEditor}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'monograph') && (tipp.editionStatement || showEmptyFields)}">
        <div class="item">
            <i class="grey ${Icon.CMD.COPY} la-popup-tooltip"
               data-content="${message(code: 'title.editionStatement.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'title.editionStatement.label') + ':'} ${tipp.editionStatement}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'monograph') && (tipp.editionNumber || showEmptyFields)}">
        <div class="item">
            <i class="grey ${Icon.CMD.COPY} la-popup-tooltip"
               data-content="${message(code: 'tipp.editionNumber.tooltip')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.editionNumber') + ':'} ${tipp.editionNumber}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'monograph') && (tipp.summaryOfContent || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_SUMMARY_OF_CONTENT} la-popup-tooltip" data-content="${message(code: 'title.summaryOfContent.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'title.summaryOfContent.label') + ':'} ${tipp.summaryOfContent}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.seriesName || showEmptyFields)}">
        <div class="item">
            <i class="grey icon list la-popup-tooltip"
               data-content="${message(code: 'tipp.seriesName')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.seriesName') + ':'} ${tipp.seriesName}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.subjectReference || showEmptyFields)}">
        <div class="item">
            <i class="grey icon comment alternate la-popup-tooltip"
               data-content="${message(code: 'tipp.subjectReference')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.subjectReference') + ':'} ${tipp.subjectReference}
            </div>
        </div>

    </g:if>

    <g:if test="${(tipp.delayedOA || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_ACCESS_TYPE} la-popup-tooltip"
               data-content="${message(code: 'tipp.delayedOA')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.delayedOA') + ':'} ${tipp.delayedOA?.getI10n("value")}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.hybridOA || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_ACCESS_TYPE} la-popup-tooltip"
               data-content="${message(code: 'tipp.hybridOA')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.hybridOA') + ':'} ${tipp.hybridOA?.getI10n("value")}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.ddcs || showEmptyFields)}">
        <div class="item">
            <i class="grey sort numeric down icon la-popup-tooltip"
               data-content="${message(code: 'tipp.ddc')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.ddc') + ':'}
                <div class="ui list la-titleAccordionList">
                    <g:each in="${tipp.ddcs}" var="ddc">
                        <div class="item">${ddc.ddc.value} - ${ddc.ddc.getI10n("value")}</div>
                    </g:each>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.languages || showEmptyFields)}">
        <div class="item">
            <i class="grey ${Icon.SYM.LANGUAGE} la-popup-tooltip"
               data-content="${message(code: 'tipp.language')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.language') + ':'}
                <div class="ui list la-titleAccordionList">
                    <g:each in="${tipp.languages}" var="language">
                        <div class="item">${language.language.getI10n("value")}</div>
                    </g:each>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.publisherName || showEmptyFields)}">
        <div class="item">
            <i class="grey building icon la-popup-tooltip"
               data-content="${message(code: 'tipp.publisher')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.publisher') + ':'} ${tipp.publisherName}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.accessType || showEmptyFields)}">
        <div class="item">
            <i class="${Icon.ATTR.TIPP_ACCESS_TYPE} la-popup-tooltip"
               data-content="${message(code: 'tipp.accessType')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.accessType') + ':'} ${tipp.accessType?.getI10n("value")}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.openAccess || showEmptyFields)}">
        <div class="item">
            <i class="ellipsis vertical grey icon la-popup-tooltip"
               data-content="${message(code: 'tipp.openAccess')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.openAccess') + ':'} ${tipp.openAccess?.getI10n("value")}
            </div>
        </div>
    </g:if>


    <div class="item">
        <i class="${Icon.PROVIDER} grey la-popup-tooltip"
           data-content="${message(code: 'tipp.provider')}"></i>

        <div class="content">
            ${showCompact ? '' : message(code: 'tipp.provider') + ':'}
            <g:if test="${tipp.pkg.provider}">

                <g:link controller="provider" action="show" target="_blank"
                        id="${tipp.pkg.provider.id}">${tipp.pkg.provider.name}</g:link>

                    <g:if test="${tipp.pkg.provider.gokbId}">
                        <a role="button" class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
                           data-content="${message(code: 'wekb')}"
                           href="${Wekb.getURL() + '/public/tippContent/?id=' + tipp.gokbId}"
                           target="_blank"><i class="${Icon.WEKB}"></i>
                        </a>
                    </g:if>

            </g:if>
        </div>
    </div>

%{--<g:if test="${ie && (ie.availabilityStatus || showEmptyFields)}">
    <g:if test="${ie.availabilityStatus?.value == 'Expected'}">
        ${message(code: 'default.on')} <g:formatDate
            format="${message(code: 'default.date.format.notime')}"
            date="${ie.accessStartDate}"/>
    </g:if>

    <g:if test="${ie.availabilityStatus?.value == 'Expired'}">
        ${message(code: 'default.on')} <g:formatDate
            format="${message(code: 'default.date.format.notime')}"
            date="${ie.accessEndDate}"/>
    </g:if>
</g:if>--}%

    <g:if test="${showPackage}">
        <g:if test="${tipp.pkg.id}">
            <div class="item">
                <i class="grey ${Icon.PACKAGE} la-popup-tooltip"
                   data-content="${message(code: 'package.label')}"></i>

                <div class="content">
                    <g:link controller="package" action="show" target="_blank"
                            id="${tipp.pkg.id}">${tipp.pkg.name}</g:link>
                </div>
            </div>
        </g:if>
    </g:if>
    <g:if test="${showPlattform}">
        <g:if test="${tipp.platform.name}">
            <div class="item">
                <i class="${Icon.PLATFORM} grey la-popup-tooltip" data-content="${message(code: 'tipp.platform')}"></i>

                <div class="content">
                    <g:if test="${tipp.platform.name}">
                        <g:link controller="platform" action="show" target="_blank"
                                id="${tipp.platform.id}">
                            ${tipp.platform.name}
                        </g:link>
                    </g:if>
                    <g:else>
                        ${message(code: 'default.unknown')}
                    </g:else>
                </div>
            </div>
        </g:if>
    </g:if>

    <div class="la-title">${message(code: 'default.details.label')}</div>

    <g:if test="${controllerName != 'tipp' && tipp.id}">
        <g:link class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
                data-content="${message(code: 'laser')}"
                target="_blank"
                controller="tipp" action="show"
                id="${tipp.id}">
            <i class="${Icon.TIPP}"></i>
        </g:link>
    </g:if>

        <g:if test="${tipp.gokbId}">
            <a role="button" class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
               data-content="${message(code: 'wekb')}"
               href="${Wekb.getURL() + '/public/tippContent/?id=' + tipp.gokbId}"
               target="_blank"><i class="${Icon.WEKB}"></i>
            </a>
        </g:if>

</div>

