<h5>${message(code: 'accessPoint.link.with.platform')}
    <span class="la-long-tooltip la-popup-tooltip la-delay"
          data-content="${message(code:'accessPoint.platformHelp')}">
        <i class="question circle icon la-popup"></i>
    </span>
</h5>

<g:form class="ui form" url="[controller: 'accessPoint', action: 'linkPlatform']" id="linkPlatform" method="POST">
    <g:if test="${ accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
        <g:select id="platforms" class="ui dropdown search" name="platforms"
                  from="${platformList}"
                  optionKey="id"
                  optionValue="name"
                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
        <input type="Submit" class="ui tiny button" value="${message(code:'accessPoint.button.linkPlatform')}" onClick="this.form.submit()" class="ui button"/>
    </g:if>
    <g:hiddenField name="accessPointId" value="${accessPoint?.id}" />
    <g:hiddenField name="accessMethod" value="${accessPoint?.accessMethod}" />
</g:form>
    <g:render template="linked_platforms_wrapper"/>
    <h5>${message(code: 'accessPoint.link.with.subscription')}
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-html='${message(code: "accessPoint.linkedSubscriptionHelp")}'>
            <i class="question circle icon la-popup"></i>
        </span>
    </h5>
    <g:if test="${linkedPlatformSubscriptionPackages}">
        <g:render template="linked_subs_wrapper"/>
    </g:if>
    <g:else>
        <p>${message(code: "accessPoint.info.noCustomLink")}</p>
    </g:else>

