<%@ page import="com.k_int.kbplus.Person; de.laser.helper.RDStore" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.members.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscriptionDetails" action="show" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>

    <semui:crumb class="active" text="${message(code: 'subscription.details.linkLicenseConsortium.label')}"/>

</semui:breadcrumbs>


<h1 class="ui left aligned icon header"><semui:headerIcon/>
${message(code: 'subscription.linkLicenseConsortium.header')}
</h1>

<semui:messages data="${flash}"/>

<h3>
    ${message(code: 'subscription.linkLicenseConsortium.consortialSubscription')}: <g:link
        controller="subscriptionDetails" action="show"
        id="${parentSub.id}">${parentSub.name}</g:link><br><br>

    <g:if test="${parentLicense}">
        ${message(code: 'subscription.linkLicenseConsortium.consortialLicense')}: <g:link controller="licenseDetails"
                                                                                          action="show"
                                                                                          id="${parentLicense.id}">${parentLicense?.reference}</g:link>
    </g:if>
</h3>


<g:if test="${filteredSubChilds}">

    <div class="ui segment">
        <g:form action="processLinkLicenseConsortia" method="post" class="ui form">
            <g:hiddenField name="id" value="${params.id}"/>


            <div class="field required">
                <h4>${message(code: 'subscription.linkLicenseConsortium.info')}</h4>

                <label>${message(code: 'subscription.linktoLicense')}</label>

                <g:select class="ui search dropdown"
                          optionKey="id" optionValue="reference"
                          from="${validLicenses}" name="license_All" value=""
                          required="required"
                          noSelection='["": "${message(code: 'subscription.linkLicenseConsortium.noSelection')}"]'/>
            </div>
            <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
        </g:form>
    </div>

    <div class="divider"></div>

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
                <th></th>
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
                            <g:link controller="organisations" action="show" id="${subscr.id}">${subscr}</g:link>

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
                        <g:select class="ui search dropdown"
                                  optionKey="id" optionValue="reference"
                                  from="${validLicenses}" name="license_${sub.id}" value="${sub?.owner?.id}"
                                  noSelection='["": "${message(code: 'subscription.linkLicenseConsortium.noSelection')}"]'/>

                    </td>

                    <td class="x">
                        <g:link controller="subscriptionDetails" action="show" id="${sub.id}" class="ui icon button"><i
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

