<%@ page import="de.laser.utils.LocaleUtils; de.laser.RefdataCategory; de.laser.Address; de.laser.Org; de.laser.FormService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.storage.RDConstants; de.laser.I10nTranslation;" %>
<laser:serviceInjection />
<ui:modal id="addressFormModal" text="${modalText ?: message(code: 'address.add.addressForPublic.label')}" msgClose="${message(code: 'default.button.cancel')}" msgSave="${modalMsgSave ?: message(code: 'default.button.create.label')}">
    <g:form id="create_address" class="ui form" url="${url}" method="POST">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <input type="hidden" name="tab" value="addresses"/>
        <g:if test="${addressInstance}">
            <input type="hidden" name="id" value="${addressInstance.id}"/>
        </g:if>
        <g:if test="${orgId && orgId != contextOrg}">
            <input id="org" name="org" type="hidden" value="${orgId}"/>
            <label for="org">
                <g:message code="person.belongsTo"/>
            </label>
            <g:link controller="organisation" action="show" id="${orgId}">${Org.get(orgId).name}</g:link>
        </g:if>
        <g:elseif test="${orgList}">
            <div class="field ${hasErrors(bean: addressInstance, field: 'org', 'error')} ">
                <label for="org">
                    <g:message code="person.belongsTo"/> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown search selection"
                          name="org"
                          from="${orgList}"
                          value=""
                          optionKey="id"
                          optionValue="${{ it.sortname ?: it.name }}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </g:elseif>
        <g:if test="${prsId}">
            <input id="prs" name="prs" type="hidden" value="${prsId}"/>
        </g:if>
        <g:if test="${tenant}">
            <input id="tenant" name="tenant" type="hidden" value="${tenant}"/>
        </g:if>

        <div class="field ${hasErrors(bean: addressInstance, field: 'type', 'error')} ">
            <label for="type">
                ${RefdataCategory.getByDesc(RDConstants.ADDRESS_TYPE).getI10n('desc')}
            </label>
            %{--<ui:select class="ui dropdown multiple" id="type" name="type.id"--}%
            <ui:select class="ui dropdown search selection" id="type" name="type.id"
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
                    <ui:select class="ui dropdown" id="country" name="country.id"
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
                    <select id="region" name="region" class="ui search fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>
                    </select>
                </div>
            </div>

        </div>
    </g:form>

    <g:set var="languageSuffix" value="${LocaleUtils.getCurrentLang()}"/>
    <laser:script file="${this.getGroovyPageFileName()}">
        
$('#create_address').form(
    %{--            let addressElements = [$('#type'), $('#name'), $('#additionFirst'), $('#additionSecond'), $('#street_1'), $('#street_2'), $('#zipcode'), $('#city'), $('#pob'), $('#pobZipcode'), $('#pobCity'), $('#country'), $('#region')];
                if(!JSPC.app.areElementsFilledOut(addressElements)) {
                    $('#create_address .ui.info.message').show();
                }--}%
        {
        on: 'blur',
        inline: true,
        fields: {
            org: {
                identifier  : 'org',
                rules: [
                    {
                        type   : 'empty',
                        prompt : '{name} <g:message code="validation.needsToBeFilledOut"/>'
                        }
                    ]
                },
                additionFirst: {
                    identifier: 'additionFirst',
                    rules: [
                        {
                            type   : 'empty',
                            prompt : '{name} <g:message code="validation.needsToBeFilledOut"/>'
                        }
                    ]
                },
                additionSecond: {
                    identifier  : 'additionSecond',
                    rules: [
                        {
                            type   : 'empty',
                            prompt : '{name} <g:message code="validation.needsToBeFilledOut"/>'
                        }
                    ]
                }
            }
        });

        JSPC.app.updateDropdown = function() {
            var dropdownRegion = $('#region');
            var selectedCountry = $("#country").val();
            var selectedRegions = ${raw(params.list('region') as String)};

            dropdownRegion.empty();
            dropdownRegion.append('<option selected="selected" disabled>${message(code: 'default.select.choose.label')}</option>');
            dropdownRegion.prop('selectedIndex', 0);

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getRegions"/>'
                + '?country=' + selectedCountry + '&format=json',
                success: function (data) {
                    $.each(data, function (key, entry) {
                        if(jQuery.inArray(entry.id, selectedRegions) >=0 ){
                            dropdownRegion.append($('<option></option>').attr('value', entry.id).attr('selected', 'selected').text(entry.${"value_" + languageSuffix}));
                        }else{
                            dropdownRegion.append($('<option></option>').attr('value', entry.id).text(entry.${"value_" + languageSuffix}));
                        }
                     });
                }
            });
        }

        if($("#country").val()) { JSPC.app.updateDropdown(); }

        $("#country").change(function() { JSPC.app.updateDropdown(); });

%{--        $("#create_address").submit(function (e) {
            e.preventDefault();
            let addressElements = [$('#type'), $('#name'), $('#additionFirst'), $('#additionSecond'), $('#street_1'), $('#street_2'), $('#zipcode'), $('#city'), $('#pob'), $('#pobZipcode'), $('#pobCity'), $('#country'), $('#region')];
            if(!JSPC.app.areElementsFilledOut(addressElements)) {
                if(confirm("${message(code:'person.create.noAddressConfirm')}")) {
                    $('#create_address').unbind('submit').submit();
                }
            }
            else $('#create_address').unbind('submit').submit();
        });--}%

        JSPC.app.areElementsFilledOut = function (elems) {
            let filledOut = false;
            if(elems !== null) {
                for(let i = 0; i < elems.length; i++) {
                    filledOut = elems[i].val() !== null && elems[i].val() !== "null" && elems[i].val().length > 0
                    if(filledOut)
                        break;
                }
            }
            else filledOut = true;
            return filledOut;
        };
    </laser:script>

</ui:modal>
