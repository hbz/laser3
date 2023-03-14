<div class="ui card">
    <div class="content">
        <h2 class="ui header">${message(code: 'accessPoint.link.with.platform')}
            <span class="la-long-tooltip la-popup-tooltip la-delay"
                  data-content="${message(code: 'accessPoint.platformHelp')}">
                <i class="question circle icon la-popup"></i>
            </span>
        </h2>

        <g:form class="ui form" url="[controller: 'accessPoint', action: 'linkPlatform']" id="linkPlatform"
                method="POST">
            <g:if test="${(accessService.checkPermAffiliation('ORG_MEMBER_BASIC', 'INST_EDITOR') && inContextOrg )|| (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))}">
                <g:select id="platforms" class="ui dropdown search" name="platforms"
                          from="${platformList}"
                          optionKey="id"
                          optionValue="name"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                <input type="submit" class="ui button" value="${message(code: 'accessPoint.button.linkPlatform')}" />
            </g:if>
            <g:hiddenField id="accessPoint_id_${accessPoint.id}" name="accessPointId" value="${accessPoint.id}"/>
            <g:hiddenField name="accessMethod" value="${accessPoint.accessMethod}"/>
        </g:form>
        <laser:render template="linked_platforms_wrapper"/>
    </div>
</div>

<div class="ui message info">
    <div class="header">
        ${message(code: 'accessPoint.link.with.subscription')}
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-html='${message(code: "accessPoint.linkedSubscriptionHelp")}'>
            <i class="question circle icon la-popup"></i>
        </span>
    </div>
    <g:if test="${linkedPlatformSubscriptionPackages}">
        <laser:render template="linked_subs_wrapper"/>
    </g:if>
    <g:else>
        <p>${message(code: "accessPoint.info.noCustomLink")}</p>
    </g:else>
</div>

