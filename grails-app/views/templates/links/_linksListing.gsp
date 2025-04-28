<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.Subscription; de.laser.License; de.laser.Links" %>
<laser:serviceInjection/>

<div class="content">
    <div class="ui header la-flexbox la-justifyContent-spaceBetween">

        <g:if test="${subscriptionLicenseLink}">
            <h2><g:message code="license.plural"/></h2>
        </g:if>
        <g:elseif test="${subscription}">
            <h2><g:message code="subscription.details.linksHeader"/></h2>
        </g:elseif>
        <g:elseif test="${license}">
            <h2><g:message code="license.details.linksHeader"/></h2>
        </g:elseif>

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
                         tmplIcon               : 'add',
                         tmplTooltip            : addLink,
                         tmplModalID            : 'sub_add_license_link',
                         editmode               : editable,
                         subscriptionLicenseLink: true,
                         atConsortialParent     : contextService.getOrg() == subscription.getConsortium(),
                         context                : subscription,
                         linkInstanceType       : Links.class.name,
                         tmplCss                : ''
                ]
            } else {
                model = [tmplText          : addLink,
                         tmplID            : 'addLink',
                         tmplIcon          : 'add',
                         tmplTooltip        :addLink,
//                             tmplButtonText    : addLink,
                         tmplModalID       : 'sub_add_link',
                         editmode          : editable,
                         atConsortialParent: atConsortialParent,
                         context           : entry,
                         linkInstanceType  : Links.class.name,
                         tmplCss       : ''
                ]
            }
        %>
        <div class="right aligned four wide column">
            <laser:render template="/templates/links/subLinksModal"
                          model="${model}"/>
            %{--                <a type="button" class="ui button icon la-modern-button" data-ui="modal" href="#sub_add_link">
                                <i aria-hidden="true" class="plus icon"></i>
                            </a>--}%
        </div>
    </div>

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
                                                <i aria-hidden="true" class="grey ${Icon.SYM.DATE}"></i>

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

                                    <div class="five wide right aligned column">
                                        <g:if test="${license}"></g:if>
                                        <g:else>
                                            <g:if test="${pair.propertySet && pair instanceof License}">
                                                <div id="derived-license-properties-toggle${link.id}"
                                                     class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                                     data-content="${message(code: 'subscription.details.viewLicenseProperties')}">
                                                    <i class="${Icon.CMD.SHOW_MORE}"></i>
                                                </div>
                                            </g:if>
                                        </g:else>

                                        <laser:render template="/templates/links/subLinksModal"
                                                      model="${[tmplText               : message(code: 'subscription.details.editLink'),
                                                                tmplIcon               : 'write',
                                                                tmplCss                : 'icon la-modern-button la-selectable-button la-popup-tooltip',
                                                                tmplID                 : 'editLink',
                                                                tmplModalID            : "sub_edit_link_${link.id}",
                                                                subscriptionLicenseLink: subscriptionLicenseLink,
                                                                editmode               : editable,
                                                                context                : entry,
                                                                atConsortialParent     : atConsortialParent,
                                                                link                   : link,
                                                                linkInstanceType       : Links.class.name
                                                      ]}"/>
                                        <g:if test="${editable}">
                                            <g:if test="${subscriptionLicenseLink}">
                                                <%-- saved on server-side as String because the value is being handed as an AJAX query argument into an URL --%>
                                                <g:if test="${Boolean.valueOf(atConsortialParent)}">
                                                    <div class="ui buttons">
                                                        <div class="ui simple dropdown negative icon button la-modern-button" data-content="${message(code: 'license.details.unlink')}">
                                                            <i aria-hidden="true" class="${Icon.CMD.UNLINK}"></i>
                                                            <div class="menu">
                                                                <g:link controller="subscription" action="unlinkLicense" class="item js-open-confirm-modal" params="${[license: link.sourceLicense.id, id: subscription.id]}"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.license")}"
                                                                        data-confirm-term-how="unlink" role="button" aria-label="${message(code: "ariaLabel.unlink.universal")}">
                                                                    <g:message code="license.details.unlink"/>
                                                                </g:link>
                                                                <g:link controller="subscription" action="unlinkLicense" class="item js-open-confirm-modal" params="${[license: link.sourceLicense.id, id: subscription.id, unlinkWithChildren: true]}"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.child.license")}"
                                                                        data-confirm-term-how="unlink" role="button" aria-label="${message(code: "ariaLabel.unlink.universal")}">
                                                                    <g:message code="license.details.unlink.child"/>
                                                                </g:link>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </g:if>
                                                <g:else>
                                                    <span class="la-popup-tooltip" data-content="${message(code: 'license.details.unlink')}">
                                                        <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.license")}"
                                                                data-confirm-term-how="unlink"
                                                                controller="subscription" action="unlinkLicense"
                                                                params="${[license: link.sourceLicense.id, id: subscription.id]}"
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                            <i class="${Icon.CMD.UNLINK}"></i>
                                                        </g:link>
                                                    </span>
                                                </g:else>
                                            <%--

                                                <g:if test="${atConsortialParent}">
                                                    <div class="or" data-text="|"></div>
                                                    <span class="la-popup-tooltip"
                                                          data-content="${message(code: 'license.details.unlink.child')}">
                                                        <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.child.license")}"
                                                                data-confirm-term-how="unlink"
                                                                controller="subscription" action="unlinkLicense"
                                                                params="${[license: link.sourceLicense.id, id: subscription.id, unlinkWithChildren: true]}"
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                            <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                                        </g:link>
                                                    </span>
                                                </g:if>
                                            --%>
                                            </g:if>
                                            <g:else>
                                                <span class="la-popup-tooltip"
                                                      data-content="${message(code: 'license.details.unlink')}">
                                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.license.license")}"
                                                            data-confirm-term-how="unlink"
                                                            controller="myInstitution" action="unlinkObjects"
                                                            params="${[oid: genericOIDService.getOID(link)]}"
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                        <i class="${Icon.CMD.UNLINK}"></i>
                                                    </g:link>
                                                </span>
                                            </g:else>
                                        </g:if>

                                    </div>
                                </div>
                            </div>
                            <g:if test="${pair.propertySet && pair instanceof License}">
                                <div class="ui fluid segment content">
                                    <laser:render template="/subscription/licProp"
                                                  model="[license: pair, derivedPropDefGroups: pair.getCalculatedPropDefGroups(contextService.getOrg()), linkId: link.id]"/>
                                </div>
                            </g:if>
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

    </div>
</div>