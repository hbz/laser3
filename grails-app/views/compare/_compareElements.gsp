<%@ page import="de.laser.Subscription; de.laser.storage.RDStore; de.laser.AuditConfig; de.laser.interfaces.CalculatedType; de.laser.Person; de.laser.License" %>
<laser:serviceInjection/>

<ui:form>

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
                                    if (object.isSlaved) {
                                        println '<span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                    } else {
                                        println '<span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
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
                    <strong><i class="balance scale icon"></i>${message(code: 'license.label')}:</strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                        <article class="la-readmore">
                        <g:each in="${object.getLicenses()?.sort{it.reference}}" var="license">
                            <g:if test="${contextOrg.id in license.orgRelations?.org.id}">
                            <strong><i
                                    class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:
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
                    <strong><i class="clipboard icon"></i>${message(code: 'subscription.label')}:</strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                        <article class="la-readmore">
                        <g:each in="${object.getSubscriptions()?.sort{it.name}}" var="subscription">
                            <g:if test="${contextOrg.id in subscription.orgRelations?.org.id}">
                            <strong><i
                                    class="clipboard icon"></i>&nbsp${subscription.kind?.getI10n("value")}:
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
                            class="university icon"></i>&nbsp${message(code: "${objects[0].getClass().getSimpleName().toLowerCase()}.organisations.label")}:
                    </strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                        <g:each in="${object.orgRelations?.sort{it.org.name}}" var="role">
                            <g:if test="${(role.roleType in [RDStore.OR_AGENCY, RDStore.OR_PROVIDER, RDStore.OR_LICENSOR, RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_SUBSCRIPTION_CONSORTIA]) && role.org.id != contextOrg.id}">
                                <strong><i class="university icon"></i>&nbsp${role.roleType.getI10n("value")}:
                                </strong>
                                <g:link controller="organisation" action="show" target="_blank"
                                        id="${role.org.id}">
                                    ${role.org.name}
                                </g:link>
                                <g:if test="${object._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_PARTICIPATION]}">
                                    <div class="right aligned wide column">
                                        <g:if test="${role.isShared}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'property.share.tooltip.on')}">
                                                <i class="la-share icon la-js-editmode-icon"></i>
                                            </span>

                                        </g:if>
                                        <g:else>
                                            <span data-position="top left" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'property.share.tooltip.off')}">
                                                <i class="la-share slash icon la-js-editmode-icon"></i>
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

        <g:if test="${objects[0] instanceof Subscription}">
            <tr>
                <td>

                    <strong>
                        <i class="address card icon"></i>
                        ${message(code: 'subscription.specificSubscriptionEditors')}:
                    </strong>
                </td>
                <g:each in="${objects}" var="object">
                    <td>
                    <g:each in="${object.orgRelations?.sort{it.org.name}}" var="role">
                        <g:if test="${role.roleType in [RDStore.OR_AGENCY, RDStore.OR_PROVIDER, RDStore.OR_LICENSOR]}">
                            <g:if test="${Person.getPublicByOrgAndObjectResp(role.org, object, 'Specific subscription editor') ||
                                    Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, object, 'Specific subscription editor', contextOrg)}">

                            <%-- public --%>
                                <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, object, 'Specific subscription editor')}"
                                        var="resp">
                                    <span class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'address.public')}"
                                          data-position="top right">
                                        <i class="address card icon"></i>
                                    </span>
                                    ${resp}
                                    (<strong><i
                                        class="university icon"></i>&nbsp${role.roleType.getI10n("value")}:
                                </strong>
                                    <g:link controller="organisation" action="show" target="_blank"
                                            id="${role.org.id}">${role.org.name}</g:link>)
                                            </div>
                                    <br />
                                </g:each>
                            <%-- public --%>
                            <%-- private --%>
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, object, 'Specific subscription editor', contextOrg)}"
                                        var="resp">
                                    <span class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'address.private')}"
                                          data-position="top right">
                                        <i class="address card outline icon"></i>
                                    </span>
                                    ${resp}
                                    (<strong><i
                                        class="university icon"></i>&nbsp${role.roleType.getI10n("value")}:
                                </strong>
                                    <g:link controller="organisation" action="show" target="_blank"
                                            id="${role.org.id}">${role.org.name}</g:link>)
                                            </div>
                                    <br />
                                </g:each><%-- private --%>
                            </g:if>
                        </g:if>
                    </g:each>
                    </td>
                </g:each>
            </tr>
        </g:if>

        <g:if test="${objects[0].hasProperty("ids")}">
            <tr>
                <td name="copyObject.takeIdentifier.source">
                    <strong><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:
                    </strong><br />
                </td>
                <g:each in="${objects}" var="object">
                    <td class="center aligned">
                        <g:each in="${object.ids?.sort{it.ns.ns}}" var="ident">
                            <strong>${ident.ns.ns}:</strong>&nbsp${ident.value}<br />
                        </g:each>
                    </td>
                </g:each>
            </tr>
        </g:if>
        </tbody>
    </table>

</ui:form>



