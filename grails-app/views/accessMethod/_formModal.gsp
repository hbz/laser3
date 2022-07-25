<%@ page import="de.laser.Address;de.laser.PlatformAccessMethod;de.laser.storage.RDConstants" %>

<ui:modal id="accessMethodFormModal" text="${message(code: 'default.add.label', args: [message(code: 'accessMethod.label')])}">
    <g:form class="ui form" url="[controller: 'accessMethod', action: 'create']" method="POST">
         <div class="field" style="height: 330px;">
            <div class="three fields">
                <div class="field wide four">
                    <label for="accessMethod">
                        <g:message code="default.type.label" />
                    </label>
                    <ui:select class="values"
                                     id="accessMethod" name="accessMethod"
                                     from="${PlatformAccessMethod.getAllRefdataValues(RDConstants.ACCESS_METHOD)}"
                                     optionKey="id"
                                     optionValue="value" />
                </div>
                <div class="field wide six">
                    <ui:datepicker label="accessMethod.valid_from" id="validFrom" name="validFrom" placeholder="default.date.label" value="${params.validFrom}">
                    </ui:datepicker>
                    
                </div>

                <div class="field wide six">
                    <ui:datepicker label="accessMethod.valid_to" name="validTo" placeholder="default.date.label" value="${params.validTo}">
                    </ui:datepicker>
                </div>

                <g:hiddenField id="platf_id_${platfId}" name="platfId" value="${platfId}"/>
               
            </div>
        </div>
    </g:form>
</ui:modal>