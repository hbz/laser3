<h5>${message(code: 'accessPoint.link.with.platform')}
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
        <input type="Submit" class="ui tiny button" value="${message(code:'accessPoint.button.linkPlatform')}" onClick="this.form.submit()" class="ui button"/>
    </td>
    </g:if>

    <g:hiddenField name="accessPointId" value="${accessPoint?.id}" />
    <g:hiddenField name="accessMethod" value="${accessPoint?.accessMethod}" />
    <table class="ui celled la-table table compact">
        <thead>
        <tr>
            <g:sortableColumn property="platform" title="${message(code: "platform.label")}" />
    <g:if test="${ accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
        <th>${message(code: "accessPoint.subscriptions.label")}</th>
        <th>${message(code: 'accessPoint.platformLink.action')}</th>
    </g:if>
        </tr>
        </thead>
        <tbody>
        <g:each in="${linkedPlatforms}" var="linkedPlatform">
            <tr>
                <td><g:link controller="platform" action="show" id="${linkedPlatform.platform.id}">${linkedPlatform.platform.name}</g:link></td>
                <td>
                    <g:each in="${linkedPlatform.linkedSubs}" var="linkedSub">
                        <g:link controller="Subscription" action="show" id="${linkedSub.id}">${linkedSub.name}</g:link><br/>
                    </g:each>
                </td>
            <g:if test="${ accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
                <td class="center aligned">
                    <g:link role="button" class="ui negative icon button button js-open-confirm-modal" controller="accessPoint" action="unlinkPlatform" id="${linkedPlatform.aplink.id}"
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
    <h5>${message(code: 'accessPoint.link.with.subscription')}
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-html='${message(code: "accessPoint.linkedSubscriptionHelp")}'>
            <i class="question circle icon la-popup"></i>
        </span>
    </h5>
    <g:if test="${linkedPlatformSubscriptionPackages}">
        <table class="ui celled la-table table compact">
            <thead>
            <tr>
                <th>${message(code: "accessPoint.subscription.label")}</th>
                <th>${message(code: "accessPoint.package.label")}</th>
                <g:sortableColumn property="platform" title="${message(code: "platform.label")}"/>
            </tr>
            </thead>
            <tbody>
            <g:each in="${linkedPlatformSubscriptionPackages}" var="linkedPlatformSubscriptionPackage">
                <tr>
                    <td><g:link controller="subscription" action="show"
                                id="${linkedPlatformSubscriptionPackage[1].subscription.id}">${linkedPlatformSubscriptionPackage[1].subscription.name}</g:link></td>
                    <td><g:link controller="package" action="show"
                                id="${linkedPlatformSubscriptionPackage[1].pkg.id}">${linkedPlatformSubscriptionPackage[1].pkg.name}</g:link></td>
                    <td><g:link controller="platform" action="show"
                                id="${linkedPlatformSubscriptionPackage[0].id}">${linkedPlatformSubscriptionPackage[0].name}</g:link></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <p>${message(code: "accessPoint.info.noCustomLink")}</p>
    </g:else>
</g:form>
