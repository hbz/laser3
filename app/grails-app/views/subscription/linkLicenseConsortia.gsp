<%@ page import="com.k_int.kbplus.Person; de.laser.helper.RDStore" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.subscriberManagement.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="show" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>

    <semui:crumb class="active" text="${message(code: 'subscription.details.subscriberManagement.label')}"/>

</semui:breadcrumbs>

<h1 class="ui left aligned icon header">
${message(code: 'subscription.details.subscriberManagement.label')}
</h1>

<g:render template="navSubscriberManagement" />


<h3 class="ui left aligned icon header"><semui:headerIcon/>
${message(code: 'subscription.linkLicenseConsortium.header')}
</h3>

<semui:messages data="${flash}"/>

<h4>
    ${message(code: 'subscription.linkLicenseConsortium.consortialSubscription')}: <g:link
        controller="subscription" action="show"
        id="${parentSub.id}">${parentSub.name}</g:link><br><br>

    <g:if test="${parentLicense}">
        ${message(code: 'subscription.linkLicenseConsortium.consortialLicense')}: <g:link controller="license"
                                                                                          action="show"
                                                                                          id="${parentLicense?.id}">${parentLicense?.reference}</g:link>
    </g:if>
</h4>


<g:if test="${filteredSubChilds}">

    <div class="ui segment">
        <g:form action="processLinkLicenseConsortia" method="post" class="ui form">
            <g:hiddenField name="id" value="${params.id}"/>


            <div class="field required">
                <h4>${message(code: 'subscription.linkLicenseConsortium.info')}</h4>

                <label>${message(code: 'subscription.linktoLicense')}</label>
                <g:if test="${validLicenses}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="reference"
                              from="${validLicenses}" name="license_All" value=""
                              required=""
                              noSelection='["": "${message(code: 'subscription.linkLicenseConsortium.noSelection')}"]'/>
                </g:if><g:else>
                    ${message(code: 'subscription.linkLicenseConsortium.noValidLicenses')}
                </g:else>
            </div>
            <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
        </g:form>
    </div>

    <div class="divider"></div>

    <div class="ui segment">
        <h4>${message(code: 'subscription.linkLicenseConsortium.deleteLicensesInfo')}</h4>

        <g:link class="ui button js-open-confirm-modal"
                data-confirm-term-content = "${message(code: 'subscription.linkLicenseConsortium.deleteLicenses.button.confirm')}"
                data-confirm-term-how="ok" action="processUnLinkLicenseConsortia" id="${params.id}" params="[filterPropDef: filterPropDef]">${message(code: 'subscription.linkLicenseConsortium.deleteLicenses.button')}</g:link>

    </div>

    <g:form action="processLinkLicenseConsortia" method="post" class="ui form">
        <g:hiddenField name="id" value="${params.id}"/>
        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'default.sortname.label')}</th>
                <th>${message(code: 'subscriptionDetails.members.members')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'subscription.details.status')}</th>
                <th>${message(code: 'subscription.linktoLicense')}</th>
                <th class="la-action-info">${message(code:'default.actions')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubChilds}" status="i" var="zeile">
                <g:set var="sub" value="${zeile.sub}"/>
                <tr>
                    <td>${i + 1}</td>
                    <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                    <g:each in="${filteredSubscribers}" var="subscr">
                        <td>${subscr.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved?.value?.equalsIgnoreCase('yes')}">
                                <span data-position="top right"
                                      data-tooltip="${message(code: 'license.details.isSlaved.tooltip')}">
                                    <i class="thumbtack blue icon"></i>
                                </span>
                            </g:if>

                        </td>
                    </g:each>
                    <g:if test="${!sub.getAllSubscribers()}">
                        <td></td>
                        <td></td>
                    </g:if>

                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <td>${sub.status.getI10n('value')}</td>
                    <td>
                        <g:if test="${validLicenses}">
                            <g:select class="ui search dropdown"
                                      optionKey="id" optionValue="reference"
                                      from="${validLicenses}" name="license_${sub.id}" value="${sub?.owner?.id}"
                                      noSelection='["": "${message(code: 'subscription.linkLicenseConsortium.noSelection')}"]'/>
                        </g:if><g:else>
                            ${message(code: 'subscription.linkLicenseConsortium.noValidLicenses')}
                        </g:else>
                    </td>

                    <td class="x">
                        <g:link controller="subscription" action="show" id="${sub.id}" class="ui icon button"><i
                                class="write icon"></i></g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
    </g:form>
</g:if>
<g:else>
    <br><strong><g:message code="subscription.details.nomembers.label"
                           default="No members have been added to this license. You must first add members."/></strong>
</g:else>

</body>
</html>

