<%@page import="de.laser.helper.RDStore; de.laser.Subscription; de.laser.License" %>
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
    <g:if test="${links.entrySet()}">
        <table class="ui three column table">
            <style>
                .ui.table tr:first-child th {
                    border-top: none;
                }
            </style>
            <g:each in="${links.entrySet()}" var="linkTypes">
                <g:if test="${linkTypes.getValue().size() > 0}">
                    <g:each in="${linkTypes.getValue()}" var="link">
                        <tr>
                            <%
                                int perspectiveIndex
                                if(entry in [link.sourceSubscription, link.sourceLicense])
                                    perspectiveIndex = 0
                                else if(entry in [link.destinationSubscription, link.destinationLicense])
                                    perspectiveIndex = 1
                            %>
                            <g:set var="pair" value="${link.getOther(entry)}"/>
                            <g:if test="${subscriptionLicenseLink}">
                                <th scope="row" class="control-label la-js-dont-hide-this-card">${pair.licenseCategory?.getI10n("value")}</th>
                            </g:if>
                            <g:else>
                                <th scope="row" class="control-label la-js-dont-hide-this-card">${genericOIDService.resolveOID(linkTypes.getKey()).getI10n("value").split("\\|")[perspectiveIndex]}</th>
                            </g:else>
                            <td>
                                <g:if test="${pair instanceof Subscription}">
                                    <g:link controller="subscription" action="show" id="${pair.id}">
                                        ${pair.name}
                                    </g:link>
                                </g:if>
                                <g:elseif test="${pair instanceof License}">
                                    <g:link controller="license" action="show" id="${pair.id}">
                                        ${pair.reference} (${pair.status.getI10n("value")})
                                    </g:link>
                                </g:elseif>
                                <p><g:formatDate date="${pair.startDate}" format="${message(code:'default.date.format.notime')}"/>–<g:formatDate date="${pair.endDate}" format="${message(code:'default.date.format.notime')}"/></p>
                                <g:set var="comment" value="${link.document}"/>
                                <g:if test="${comment}">
                                    <p><em>${comment.owner.content}</em></p>
                                </g:if>
                            </td>
                            <td class="right aligned">
                                <g:if test="${license}"></g:if>
                                <g:else>
                                    <g:if test="${pair.propertySet && pair instanceof License}">
                                        <button id="derived-license-properties-toggle${link.id}"
                                                class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                                                data-content="${message(code:'subscription.details.viewLicenseProperties')}">
                                            <i class="ui angle double down icon"></i>
                                        </button>
                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            $("#derived-license-properties-toggle${link.id}").on('click', function() {
                                            $("#derived-license-properties${link.id}").transition('slide down');
                                            //$("#derived-license-properties${link.id}").toggleClass('hidden');

                                            if ($("#derived-license-properties${link.id}").hasClass('visible')) {
                                                $(this).html('<i class="ui angle double down icon"></i>');
                                            } else {
                                                $(this).html('<i class="ui angle double up icon"></i>');
                                            }
                                        })
                                        </laser:script>
                                    </g:if>
                                </g:else>

                                <g:render template="/templates/links/subLinksModal"
                                          model="${[tmplText:message(code:'subscription.details.editLink'),
                                                    tmplIcon:'write',
                                                    tmplCss: 'icon blue la-selectable-button la-popup-tooltip la-delay',
                                                    tmplID:'editLink',
                                                    tmplModalID:"sub_edit_link_${link.id}",
                                                    subscriptionLicenseLink: subscriptionLicenseLink,
                                                    editmode: editable,
                                                    context: entry,
                                                    atConsortialParent: atConsortialParent,
                                                    link: link
                                          ]}" />
                                <g:if test="${editable}">
                                    <g:if test="${subscriptionLicenseLink}">
                                        <div class="ui icon negative buttons">
                                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.unlink')}">
                                                <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                        data-confirm-term-how="unlink"
                                                        controller="subscription" action="unlinkLicense" params="${[license: link.sourceLicense.id, id:subscription.id]}"
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                    <i class="unlink icon"></i>
                                                </g:link>
                                            </span>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.unlink')}">
                                            <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                    data-confirm-term-how="unlink"
                                                    controller="myInstitution" action="unlinkObjects" params="${[oid : genericOIDService.getOID(link)]}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                <i class="unlink icon"></i>
                                            </g:link>
                                        </span>
                                    </g:else>
                                </g:if>
                            </td>
                        </tr>
                        <g:if test="${license}"></g:if>
                        <g:else>
                            <g:if test="${pair.propertySet && pair instanceof License}">
                                <tr>
                                    <td style="border-top: none; padding:0" colspan="3">

                                            <g:render template="/subscription/licProp" model="[license: pair, derivedPropDefGroups: pair.getCalculatedPropDefGroups(contextOrg), linkId: link.id]"/>

                                    </td>
                                </tr>
                            </g:if>
                        </g:else>
                    </g:each>
                </g:if>
            </g:each>
        </table>
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
            Map<String,Object> model
            String addLink = ""
            if(license || subscriptionLicenseLink)
                addLink = message(code:'license.details.addLink')
            else if(subscription)
                addLink = message(code:'subscription.details.addLink')
            if(subscriptionLicenseLink) {
                model = [tmplText:addLink,
                         tmplID:'addLicenseLink',
                         tmplButtonText:addLink,
                         tmplModalID:'sub_add_license_link',
                         editmode: editable,
                         subscriptionLicenseLink: true,
                         atConsortialParent: contextOrg == subscription.getConsortia(),
                         context: subscription
                ]
            }
            else {
                model = [tmplText:addLink,
                 tmplID:'addLink',
                 tmplButtonText:addLink,
                 tmplModalID:'sub_add_link',
                 editmode: editable,
                 atConsortialParent: atConsortialParent,
                 context: entry
                ]
            }
        %>
        <g:render template="/templates/links/subLinksModal"
                  model="${model}" />
    </div>
</div>