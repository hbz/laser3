<%@page import="de.laser.storage.RDStore; de.laser.Subscription; de.laser.License" %>
<laser:serviceInjection/>
<section>
    <header>
        <h3>
            <g:if test="${subscriptionLicenseLink}">
                <g:message code="license.plural"/>
            </g:if>
            <g:elseif test="${subscription}">
                <g:message code="subscription.details.linksHeader"/>
            </g:elseif>
            <g:elseif test="${license}">
                <g:message code="license.details.linksHeader"/>
            </g:elseif>
        </h3>
    </header>
    <g:if test="${links.entrySet()}">
        <table>
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
                                <th>${pair.licenseCategory?.getI10n("value")}</th>
                            </g:if>
                            <g:else>
                                <th>${genericOIDService.resolveOID(linkTypes.getKey()).getI10n("value").split("\\|")[perspectiveIndex]}</th>
                            </g:else>
                            <td>
                                <g:if test="${pair instanceof Subscription}">
                                    <g:link controller="subscription" action="show" id="${pair.id}" absolute="true">
                                        ${pair.name}
                                    </g:link>
                                </g:if>
                                <g:elseif test="${pair instanceof License}">
                                    <g:link controller="license" action="show" id="${pair.id}" absolute="true">
                                        ${pair.reference} (${pair.status.getI10n("value")})
                                    </g:link>
                                </g:elseif>
                            </td>
                            <td>
                                <g:formatDate date="${pair.startDate}" format="${message(code:'default.date.format.notime')}"/>–<g:formatDate date="${pair.endDate}" format="${message(code:'default.date.format.notime')}"/>
                            </td>
                            <td>
                                <g:set var="comment" value="${link.document}"/>
                                <g:if test="${comment}">
                                    <em>${comment.owner.content}</em>
                                </g:if>
                            </td>
                        </tr>
                        <%-- once the subscription export comes, we need or may need to display here the license's properties --%>
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
</section>