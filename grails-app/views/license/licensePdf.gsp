<%@ page import="de.laser.utils.DateUtils;de.laser.Links;de.laser.storage.RDStore;de.laser.Person;de.laser.RefdataValue;de.laser.storage.RDConstants;de.laser.Contact;de.laser.Identifier" %>
<laser:serviceInjection/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>${license.reference}</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
            body {
                font-size: 16px;
                font-family: sans-serif;
            }
            .card {
                display: inline-block;
            }
            .green {
                color: #98b500;
            }
            .blue {
                color: #004678;
            }
            .yellow {
                color: #FBBD08;
            }
            .orange {
                color: #fa820a;
            }
            h1 > span {
                font-size: 80%;
                color: rgba(0,0,0, 0.35);
            }
            table {
                margin-top: 3em;
                border-spacing: 0;
                border-collapse: collapse;
                border-width: 0;
            }
            table thead tr {
                text-align: left;
                color: #FFFFFF;
                background-color: #2471a3;
            }
            table thead tr th {
                padding: 1em 0.6em;
                border-color: #2471a3;
                border-bottom: 0.5em solid #FFFFFF;
            }
            table tbody tr.even {
                background-color: #F6F7F7;
            }
            table tbody tr td {
                padding: 0.35em 0.6em;
            }
        </style>
    </head>
    <body>
        <header>
            <h1>LAS:eR-Vertrag ${license.reference} <span>- Stand vom ${DateUtils.getLocalizedSDF_noTime().format( new Date() )}</span></h1>
        </header>
        <article>
            <h2><g:message code="default.identifiers.label"/></h2>
            <g:set var="objectIds" value="${identifierService.prepareIDsForTable(license, institution).objectIds}" />
            <table>
                <g:each in="${objectIds}" var="row">
                    <g:set var="namespace" value="${row.getKey()}"/>
                    <g:each in="${row.getValue()}" var="ident">
                        <tr>
                            <td>
                                ${namespace}
                                <g:if test="${ident instanceof Identifier && ident.ns.getI10n("description")}">
                                    <i>${ident.ns.getI10n("description")}</i>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${ident instanceof Identifier}">
                                    <g:if test="${!ident.instanceOf}">
                                        ${ident.value}
                                    </g:if>
                                    <g:else>${ident.value}</g:else>
                                    <g:if test="${ident.ns.urlPrefix}"><a target="_blank" href="${ident.ns.urlPrefix}${ident.value}"><i title="${ident.ns.getI10n('name')} Link" class="external alternate icon"></i></a></g:if>
                                </g:if>
                                <g:else>
                                    ${ident}
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${ident instanceof Identifier}">
                                    ${ident.note}
                                </g:if>
                            </td>
                            <%--<td>
                                <g:if test="${ident instanceof Identifier}">
                                    <g:if test="${!ident.instanceOf && AuditConfig.getConfig(ident)}">
                                        <span class="green">V</span>
                                    </g:if>
                                    <g:elseif test="${ident.instanceOf}">
                                        <span class="blue">V</span>
                                    </g:elseif>
                                </g:if>
                            </td>--%>
                        </tr>
                    </g:each>
                </g:each>
            </table>
        </article>
        <article>
            <h2><g:message code="default.object.generalInformation"/></h2>
            <section class="card">
                <ul>
                    <li>
                        <strong><g:message code="license.startDate.label"/>: </strong><g:formatDate date="${license.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                        %{--<g:if test="${auditService.getAuditConfig(license, 'startDate')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'startDate')}">
                            <span class="blue">V</span>
                        </g:elseif>--}%
                    </li>
                    <li>
                        <strong><g:message code="license.endDate.label"/>: </strong><g:formatDate date="${license.endDate}" format="${message(code: 'default.date.format.notime')}"/>
                        %{--<g:if test="${auditService.getAuditConfig(license, 'endDate')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'endDate')}">
                            <span class="blue">V</span>
                        </g:elseif>--}%
                    </li>
                    <li>
                        <strong><g:message code="license.openEnded.label"/>: </strong>${license.openEnded.getI10n("value")}
                        %{--<g:if test="${auditService.getAuditConfig(license, 'openEnded')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'openEnded')}">
                            <span class="blue">V</span>
                        </g:elseif>--}%
                    </li>
                </ul>
            </section>
            <section class="card">
                <ul>
                    <li>
                        <strong><g:message code="license.status.label"/>: </strong>${license.status.getI10n("value")}
                        %{--<g:if test="${auditService.getAuditConfig(license, 'status')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'status')}">
                            <span class="blue">V</span>
                        </g:elseif>--}%
                    </li>
                    <li>
                        <strong><g:message code="license.licenseCategory.label"/>: </strong>${license.licenseCategory?.getI10n("value")}
                        %{--<g:if test="${auditService.getAuditConfig(license, 'licenseCategory')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'licenseCategory')}">
                            <span class="blue">V</span>
                        </g:elseif>--}%
                    </li>
                    <g:if test="${license.instanceOf && institution.id == license.getLicensingConsortium().id}">
                        <li>
                            <strong><g:message code="license.linktoLicense"/>: </strong><g:link controller="license" action="show" id="${license.instanceOf.id}" absolute="true">${license.instanceOf.reference}</g:link>
                        </li>
                    </g:if>
                    <li>
                        <strong><g:message code="license.isPublicForApi.label"/>: </strong>${license.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                        %{--<g:if test="${auditService.getAuditConfig(license, 'isPublicForApi')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'isPublicForApi')}">
                            <span class="blue">V</span>
                        </g:elseif>--}%
                    </li>
                </ul>
            </section>
        </article>
        <article>
            <header>
                <h2><g:message code="default.object.links"/></h2>
            </header>
            <g:render template="/templates/links/linksListingPdf"/>
        </article>
        <article>
            <header>
                <h2><g:message code="default.object.properties"/></h2>
            </header>
            <g:render template="/templates/properties/propertiesPdf"/>
        </article>
        <article>
            <header>
                <h2><g:message code="subscription.plural"/></h2>
            </header>
            <g:if test="${linkedSubscriptions}">
                <table>
                    <g:each in="${linkedSubscriptions}" var="subscription">
                        <tr>
                            <td><g:link controller="subscription" action="show" id="${subscription.id}" absolute="true">${subscription.dropdownNamingConvention(institution)}</g:link></td>
                        </tr>
                    </g:each>
                </table>
            </g:if>
        </article>
        <article>
            <header>
                <h2><g:message code="license.details.incoming.childs"/></h2>
            </header>
            <g:if test="${memberLicenses}">
                <table>
                    <g:each in="${memberLicenses}" var="member">
                        <g:set var="subCount" value="${Links.executeQuery('select count(li.id) from Links li where li.sourceLicense = :source and li.linkType = :license and li.destinationSubscription.status = :current',[source: member, license: RDStore.LINKTYPE_LICENSE, current: RDStore.SUBSCRIPTION_CURRENT])}"/>
                        <tr>
                            <%
                                String memberRunningTimes = " - ${member.status.getI10n("value")}"
                                if(member.startDate)
                                    memberRunningTimes += " (${formatDate(date: member.startDate, format: message(code: 'default.date.format.notime'))}-"
                                if(member.endDate)
                                    memberRunningTimes += " ${formatDate(date: member.endDate, format: message(code: 'default.date.format.notime'))})"
                                else memberRunningTimes += ")"
                            %>
                            <td><g:link controller="license" action="show" id="${member.id}" absolute="true">${member.reference} ${memberRunningTimes}</g:link></td>
                            <td>(${subCount[0]} <g:message code="consortium.member.plural"/>)</td>
                        </tr>
                    </g:each>
                </table>
            </g:if>
        </article>
        <article>
            <header>
                <h2><g:message code="task.plural"/></h2>
            </header>
            <g:if test="${tasks}">
                <table>
                    <g:each in="${tasks}" var="task">
                        <tr>
                            <td>${task.title}</td>
                            <td><i><g:message code="task.endDate.label"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${task.endDate}"/></i></td>
                        </tr>
                    </g:each>
                </table>
            </g:if>
        </article>
        <article>
            <header>
                <h2><g:message code="license.documents"/></h2>
            </header>
            <g:if test="${documents.filteredDocuments}">
                <table>
                    <g:each in="${documents.filteredDocuments}" var="docctx">
                        <tr>
                            <td>
                                <g:if test="${docctx.owner.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:elseif test="${docctx.owner.filename}">
                                    ${docctx.owner.filename}
                                </g:elseif>
                            </td>
                            <td>
                                 ${docctx.getDocType()?.getI10n("value")}
                            </td>
                            %{--<td>
                                <g:if test="${docctx.isShared}">
                                    <span class="green">G</span>
                                </g:if>
                            </td>--}%
                        </tr>
                    </g:each>
                </table>
            </g:if>
        </article>
        <g:if test="${documents.sharedItems}">
            <article>
                <header>
                    <h2><g:message code="license.documents.shared"/></h2>
                </header>
                <g:if test="${documents.sharedItems}">
                    <table>
                        <g:each in="${documents.sharedItems}" var="docctx">
                            <tr>
                                <td>
                                    <g:if test="${docctx.owner.title}">
                                        ${docctx.owner.title}
                                    </g:if>
                                    <g:elseif test="${docctx.owner?.filename}">
                                        ${docctx.owner.filename}
                                    </g:elseif>
                                </td>
                                <td>
                                    ${docctx.getDocType()?.getI10n("value")}
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </g:if>
            </article>
        </g:if>
        <article>
            <header>
                <h2><g:message code="license.notes"/></h2>
            </header>
            <g:if test="${notes.filteredDocuments}">
                <table>
                    <g:each in="${notes.filteredDocuments}" var="note">
                        <tr>
                            <td>
                                <g:if test="${note.owner.title}">
                                    ${note.owner.title}
                                </g:if>
                                <g:else>
                                    <g:message code="license.notes.noTitle"/>
                                </g:else>
                            </td>
                            <td>
                                <g:message code="template.notes.created"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${note.owner.dateCreated}"/>
                            </td>
                            %{--<td>
                                <g:if test="${note.sharedFrom}">
                                    <span class="green">G</span>
                                </g:if>
                            </td>--}%
                        </tr>
                    </g:each>
                </table>
            </g:if>
        </article>
        <g:if test="${notes.sharedItems}">
            <article>
                <header>
                    <h2><g:message code="license.notes.shared"/></h2>
                </header>
                <g:if test="${notes.sharedItems}">
                    <table>
                        <g:each in="${notes.sharedItems}" var="note">
                            <tr>
                                <td>
                                    <g:if test="${note.owner.title}">
                                        ${note.owner.title}
                                    </g:if>
                                    <g:else>
                                        <g:message code="license.notes.noTitle"/>
                                    </g:else>
                                </td>
                                <td>
                                    <g:message code="template.notes.created"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${note.owner.dateCreated}"/>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </g:if>
            </article>
        </g:if>
        <article>
            <header>
                <h2><g:message code="license.licensor.label"/></h2>
                <table>
                    <g:each in="${visibleOrgRelations}" var="role">
                        <g:if test="${role.org}">
                            <tr>
                                <td>
                                    <g:link controller="organisation" action="show" id="${role.org.id}" absolute="true">${role.org.name}</g:link> <i>${role.roleType.getI10n("value")}</i>
                                </td>
                            </tr>
                            <g:if test="${(Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                                    Person.getPublicByOrgAndObjectResp(role.org, license, 'Specific license editor') ||
                                    Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', institution) ||
                                    Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, license, 'Specific license editor', institution))}">
                                <%-- public --%>
                                <g:if test="${ Person.getPublicByOrgAndFunc(role.org, 'General contact person') || Person.getPublicByOrgAndObjectResp(role.org, license, 'Specific license editor')  }">
                                    <g:each in="${Person.getPublicByOrgAndFunc(role.org, 'General contact person')}" var="func">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.public')}</i>
                                            </td>
                                            <td>
                                                ${RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION).getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${institution.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO'] ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentType(func, RDStore.CCT_EMAIL)}" var="email">
                                                        <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                    <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, license, 'Specific license editor')}" var="resp">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.public')}</i>
                                            </td>
                                            <td>
                                                ${RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION).getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${institution.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO'] ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${resp}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentType(resp, RDStore.CCT_EMAIL)}" var="email">
                                                        <li>${email.contentType.'value'} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                </g:if>
                                <%-- public --%>
                                <%-- private --%>
                                <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', institution) || Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, license, 'Specific license editor', institution)}">
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextOrg)}" var="func">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.private')}</i>
                                            </td>
                                            <td>
                                                ${RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION).getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${institution.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO'] ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentType(func, RDStore.CCT_EMAIL)}" var="email">
                                                        <li>${email.contentType.'value'} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, license, 'Specific license editor', institution)}" var="resp">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.private')}</i>
                                            </td>
                                            <td>
                                                ${RefdataValue.getByValue('Specific license editor').getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${institution.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO'] ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${resp}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentType(resp, RDStore.CCT_EMAIL)}" var="email">
                                                        <li>${email.contentType.'value'} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                </g:if>
                                <%-- private --%>
                            </g:if>
                        </g:if>
                    </g:each>
                </table>
            </header>
        </article>
    </body>
</html>

