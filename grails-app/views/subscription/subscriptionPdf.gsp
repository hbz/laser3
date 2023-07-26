<%@ page import="de.laser.utils.DateUtils;de.laser.Links;de.laser.storage.RDStore;de.laser.Person;de.laser.RefdataValue;de.laser.storage.RDConstants;de.laser.Contact;de.laser.Identifier;de.laser.interfaces.CalculatedType;de.laser.remote.ApiSource" %>
<laser:serviceInjection/>
<g:set var="apiSource" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>${subscription.name}</title>
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
            <h1>LAS:eR-Lizenz ${subscription.name} <span>- Stand vom ${DateUtils.getLocalizedSDF_noTime().format( new Date() )}</span></h1>
        </header>
        <article>
            <h2><g:message code="default.identifiers.label"/></h2>
            <g:set var="objectIds" value="${identifierService.prepareIDsForTable(subscription, institution).objectIds}" />
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
                        <strong><g:message code="subscription.startDate.label"/>: </strong><g:formatDate date="${subscription.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                    </li>
                    <li>
                        <strong><g:message code="subscription.endDate.label"/>: </strong><g:formatDate date="${subscription.endDate}" format="${message(code: 'default.date.format.notime')}"/>
                    </li>
                    <li>
                        <strong><g:message code="subscription.manualCancellationDate.label"/>: </strong><g:formatDate date="${subscription.manualCancellationDate}" format="${message(code: 'default.date.format.notime')}"/>
                    </li>
                    <li>
                        <strong><g:message code="subscription.referenceYear.label"/>: </strong>${subscription.referenceYear}
                    </li>
                    <li>
                        <strong><g:message code="subscription.status.label"/>: </strong>${subscription.status.getI10n("value")}
                    </li>
                    <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL && subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) ||
                            (subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL && subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                        <li>
                            <strong><g:message code="subscription.isMultiYear.label"/>: </strong>${subscription.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                        </li>
                    </g:if>
                    <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL && subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                        <li>
                            <strong><g:message code="subscription.isAutomaticRenewAnnually.label"/>: </strong>${subscription.isAutomaticRenewAnnually ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                        </li>
                    </g:if>
                </ul>
            </section>
            <section class="card">
                <ul>
                    <li>
                        <strong><g:message code="subscription.kind.label"/>: </strong>${subscription.kind?.getI10n("value")}
                    </li>
                    <li>
                        <strong><g:message code="subscription.form.label"/>: </strong>${subscription.form?.getI10n("value")}
                    </li>
                    <li>
                        <strong><g:message code="subscription.resource.label"/>: </strong>${subscription.resource?.getI10n("value")}
                    </li>
                    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia().id}">
                        <li>
                            <strong><g:message code="subscription.isInstanceOfSub.label"/>: </strong><g:link controller="subscription" action="show" id="${subscription.instanceOf.id}" absolute="true">${subscription.instanceOf.name}</g:link>
                        </li>
                    </g:if>
                    <li>
                        <strong><g:message code="subscription.isPublicForApi.label"/>: </strong>${subscription.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                    </li>
                    <li>
                        <strong><g:message code="subscription.hasPerpetualAccess.label"/>: </strong>${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                    </li>
                    <li>
                        <strong><g:message code="subscription.hasPublishComponent.label"/>: </strong>${subscription.hasPublishComponent ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                    </li>
                    <li>
                        <strong><g:message code="subscription.holdingSelection.label"/>: </strong>${subscription.holdingSelection ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                    </li>
                </ul>
            </section>
        </article>
        <article>
            <g:if test="${subscription.packages}">
                <header>
                    <h2><g:message code="subscription.packages.label"/></h2>
                </header>
                <table>
                    <%-- possible bottleneck!!!! --%>
                    <g:each in="${subscription.packages}" var="sp">
                        <tr>
                            <td><a href="${apiSource.baseUrl}/resource/show/${sp.pkg.gokbId}">${sp.pkg.name}</a></td>
                            <td>(<g:message code="platform"/>: <a href="${apiSource.baseUrl}/resource/show/${sp.pkg.nominalPlatform.gokbId}">${sp.pkg.nominalPlatform.name}</a>)</td>
                        </tr>
                    </g:each>
                </table>
            </g:if>
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
                <h2><g:message code="license.plural"/></h2>
            </header>
            <g:if test="${linkedLicenses}">
                <table>
                    <g:each in="${linkedLicenses}" var="license">
                        <tr>
                            <td><g:link controller="license" action="show" id="${license.id}" absolute="true">${license.dropdownNamingConvention()}</g:link></td>
                        </tr>
                    </g:each>
                </table>
            </g:if>
        </article>
        <g:if test="${memberSubscriptions}">
            <article>
                <header>
                    <h2><g:message code="subscription.member.plural"/></h2>
                </header>
                <table>
                    <g:each in="${memberSubscriptions}" var="member">
                        <tr>
                            <%
                                String memberRunningTimes = " - ${member.status.getI10n("value")}"
                                if(member.startDate)
                                    memberRunningTimes += " (${formatDate(date: member.startDate, format: message(code: 'default.date.format.notime'))}-"
                                if(member.endDate)
                                    memberRunningTimes += " ${formatDate(date: member.endDate, format: message(code: 'default.date.format.notime'))})"
                                else memberRunningTimes += ")"
                            %>
                            <td><g:link controller="subscription" action="show" id="${member.id}" absolute="true">${member.getSubscriber().sortname} ${memberRunningTimes}</g:link></td>
                        </tr>
                    </g:each>
                </table>
            </article>
        </g:if>
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
                                ${raw(note.owner.content)}
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
                                    ${raw(note.owner.content)}
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
                <h2><g:message code="default.ProviderAgency.singular"/></h2>
                <table>
                    <g:each in="${visibleOrgRelations}" var="role">
                        <g:if test="${role.org}">
                            <%
                                Set<Person> techSupports = [], serviceSupports = [], metadataContacts = [], privateTechSupports = [], privateServiceSupports = [], privateMetadataContacts = []
                                boolean contactsExWekb = false
                                if(role.org.gokbId) {
                                    contactsExWekb = true
                                    techSupports.addAll(Person.getPublicByOrgAndFunc(role.org, 'Technical Support', role.org))
                                    serviceSupports.addAll(Person.getPublicByOrgAndFunc(role.org, 'Service Support', role.org))
                                    metadataContacts.addAll(Person.getPublicByOrgAndFunc(role.org, 'Metadata Contact', role.org))
                                }
                                else {
                                    techSupports.addAll(Person.getPublicByOrgAndFunc(role.org, 'Technical Support'))
                                    serviceSupports.addAll(Person.getPublicByOrgAndFunc(role.org, 'Service Support'))
                                    metadataContacts.addAll(Person.getPublicByOrgAndFunc(role.org, 'Metadata Contact'))
                                }
                                privateTechSupports.addAll(Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Technical Support', institution))
                                privateServiceSupports.addAll(Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Service Support', institution))
                                privateMetadataContacts.addAll(Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Metadata Contact', institution))
                            %>
                            <tr>
                                <td>
                                    <g:link controller="organisation" action="show" id="${role.org.id}" absolute="true">${role.org.name}</g:link> <i>${role.roleType.getI10n("value")}</i>
                                </td>
                            </tr>
                            <g:if test="${(Person.getPublicByOrgAndFunc(role.org, 'General contact person') || techSupports || serviceSupports || metadataContacts ||
                                    Person.getPublicByOrgAndObjectResp(role.org, subscription, 'Specific subscription editor') ||
                                    Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', institution) ||
                                    privateTechSupports || privateServiceSupports || privateMetadataContacts ||
                                    Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, subscription, 'Specific subscription editor', institution))}">
                                <%-- public --%>
                                <g:each in="${techSupports}" var="func">
                                    <tr>
                                        <td>
                                            <i>
                                                <g:if test="${contactsExWekb}">
                                                    <a href="${apiSource.baseUrl}/resource/show/${role.org.gokbId}"><g:message code="org.isWekbCurated.header.label"/> (we:kb Link)</a>
                                                </g:if>
                                                <g:else>
                                                    <g:message code="address.public"/>
                                                </g:else>
                                            </i>
                                        </td>
                                        <td>
                                            ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                        </td>
                                        <td>
                                            <ul>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                    <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                </g:each>
                                            </ul>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:each in="${serviceSupports}" var="func">
                                    <tr>
                                        <td>
                                            <i>
                                                <g:if test="${contactsExWekb}">
                                                    <a href="${apiSource.baseUrl}/resource/show/${role.org.gokbId}"><g:message code="org.isWekbCurated.header.label"/> (we:kb Link)</a>
                                                </g:if>
                                                <g:else>
                                                    <g:message code="address.public"/>
                                                </g:else>
                                            </i>
                                        </td>
                                        <td>
                                            ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                        </td>
                                        <td>
                                            <ul>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                    <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                </g:each>
                                            </ul>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:each in="${metadataContacts}" var="func">
                                    <tr>
                                        <td>
                                            <i>
                                                <g:if test="${contactsExWekb}">
                                                    <a href="${apiSource.baseUrl}/resource/show/${role.org.gokbId}"><g:message code="org.isWekbCurated.header.label"/> (we:kb Link)</a>
                                                </g:if>
                                                <g:else>
                                                    <g:message code="address.public"/>
                                                </g:else>
                                            </i>
                                        </td>
                                        <td>
                                            ${RDStore.PRS_FUNC_METADATA.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                        </td>
                                        <td>
                                            <ul>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                    <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                </g:each>
                                            </ul>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:if test="${ Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                                        Person.getPublicByOrgAndObjectResp(role.org, subscription, 'Specific subscription editor')  }">

                                    <g:each in="${Person.getPublicByOrgAndFunc(role.org, 'General contact person')}" var="func">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.public')}</i>
                                            </td>
                                            <td>
                                                ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                        <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                    <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, subscription, 'Specific subscription editor')}" var="resp">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.public')}</i>
                                            </td>
                                            <td>
                                                ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${resp}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(resp, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                        <li>${email.contentType.'value'} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                </g:if>
                                <%-- public --%>
                                <%-- private --%>
                                <g:each in="${privateTechSupports}" var="func">
                                    <tr>
                                        <td>
                                            <i>
                                                <g:message code="address.private"/>
                                            </i>
                                        </td>
                                        <td>
                                            ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                        </td>
                                        <td>
                                            <ul>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                    <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                </g:each>
                                            </ul>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:each in="${privateServiceSupports}" var="func">
                                    <tr>
                                        <td>
                                            <i>
                                                <g:message code="address.private"/>
                                            </i>
                                        </td>
                                        <td>
                                            ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                        </td>
                                        <td>
                                            <ul>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                    <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                </g:each>
                                            </ul>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:each in="${privateMetadataContacts}" var="func">
                                    <tr>
                                        <td>
                                            <i>
                                                <g:message code="address.private"/>
                                            </i>
                                        </td>
                                        <td>
                                            ${RDStore.PRS_FUNC_METADATA.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                        </td>
                                        <td>
                                            <ul>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                    <li>${email.contentType.value} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                </g:each>
                                            </ul>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', institution) || Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, subscription, 'Specific subscription editor', institution)}">
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextOrg)}" var="func">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.private')}</i>
                                            </td>
                                            <td>
                                                ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${func}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(func, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
                                                        <li>${email.contentType.'value'} ${email.content} <g:if test="${email.language}">(${email.language.getI10n("value")})</g:if></li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                    </g:each>
                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, subscription, 'Specific subscription editor', institution)}" var="resp">
                                        <tr>
                                            <td>
                                                <i>${message(code:'address.private')}</i>
                                            </td>
                                            <td>
                                                ${RefdataValue.getByValue('Specific license editor').getI10n('value')}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) ? 'addressbook' : 'show'}" params="[id: role.org.id]" absolute="true">${resp}</g:link>
                                            </td>
                                            <td>
                                                <ul>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(resp, [RDStore.CCT_EMAIL, RDStore.CCT_URL])}" var="email">
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
        <g:if test="${costItemSums.ownCosts || costItemSums.consCosts || costItemSums.subscrCosts}">
            <article>
                <g:if test="${costItemSums.ownCosts}">
                    <g:if test="${(institution.id != subscription.getConsortia()?.id && subscription.instanceOf) || !subscription.instanceOf}">
                        <h2>${message(code: 'financials.label')}: ${message(code: 'financials.tab.ownCosts')}</h2>
                        <g:render template="financialsPdf" model="[data: costItemSums.ownCosts]"/>
                    </g:if>
                </g:if>
                <g:if test="${costItemSums.consCosts}">
                    <h2>${message(code: 'financials.label')}: ${message(code: 'financials.tab.consCosts')}</h2>
                    <g:render template="financialsPdf" model="[data: costItemSums.consCosts]"/>
                </g:if>
                <g:elseif test="${costItemSums.subscrCosts}">
                    <h2>${message(code: 'financials.label')}: ${message(code: 'financials.tab.subscrCosts')}</h2>
                    <g:render template="financialsPdf" model="[data: costItemSums.subscrCosts]"/>
                </g:elseif>
            </article>
        </g:if>
    </body>
</html>

