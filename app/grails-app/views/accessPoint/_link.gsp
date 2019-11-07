<h5>${message(code: 'accessPoint.link.with.platform', default: 'Link with Platform')}
    <span class="la-long-tooltip la-popup-tooltip la-delay"
          data-content="${message(code:'accessPoint.platformHelp')}">
        <i class="question circle icon la-popup"></i>
    </span>
</h5>

<g:form class="ui form" url="[controller: 'accessPoint', action: 'linkPlatform']" id="linkPlatform" method="POST">
    <g:if test="${ accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
    <td>
        <g:select id="platforms" class="ui dropdown search" name="platforms"
                  from="${platformList}"
                  optionKey="id"
                  optionValue="name"
                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
    </td>
    <td class="center aligned">
        <input type="Submit" class="ui tiny button" value="${message(code:'accessPoint.button.linkPlatform', default:'Create link')}" onClick="this.form.submit()" class="ui button"/>
    </td>
    </g:if>

    <g:hiddenField name="accessPointId" value="${accessPoint?.id}" />
    <g:hiddenField name="accessMethod" value="${accessPoint?.accessMethod}" />
    <table class="ui celled la-table table compact">
        <thead>
        <tr>
            <g:sortableColumn property="platform" title="${message(code: "platform.label", default: "Platform")}" />
            <th>${message(code: 'accessPoint.cusomerId', default: 'Customer ID')}</th>
            <th>${message(code: 'accessPoint.notifcationType', default: 'Notification Type')}</th>
            <th>${message(code: 'accessPoint.lastNotificationDate', default: 'Last Notification')}</th>
            <th>${message(code: 'accessPoint.providerConfirmation', default: 'Provider Confirmation')}</th>
            <th>${message(code: 'accessPoint.note', default: 'Note')}</th>
    <g:if test="${ accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
            <th>${message(code: 'accessPoint.action', default: 'Action')}</th>
    </g:if>
        </tr>
        </thead>
        <tbody>
        <g:each in="${linkedPlatformsMap}" var="linkedPlatform">
            <tr>
                <td><g:link controller="platform" action="show" id="${linkedPlatform.platform.id}">${linkedPlatform.platform.name}</g:link></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
            <g:if test="${ accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
                <td class="center aligned">
                    <g:link class="ui negative icon button button js-open-confirm-modal" controller="accessPoint" action="unlinkPlatform" id="${linkedPlatform.aplink.id}"
                            data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform', args: [accessPoint.name, linkedPlatform.platform.name])}"
                            data-confirm-term-how="unlink"
                    >
                        <i class="unlink icon"></i>
                    </g:link>
                </td>
            </g:if>
            </tr>
        </g:each>
        </tbody>
    </table>


    <h5>${message(code: 'accessPoint.link.with.subscription', default: 'Link with Platform')}
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-content="${message(code:'accessPoint.platformHelp')}">
            <i class="question circle icon la-popup"></i>
        </span>
    </h5>

    <table class="ui celled la-table table compact">
        <thead>
        <tr>
            <g:sortableColumn property="platform" title="${message(code: "license.label", default: "License")}" />
        </tr>
        </thead>
        <tbody>
        <g:each in="${linkedSubscriptionsMap}" var="linkedSubscription">
            <tr>
                <td><g:link controller="subscription" action="show" id="${linkedSubscription.subscription.id}">${linkedSubscription.subscription.name}</g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:form>
