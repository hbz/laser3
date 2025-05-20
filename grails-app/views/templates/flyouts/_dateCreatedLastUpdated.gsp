<div class="ui wide flyout" id="dateCreatedLastUpdated-content">

    <h1 class="ui header"><g:message code="default.dateCreated.label"/>
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            & <g:message code="default.lastUpdated.label"/>
        </sec:ifAnyGranted>
    </h1>

    <div class="content">

        <dl>
            <dt class="control-label">${message(code: 'default.dateCreated.label')} ${message(code: 'default.on')}:</dt>
            <dd>
                <g:if test="${obj.dateCreated}">
                <g:formatDate formatName="default.date.format.notime"
                              date="${obj.dateCreated}"/>
                </g:if>
            </dd>
        </dl>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <dl>
            <dt class="control-label">${message(code: 'default.lastUpdated.label')} ${message(code: 'default.on')}:</dt>
            <dd>
                <g:if test="${obj.lastUpdated}">
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${obj.lastUpdated}"/>
                </g:if>
            </dd>
        </dl>
    </sec:ifAnyGranted>

    </div>

</div>