<%@ page import="com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<!doctype html>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.linkPackage.heading')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.linkPackage.heading', default: 'Link Subscription to Packages')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${subscriptionInstance.name}</h1>
<br>
<h2 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'subscription.details.linkPackage.heading')}</h2>

<semui:filter>
    <g:form name="LinkPackageForm" action="linkPackage" method="get" params="${params}" class="ui form">
        <input type="hidden" name="offset" value="${params.offset}"/>
        <input type="hidden" name="id" value="${params.id}"/>

            <div class="field">
                <label for="q">${message(code: 'package.show.pkg_name', default: 'Package Name')}</label>
                <input id="q" name="q" value="${params.q}"/>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.filterreset.label')}</a>
                <button type="submit" name="search" value="yes"
                        class="ui secondary button">${message(code: 'default.button.filter.label', default: 'Filter')}</button>
            </div>

    </g:form>
</semui:filter>

<semui:messages data="${flash}"/>

<div class="ui icon message" id="durationAlert" style="display: none">
    <i class="notched circle loading icon"></i>

    <div class="content">
        <div class="header">
            <g:message code="globalDataSync.requestProcessing"/>
        </div>
        <g:message code="globalDataSync.requestProcessingInfo"/>

    </div>
</div>

<div class="ui grid">

    <div class="twelve wide column">
        <div>
            <g:if test="${hits}">
                <div class="paginateButtons" style="text-align:center">
                    <g:if test="${params.int('offset')}">
                        ${message(code: 'default.search.offset.text', args: [(params.int('offset') + 1), (resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))), resultsTotal])}
                    </g:if>
                    <g:elseif test="${resultsTotal && resultsTotal > 0}">
                        ${message(code: 'default.search.no_offset.text', args: [(resultsTotal < params.int('max') ? resultsTotal : params.int('max')), resultsTotal])}
                    </g:elseif>
                    <g:else>
                        ${message(code: 'default.search.no_pagiantion.text', args: [resultsTotal])}
                    </g:else>
                </div>

                <div id="resultsarea">
                    <table class="ui sortable celled la-table table">
                        <thead>
                        <tr>
                            <g:sortableColumn property="name"
                                              title="${message(code: 'package.show.pkg_name', default: 'Package Name')}"
                                              params="${params}"/>
                            <g:sortableColumn property="providerName"
                                              title="${message(code: 'package.content_provider')}"
                                              params="${params}"/>
                            <g:sortableColumn property="platformName"
                                              title="${message(code: 'package.nominalPlatform')}"
                                              params="${params}"/>
                            <th>${message(code: 'default.action.label', default: 'Action')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${hits}" var="hit">
                            <g:if test="${!params.gokbApi}">
                                <tr>
                                    <td><g:link controller="package" action="show"
                                                id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>
                                        <br><b>(${hit.getSource().titleCountCurrent ?: '0'} ${message(code: 'title.plural', default: 'Titles')})</b>
                                    </td>
                                    <td><g:if test="${com.k_int.kbplus.Org.get(hit.getSource().providerId)}"><g:link
                                            controller="organisation" action="show"
                                            id="${hit.getSource().providerId}">${hit.getSource().providerName}</g:link></g:if>
                                    <g:else>${hit.getSource().providerName}</g:else>
                                    </td>
                                    <td><g:if test="${com.k_int.kbplus.Platform.get(hit.getSource().nominalPlatformId)}"><g:link
                                            controller="platform" action="show"
                                            id="${hit.getSource().nominalPlatformId}">${hit.getSource().nominalPlatformName}</g:link></g:if>
                                        <g:else>${hit.getSource().nominalPlatformName}</g:else></td>
                                    <td>
                                        <g:if test="${editable && (!pkgs || !pkgs.contains(hit.getSource().getSource().dbId.toLong()))}">
                                            <g:link action="linkPackage" class="ui mini button packageLinkWithoutIE"
                                                    id="${params.id}"
                                                    params="${[addId: hit.getSource().dbId, addType: 'Without']}"
                                                    style="white-space:nowrap;">${message(code: 'subscription.details.link.no_ents', default: 'Link (no Entitlements)')}</g:link>
                                            <br/><br/>
                                            <g:link action="linkPackage" class="ui mini button packageLink"
                                                    id="${params.id}"
                                                    params="${[addId: hit.getSource().dbId, addType: 'With']}"
                                                    style="white-space:nowrap;">${message(code: 'subscription.details.link.with_ents', default: 'Link (with Entitlements)')}</g:link>
                                        </g:if>
                                        <g:else>
                                            <span></span>
                                        </g:else>
                                    </td>
                                </tr>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td>
                                        <g:if test="${com.k_int.kbplus.Package.findByGokbId(hit.uuid)}">
                                            <g:link controller="package" target="_blank" action="show"
                                                    id="${com.k_int.kbplus.Package.findByGokbId(hit.uuid).id}">${hit.name}</g:link>
                                        </g:if>
                                        <g:else>
                                            ${hit.name} <a target="_blank"
                                                           href="${hit.url ? hit.url + '/gokb/public/packageContent/' + hit.id : '#'}"><i
                                                    title="GOKB Link" class="external alternate icon"></i></a>
                                        </g:else>
                                        <br><b>(${hit.titleCount ?: '0'} ${message(code: 'title.plural', default: 'Titles')})</b>
                                    </td>

                                    <td><g:if test="${com.k_int.kbplus.Org.findByGokbId(hit.providerUuid)}"><g:link
                                            controller="organisation" action="show"
                                            id="${com.k_int.kbplus.Org.findByGokbId(hit.providerUuid).id}">${hit.providerName}</g:link></g:if>
                                    <g:else>${hit.providerName}</g:else>
                                    </td>
                                    <td><g:if test="${com.k_int.kbplus.Platform.findByGokbId(hit.platformUuid)}"><g:link
                                            controller="platform" action="show"
                                            id="${com.k_int.kbplus.Platform.findByGokbId(hit.platformUuid).id}">${hit.platformName}</g:link></g:if>
                                        <g:else>${hit.platformName}</g:else></td>

                                    <td class="right aligned">
                                        <g:if test="${editable && (!pkgs || !(hit.uuid in pkgs))}">
                                            <g:link action="linkPackage" class="ui mini button packageLinkWithoutIE"
                                                    id="${params.id}"
                                                    params="${[impId: hit.uuid, source: hit.url, addType: 'Without']}"
                                                    style="white-space:nowrap;">${message(code: 'subscription.details.link.no_ents', default: 'Link (no Entitlements)')}</g:link>
                                            <br/><br/>
                                            <g:link action="linkPackage" class="ui mini button packageLink"
                                                    id="${params.id}"
                                                    params="${[impId: hit.uuid, source: hit.url, addType: 'With']}"
                                                    style="white-space:nowrap;">${message(code: 'subscription.details.link.with_ents', default: 'Link (with Entitlements)')}</g:link>
                                        </g:if>
                                        <g:else>
                                            <span><b>${message(code: 'subscription.details.linkPackage.currentPackage', default: 'This package is already linked to the license!')}</b>
                                            </span>
                                            <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.pkg.gokbId = :hit and ci.subPkg.subscription = :sub',[hit:hit.uuid,sub:subscriptionInstance])}" />
                                            <br>
                                            <g:if test="${editable && !hasCostItems}">
                                                <div class="ui icon negative buttons">
                                                    <button class="ui button la-selectable-button"
                                                            onclick="unlinkPackage(${com.k_int.kbplus.Package.findByGokbId(hit.uuid)?.id})">
                                                        <i class="unlink icon"></i>
                                                    </button>
                                                </div>
                                            </g:if>
                                            <g:elseif test="${editable && hasCostItems}">
                                                <div class="ui icon negative buttons la-popup-tooltip" data-content="${message(code:'subscription.delete.existingCostItems')}">
                                                    <button class="ui disabled button la-selectable-button">
                                                        <i class="unlink icon"></i>
                                                    </button>
                                                </div>
                                            </g:elseif>
                                            <br/>
                                        </g:else>
                                    </td>
                                </tr>
                            </g:else>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </g:if>
            <div class="paginateButtons" style="text-align:center">
                <g:if test="${params.int('offset')}">
                    ${message(code: 'default.search.offset.text', args: [(params.int('offset') + 1), (resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))), resultsTotal])}
                </g:if>
                <g:elseif test="${resultsTotal && resultsTotal > 0}">
                    ${message(code: 'default.search.no_offset.text', args: [(resultsTotal < params.int('max') ? resultsTotal : params.int('max')), resultsTotal])}
                </g:elseif>
                <g:else>
                    ${message(code: 'default.search.no_pagiantion.text', args: [resultsTotal])}
                </g:else>
            </div>

            <g:if test="${hits}">
                <semui:paginate action="linkPackage" controller="subscription" params="${params}"
                                next="${message(code: 'default.paginate.next', default: 'Next')}"
                                prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                                total="${resultsTotal}"/>
            </g:if>
        </div>
    </div>

    <div class="four wide column">
        <div class="ui card">
            <div class="content">
                <div class="header">${message(code: 'subscription.details.linkPackage.current', default: 'Current Links', args: [subscriptionInstance.name])}</div>
            </div>
            <g:each in="${subscriptionInstance.packages.sort { it.pkg.name }}" var="sp">
                <div class="content">
                    <div class="item"><g:link controller="package" action="show"
                                          id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                        <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.subscription = :sub and ci.subPkg = :sp',[sub:subscriptionInstance,sp:sp])}"/>
                        <br>
                        <g:if test="${editable && !hasCostItems}">
                            <div class="ui mini icon buttons">
                                <button class="ui button la-selectable-button"
                                        onclick="unlinkPackage(${sp.pkg.id})">
                                    <i class="times icon red"></i>${message(code: 'default.button.unlink.label')}
                                </button>
                            </div>
                        </g:if>
                        <g:elseif test="${editable && hasCostItems}">
                            <div class="ui mini icon buttons la-popup-tooltip" data-content="${message(code:'subscription.delete.existingCostItems')}">
                                <button class="ui disabled button la-selectable-button">
                                    <i class="times icon red"></i>${message(code: 'default.button.unlink.label')}
                                </button>
                            </div>
                        </g:elseif>
                        <br/>
                    </div>
                </div>
            </g:each>
        </div>
    </div>


<div id="magicArea"></div>

<r:script language="JavaScript">

      function unlinkPackage(pkg_id){
        var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage', params: [subscription: subscriptionInstance.id])}&package="+pkg_id

        $.ajax({url: req_url,
          success: function(result){
             $('#magicArea').html(result);
          },
          complete: function(){
            $("#unlinkPackageModal").modal("show");
          }
        });
      }
      $(document).ready(function () {
        $(".packageLink").click(function(evt) {

            evt.preventDefault();

            var check = confirm('${message(code: 'subscription.details.link.with_ents.confirm', default: 'Are you sure you want to add with entitlements?')}');
            console.log(check)
            if (check == true) {
                toggleAlert();
                window.open($(this).attr('href'), "_self");
            }
        });

        $(".packageLinkWithoutIE").click(function(evt) {

            evt.preventDefault();

            var check = confirm('${message(code: 'subscription.details.link.no_ents.confirm', default: 'Are you sure you want to add with entitlements?')}');
            console.log(check)
            if (check == true) {
                toggleAlert();
                window.open($(this).attr('href'), "_self");
            }
        });

         function toggleAlert() {
            $('#durationAlert').toggle();
        }
      });
</r:script>
<!-- ES Query String: ${es_query} -->
</body>
</html>
