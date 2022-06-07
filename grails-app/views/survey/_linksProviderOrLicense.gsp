<%@ page import="de.laser.storage.RDStore; de.laser.Subscription; de.laser.License" %>
<laser:serviceInjection/>
<div class="content">
    <g:if test="${linkType == 'License'}">
        <h2 class="ui header">
            <g:message code="license.label"/>
        </h2>

        <g:if test="${surveyInfo.license}">
            <g:link controller="license" action="show" id="${surveyInfo.license.id}">
                ${surveyInfo.license.reference} (${surveyInfo.license.status.getI10n("value")})
            </g:link>

            <g:if test="${editable && contextOrg.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                <span class="la-popup-tooltip la-delay" data-content="${message(code: 'default.button.unlink.label')}">
                    <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "surveyInfo.unlink.license.confirm.dialog")}"
                            data-confirm-term-how="unlink"
                            controller="survey" action="setProviderOrLicenseLink"
                            params="${[unlinkLicense: true, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                        <i class="unlink icon"></i>
                    </g:link>
                </span>
            </g:if>

        </g:if>
    </g:if>
    <g:elseif test="${linkType == 'Provider'}">

        <h2 class="ui header">
            <g:message code="default.provider.label"/>
        </h2>

        <g:if test="${surveyInfo.provider}">
            <g:link controller="organisation" action="show" id="${surveyInfo.provider.id}">
                ${surveyInfo.provider.name}
            </g:link>

            <g:if test="${editable && contextOrg.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                <span class="la-popup-tooltip la-delay" data-content="${message(code: 'default.button.unlink.label')}">
                    <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "surveyInfo.unlink.provider.confirm.dialog")}"
                            data-confirm-term-how="unlink"
                            controller="survey" action="setProviderOrLicenseLink"
                            params="${[unlinkProvider: true, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                        <i class="unlink icon"></i>
                    </g:link>
                </span>
            </g:if>
        </g:if>
    </g:elseif>

    <g:if test="${editable && contextOrg.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
        <div class="ui la-vertical buttons">
            <g:if test="${linkType == 'License' && !surveyInfo.license}">
                <laser:render template="linksProviderOrLicenseModal"
                          model="${[tmplText      : message(code: 'surveyInfo.link.license'),
                                    tmplButtonText: message(code: 'surveyInfo.link.license'),
                                    tmplModalID   : 'survey_link_license',
                                    editable      : editable,
                                    surveyInfo    : surveyInfo,
                                    surveyConfig  : surveyConfig,
                                    linkField     : 'license',
                          ]}"/>
            </g:if>
            <g:elseif test="${linkType == 'Provider' && !surveyInfo.provider}">
                <laser:render template="linksProviderOrLicenseModal"
                          model="${[tmplText      : message(code: 'surveyInfo.link.provider'),
                                    tmplButtonText: message(code: 'surveyInfo.link.provider'),
                                    tmplModalID   : 'survey_link_provider',
                                    editable      : editable,
                                    surveyInfo    : surveyInfo,
                                    surveyConfig  : surveyConfig,
                                    linkField     : 'provider'
                          ]}"/>
            </g:elseif>
        </div>
    </g:if>

</div>