<%@ page import="de.laser.AuditConfig;de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType; de.laser.helper.RDStore;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} ${message(code: 'myinst.renewals')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <semui:crumb action="show" controller="subscription" id="${subscription.id}" text="${subscription.name}"/>
    <semui:crumb message="myinst.renewals" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'myinst.renewals')}: ${subscription.name}</h1>

<semui:messages data="${flash}"/>

<semui:form>
    <g:form action="processRenewSubscription" method="post" params="${params}">

        <div>
            <table class="ui celled la-js-responsive-table la-table table">
                <tbody>

                <tr>
                    <th>${message(code: 'myinst.renewalUpload.props')}</th>
                    <th>${message(code: 'default.value.label')}</th>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <th>${message(code: 'copyElementsIntoObject.audit')}</th>
                    </g:if>
                </tr>
                <tr>
                    <th>${message(code: 'myinst.emptySubscription.name')}</th>
                    <td>
                        <div class="ui form field">
                            <input type="text" name="subscription.name" value="${permissionInfo?.sub_name}">
                        </div>
                    </td>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <td class="center aligned">
                            <div class="ui checkbox">
                                <input type="checkbox" name="auditList"
                                       value="name" ${AuditConfig.getConfig(subscription, 'name') ? 'checked' : ''}/>
                            </div>
                        </td>
                    </g:if>

                </tr>
                <tr>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <td><semui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date"
                                          placeholder="default.date.label" value="${permissionInfo?.sub_startDate}"
                                          required=""/></td>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <td class="center aligned">
                            <div class="ui checkbox">
                                <input type="checkbox" name="auditList"
                                       value="startDate" ${AuditConfig.getConfig(subscription, 'startDate') ? 'checked' : ''}/>
                            </div>
                        </td>
                    </g:if>
                </tr>
                <tr>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <td><semui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date"
                                          placeholder="default.date.label" value="${permissionInfo?.sub_endDate}"/></td>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <td class="center aligned">
                            <div class="ui checkbox">
                                <input type="checkbox" name="auditList"
                                       value="endDate" ${AuditConfig.getConfig(subscription, 'endDate') ? 'checked' : ''}/>
                            </div>
                        </td>
                    </g:if>
                </tr>
                <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                        subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) ||
                        (subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL &&
                                subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                    <tr>
                        <th>${message(code: 'subscription.isMultiYear.label')}</th>
                        <td>
                            <div class="ui checkbox">
                                <input type="checkbox"
                                       name="subscription.isMultiYear" ${subscription.isMultiYear ? 'checked' : ''}/>
                            </div>
                        </td>
                        <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                            <td class="center aligned">
                            </td>
                        </g:if>
                    </tr>
                </g:if>
                <tr>
                    <th>${message(code: 'default.status.label')}</th>
                    <td>
                        <g:set var="rdcSubStatus"
                               value="${RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_STATUS)}"/>
                        <g:select
                                from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                class="ui dropdown"
                                optionKey="id"
                                optionValue="${{ it.getI10n('value') }}"
                                name="subStatus"
                                value="${permissionInfo?.sub_status}"/>
                    </td>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <td class="center aligned">
                            <div class="ui checkbox">
                                <input type="checkbox" name="auditList"
                                       value="status" ${AuditConfig.getConfig(subscription, 'status') ? 'checked' : ''}/>
                            </div>
                        </td>
                    </g:if>
                </tr>
                </tbody>
            </table>

            <div class="la-float-right">
                <button type="submit"
                        class="ui button">${message(code: 'myinst.renewalUpload.renew')}</button>
            </div>
            <br />

        </div>
    </g:form>
</semui:form>

</body>
</html>
