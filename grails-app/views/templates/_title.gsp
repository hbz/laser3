<div class="la-icon-list">

    <semui:listIcon type="${tipp.titleType}"/>
    <g:if test="${ie}">
        <g:link controller="issueEntitlement" id="${ie.id}"
                action="show"><strong>${ie.tipp.name}</strong>
        </g:link>
    </g:if>
    <g:else>
        <g:link controller="tipp" id="${tipp.id}"
                action="show"><strong>${tipp.name}</strong>
        </g:link>
    </g:else>

    <g:if test="${tipp.hostPlatformURL}">
        <semui:linkIcon
                href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
    </g:if>
    <br/>

    <g:if test="${!showCompact}">
        <br/>
    </g:if>

    <g:each in="${tipp.ids.sort { it.ns.ns }}" var="title_id">
        <span class="ui small basic image label" style="background: none">
            ${title_id.ns.ns}: <div class="detail">${title_id.value}</div>
        </span>
    </g:each>
<!--                  ISSN:<strong>${tipp.getIdentifierValue('ISSN') ?: ' - '}</strong>,
                  eISSN:<strong>${tipp.getIdentifierValue('eISSN') ?: ' - '}</strong><br />-->
    <br/>

    <g:if test="${!showCompact}">
        <br/>
    </g:if>

    <div class="item">
        <semui:listIcon type="${tipp.titleType}"/>

        <div class="content">
            ${showCompact ? '' : message(code: 'tipp.titleType') + ':'} ${tipp.titleType}
        </div>
    </div>

    <g:if test="${ie && (ie.medium || showEmptyFields)}">
        <div class="item">
            <i class="grey medium icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.medium')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.medium') + ':'} ${ie.medium?.getI10n('value')}
            </div>
        </div>
    </g:if>
    <g:else>
        <g:if test="${(tipp.medium || showEmptyFields)}">
            <div class="item">
                <i class="grey medium icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.medium')}"></i>

                <div class="content">
                    ${showCompact ? '' : message(code: 'tipp.medium') + ':'} ${tipp.medium?.getI10n('value')}
                </div>
            </div>
        </g:if>
    </g:else>


    <g:if test="${ie && (ie.status || showEmptyFields)}">
        <div class="item">
            <i class="grey key icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.status')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.status') + ':'} ${ie.status?.getI10n('value')}
            </div>
        </div>
    </g:if>
    <g:else>
        <g:if test="${(tipp.status || showEmptyFields)}">
            <div class="item">
                <i class="grey key icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.status')}"></i>

                <div class="content">
                    ${showCompact ? '' : message(code: 'tipp.status') + ':'} ${tipp.status?.getI10n('value')}
                </div>
            </div>
        </g:if>
    </g:else>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.volume || showEmptyFields)}">
        <div class="item">
            <i class="grey icon la-books la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.volume')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.volume') + ':'} ${tipp.volume}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.firstAuthor || showEmptyFields)}">
        <div class="item">
            <i class="grey icon user circle la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.firstAuthor')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.firstAuthor') + ':'} ${tipp.firstAuthor}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.firstEditor || showEmptyFields)}">
        <div class="item">
            <i class="grey icon user circle la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.firstEditor')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.firstEditor') + ':'} ${tipp.firstEditor}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.editionStatement || showEmptyFields)}">
        <div class="item">
            <i class="grey icon copy la-popup-tooltip la-delay"
               data-content="${message(code: 'title.editionStatement.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'title.editionStatement.label') + ':'} ${tipp.editionStatement}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.editionNumber || showEmptyFields)}">
        <div class="item">
            <i class="grey icon copy la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.editionNumber.tooltip')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.editionNumber') + ':'} ${tipp.editionNumber}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.summaryOfContent || showEmptyFields)}">
        <div class="item">
            <i class="grey icon desktop la-popup-tooltip la-delay"
               data-content="${message(code: 'title.summaryOfContent.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'title.summaryOfContent.label') + ':'} ${tipp.summaryOfContent}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.seriesName || showEmptyFields)}">
        <div class="item">
            <i class="grey icon list la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.seriesName')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.seriesName') + ':'} ${tipp.seriesName}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.subjectReference || showEmptyFields)}">
        <div class="item">
            <i class="grey icon comment alternate la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.subjectReference')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.subjectReference') + ':'} ${tipp.subjectReference}
            </div>
        </div>

    </g:if>

    <g:if test="${(tipp.delayedOA || showEmptyFields)}">
        <div class="item">
            <i class="grey lock open icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.delayedOA')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.delayedOA') + ':'} ${tipp.delayedOA?.getI10n("value")}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.hybridOA || showEmptyFields)}">
        <div class="item">
            <i class="grey lock open alternate icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.hybridOA')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.hybridOA') + ':'} ${tipp.hybridOA?.getI10n("value")}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.payment || showEmptyFields)}">
        <div class="item">
            <i class="grey money icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.paymentType')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.paymentType') + ':'} ${tipp.payment?.getI10n("value")}
            </div>
        </div>
    </g:if>

    <g:set var="publishers" value="${tipp.getPublishers()}"/>
    <g:if test="${(publishers || showEmptyFields)}">
        <div class="item">
            <i class="grey university icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.publisher')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.publisher') + ':'}
                <g:if test="${publishers}">
                    <div class="ui list">
                        <g:each in="${publishers}" var="publisher">

                                <g:link controller="organisation" action="show" id="${publisher.id}">${publisher.name}</g:link>

                                <g:each in="${apisources}" var="gokbAPI">
                                    <g:if test="${publisher.gokbId}">
                                        <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                           data-content="${message(code: 'gokb')}"
                                           href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/resource/show/?id=' + publisher.gokbId : '#'}"
                                           target="_blank"><i class="la-gokb  icon"></i>
                                        </a>
                                    </g:if>
                                </g:each>

                        </g:each>
                    </div>
                </g:if>
            </div>
        </div>
    </g:if>

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
                <i class="grey icon gift scale la-popup-tooltip la-delay"
                   data-content="${message(code: 'package.label')}"></i>

                <div class="content">
                    <g:link controller="package" action="show"
                            id="${tipp.pkg.id}">${tipp.pkg.name}</g:link>
                </div>
            </div>
        </g:if>
    </g:if>
    <g:if test="${showPlattform}">
        <g:if test="${tipp.platform.name}">
            <div class="item">
                <i class="grey icon cloud la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.platform')}"></i>

                <div class="content">
                    <g:if test="${tipp.platform.name}">
                        <g:link controller="platform" action="show"
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
        <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                data-content="${message(code: 'laser')}"
                target="_blank"
                controller="tipp" action="show"
                id="${tipp.id}">
            <i class="book icon"></i>
        </g:link>
    </g:if>

    <g:each in="${apisources}" var="gokbAPI">
        <g:if test="${tipp.gokbId}">
            <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
               data-content="${message(code: 'gokb')}"
               href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/resource/show/?id=' + tipp.gokbId : '#'}"
               target="_blank"><i class="la-gokb  icon"></i>
            </a>
        </g:if>
    </g:each>

</div>




