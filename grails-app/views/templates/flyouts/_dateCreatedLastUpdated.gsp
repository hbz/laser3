<div class="ui flyout" id="dateCreatedLastUpdated-content" style="padding:50px 0 10px 0;overflow:scroll">

    <h1 class="ui header"><g:message code="default.dateCreated.label"/> & <g:message code="default.lastUpdated.label"/></h1>

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

        <dl>
            <dt class="control-label">${message(code: 'default.lastUpdated.label')} ${message(code: 'default.on')}:</dt>
            <dd>
                <g:if test="${obj.lastUpdated}">
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${obj.lastUpdated}"/>
                </g:if>
            </dd>
        </dl>

    </div>

</div>