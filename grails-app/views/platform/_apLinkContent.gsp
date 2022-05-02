<semui:filter>
    <g:form action="linkAccessPoint" controller="platform" method="get" class="ui small form">
        <input type="hidden" name="platform_id" value="${platformInstance.id}">
        <div class="fields">
            <div class="field">
                <g:select class="ui dropdown" name="institutions"
                          from="${institution}"
                          optionKey="id"
                          optionValue="name"
                          value="${selectedInstitution} "
                          onchange="${laser.remoteJsOnChangeHandler(
                                  action:   'dynamicApLink',
                                  data:     '{platform_id:' + platformInstance.id + ', institution_id:this.value}',
                                  update:   '#dynamicUpdate',
                                  updateOnFailure: '#failure'
                          )}"/>
            </div>
            <div class="field">
                <g:select class="ui dropdown" name="AccessPoints"
                          from="${accessPointList}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.status}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <g:submitButton name="submit" class="ui button trash alternate" value="${message(code:'platform.link.accessPoint.button.label')}" onClick="return confirmSubmit()"/>
            </div>
        </div>
    </g:form>
</semui:filter>
<table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header">
    <thead>
    <tr>
        <th>${message(code:'platform.link.accessPoint.grid.activeConfiguration')}</th>
        <th>${message(code:'default.action.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${accessPointLinks}" var="oapl">
        <tr>
            <td>${oapl.oap.name}</td>
            <td>
                <g:if test="${oapl.oap.hasActiveLink()}">
                    <g:link controller="platform" action="removeAccessPoint" id="${platformInstance.id}" params="[oapl_id: oapl.id]" onclick="return confirm('Zugangspunkt entfernen?')"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                        <i class="trash icon red"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="platform" action="linkAccessPoint" id="${platformInstance.id}" params="[oapl_id: oapl.id]" onclick="return confirm('Zugangspunkt hinzufügen?')">
                        <i class="linkify icon"></i>
                    </g:link>
                </g:else>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>