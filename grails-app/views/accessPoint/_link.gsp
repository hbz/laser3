<div class="ui card">
    <div class="content">
        <div class="header">
            <h3>${message(code: 'accessPoint.link.with.platform')}
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code: 'accessPoint.platformHelp')}">
                    <i class="question circle icon la-popup"></i>
                </span>
            </h3>
        </div>
    </div>
    <div class="content">
        <g:if test="${contextService.is_INST_EDITOR_with_PERMS_BASIC_or_ROLEADMIN(inContextOrg)}">
            <a class="ui icon right floated button" data-ui="modal" href="#linkPlatformModal">
                <i class="plus icon"></i>
            </a>

            <ui:modal formID="linkPlatform" id="linkPlatformModal"
                      msgSave="${message(code: 'accessPoint.button.linkPlatform')}"
                      message="accessPoint.link.with.platform">
                <g:form class="ui form" url="[controller: 'accessPoint', action: 'linkPlatform']" id="linkPlatform"
                        method="POST">
                    <g:hiddenField id="accessPoint_id_${accessPoint.id}" name="accessPointId" value="${accessPoint.id}"/>
                    <g:hiddenField name="accessMethod" value="${accessPoint.accessMethod}"/>
                    <div class="field">
                        <label><g:message code="platform.label"/></label>
                        <g:select id="platforms" class="ui dropdown search" name="platforms"
                                  from="${platformList}"
                                  optionKey="id"
                                  optionValue="name"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                    </div>

                </g:form>
            </ui:modal>
        </g:if>

        <br>
        <br>
        <laser:render template="linked_platforms_wrapper"/>
    </div>
</div>

<div class="ui card">
    <div class="content">
        <div class="header">
            <h3>
                ${message(code: 'accessPoint.link.with.subscription')}
                <span class="la-long-tooltip la-popup-tooltip la-delay"
                      data-html='${message(code: "accessPoint.linkedSubscriptionHelp")}'>
                    <i class="question circle icon la-popup"></i>
                </span>
            </h3>
        </div>
    </div>

    <div class="content">

        <g:if test="${linkedPlatformSubscriptionPackages}">
            <laser:render template="linked_subs_wrapper"/>
        </g:if>
        <g:else>
            <div class="ui message info"><p>${message(code: "accessPoint.info.noCustomLink")}</p></div>
        </g:else>
    </div>
</div>

