<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Address;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory;de.laser.helper.RDConstants" %>
<semui:modal id="addressFormModal" text="${modalText}" msgSave="${modalMsgSave}">
    <g:form id="create_address" class="ui form" url="${url}" method="POST">
        <g:if test="${addressInstance}">
            <input type="hidden" name="id" value="${addressInstance.id}"/>
        </g:if>
        <g:if test="${orgId}">
            <input id="org" name="org.id" type="hidden" value="${orgId}"/>
        </g:if>
        <g:if test="${prsId}">
            <input id="prs" name="prs.id" type="hidden" value="${prsId}"/>
        </g:if>
        <input type="hidden" name="redirect" value="true"/>
        <input id="type" name="type.id" type="hidden" value="${typeId}"/>

        <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'name', 'error')} ">
            <label for="name">
                <g:message code="address.name.label" />
            </label>
            <g:textField id="name" name="name" value="${addressInstance?.name}"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'additionFirst', 'error')} ">
            <label for="additionFirst">
                <g:message code="address.additionFirst.label" />
            </label>
            <g:textField id="additionFirst" name="additionFirst" value="${addressInstance?.additionFirst}"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'additionSecond', 'error')} ">
            <label for="additionSecond">
                <g:message code="address.additionSecond.label" />
            </label>
            <g:textField id="additionSecond" name="additionSecond" value="${addressInstance?.additionSecond}"/>
        </div>
        <div class="field">
            <div class="two fields">
                <div class="field nine wide fieldcontain ${hasErrors(bean: addressInstance, field: 'region',
                        'error')}">
                    <label for="region">
                        <g:message code="address.region.label" />
                    </label>
                    <laser:select class="ui dropdown" id="region" name="region.id"
                                  from="${RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE,
                                                                               RDConstants.REGIONS_AT,
                                                                               RDConstants.REGIONS_CH])}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>

                <div class="field seven wide fieldcontain ${hasErrors(bean: addressInstance, field: 'country',
                        'error')}">
                    <label for="country">
                        <g:message code="address.country.label" />
                    </label>
                    <laser:select class="ui dropdown" id="country" name="country.id"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.COUNTRY)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${addressInstance?.country?.id}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </div>
        </div>

        <h4 class="ui dividing header"><g:message code="address.streetaddress.label" /></h4>

        <div class="field">
            <div class="two fields">
                <div class="field thirteen wide fieldcontain ${hasErrors(bean: addressInstance, field:
                        'street_1', 'error')}">
                    <label for="street_1">
                        <g:message code="address.street_1.label" />
                    </label>
                    <g:textField id="street_1" name="street_1" value="${addressInstance?.street_1}" />
                </div>

                <div class="field three wide fieldcontain ${hasErrors(bean: addressInstance, field: 'street_2',
                        'error')} ">
                    <label for="street_2">
                        <g:message code="address.street_2.label" />
                    </label>
                    <g:textField id="street_2" name="street_2" value="${addressInstance?.street_2}"/>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field three wide fieldcontain ${hasErrors(bean: addressInstance, field: 'zipcode', 'error')}">
                    <label for="zipcode">
                        <g:message code="address.zipcode.label" />
                    </label>
                    <g:textField id="zipcode" name="zipcode" value="${addressInstance?.zipcode}" />
                </div>

                <div class="field thirteen wide fieldcontain ${hasErrors(bean: addressInstance, field: 'city', 'error')}">
                    <label for="city">
                        <g:message code="address.city.label" />
                    </label>
                    <g:textField id="city" name="city" value="${addressInstance?.city}" />
                </div>
            </div>
        </div>

        <h4 class="ui dividing header"><g:message code="address.pob.label" /></h4>

        <div class="field">
            <div class="three fields">
                <div class="field six wide fieldcontain ${hasErrors(bean: addressInstance, field: 'pob', 'error')} ">
                    <label for="pob">
                        <g:message code="address.pob.label" />
                    </label>
                    <g:textField id="pob" name="pob" value="${addressInstance?.pob}"/>
                </div>

                <div class="field three wide fieldcontain ${hasErrors(bean: addressInstance, field: 'pobZipcode', 'error')} ">
                    <label for="pobZipcode">
                        <g:message code="address.zipcode.label" />
                    </label>
                    <g:textField id="pobZipcode" name="pobZipcode" value="${addressInstance?.pobZipcode}"/>
                </div>

                <div class="field seven wide fieldcontain ${hasErrors(bean: addressInstance, field: 'pobCity', 'error')} ">
                    <label for="pobCity">
                        <g:message code="address.city.label" />
                    </label>
                    <g:textField id="pobCity" name="pobCity" value="${addressInstance?.pobCity}"/>
                </div>
            </div>
        </div>
        <g:if test="${ ! typeId }">
            <h4 class="ui dividing header"><g:message code="address.additionals.label"/></h4>
            <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'type', 'error')} ">
                <label for="type">
                    ${RefdataCategory.getByDesc(RDConstants.ADDRESS_TYPE).getI10n('desc')}
                </label>
                <laser:select class="ui dropdown" id="type" name="type.id"
                              from="${Address.getAllRefdataValues()}"
                              optionKey="id"
                              optionValue="value"
                              value="${addressInstance?.type?.id}"/>
            </div>
        </g:if>
    </g:form>
</semui:modal>
