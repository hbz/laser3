<%@ page import="com.k_int.kbplus.Address" %>

<semui:modal id="accessMethodFormModal" text="${message(code: 'default.add.label', args: [message(code: 'accessMethod.label', default: 'Address')])}">
    <g:form class="ui form" url="[controller: 'accessMethod', action: 'create']" method="POST">
         <div class="field" style="height: 330px;">
            <div class="three fields">
                <div class="field wide four fieldcontain">
                    <label for="accessMethod">
                        <g:message code="accessMethod.type.label" default="accessMethod" />
                    </label>
                    <laser:select class="values"
                                     name="accessMethod"
                                     from="${com.k_int.kbplus.PlatformAccessMethod.getAllRefdataValues('Access Method')}"
                                     optionKey="id"
                                     optionValue="value" />
                </div>
                <div class="field wide six fieldcontain ">
                    <semui:datepicker label ="accessMethod.valid_from" name="validFrom" placeholder ="default.date.label" value ="${params.validFrom}">
                    </semui:datepicker>
                    
                </div>

                <div class="field wide six fieldcontain  ">
                    <semui:datepicker label ="accessMethod.valid_to" name="validTo" placeholder ="default.date.label" value ="${params.validTo}">
                    </semui:datepicker>
                </div>

                <g:hiddenField name="platfId" value="${platfId}"/>
               
            </div>
        </div>
    </g:form>
</semui:modal>