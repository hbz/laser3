<%@ page import="de.laser.Address; de.laser.FormService; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.RefdataCategory;de.laser.storage.RDConstants; de.laser.I10nTranslation;" %>

<laser:serviceInjection />
<div id="addressFields1">
        <g:if test="${multipleAddresses}">
            <input type="hidden" name="multipleAddresses" value="multipleAddresses"/>
        </g:if>
        <g:if test="${addressInstance}">
            <input type="hidden" name="id" value="${addressInstance.id}"/>
        </g:if>
        <g:if test="${orgId}">
            <input id="org" name="org" type="hidden" value="${orgId}"/>
        </g:if>
        <g:if test="${prsId}">
            <input id="prs" name="prs" type="hidden" value="${prsId}"/>
        </g:if>
        %{--<g:if test="${typeId}">
            <input id="type" name="type.id" type="hidden" value="${typeId}"/>
        </g:if>--}%

        <div class="field ${hasErrors(bean: addressInstance, field: 'type', 'error')} ">
            <label for="type">
                ${RefdataCategory.getByDesc(RDConstants.ADDRESS_TYPE).getI10n('desc')}
            </label>
            %{--<ui:select class="ui dropdown multiple" id="type" name="type.id"--}%
            <ui:select class="ui dropdown search selection" id="type" name="type"
                          from="${Address.getAllRefdataValues()}"
                          optionKey="id"
                          optionValue="value"
                          multiple=""
                          value="${addressInstance?.type?.id ?: typeId}"/>
        </div>
        <div class="field ${hasErrors(bean: addressInstance, field: 'name', 'error')} ">
            <label for="name">
                <g:message code="address.name.label" />
            </label>
            <g:textField id="name" name="name" value="${addressInstance?.name}"/>
        </div>

        <div class="field ${hasErrors(bean: addressInstance, field: 'additionFirst', 'error')} ">
            <label for="additionFirst">
                <g:message code="address.additionFirst.label" />
            </label>
            <g:textField id="additionFirst" name="additionFirst" value="${addressInstance?.additionFirst}"/>
        </div>

        <div class="field ${hasErrors(bean: addressInstance, field: 'additionSecond', 'error')} ">
            <label for="additionSecond">
                <g:message code="address.additionSecond.label" />
            </label>
            <g:textField id="additionSecond" name="additionSecond" value="${addressInstance?.additionSecond}"/>
        </div>

        <h4 class="ui dividing header"><g:message code="address.streetaddress.label" /></h4>

        <div class="field">
            <div class="two fields">
                <div class="field thirteen wide ${hasErrors(bean: addressInstance, field:
                        'street_1', 'error')}">
                    <label for="street_1">
                        <g:message code="address.street_1.label" />
                    </label>
                    <g:textField id="street_1" name="street_1" value="${addressInstance?.street_1}" />
                </div>

                <div class="field three wide ${hasErrors(bean: addressInstance, field: 'street_2',
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
                <div class="field three wide ${hasErrors(bean: addressInstance, field: 'zipcode', 'error')}">
                    <label for="zipcode">
                        <g:message code="address.zipcode.label" />
                    </label>
                    <g:textField id="zipcode" name="zipcode" value="${addressInstance?.zipcode}" />
                </div>

                <div class="field thirteen wide ${hasErrors(bean: addressInstance, field: 'city', 'error')}">
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
                <div class="field six wide ${hasErrors(bean: addressInstance, field: 'pob', 'error')} ">
                    <label for="pob">
                        <g:message code="address.pob.label" />
                    </label>
                    <g:textField id="pob" name="pob" value="${addressInstance?.pob}"/>
                </div>

                <div class="field three wide ${hasErrors(bean: addressInstance, field: 'pobZipcode', 'error')} ">
                    <label for="pobZipcode">
                        <g:message code="address.zipcode.label" />
                    </label>
                    <g:textField id="pobZipcode" name="pobZipcode" value="${addressInstance?.pobZipcode}"/>
                </div>

                <div class="field seven wide ${hasErrors(bean: addressInstance, field: 'pobCity', 'error')} ">
                    <label for="pobCity">
                        <g:message code="address.city.label" />
                    </label>
                    <g:textField id="pobCity" name="pobCity" value="${addressInstance?.pobCity}"/>
                </div>
            </div>
        </div>
        <div class="field">
            <div class="two fields">
                <div class="field seven wide ${hasErrors(bean: addressInstance, field: 'country',
                        'error')}">
                    <label for="country">
                        <g:message code="address.country.label" />
                    </label>
                    <ui:select class="ui dropdown" id="country" name="country"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.COUNTRY)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${addressInstance?.country?.id}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>

                <div class="field nine wide ${hasErrors(bean: addressInstance, field: 'region',
                        'error')}">
                    <label for="region">
                        <g:message code="address.region.label" />
                    </label>
                    <ui:select class="ui dropdown" id="region" name="region"
                                  from="${RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH])}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${addressInstance?.region?.id}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </div>

        </div>

</div>
