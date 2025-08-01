<%@ page import="de.laser.addressbook.PersonRole; de.laser.addressbook.Person; de.laser.ui.Icon; de.laser.Subscription; de.laser.storage.RDStore; de.laser.AuditConfig; de.laser.interfaces.CalculatedType; de.laser.License" %>
<laser:serviceInjection/>

<ui:greySegment>

    <table class="ui selectable celled table la-js-responsive-table la-table la-ignore-fixed">
        <thead>
        <tr>
            <th>${message(code: 'default.compare.elements')}</th>
            <g:each in="${objects}" var="object">
                <th>
                        <g:if test="${object}"><g:link
                                controller="${object.getClass().getSimpleName().toLowerCase()}" action="show"
                                id="${object.id}">${object.dropdownNamingConvention()}</g:link></g:if>
                </th>
            </g:each>
        </tr>
        </thead>
        <tbody>

        <g:each in="${compareService.compareElements(objects[0])}" var="objProperty">
            <tr>
                <td>
                    <strong>
                        <ui:propertyIcon object="${objects[0]}" propertyName="${objProperty}"
                                            showToolTipp="true"/> ${message(code: "${objects[0].getClass().getSimpleName().toLowerCase()}.${objProperty}.label")}:
                    </strong>

                </td>
                <g:each in="${objects}" var="object">
                    <td class="center aligned">
                    <ui:showPropertyValue property="${object."${objProperty}"}"/>

                    <g:if test="${object._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_PARTICIPATION]}">
                        <div class="right aligned wide column">
                            <%
                                if (AuditConfig.getConfig(object, objProperty)) {
                                    if (object.instanceOf) {
                                        println ui.auditIcon(type: 'auto')
                                    } else {
                                        println ui.auditIcon(type: 'default')
                                    }
                                }
                            %>
                        </div>
                    </g:if>

                    </div>
                </td>
                </g:each>
            </tr>
        </g:each>


        <g:if test="${objects[0] instanceof Subscription}">
            <tr>
                <td>
                    <strong><i class="${Icon.LICENSE}"></i>${message(code: 'license.label')}:</strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                        <article class="la-readmore">
                        <g:each in="${object.getLicenses()?.sort{it.reference}}" var="license">
                            <g:if test="${contextService.getOrg().id in license.orgRelations?.org.id}">
                            <strong>
                                <i class="${Icon.LICENSE}"></i>&nbsp;${license.licenseCategory?.getI10n("value")}:
                            </strong>
                            <g:link controller="license" action="show" target="_blank" id="${license.id}">
                                ${license.dropdownNamingConvention()}
                            </g:link>
                            <br />
                            </g:if>
                        </g:each>
                        </article>
                    </td>
                </g:each>
            </tr>
        </g:if>

        <g:if test="${objects[0] instanceof License}">
            <tr>
                <td>
                    <strong><i class="${Icon.SUBSCRIPTION}"></i>${message(code: 'subscription.label')}:</strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                        <article class="la-readmore">
                        <g:each in="${object.getSubscriptions()?.sort{it.name}}" var="subscription">
                            <g:if test="${contextService.getOrg().id in subscription.orgRelations?.org.id}">
                            <strong><i class="${Icon.SUBSCRIPTION}"></i>&nbsp;${subscription.kind?.getI10n("value")}:
                            </strong>
                            <g:link controller="subscription" action="show" target="_blank" id="${subscription.id}">
                                ${subscription.dropdownNamingConvention()}
                            </g:link>
                            <br />
                            </g:if>
                        </g:each>
                        </article>
                    </td>
                </g:each>
            </tr>
        </g:if>

        <g:if test="${objects[0].hasProperty("orgRelations")}">
            <tr>
                <td>
                    <strong><i
                            class="${Icon.ORG}"></i>&nbsp;${RDStore.OR_LICENSOR.getI10n('value')}/${RDStore.OR_LICENSING_CONSORTIUM.getI10n('value')}/${RDStore.OR_SUBSCRIPTION_CONSORTIUM.getI10n('value')}}:
                    </strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                        <g:each in="${object.orgRelations?.sort{it.org.name}}" var="role">
                            <g:if test="${(role.roleType in [RDStore.OR_LICENSOR, RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_SUBSCRIPTION_CONSORTIUM]) && role.org.id != contextService.getOrg().id}">
                                <strong><i class="${Icon.ORG}"></i>&nbsp;${role.roleType.getI10n("value")}:
                                </strong>
                                <g:link controller="organisation" action="show" target="_blank"
                                        id="${role.org.id}">
                                    ${role.org.name}
                                </g:link>
                                <g:if test="${object._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_PARTICIPATION]}">
                                    <div class="right aligned wide column">
                                        <g:if test="${role.isShared}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code: 'property.share.tooltip.on')}">
                                                <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                            </span>

                                        </g:if>
                                        <g:else>
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code: 'property.share.tooltip.off')}">
                                                <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                            </span>
                                        </g:else>

                                    </div>
                                </g:if>
                                <br />
                            </g:if>
                        </g:each>
                    </td>
                </g:each>
            </tr>
        </g:if>


        <tr>
            <td>
                <strong>
                    <i class="${Icon.PROVIDER} la-list-icon la-popup-tooltip" data-content="${message(code: 'provider.label')}"></i>${message(code: 'provider.label')} :
                </strong>
            </td>
            <g:each in="${objects}" var="object">
                <td>
                    <g:each in="${object.providers}" var="provider">
                        <g:link controller="provider" action="show" id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                            <g:if test="${provider.abbreviatedName}">
                                <br /> (${fieldValue(bean: provider, field: "abbreviatedName")})
                            </g:if>
                        </g:link><br />
                    </g:each>
                </td>
            </g:each>
        </tr>

        <tr>
            <td>
                <strong>
                    <i class="${Icon.VENDOR} la-list-icon la-popup-tooltip" data-content="${message(code:'vendor.label')}"></i> ${message(code:'vendor.label')}:
                </strong>
            </td>
            <g:each in="${objects}" var="object">
                <td>
                    <g:each in="${object.vendors}" var="vendor">
                        <g:link controller="vendor" action="show" id="${vendor.id}">
                            ${fieldValue(bean: vendor, field: "name")}
                            <g:if test="${vendor.abbreviatedName}">
                                <br /> (${fieldValue(bean: vendor, field: "abbreviatedName")})
                            </g:if>
                        </g:link><br />
                    </g:each>
                </td>
            </g:each>
        </tr>


        <g:if test="${objects[0] instanceof Subscription}">
            <tr>
                <td>

                    <strong>
                        <i class="${Icon.ACP_PUBLIC}"></i>
                        ${message(code: 'subscription.specificSubscriptionEditors')}:
                    </strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                    <g:each in="${object.providers}" var="provider">
                            <g:if test="${Person.getPublicByOrgAndObjectResp(provider, object, 'Specific subscription editor') ||
                                    Person.getPrivateByOrgAndObjectRespFromAddressbook(provider, object, 'Specific subscription editor')}">

                            <%-- public --%>
                                <g:each in="${Person.getPublicByOrgAndObjectResp(provider, object, 'Specific subscription editor')}"
                                        var="resp">
                                    <span class="la-popup-tooltip"
                                          data-content="${message(code: 'address.public')}"
                                          data-position="top right">
                                        <i class="${Icon.ACP_PUBLIC}"></i>
                                    </span>
                                    ${resp}
                                    (<strong><i class="${Icon.PROVIDER} la-list-icon la-popup-tooltip" data-content="${message(code: 'provider.label')}"></i>:</strong>
                                        <g:link controller="provider" action="show" target="_blank" id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                                        </g:link>)
                                    <br />
                                </g:each>
                            <%-- public --%>
                            <%-- private --%>
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(provider, object, 'Specific subscription editor')}"
                                        var="resp">
                                    <span class="la-popup-tooltip"
                                          data-content="${message(code: 'address.private')}"
                                          data-position="top right">
                                        <i class="${Icon.ACP_PRIVATE}"></i>
                                    </span>
                                    ${resp}
                                    (<strong><i class="${Icon.PROVIDER} la-list-icon la-popup-tooltip" data-content="${message(code: 'provider.label')}"></i>:</strong>
                                    <g:link controller="provider" action="show" target="_blank" id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                                    </g:link>)
                                    <br />
                                </g:each><%-- private --%>
                            </g:if>
                    </g:each>

                    <g:each in="${object.vendors}" var="vendor">
                        <g:if test="${Person.getPublicByOrgAndObjectResp(vendor, object, 'Specific subscription editor') ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(vendor, object, 'Specific subscription editor')}">

                        <%-- public --%>
                            <g:each in="${Person.getPublicByOrgAndObjectResp(vendor, object, 'Specific subscription editor')}"
                                    var="resp">
                                <span class="la-popup-tooltip"
                                      data-content="${message(code: 'address.public')}"
                                      data-position="top right">
                                    <i class="${Icon.ACP_PUBLIC}"></i>
                                </span>
                                ${resp}
                                (<strong><i class="${Icon.VENDOR} la-list-icon la-popup-tooltip" data-content="${message(code:'vendor.label')}"></i>:</strong>
                                <g:link controller="vendor" action="show" target="_blank" id="${vendor.id}">${fieldValue(bean: vendor, field: "name")}
                                </g:link>)
                                <br />
                            </g:each>
                        <%-- public --%>
                        <%-- private --%>
                            <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(vendor, object, 'Specific subscription editor')}"
                                    var="resp">
                                <span class="la-popup-tooltip"
                                      data-content="${message(code: 'address.private')}"
                                      data-position="top right">
                                    <i class="${Icon.ACP_PRIVATE}"></i>
                                </span>
                                ${resp}
                                (<strong><i class="${Icon.VENDOR} la-list-icon la-popup-tooltip" data-content="${message(code:'vendor.label')}"></i>:</strong>
                                <g:link controller="vendor" action="show" target="_blank" id="${vendor.id}">${fieldValue(bean: vendor, field: "name")}
                                </g:link>)
                                <br />
                            </g:each><%-- private --%>
                        </g:if>
                    </g:each>


                    </td>
                </g:each>
            </tr>
        </g:if>

        <g:if test="${objects[0].hasProperty("ids")}">
            <tr>
                <td data-element="copyObject.takeIdentifier.source">
                    <strong><i class="${Icon.IDENTIFIER}"></i>&nbsp;${message(code: 'default.identifiers.label')}:
                    </strong><br />
                </td>
                <g:each in="${objects}" var="object">
                    <td class="center aligned">
                        <g:each in="${object.ids?.sort{it.ns.ns}}" var="ident">
                            <strong>${ident.ns.ns}:</strong>&nbsp;${ident.value}<br />
                        </g:each>
                    </td>
                </g:each>
            </tr>
        </g:if>
        </tbody>
    </table>

</ui:greySegment>



