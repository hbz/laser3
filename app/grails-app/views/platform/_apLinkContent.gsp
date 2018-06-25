<semui:filter>
    <g:form action="linkAccessPoint" controller="platform" method="get" class="form-inline ui small form">
        <input type="hidden" name="platform_id" value="${platformInstance.id}">
        <div class="fields">
            <div class="field">
                <g:select class="ui dropdown" name="institutions"
                          from="${institution}"
                          optionKey="id"
                          optionValue="name"
                          value="${selectedInstitution} "
                          onchange="${remoteFunction (
                                  action: 'dynamicApLink',
                                  params: '{platform_id:'+platformInstance.id+', institution_id:this.value}',
                                  update: [success: 'dynamicUpdate', failure: 'failure'],
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
                <g:submitButton name="submit" class="ui button" value="${message(code:'accessPoint.link.button.label', default:'Zugangsverfahren verknüpfen')}" onClick="return confirmSubmit()"/>

                %{--<g:actionSubmit action="linkAccessPoint" class="ui primary button" value="${message(code:'accessPoint.link.button.label', default:'Zugangsverfahren verknüpfen')}" onClick="return confirmSubmit()"/>--}%
            </div>
        </div>
    </g:form>
</semui:filter>
<table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
    <thead>
    <tr>
        <th></th>
        <th>Aktive Zugangspunkte</th>
        <th>Aktion</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${accessPointLinks}" var="oapl">
        <tr>
            <td><input type="checkbox" name="_bulkflag.aopp" class="bulkcheck"/></td>
            <td>${oapl.oap.name}</td>
            <td>
                <g:if test="${oapl.oap.hasActiveLink()}">
                    <g:link controller="platform" action="removeAccessPoint" id="${platformInstance.id}" params="[oapl_id: oapl.id]" onclick="return confirm('Zugangspunkt entfernen?')">
                        <i class="unlink icon red"></i>
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