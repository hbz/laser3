<%@ page import="com.k_int.kbplus.Subscription" %>
<!doctype html>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.label', default: 'Subscription')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}"/>
    <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.linkPackage.heading', default: 'Link Subscription to Packages')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>


<h1 class="ui left aligned icon header"><semui:headerIcon/>${subscriptionInstance.name} : ${message(code: 'subscription.details.linkPackage.heading', default: 'Link Subscription to Packages')}</h1>


<semui:filter>
    <g:form name="LinkPackageForm" action="linkPackage" method="get" params="${params}" class="ui form">
        <input type="hidden" name="offset" value="${params.offset}"/>
        <input type="hidden" name="id" value="${params.id}"/>

        <div class="field">
            <label>${message(code: 'package.show.pkg_name', default: 'Package Name')}</label>
            <input name="q" value="${params.q}"/>
        </div>

        <div class="field">
            <button type="submit" name="search" value="yes"
                    class="ui secondary button">${message(code: 'default.button.filter.label', default: 'Filter')}</button>
            <a href="${request.forwardURI}"
               class="ui button">${message(code: 'default.button.filterreset.label')}</a>
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
                            <g:if test="${!params.gokbApi}">
                                <th>${message(code: 'package.show.pkg_name', default: 'Package Name')}</th>
                            <%--<th>${message(code:'consortium.label', default:'Consortium')}</th>--%>
                                <th>${message(code: 'default.action.label', default: 'Action')}</th>
                            </g:if> <g:else>
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
                        </g:else>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${hits}" var="hit">
                            <g:if test="${!params.gokbApi}">
                                <tr>
                                    <td><g:link controller="packageDetails" action="show"
                                                id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>(${hit.getSource()?.titleCount ?: '0'} ${message(code: 'title.plural', default: 'Titles')})</td>
                                    <%--<td>${hit.getSource().consortiaName}</td>--%>
                                    <td>
                                        <g:if test="${editable && (!pkgs || !pkgs.contains(hit.getSource().dbId.toLong()))}">
                                            <g:link action="linkPackage" class="ui mini button packageLink"
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
                                        <g:if test="${com.k_int.kbplus.Package.findByImpId(hit.id)}">
                                            <g:link controller="packageDetails" target="_blank" action="show"
                                                    id="${com.k_int.kbplus.Package.findByImpId(hit.id).id}">${hit.name}</g:link>
                                        </g:if>
                                        <g:else>
                                            ${hit.name} <a target="_blank"
                                                           href="${hit.url ? hit.url + '/gokb/resource/show/' + hit.id : '#'}"><i
                                                    title="GOKB Link" class="external alternate icon"></i></a>
                                        </g:else>
                                        <br><b>(${hit.tippsCountCurrent ?: '0'} ${message(code: 'title.plural', default: 'Titles')})</b>
                                    </td>

                                    <td><g:if test="${com.k_int.kbplus.Org.findByName(hit.providerName)}"><g:link
                                            controller="organisations" action="show"
                                            id="${com.k_int.kbplus.Org.findByName(hit.providerName).id}">${hit.providerName}</g:link></g:if>
                                    <g:else>${hit.providerName}</g:else>
                                    </td>
                                    <td><g:if test="${com.k_int.kbplus.Platform.findByName(hit.platformName)}"><g:link
                                            controller="platform" action="show"
                                            id="${com.k_int.kbplus.Platform.findByName(hit.platformName).id}">${hit.platformName}</g:link></g:if>
                                        <g:else>${hit.platformName}</g:else></td>

                                    <td>
                                        <g:if test="${editable && (!pkgs || !(hit.id in pkgs))}">
                                            <g:link action="linkPackage" class="ui mini button packageLink"
                                                    id="${params.id}"
                                                    params="${[addId: hit.id, addType: 'Without']}"
                                                    style="white-space:nowrap;">${message(code: 'subscription.details.link.no_ents', default: 'Link (no Entitlements)')}</g:link>
                                            <br/><br/>
                                            <g:link action="linkPackage" class="ui mini button packageLink"
                                                    id="${params.id}"
                                                    params="${[addId: hit.id, addType: 'With']}"
                                                    style="white-space:nowrap;">${message(code: 'subscription.details.link.with_ents', default: 'Link (with Entitlements)')}</g:link>
                                        </g:if>
                                        <g:else>
                                            <span><b>${message(code: 'subscription.details.linkPackage.currentPackage', default: 'This package is already linked to the license!')}</b>
                                            </span>
                                            <g:if test="${editable}">
                                                <br>

                                                <div class="ui mini icon buttons">
                                                    <button class="ui button la-selectable-button"
                                                            onclick="unlinkPackage(${com.k_int.kbplus.Package.findByImpId(hit.id)?.id})">
                                                        <i class="times icon red"></i>${message(code: 'default.button.unlink.label')}
                                                    </button>
                                                </div>
                                                <br/>
                                            </g:if>
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
                <semui:paginate action="linkPackage" controller="subscriptionDetails" params="${params}"
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

            <div class="content">
                <g:each in="${subscriptionInstance.packages.sort { it.pkg.name }}" var="sp">
                    <div class="item"><g:link controller="packageDetails" action="show"
                                              id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                        <g:if test="${editable}">
                            <br>

                            <div class="ui mini icon buttons">
                                <button class="ui button la-selectable-button"
                                        onclick="unlinkPackage(${sp.pkg.id})">
                                    <i class="times icon red"></i>${message(code: 'default.button.unlink.label')}
                                </button>
                            </div>
                            <br/>
                        </g:if>
                    </div><hr>
                </g:each>
            </div>
        </div>
    </div>
</div>

<div class="ui segment">

    <div class="ui dimmer">
        <div class="ui large text loader">Die Paket Verküpfung kann einige Zeit beanspruchen!</div>
    </div>

</div>

<div id="magicArea"></div>

<r:script language="JavaScript">

      function unlinkPackage(pkg_id){
        var req_url = "${createLink(controller: 'subscriptionDetails', action: 'unlinkPackage', params: [subscription: subscriptionInstance.id])}&package="+pkg_id

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

         function toggleAlert() {
            $('#durationAlert').toggle();
        }
      });
</r:script>
<!-- ES Query String: ${es_query} -->
</body>
</html>
