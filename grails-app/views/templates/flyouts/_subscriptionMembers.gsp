<%@ page import="de.laser.Subscription;"%>

<div class="ui six wide flyout" id="subscriptionMembers-content" style="padding:50px 0 10px 0;overflow:scroll">

    <h1 class="ui header">
%{--        <i class="icon university la-list-icon"></i>--}%
%{--        <i class="icon clipboard la-list-icon"></i>--}%
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
                    <th>${message(code:'subscription.member')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${memberSubs}" var="sub" status="i">
                    <g:set var="subInst" value="${sub.getSubscriber()}" />
                    <tr <%= sub.id == subscription.id ? 'class="warning"' : '' %>>
                        <td> ${i+1} </td>
                        <td>
                            <g:link controller="org" action="show" id="${subInst.id}" class="item">
                                <i class="icon university la-list-icon"></i>
                                ${subInst.sortname ?: subInst.name}
                            </g:link>
                        </td>
                        <td>
                            <g:link controller="subscription" action="show" id="${sub.id}" class="item">
                                <i class="icon clipboard la-list-icon"></i>
                                ${sub}
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