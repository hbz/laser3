<%@ page import="de.laser.finance.CostItem; de.laser.Person; de.laser.helper.RDStore; de.laser.FormService; de.laser.SubscriptionPackage" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.linkPackagesMembers.label', args: args.memberTypeGenitive)}</title>
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


<g:message code="subscription.linkPackagesMembers.package.label"
           args="${args.superOrgType}"/></label>

    <g:if test="${validPackages}">
        <div class="ui middle aligned selection list">
            <g:each in="${validPackages}" var="subPkg">
                <div class="item">
                    <g:link controller="package" action="show"
                            id="${subPkg.pkg.id}">${subPkg.pkg.name} ${raw(subPkg.getIEandPackageSize())}</g:link>

                    <div class="right floated content">
                        <button class="ui negative button la-selectable-button unlinkPackages"
                                data-package="${subPkg.pkg.id}" data-subscription="${subPkg.subscription.id}">
                            <i class="unlink icon"></i>
                        </button>
                    </div>
                </div>
            </g:each>
        </div>

    </g:if>
    <g:else>
        <g:message code="subscription.linkPackagesMembers.noValidLicenses" args="${args.superOrgType}"/>
    </g:else>
</h4>

<div class="ui icon info message">
    <i class="info icon"></i>

    <div class="content">
        <div class="header">Info</div>

        <p>${message(code: 'subscription.linkPackagesMembers.package.info')}</p>
    </div>
</div>


<g:if test="${filteredSubChilds}">

    <div class="ui segment">
        <h4 class="ui header">${message(code: 'subscription.linkPackagesMembers.unlinkInfo')}</h4>

        <div class="ui buttons">
            <g:link class="ui button negative js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: 'subscription.linkPackagesMembers.unlinkInfo.onlyPackage.confirm')}"
                    data-confirm-term-how="ok" action="processUnLinkPackagesMembers" id="${params.id}"
                    params="[withIE: false]">${message(code: 'subscription.linkPackagesMembers.unlinkInfo.onlyPackage')}</g:link>
            <div class="or" data-text="${message(code:'default.or')}"></div>
            <g:link class="ui button negative js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: 'subscription.linkPackagesMembers.unlinkInfo.withIE.confirm')}"
                    data-confirm-term-how="ok" action="processUnLinkPackagesMembers" id="${params.id}"
                    params="[withIE: true]">${message(code: 'subscription.linkPackagesMembers.unlinkInfo.withIE')}</g:link>
        </div>

    </div>

    <g:form action="processLinkPackagesMembers" method="post" class="ui form">
        <g:hiddenField id="plpm_id_${params.id}" name="id" value="${params.id}"/>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <div class="ui segment">

            <div class="field required">
                <h4 class="ui header">${message(code: 'subscription.linkPackagesMembers.info', args: args.memberType)}</h4>

                <label><g:message code="subscription.linkPackagesMembers.package.label"
                                  args="${args.superOrgType}"/></label>
                <g:if test="${validPackages}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getPackageName() }}"
                              from="${validPackages}" name="package_All" value=""
                              required=""
                              noSelection='["": "${message(code: 'subscription.linkPackagesMembers.noSelection')}"]'/>
                </g:if>
                <g:else>
                    <g:message code="subscription.linkPackagesMembers.noValidLicenses" args="${args.superOrgType}"/>
                </g:else>
            </div>


            <div class="two fields">
                <div class="eight wide field">
                    <div class="ui buttons">
                        <button class="ui button" type="submit" name="processOption"
                                value="linkwithoutIE">${message(code: 'subscription.linkPackagesMembers.linkwithoutIE')}</button>
                        <div class="or" data-text="${message(code:'default.or')}"></div>
                        <button class="ui button" type="submit" name="processOption"
                                value="linkwithIE">${message(code: 'subscription.linkPackagesMembers.linkwithIE', args: args.superOrgType)}</button>
                    </div>
                </div>
                <div class="eight wide field">
                    <div class="ui buttons">
                        <button class="ui button negative js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: 'subscription.linkPackagesMembers.unlinkInfo.onlyPackage.confirm')}"
                                data-confirm-term-how="ok" type="submit" name="processOption"
                                value="unlinkwithoutIE">${message(code: 'subscription.linkPackagesMembers.unlinkInfo.onlyPackage')}</button>
                        <div class="or" data-text="${message(code:'default.or')}"></div>
                        <button class="ui button negative js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: 'subscription.linkPackagesMembers.unlinkInfo.withIE.confirm')}"
                                data-confirm-term-how="ok" type="submit" name="processOption"
                                value="unlinkwithIE">${message(code: 'subscription.linkPackagesMembers.unlinkInfo.withIE')}</button>
                    </div>
                </div>
            </div>

            <div class="divider"></div>
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
                    <th>${message(code: 'subscription.packages.label')}</th>
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubChilds}" status="i" var="zeile">
                    <g:set var="sub" value="${zeile.sub}"/>
                    <tr>

                        <g:set var="subscr" value="${zeile.orgs}"/>
                            <td>
                                <g:checkBox id="selectedMembers_${sub.id}" name="selectedMembers" value="${sub.id}" checked="false"/>
                            </td>
                            <td>${i + 1}</td>
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

                            <div class="ui middle aligned selection list">
                                <g:each in="${sub.packages}" var="sp">
                                    <div class="item"><div class="content">
                                            <g:link controller="subscription" action="index" id="${sub.id}"
                                                    params="[pkgfilter: sp.pkg.id]">
                                                ${sp.pkg.name}<br />${raw(sp.getIEandPackageSize())}
                                            </g:link>
                                            <g:if test="${editable && childWithCostItems.find { SubscriptionPackage row -> row.id == sp.id }}">
                                                <br /><g:message code="subscription.delete.existingCostItems"/>
                                            </g:if>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
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
                            <g:link controller="subscription" action="show" id="${sub.id}" class="ui icon button"><i
                                    class="write icon"></i></g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>
    </g:form>
</g:if>
<g:else>
    <br /><strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
        $('#membersListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', true)
            }
            else {
                $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', false)
            }
        });

      $('.unlinkPackages').on('click',function() {
          JSPC.app.unlinkPackage($(this).attr("data-package"),$(this).attr("data-subscription"));
      });

      JSPC.app.unlinkPackage = function (pkg_id, subscriptionID) {

        var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage')}?subscription="+subscriptionID+"&package="+pkg_id

        $.ajax({url: req_url,
          done: function(result){
             $('#magicArea').html(result);
            $("#unlinkPackageModal").modal("show");
          }
        });
      }
</laser:script>

</body>
</html>

