<%@ page import="de.laser.storage.RDStore; grails.converters.JSON;de.laser.OrgRole" %>
<laser:htmlStart message="myinst.subscriptionImport.post.title" serviceInjection="true"/>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.institutions.subscriptionImport" class="active"/>
        </semui:breadcrumbs>
        <br />
        <semui:messages data="${flash}" />
        <h2 class="ui header"><g:message code="myinst.subscriptionImport.post.header2"/></h2>
        <h3 class="ui header"><g:message code="myinst.subscriptionImport.post.header3"/></h3>
    <semui:form>
        <g:form name="subscriptionParameter" action="addSubscriptions" controller="subscription" method="post">
            <g:hiddenField name="candidates" value="${candidates.keySet() as JSON}"/>
            <table class="ui striped table">
                <thead>
                    <tr>
                        <th rowspan="2"></th>
                        <th rowspan="2"><g:message code="default.subscription.label"/></th>
                        <th><g:message code="myinst.subscriptionImport.post.takeItem"/></th>
                    </tr>
                    <tr>
                        <th><g:message code="myinst.subscriptionImport.post.takeAllItems"/> <g:checkBox name="takeAll"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${candidates.entrySet()}" var="row" status="r">
                        <tr>
                            <td>${r+1}</td>
                            <td>
                                <ul>
                                    <g:set var="sub" value="${row.getKey()}"/>
                                    <g:set var="errors" value="${row.getValue()}"/>
                                    <li><g:message code="default.name.label"/>: ${sub.name}</li>
                                    <li><g:message code="license.label"/>:
                                        <g:each in="${sub.licenses}" var="licenseKey">
                                            <g:set var="license" value="${genericOIDService.resolveOID(licenseKey)}"/>
                                            ${license.reference}
                                        </g:each>
                                    </li>
                                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM") && sub.instanceOf && sub.member}">
                                        <li><g:message code="myinst.subscriptionImport.instanceOf" args="${parentSubType}"/>: ${genericOIDService.resolveOID(sub.instanceOf)}</li>
                                        <li><g:message code="myinst.subscriptionImport.member"/>: ${genericOIDService.resolveOID(sub.member)}</li>
                                    </g:if>
                                    <li><g:message code="default.status.label"/>: ${genericOIDService.resolveOID(sub.status)?.getI10n('value')}</li>
                                    <li><g:message code="default.type.label"/>: ${genericOIDService.resolveOID(sub.kind)?.getI10n('value')}</li>
                                    <li><g:message code="myinst.subscriptionImport.form"/>: ${genericOIDService.resolveOID(sub.form)?.getI10n('value')}</li>
                                    <li><g:message code="myinst.subscriptionImport.resource"/>: ${genericOIDService.resolveOID(sub.resource)?.getI10n('value')}</li>
                                    <li><g:message code="myinst.subscriptionImport.provider"/>: ${genericOIDService.resolveOID(sub.provider)}</li>
                                    <li><g:message code="myinst.subscriptionImport.agency"/>: ${genericOIDService.resolveOID(sub.agency)}</li>
                                    <li><g:message code="myinst.subscriptionImport.startDate"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.startDate}"/></li>
                                    <li><g:message code="myinst.subscriptionImport.endDate"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.endDate}"/></li>
                                    <li><g:message code="myinst.subscriptionImport.manualCancellationDate"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.manualCancellationDate}"/></li>
                                    <li><g:message code="myinst.subscriptionImport.hasPerpetualAccess"/>: ${sub.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</li>
                                    <li><g:message code="myinst.subscriptionImport.hasPublishComponent"/>: ${sub.hasPublishComponent ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</li>
                                    <li><g:message code="myinst.subscriptionImport.isPublicForApi"/>: ${sub.isPublicForApi ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</li>
                                    <li>
                                        <g:message code="properties"/>:
                                        <ul>
                                            <g:each in="${sub.properties?.entrySet()}" var="prop">
                                                <g:if test="${prop.getValue().propValue}">
                                                    <%
                                                        String value = genericOIDService.resolveOID(prop.getValue().propValue)?.getI10n("value")
                                                        if(!value)
                                                            value = prop.getValue().propValue
                                                    %>
                                                    <li>${genericOIDService.resolveOID(prop.getKey()).getI10n("name")}: ${value} (${prop.getValue().propNote ?: 'Keine Anmerkung'})</li>
                                                </g:if>
                                            </g:each>
                                        </ul>
                                    </li>
                                    <li>
                                        <g:message code="myinst.subscriptionImport.notes"/>: ${sub.notes}
                                    </li>
                                </ul>

                                <g:if test="${errors}">
                                <div class="item red">
                                    <i class="bug icon red"></i><g:message code="default.error"/>:
                                    <div class="content">
                                        <div class="description">
                                            <ul>
                                                <g:each in="${errors}" var="error">
                                                    <g:if test="${error.getKey() in criticalErrors}">
                                                        <g:set var="withCriticalErrors" value="true"/>
                                                    </g:if>
                                                    <%
                                                        List args
                                                        if(!(error.getValue() instanceof List))
                                                            args = [error.getValue()]
                                                        else args = error.getValue()
                                                    %>
                                                    <li>${message(code:"myinst.subscriptionImport.post.error.${error.getKey()}",args:args)}</li>
                                                </g:each>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                                </g:if>
                            </td>
                            <td class="center aligned">
                                <g:if test="${!withCriticalErrors}">
                                    <g:checkBox name="take${r}" class="ciSelect ui large checkbox"/>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="3">
                            <input type="submit" class="ui button primary" value="${message(code:'default.button.save.label')}">
                            <g:link action="subscriptionImport" class="ui button"><g:message code="default.button.back"/></g:link>
                        </td>
                    </tr>
                </tfoot>
            </table>
        </g:form>
    </semui:form>

    <laser:script file="${this.getGroovyPageFileName()}">
            $("#takeAll").change(function(){
                if($(this).is(":checked")) {
                    $(".ciSelect").prop('checked',true);
                }
                else {
                    $(".ciSelect").prop('checked',false);
                }
            });
    </laser:script>
<laser:htmlEnd />