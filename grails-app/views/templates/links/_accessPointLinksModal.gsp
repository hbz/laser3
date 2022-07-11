
<g:if test="${editmode}">
    <a role="button" class="ui button ${tmplCss}" data-semui="modal" href="#${tmplModalID}">
        <g:if test="${tmplIcon}">
            <i class="${tmplIcon} icon"></i>
        </g:if>
        <g:if test="${tmplButtonText}">
            ${tmplButtonText}
        </g:if>
    </a>
</g:if>

<ui:modal id="${tmplModalID}" text="${tmplText}">

    <g:form action="linkAccessPoint" controller="platform" method="get" class="form-inline ui small form">
        <input type="hidden" name="platform_id" value="${platformInstance.id}">
        <g:if test="${subscriptionPackage}">
          <input type="hidden" name="subscriptionPackage_id" value="${subscriptionPackage.id}">
        </g:if>
        <div class="field">
             <div class="ui grid">
                        <div class="row">
                                <div class="column">
                                    <g:message code="${message(code: 'platform.link.accessPoint', args: [platformInstance.name])}"/>
                                </div>
                        </div>
                        <div class="row">
                            <div class="four wide column">
                                <g:message code="platform.accessPoint" />
                            </div>
                            <div class="twelve wide column">
                                <g:select class="ui dropdown la-full-width" name="AccessPoints"
                                          from="${accessPointList}"
                                          optionKey="id"
                                          optionValue="name"
                                          value="${params.status}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                            </div>
                        </div>
             </div>
        </div>
    </g:form>

</ui:modal>
