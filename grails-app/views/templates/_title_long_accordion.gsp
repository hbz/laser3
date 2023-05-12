<%@ page import="de.laser.remote.ApiSource; de.laser.storage.RDStore" %>

<div class="three wide column" data-ajaxTopic="true">
    <div class="ui list"  >
        <div class="item">
            <ui:listIcon hideSurroundingMarkup="true" type="${tipp.titleType}"/>
            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.titleType') + ':'}
                </div>

                <div class="description">
                    ${tipp.titleType}
                </div>
            </div>
        </div>

        <g:if test="${ie && (ie.tipp.medium || showEmptyFields)}">
            <div class="item">
                <i class="grey medium icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.medium')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.medium') + ':'}
                    </div>

                    <div class="description">
                        ${ie.tipp.medium?.getI10n('value')}
                    </div>
                </div>
            </div>
        </g:if>
        <g:else>
            <g:if test="${(tipp.medium || showEmptyFields)}">
                <div class="item">
                    <i class="grey medium icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.medium')}"></i>

                    <div class="content">
                        <div class="header">
                            ${message(code: 'tipp.medium') + ':'}
                        </div>

                        <div class="description">
                            ${tipp.medium?.getI10n('value')}
                        </div>
                    </div>
                </div>
            </g:if>
        </g:else>

        <%-- status is not nullable any more, see ERMS-4918 --%>
        <g:if test="${ie}">
            <div class="item">
                <i class="grey key icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'default.status.label')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'default.status.label') + ':'}
                    </div>

                    <div class="description">
                        ${ie.tipp.status.getI10n('value')}
                    </div>
                </div>
            </div>
        </g:if>
        <g:else>
            <%--<g:if test="${(tipp.status || showEmptyFields)}">--%>
                <div class="item">
                    <i class="grey key icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'default.status.label')}"></i>

                    <div class="content">
                        <div class="header">
                            ${message(code: 'default.status.label') + ':'}
                        </div>

                        <div class="description">
                            ${tipp.status.getI10n('value')}
                        </div>
                    </div>
                </div>
            <%--</g:if>--%>
        </g:else>

        <g:if test="${(tipp.titleType == 'Book') && (tipp.volume || showEmptyFields)}">
            <div class="item">
                <i class="grey icon la-books la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.volume')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.volume') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.volume}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.titleType == 'Book') && (tipp.firstAuthor || showEmptyFields)}">
            <div class="item">
                <i class="grey icon user circle la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.firstAuthor')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.firstAuthor') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.firstAuthor}
                    </div>

                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.titleType == 'Book') && (tipp.firstEditor || showEmptyFields)}">
            <div class="item">
                <i class="grey icon industry circle la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.firstEditor')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.firstEditor') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.firstEditor}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.titleType == 'Book') && (tipp.editionStatement || showEmptyFields)}">
            <div class="item">
                <i class="grey icon copy la-popup-tooltip la-delay"
                   data-content="${message(code: 'title.editionStatement.label')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'title.editionStatement.label') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.editionStatement}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.titleType == 'Book') && (tipp.editionNumber || showEmptyFields)}">
            <div class="item">
                <i class="grey icon copy la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.editionNumber.tooltip')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.editionNumber') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.editionNumber}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.titleType == 'Book') && (tipp.summaryOfContent || showEmptyFields)}">
            <div class="item">
                <i class="grey icon desktop la-popup-tooltip la-delay"
                   data-content="${message(code: 'title.summaryOfContent.label')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'title.summaryOfContent.label') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.summaryOfContent}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.seriesName || showEmptyFields)}">
            <div class="item">
                <i class="grey icon list la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.seriesName')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.seriesName') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.seriesName}
                    </div>
                </div>
            </div>
        </g:if>
    </div>
</div>
<div class="three wide column" data-ajaxTopic="true">
    <div class="ui list" >

        <g:if test="${(tipp.subjectReference || showEmptyFields)}">
            <div class="item">
                <i class="grey icon comment alternate la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.subjectReference')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.subjectReference') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.subjectReference}
                    </div>
                </div>
            </div>

        </g:if>

        <g:if test="${(tipp.delayedOA || showEmptyFields)}">
            <div class="item">
                <i class="grey lock open icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.delayedOA')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.delayedOA') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.delayedOA?.getI10n("value")}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.hybridOA || showEmptyFields)}">
            <div class="item">
                <i class="grey lock open alternate icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.hybridOA')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.hybridOA') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.hybridOA?.getI10n("value")}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.ddcs || showEmptyFields)}">
            <div class="item">
                <i class="grey sort numeric down icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.ddc')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.ddc') + ':'}
                    </div>

                    <div class="description">
                        <ul class="ui list">
                            <g:each in="${tipp.ddcs}" var="ddc">
                                <li>${ddc.ddc.value} - ${ddc.ddc.getI10n("value")}</li>
                            </g:each>
                        </ul>
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.languages || showEmptyFields)}">
            <div class="item">
                <i class="grey language icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.language')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.language') + ':'}
                    </div>

                    <div class="description">
                        <ul class="ui list">
                            <g:each in="${tipp.languages}" var="language">
                                <li class="item">${language.language.getI10n("value")}</li>
                            </g:each>
                        </ul>
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.publisherName || showEmptyFields)}">
            <div class="item">
                <i class="grey building icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.publisher')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.publisher') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.publisherName}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.accessType || showEmptyFields)}">
            <div class="item">
                <i class="grey lock open icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.accessType')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.accessType') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.accessType?.getI10n("value")}
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${(tipp.openAccess || showEmptyFields)}">
            <div class="item">
                <i class="ellipsis vertical grey icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.openAccess')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.openAccess') + ':'}
                    </div>

                    <div class="description">
                        ${tipp.openAccess?.getI10n("value")}
                    </div>
                </div>
            </div>
        </g:if>

        <g:set var="providers" value="${tipp.getPublishers()}"/>
        <g:if test="${(providers || showEmptyFields)}">
            <div class="item">
                <i class="grey university icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.provider')}"></i>

                <div class="content">
                    <div class="header">
                        ${message(code: 'tipp.provider') + ':'}
                    </div>
                    <g:if test="${providers}">
                        <div class="description">
                            <div class="ui list">
                                <g:each in="${providers}" var="provider">

                                    <g:link controller="organisation" action="show" target="_blank"
                                            id="${provider.id}">${provider.name}</g:link>

                                %{--<g:each in="${apisources}" var="gokbAPI">
                                    <g:if test="${provider.gokbId}">
                                        <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                           data-content="${message(code: 'wekb')}"
                                           href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/orgContent/?id=' + provider.gokbId : '#'}"
                                           target="_blank"><i class="la-gokb  icon"></i>
                                        </a>
                                    </g:if>
                                </g:each>--}%

                                </g:each>
                            </div>
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
                        <div class="header">
                            ${message(code: 'package.label')}
                        </div>
                        <div class="description">
                            <g:link controller="package" action="show" target="_blank"
                                    id="${tipp.pkg.id}">${tipp.pkg.name}</g:link>
                        </div>
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
                        <div class="header">
                            ${message(code: 'tipp.platform')}
                        </div>
                        <div class="description">
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
                </div>
            </g:if>
        </g:if>

    </div>
</div>

