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
${message(code: 'subscription.linkPackagesConsortium.header')}
</h3>

<semui:messages data="${flash}"/>

<h4>
    ${message(code: 'subscription.linkPackagesConsortium.consortialSubscription')}: <g:link
        controller="subscription" action="show"
        id="${parentSub.id}">${parentSub.name}</g:link><br><br>

    <g:if test="${parentPackages}">
        ${message(code: 'subscription.linkPackagesConsortium.consortialPackages')}:

        <div class="ui middle aligned selection list">
            <g:each in="${parentPackages}" var="subPkg">
                <div class="item">
                    <g:link controller="package" action="show"
                            id="${subPkg?.pkg?.id}">${subPkg?.pkg?.name} ${subPkg.getIEandPackageSize()}</g:link>
                </div>
            </g:each>
        </div>

    </g:if>
</h4>


<g:if test="${filteredSubChilds}">

    <div class="ui segment">
        <g:form action="processLinkPackagesConsortia" method="post" class="ui form">
            <g:hiddenField name="id" value="${params.id}"/>


            <div class="field required">
                <h4>${message(code: 'subscription.linkPackagesConsortium.info')}</h4>

                <label>${message(code: 'subscription.linkPackagesConsortium.package.label')}</label>
                <g:if test="${validPackages}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getPackageName() }}"
                              from="${validPackages}" name="package_All" value=""
                              required=""
                              noSelection='["": "${message(code: 'subscription.linkPackagesConsortium.noSelection')}"]'/>
                </g:if><g:else>
                    ${message(code: 'subscription.linkPackagesConsortium.noValidLicenses')}
                </g:else>
            </div>

            <div class="field ">
                <input type="checkbox" class="ui checkbox"
                       name="withIssueEntitlements">${message(code: 'subscription.linkPackagesConsortium.linkwithIE')}

            </div>

            <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
        </g:form>
    </div>

    <div class="ui segment">
        <h4>${message(code: 'subscription.linkPackagesConsortium.unlinkInfo')}</h4>

    <div class="ui buttons">
        <g:link class="ui button js-open-confirm-modal"
                data-confirm-term-content = "${message(code: 'subscription.linkPackagesConsortium.unlinkInfo.onlyPackage.confirm')}"
                data-confirm-term-how="ok" action="processUnLinkPackagesConsortia" id="${params.id}" params="[withIE: false]">${message(code: 'subscription.linkPackagesConsortium.unlinkInfo.onlyPackage')}</g:link>
        <div class="or"></div>
        <g:link class="ui button js-open-confirm-modal"
                data-confirm-term-content = "${message(code: 'subscription.linkPackagesConsortium.unlinkInfo.withIE.confirm')}"
                data-confirm-term-how="ok" action="processUnLinkPackagesConsortia" id="${params.id}" params="[withIE: true]">${message(code: 'subscription.linkPackagesConsortium.unlinkInfo.withIE')}</g:link>
    </div>

    </div>

    <div class="divider"></div>


    <div class="ui icon info message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header">Info</div>

            <p>${message(code: 'subscription.linkPackagesConsortium.package.info')}</p>
        </div>
    </div>

    <div class="ui segment">

        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'default.sortname.label')}</th>
                <th>${message(code: 'subscriptionDetails.members.members')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'subscription.details.status')}</th>
                <th>${message(code: 'subscription.packages.label')}</th>
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

                        <div class="ui middle aligned selection list">
                            <g:each in="${sub.packages.sort { it.pkg.name }}" var="sp">
                                <div class="item">
                                    <div class="right floated content">
                                        <g:if test="${editable}">
                                            <div class="ui icon negative buttons">
                                                <button class="ui button la-selectable-button"
                                                        onclick="unlinkPackage(${sp.pkg.id}, ${sub.id})">
                                                    <i class="unlink icon"></i>
                                                </button>
                                            </div>
                                            <br/>
                                        </g:if>
                                    </div>

                                    <div class="content">
                                        <g:link controller="subscription" action="index" id="${sub.id}"
                                                params="[pkgfilter: sp.pkg?.id]">
                                            ${sp?.pkg?.name} ${sp.getIEandPackageSize()}
                                        </g:link>
                                    </div>
                                </div>
                            </g:each>
                        </div>

                        <g:if test="${validPackages}">
                            <g:form action="processLinkPackagesConsortia" method="post" class="ui form">
                                <g:hiddenField name="id" value="${params.id}"/>
                                <div class="field ">
                                    <g:select class="ui search dropdown"
                                              optionKey="id" optionValue="${{ it.getPackageName() }}"
                                              from="${validPackages}" name="package_${sub.id}"
                                              noSelection='["": "${message(code: 'subscription.linkPackagesConsortium.noSelection')}"]'/>
                                </div>

                                <div class="field ">
                                    <input type="checkbox" class="ui checkbox"
                                           name="withIssueEntitlements">${message(code: 'subscription.linkPackagesConsortium.linkwithIE')}

                                </div>
                                <button class="ui button"
                                        type="submit">${message(code: 'default.button.save_changes')}</button>

                            </g:form>

                        </g:if><g:else>
                        ${message(code: 'subscription.linkPackagesConsortium.noValidLicenses')}
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

    </div>
</g:if>
<g:else>
    <br><strong><g:message code="subscription.details.nomembers.label"
                           default="No members have been added to this license. You must first add members."/></strong>
</g:else>

<div id="magicArea"></div>

<r:script language="JavaScript">

      function unlinkPackage(pkg_id, subscriptionInstanceID){

        var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage')}?subscription="+subscriptionInstanceID+"&package="+pkg_id

        $.ajax({url: req_url,
          success: function(result){
             $('#magicArea').html(result);
          },
          complete: function(){
            $("#unlinkPackageModal").modal("show");
          }
        });
      }

</r:script>

</body>
</html>

