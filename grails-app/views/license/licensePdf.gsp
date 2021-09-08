<%@ page import="de.laser.reporting.export.base.BaseExport;de.laser.reporting.myInstitution.GenericHelper;de.laser.helper.DateUtils;de.laser.Links;de.laser.helper.RDStore" %>
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
            <h1>LAS:eR-Vertrag ${license.reference} <span>- Stand vom ${DateUtils.getSDF_NoTime().format( new Date() )}</span></h1>
        </header>
        <article>
            <h2><g:message code="default.identifiers.label"/></h2>

        </article>
        <article>
            <h2><g:message code="default.object.generalInformation"/></h2>
            <section class="card">
                <ul>
                    <li>
                        <strong><g:message code="license.startDate.label"/>: </strong><g:formatDate date="${license.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                        <g:if test="${auditService.getAuditConfig(license, 'startDate')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'startDate')}">
                            <span class="blue">V</span>
                        </g:elseif>
                    </li>
                    <li>
                        <strong><g:message code="license.endDate.label"/>: </strong><g:formatDate date="${license.endDate}" format="${message(code: 'default.date.format.notime')}"/>
                        <g:if test="${auditService.getAuditConfig(license, 'endDate')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'endDate')}">
                            <span class="blue">V</span>
                        </g:elseif>
                    </li>
                    <li>
                        <strong><g:message code="license.openEnded.label"/>: </strong>${license.openEnded.getI10n("value")}
                        <g:if test="${auditService.getAuditConfig(license, 'openEnded')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'openEnded')}">
                            <span class="blue">V</span>
                        </g:elseif>
                    </li>
                </ul>
            </section>
            <section class="card">
                <ul>
                    <li>
                        <strong><g:message code="license.status.label"/>: </strong>${license.status.getI10n("value")}
                        <g:if test="${auditService.getAuditConfig(license, 'status')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'status')}">
                            <span class="blue">V</span>
                        </g:elseif>
                    </li>
                    <li>
                        <strong><g:message code="license.licenseCategory.label"/>: </strong>${license.licenseCategory.getI10n("value")}
                        <g:if test="${auditService.getAuditConfig(license, 'licenseCategory')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'licenseCategory')}">
                            <span class="blue">V</span>
                        </g:elseif>
                    </li>
                    <g:if test="${license.instanceOf && institution.id == license.getLicensingConsortium().id}">
                        <li>
                            <strong><g:message code="license.linktoLicense"/>: </strong><g:link controller="license" action="show" id="${license.instanceOf.id}">${license.instanceOf.reference}</g:link>
                        </li>
                    </g:if>
                    <li>
                        <strong><g:message code="license.isPublicForApi.label"/>: </strong>
                        <g:if test="${auditService.getAuditConfig(license, 'isPublicForApi')}">
                            <span class="green">V</span>
                        </g:if>
                        <g:elseif test="${auditService.getAuditConfig(license.instanceOf, 'isPublicForApi')}">
                            <span class="blue">V</span>
                        </g:elseif>
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
                            <td>${subscription.dropdownNamingConvention(institution)}</td>
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
                            <td>${member.reference} ${memberRunningTimes}</td>
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
                                 ${docctx.owner.type?.getI10n("value")}
                            </td>
                            <td>
                                <g:if test="${docctx.isShared}">
                                    <span class="green">G</span>
                                </g:if>
                            </td>
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
                                    ${docctx.owner.type?.getI10n("value")}
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
                            <td>
                                <g:if test="${note.sharedFrom}">
                                    <span class="green">G</span>
                                </g:if>
                            </td>
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
            </header>
        </article>
    </body>
</html>

