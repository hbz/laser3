<%@ page import="de.laser.storage.RDStore; de.laser.Subscription; de.laser.License" %>
<laser:serviceInjection/>


<div class="content">
    <h2 class="ui header">
        <g:if test="${subscriptionLicenseLink}">
            <g:message code="license.plural"/>
        </g:if>
        <g:elseif test="${subscription}">
            <g:message code="subscription.details.linksHeader"/>
        </g:elseif>
        <g:elseif test="${license}">
            <g:message code="license.details.linksHeader"/>
        </g:elseif>
    </h2>

    <div class="ui accordion la-accordion-showMore">
        <g:if test="${links.entrySet()}">
            <g:each in="${links.entrySet()}" var="linkTypes">
                <g:if test="${linkTypes.getValue().size() > 0}">
                    <g:each in="${linkTypes.getValue()}" var="link">
                        <%
                            int perspectiveIndex
                            if (entry in [link.sourceSubscription, link.sourceLicense])
                                perspectiveIndex = 0
                            else if (entry in [link.destinationSubscription, link.destinationLicense])
                                perspectiveIndex = 1
                        %>
                        <g:set var="pair" value="${link.getOther(entry)}"/>
                        <g:if test="${pair?.propertySet && pair instanceof License && subscriptionLicenseLink}">
                            <g:set var="setAccordionShowMoreClass" value="title"/>
                        </g:if>
                        <g:else>
                            <g:set var="setAccordionShowMoreClass" value=""/>
                        </g:else>
                        <div class="ui raised segments la-accordion-segments">

                            <div class="ui fluid segment ${setAccordionShowMoreClass}">
                                <div class="ui stackable equal width grid">
                                    <div class="eleven wide column">
                                        <div class="ui list">
                                            <div class="item">
                                                <g:if test="${subscriptionLicenseLink}">
                                                    <div class="ui label">
                                                        ${pair.licenseCategory?.getI10n("value")}
                                                    </div>

                                                </g:if>
                                                <g:else>
                                                    <div class="ui label">
                                                        ${genericOIDService.resolveOID(linkTypes.getKey()).getI10n("value").split("\\|")[perspectiveIndex]}
                                                    </div>
                                                </g:else>
                                            </div>
                                            <g:if test="${pair instanceof Subscription}">
                                                <div class="item">
                                                    <g:link controller="subscription" action="show" id="${pair.id}">
                                                        ${pair.name}
                                                    </g:link>
                                                </div>
                                            </g:if>
                                            <g:elseif test="${pair instanceof License}">
                                                <div class="item">
                                                    <g:link controller="license" action="show" id="${pair.id}">
                                                        ${pair.reference} (${pair.status.getI10n("value")})
                                                    </g:link>
                                                </div>
                                            </g:elseif>
                                            <div class="item">
                                                <i aria-hidden="true" class="grey calendar alternate icon icon"></i>

                                                <div class="content">
                                                    <div class="header">
                                                        <g:formatDate date="${pair.startDate}"
                                                                      format="${message(code: 'default.date.format.notime')}"/>–<g:formatDate
                                                                date="${pair.endDate}"
                                                                format="${message(code: 'default.date.format.notime')}"/>
                                                    </div>
                                                </div>
                                            </div>
                                            <g:set var="comment" value="${link.document}"/>
                                            <g:if test="${comment}">
                                                <div class="item">
                                                    <p><em>${comment.owner.content}</em></p>
                                                </div>
                                            </g:if>
                                        </div>
                                    </div>

                                    <div class="five wide right aligned  column">
                                        <g:if test="${license}"></g:if>
                                        <g:else>
                                            <g:if test="${pair.propertySet && pair instanceof License}">
                                                <div id="derived-license-properties-toggle${link.id}"
                                                        class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                                                        data-content="${message(code: 'subscription.details.viewLicenseProperties')}">
                                                    <i class="ui angle double down icon"></i>
                                                </div>
                                            </g:if>
                                        </g:else>

                                        <laser:render template="/templates/links/subLinksModal"
                                                      model="${[tmplText               : message(code: 'subscription.details.editLink'),
                                                                tmplIcon               : 'write',
                                                                tmplCss                : 'icon blue la-selectable-button la-popup-tooltip la-delay',
                                                                tmplID                 : 'editLink',
                                                                tmplModalID            : "sub_edit_link_${link.id}",
                                                                subscriptionLicenseLink: subscriptionLicenseLink,
                                                                editmode               : editable,
                                                                context                : entry,
                                                                atConsortialParent     : atConsortialParent,
                                                                link                   : link
                                                      ]}"/>
                                        <g:if test="${editable}">
                                            <g:if test="${subscriptionLicenseLink}">
                                                <div class="ui icon negative buttons">
                                                    <span class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'license.details.unlink')}">
                                                        <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                                data-confirm-term-how="unlink"
                                                                controller="subscription" action="unlinkLicense"
                                                                params="${[license: link.sourceLicense.id, id: subscription.id]}"
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                            <i class="unlink icon"></i>
                                                        </g:link>
                                                    </span>
                                                </div>
                                            </g:if>
                                            <g:else>
                                                <span class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'license.details.unlink')}">
                                                    <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                            data-confirm-term-how="unlink"
                                                            controller="myInstitution" action="unlinkObjects"
                                                            params="${[oid: genericOIDService.getOID(link)]}"
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                        <i class="unlink icon"></i>
                                                    </g:link>
                                                </span>
                                            </g:else>
                                        </g:if>

                                    </div>
                                </div>
                            </div>
                            <g:if test="${license}"></g:if>
                            <g:else>
                                <g:if test="${pair.propertySet && pair instanceof License}">
                                    <div class="ui fluid segment content">
                                        <laser:render template="/subscription/licProp"
                                                      model="[license: pair, derivedPropDefGroups: pair.getCalculatedPropDefGroups(contextOrg), linkId: link.id]"/>
                                    </div>
                                </g:if>
                            </g:else>
                        </div>

                    </g:each>
                </g:if>
            </g:each>
        </g:if>
        <g:elseif test="${license || subscriptionLicenseLink}">
            <p>
                <g:message code="license.details.noLink"/>
            </p>
        </g:elseif>
        <g:elseif test="${subscription}">
            <p>
                <g:message code="subscription.details.noLink"/>
            </p>
        </g:elseif>
        <div class="ui la-vertical buttons">
            <%
                Map<String, Object> model
                String addLink = ""
                if (license || subscriptionLicenseLink)
                    addLink = message(code: 'license.details.addLink')
                else if (subscription)
                    addLink = message(code: 'subscription.details.addLink')
                if (subscriptionLicenseLink) {
                    model = [tmplText               : addLink,
                             tmplID                 : 'addLicenseLink',
                             tmplButtonText         : addLink,
                             tmplModalID            : 'sub_add_license_link',
                             editmode               : editable,
                             subscriptionLicenseLink: true,
                             atConsortialParent     : contextOrg == subscription.getConsortia(),
                             context                : subscription
                    ]
                } else {
                    model = [tmplText          : addLink,
                             tmplID            : 'addLink',
                             tmplButtonText    : addLink,
                             tmplModalID       : 'sub_add_link',
                             editmode          : editable,
                             atConsortialParent: atConsortialParent,
                             context           : entry
                    ]
                }
            %>
            <laser:render template="/templates/links/subLinksModal"
                          model="${model}"/>
        </div>
    </div>
</div>