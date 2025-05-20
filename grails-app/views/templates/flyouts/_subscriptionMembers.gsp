<%@ page import="de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.Subscription;de.laser.OrgRole"%>

<div class="ui very wide flyout" id="subscriptionMembers-content">

    <h1 class="ui header">
%{--        <i class="${Icon.ORG} icon la-list-icon"></i>--}%
%{--        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>--}%
        ${message(code:'consortium.member.plural')} ${message(code:'default.and')} ${message(code:'subscription.member.plural')}
    </h1>

    <div class="content">
        <g:if test="${subscription.instanceOf}">
            <g:set var="memberSubs" value="${subscriptionService.getValidSubChilds(subscription.instanceOf)}" />
        </g:if>
        <g:else>
            <g:set var="memberSubs" value="${subscriptionService.getValidSubChilds(subscription)}" />
        </g:else>

        <table class="ui la-ignore-fixed compact selectable table">
            <thead>
                <tr>
                    <th>${message(code:'sidewide.number')}</th>
                    <th>${message(code:'consortium.member')}</th>
                    <th></th>
                    <th>${message(code:'subscription.member')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${memberSubs}" var="sub" status="i">
                    <g:set var="subInst" value="${sub.getSubscriberRespConsortia()}" />
                    <tr <%= sub.id == subscription.id ? 'class="warning"' : '' %>>
                        <td>
                            ${i+1}
                        </td>
                        <td>
                            <g:link controller="org" action="show" id="${subInst.id}" class="item">
                                <ui:customerTypeIcon org="${subInst}" /> ${subInst.sortname ?: subInst.name}
                            </g:link>
                        </td>
                        <td>
                            <g:if test="${OrgRole.findBySubAndOrgAndRoleType(sub, subInst, RDStore.OR_SUBSCRIBER_CONS_HIDDEN)}">
                                <span class="ui icon la-popup-tooltip" data-content="${message(code:'subscription.details.hiddenForSubscriber')}">
                                    <i class="${Icon.SIG.VISIBLE_OFF} orange"></i>
                                </span>
                            </g:if>
                        </td>
                        <td>
                            <g:link controller="subscription" action="show" id="${sub.id}" class="item">
                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                ${sub}
                                <span style="margin-left:0.5em">
                                    (<g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> - <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/>)
                                </span>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>

    <style>
        .flyout .ui.selectable.table > tbody > tr, .flyout .ui.selectable.table > tr  {
            cursor: default;
        }
    </style>
</div>