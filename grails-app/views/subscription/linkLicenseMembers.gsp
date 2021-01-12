<%@ page import="de.laser.License; de.laser.Person; de.laser.helper.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.linkLicenseMembers.label', args: args.memberTypeGenitive)}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="show" id="${subscription.id}"
                 text="${subscription.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.subscriberManagement.label', args: args.memberType)}"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${subscription.name}</h1>

<semui:anualRings object="${subscription}" controller="subscription" action="${actionName}"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="navSubscriberManagement" model="${[args: args]}"/>

<semui:messages data="${flash}"/>

<h4 class="ui header">
    <g:message code="subscription"/>: <g:link
        controller="subscription" action="show"
        id="${subscription.id}">${subscription.name}</g:link><br /><br />

    <g:if test="${parentLicense}">
        <g:message code="subscription.linkLicenseMembers.license" args="${args.superOrgType}"/>: <g:link
            controller="license"
            action="show"
            id="${parentLicense.id}">${parentLicense.reference}</g:link>
    </g:if>
</h4>


<g:if test="${filteredSubChilds}">

    <div class="ui segment">
        <h4 class="ui header">${message(code: 'subscription.linkLicenseMembers.deleteLicensesInfo', args: args.memberType)}</h4>

        <g:link class="ui button negative js-open-confirm-modal"
                data-confirm-tokenMsg="${message(code: 'subscription.linkLicenseMembers.deleteLicenses.button.confirm', args: args.memberType)}"
                data-confirm-term-how="ok" action="processUnLinkLicenseMembers" id="${params.id}"
                params="[unlinkAll:true]">${message(code: 'subscription.linkLicenseMembers.deleteAllLicenses.button')}</g:link>

    </div>

    <div class="divider"></div>

    <div class="ui segment">
    <g:form action="processLinkLicenseMembers" method="post" class="ui form" data-confirm-id="deleteLicenses_form">
        <g:hiddenField id="pllm_id_${params.id}" name="id" value="${params.id}"/>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="field required">
            <h4 class="ui header">${message(code: 'subscription.linkLicenseMembers.info', args: args.memberType)}</h4>

            <label>${message(code: 'subscription.linktoLicense')}</label>
            <g:if test="${validLicenses}">
                <g:select class="ui search dropdown"
                          optionKey="id" optionValue="reference"
                          from="${validLicenses}" name="license_All" value=""
                          required=""
                          noSelection='["": "${message(code: 'subscription.linkLicenseMembers.noSelection')}"]'/>
            </g:if><g:else>
                <g:message code="subscription.linkLicenseMembers.noValidLicenses" args="${args.superOrgType}"/>
            </g:else>
        </div>


        <div class="two fields">
            <div class="eight wide field">
                <div class="ui buttons">
                    <button class="ui button" type="submit" name="processOption"
                            value="linkLicense">${message(code: 'subscription.linkLicenseMembers.linkLicenses.button')}</button>
                </div>
            </div>

            <div class="eight wide field">
                <div class="ui buttons">
                    <button class="ui button negative js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: 'subscription.linkLicenseMembers.deleteLicenses.button.confirm', args: args.memberType)}"
                            data-confirm-term-how="ok"
                            name="processOption"
                            data-confirm-id="deleteLicenses"
                            value="unlinkLicense">${message(code: 'subscription.linkLicenseMembers.deleteLicenses.button')}</button>
                </div>

            </div>
        </div>


        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>
                    <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                </th>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'default.sortname.label')}</th>
                <th>${message(code: 'subscriptionDetails.members.members')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'default.status.label')}</th>
                <th>${message(code: 'subscription.linktoLicense')}</th>
                <th class="la-no-uppercase">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                        <i class="map orange icon"></i>
                    </span>
                </th>
                <th class="la-action-info">${message(code: 'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubChilds}" status="i" var="zeile">
                <g:set var="sub" value="${zeile.sub}"/>
                <tr>
                    <td>
                        <g:checkBox id="selectedMembers_${sub.id}" name="selectedMembers" value="${sub.id}" checked="false"/>
                    </td>
                    <td>${i + 1}</td>
                    <g:set var="subscr" value="${zeile.orgs}"/>
                    <td>
                        ${subscr.sortname}
                    </td>
                    <td>
                        <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                        <g:if test="${sub.isSlaved}">
                            <span data-position="top right"
                                  class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                <i class="thumbtack blue icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${subscr.getCustomerTypeI10n()}">
                                <i class="chess rook grey icon"></i>
                            </span>
                        </g:if>
                    </td>

                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <td>${sub.status.getI10n('value')}</td>
                    <td>
                        <g:each in="${License.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType',[subscription:sub,linkType:RDStore.LINKTYPE_LICENSE])}" var="license">
                            <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link><br />
                        </g:each>
                    </td>
                    <td>
                        <g:if test="${sub.isMultiYear}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                <i class="map orange icon"></i>
                            </span>
                        </g:if>
                    </td>

                    <td class="x">
                        <g:link controller="subscription" action="show" id="${sub.id}" class="ui icon button"
                                data-tooltip="${message(code:'subscription.details.viewMember.label')}"
                                data-position="left center"
                        >
                            <i class="write icon"></i></g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:form>
</g:if>
<g:else>
    <br /><strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', false)
        }
    });
</laser:script>
</body>
</html>

