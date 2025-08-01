<%@ page import="de.laser.wekb.Provider; de.laser.wekb.Vendor; de.laser.ui.Icon; de.laser.ui.Btn; de.laser.utils.LocaleUtils; de.laser.RefdataCategory; de.laser.addressbook.Address; de.laser.Org; de.laser.FormService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.storage.RDConstants; de.laser.I10nTranslation;" %>
<laser:serviceInjection />
<ui:modalAddress form="create_address"  modalSize="big" id="addressFormModal" text="${modalText ?: message(code: 'address.add.addressForPublic.label')}" msgClose="${message(code: 'default.button.cancel')}" msgSave="${modalMsgSave ?: message(code: 'default.button.create.label')}">
    <g:form id="create_address" class="ui form" url="${url}" method="POST">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <input type="hidden" name="tab" value="addresses"/>
        <g:if test="${addressInstance}">
            <input type="hidden" name="id" value="${addressInstance.id}"/>
        </g:if>
        <div class="fields">

            <g:if test="${orgId && orgId != contextService.getOrg()}">
                <div class="five wide field">
                    <input id="org" name="org" type="hidden" value="${orgId}"/>
                    <label for="org">
                        <g:message code="person.belongsTo"/>
                    </label>
                    <g:link controller="organisation" action="show" id="${orgId}">${Org.get(orgId).name}</g:link>
                </div>
             </g:if>
            <g:elseif test="${orgList}">
                <div class="five wide required field  ${hasErrors(bean: addressInstance, field: 'org', 'error')} ">
                    <label for="org">
                        <g:message code="person.belongsTo"/>
                    </label>
                    <g:select class="ui dropdown clearable search selection"
                              name="org"
                              from="${orgList}"
                              value=""
                              optionKey="id"
                              optionValue="${{ it.sortname ?: it.name }}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </g:elseif>
            <g:elseif test="${providerId}">
                <div class="five wide field">
                    <input id="provider" name="provider" type="hidden" value="${providerId}"/>
                    <label for="provider">
                        <g:message code="person.belongsTo"/>
                    </label>
                    <g:link controller="provider" action="show" id="${providerId}">${Provider.get(providerId).name}</g:link>
                </div>
             </g:elseif>
            <g:elseif test="${providerList}">
                <div class="five wide required field  ${hasErrors(bean: addressInstance, field: 'provider', 'error')} ">
                    <label for="provider">
                        <g:message code="person.belongsTo"/>
                    </label>
                    <g:select class="ui dropdown clearable search selection"
                              name="provider"
                              from="${providerList}"
                              value=""
                              optionKey="id"
                              optionValue="${{ it.name }}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </g:elseif>
            <g:elseif test="${vendorId}">
                <div class="five wide field">
                    <input id="vendor" name="vendor" type="hidden" value="${vendorId}"/>
                    <label for="vendor">
                        <g:message code="person.belongsTo"/>
                    </label>
                    <g:link controller="vendor" action="show" id="${vendorId}">${Vendor.get(vendorId).name}</g:link>
                </div>
             </g:elseif>
            <g:elseif test="${vendorList}">
                <div class="five wide required field  ${hasErrors(bean: addressInstance, field: 'vendor', 'error')} ">
                    <label for="vendor">
                        <g:message code="person.belongsTo"/>
                    </label>
                    <g:select class="ui dropdown clearable search selection"
                              name="vendor"
                              from="${vendorList}"
                              value=""
                              optionKey="id"
                              optionValue="${{ it.name }}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </g:elseif>

            <g:if test="${prsId}">
                <input id="prs" name="prs" type="hidden" value="${prsId}"/>
            </g:if>
            <g:if test="${tenant}">
                <input id="tenant" name="tenant" type="hidden" value="${tenant}"/>
            </g:if>

            <div class="five wide required field ${hasErrors(bean: addressInstance, field: 'type', 'error')}">
                <label for="typeId">
                    ${RefdataCategory.getByDesc(RDConstants.ADDRESS_TYPE).getI10n('desc')}
                </label>
                %{--<ui:select class="ui dropdown clearable multiple" id="type" name="type.id"--}%
                <ui:select class="ui dropdown clearable search selection" id="typeId" name="type.id"
                              from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.ADDRESS_TYPE)}"
                              optionKey="id"
                              optionValue="value"
                              multiple=""
                              value="${addressInstance?.type?.id ?: typeId}"/>
            </div>
            <div class="six wide required field ${hasErrors(bean: addressInstance, field: 'name', 'error')} ">
                <label for="name">
                    <g:message code="address.name.label" />
                </label>
                <g:textField id="name" name="name" value="${addressInstance?.name}"/>
            </div>
        </div>

        <!-- Alternating address Buttons START-->
        <div class="ui blue buttons" style="width: 100%">
            <div class="ui active button" id="buttonPhysicalAddress">
                <h2 class="ui icon inverted header">
                    <i class="${Icon.ADDRESS}"></i>
                    <div class="content">
                        <g:message code="address.streetaddress.label" />
                    </div>
                </h2>
            </div>
            <div class="or" data-text="<g:message code='search.advancedSearch.option.OR' />"></div>
            <div class="${Btn.SIMPLE}" id="buttonPostalAddress">
                <h2 class="ui icon inverted header">
                    <i class="inbox icon"></i>
                    <div class="content">
                        <g:message code="address.pob.label" />
                    </div>
                </h2>
            </div>
        </div>
        <!-- Alternating address Buttons END-->
        <!-- Alternating address Table START-->
        <div class="ui internally celled grid la-margin-bottom-1em">
            <div class="row">
                <!-- Hausanschrift START -->
                <div class="ui segment eight wide column" id="physicalAddress">
                    <h4 class="ui dividing header"><g:message code="address.streetaddress.label" /></h4>
                    <div class="fields">
                        <div class="twelve wide required field">
                            <label for="street_1">
                                <g:message code="address.street_1.label" />
                            </label>
                            <g:textField id="street_1" name="street_1" value="${addressInstance?.street_1}" />
                        </div>
                        <div class="four wide required field">
                            <label for="street_2">
                                <g:message code="address.street_2.label" />
                            </label>
                            <g:textField id="street_2" name="street_2" value="${addressInstance?.street_2}"/>
                        </div>
                    </div>
                    <div class="fields">
                        <div class="four wide required field">
                            <label for="zipcode">
                                <g:message code="address.zipcode.label" />
                            </label>
                            <g:textField id="zipcode" name="zipcode" value="${addressInstance?.zipcode}" />
                        </div>

                        <div class="twelve wide required field">
                            <label for="city">
                                <g:message code="address.city.label" />
                            </label>
                            <g:textField id="city" name="city" value="${addressInstance?.city}" />
                        </div>
                    </div>
                </div>
                <!-- Hausanschrift END -->
                <!-- Postanschrift START -->
                <div class="ui segment disabled eight wide column" id="postalAddress">
                    <h4 class="ui dividing header"><g:message code="address.pob.label" /></h4>
                    <div class="fields">
                        <div class="sixteen required wide field">
                            <label for="pob">
                                <g:message code="address.pob.label" />
                            </label>
                            <g:textField id="pob" name="pob" value="${addressInstance?.pob}"/>
                        </div>
                    </div>
                    <div class="fields">
                        <div class="four required wide field">
                            <label for="pobZipcode">
                                <g:message code="address.zipcode.label" />
                            </label>
                            <g:textField id="pobZipcode" name="pobZipcode" value="${addressInstance?.pobZipcode}"/>
                        </div>
                        <div class="twelve required wide field">
                            <label for="pobCity">
                                <g:message code="address.city.label" />
                            </label>
                            <g:textField id="pobCity" name="pobCity" value="${addressInstance?.pobCity}"/>
                        </div>
                    </div>

                </div>
                <!-- Postanschrift END -->
            </div>
        </div>
        <!-- Alternating address Table END-->
        <br>
        <div class="field">
            <div class="two fields">
                <div class="eight wide field">
                    <label for="additionFirst">
                        <g:message code="address.additionFirst.label" />
                    </label>
                    <g:textField id="additionFirst" name="additionFirst" value="${addressInstance?.additionFirst}"/>
                </div>
                <div class="eight wide field">
                    <label for="additionSecond">
                        <g:message code="address.additionSecond.label" />
                    </label>
                    <g:textField id="additionSecond" name="additionSecond" value="${addressInstance?.additionSecond}"/>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field eight wide required  ${hasErrors(bean: addressInstance, field: 'country',
                        'error')}">
                    <label for="country">
                        <g:message code="address.country.label" />
                    </label>
                    <ui:select class="ui dropdown clearable search" id="country" name="country.id"
                                  from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.COUNTRY)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${addressInstance?.country?.id}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>

                <div class="field eight wide ${hasErrors(bean: addressInstance, field: 'region',
                        'error')}">
                    <label for="region">
                        <g:message code="address.region.label" />
                    </label>
                    <select id="region" name="region" class="ui search fluid dropdown three column">
                        <option value="">${message(code: 'default.select.choose.label')}</option>
                    </select>
                </div>
            </div>

        </div>
    </g:form>

    <g:set var="languageSuffix" value="${LocaleUtils.getCurrentLang()}"/>
    <laser:script file="${this.getGroovyPageFileName()}">

        /* Mandatory fields */
        $("#create_address").form({
          on: 'submit',
          inline: true,
          fields: {
        <g:if test="${orgId && orgId != contextService.getOrg() || orgList}">
            org: {
              identifier: 'org',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
        </g:if>
        <g:elseif test="${providerList}">
            provider: {
              identifier: 'provider',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
        </g:elseif>
        <g:elseif test="${vendorList}">
            vendor: {
              identifier: 'vendor',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
        </g:elseif>
            typeId: {
              identifier: 'typeId',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
            country: {
              identifier: 'country',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
            name: {
              identifier: 'name',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
            /* physical Address  */
            street_1: {
              identifier: 'street_1',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
            street_2: {
              identifier: 'street_2',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
            zipcode: {
              identifier: 'zipcode',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
            city: {
              identifier: 'city',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            }
          }
        });
        $(".green.button").on("click", function () {
          if ($("#buttonPhysicalAddress").hasClass("active")) {
            JSPC.app.removePostalAddress();
          }
          if ($("#buttonPostalAddress").hasClass("active")) {
            JSPC.app.removePhysicalAddress();
          }
        });

        /* Alternating address  */
        JSPC.app.removePostalAddress = function(){
            // adding multiple at once from Hausanschrift
          $("#create_address").form("add rule", "street_1", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
          $("#create_address").form("add rule", "street_2", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
          $("#create_address").form("add rule", "zipcode", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
          $("#create_address").form("add rule", "city", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
          // removing multiple at once from Postanschrift
          $("#create_address").form("remove fields", ["pob", "pobZipcode", "pobCity"]);
        }

        JSPC.app.removePhysicalAddress = function() {
          // removing multiple at once from Hausanschrift
          $("#create_address").form("remove fields", [
            "street_1",
            "street_2",
            "zipcode",
            "city"
          ]);

            // adding multiple at once to Postanschrift
          $("#create_address").form("add rule", "pob", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
          $("#create_address").form("add rule", "pobZipcode", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
          $("#create_address").form("add rule", "pobCity", {
            rules: [
              {
                type: 'empty',
                prompt:  '{name} <g:message code="validation.needsToBeFilledOut"/>'
              }
            ]
          });
        }

        $("#buttonPhysicalAddress").click(function () {
          $(this).addClass("active");
          $("#buttonPostalAddress").removeClass("active");
          JSPC.app.deleteInputs(postalAddressInputs);
          $("#postalAddress").addClass("disabled");
          $("#physicalAddress").removeClass("disabled");
          JSPC.app.removePostalAddress()
        });

        $("#buttonPostalAddress").click(function () {
          $(this).addClass("active");
          $("#buttonPhysicalAddress").removeClass("active");
          JSPC.app.deleteInputs(physicalAddressInputs);
          $("#postalAddress").removeClass("disabled");
          $("#physicalAddress").addClass("disabled");
          JSPC.app.removePhysicalAddress()
        });

        let postalAddressInputs = [
          $("#pob"),
          $("#pobZipcode"),
          $("#pobCity")
        ];
        let physicalAddressInputs = [
          $("#street_1"),
          $("#street_2"),
          $("#zipcode"),
          $("#city")
        ];

        let postalAddressFound = false;
        for (var i = 0; i < postalAddressInputs.length; i++) {

            if ($(postalAddressInputs[i]).val() !== "") {
                postalAddressFound = true;
                console.log("postalAddressInputs: Non empty");
                $("#buttonPostalAddress").addClass("active");
                $("#buttonPhysicalAddress").removeClass("active");
                $("#physicalAddress").addClass("disabled");
                $("#postalAddress").removeClass("disabled");
                $("#create_address").form("remove fields", ["street_1","street_2","zipcode","city"]);
                break;
            }
        }

        let physicalAddressFound = false;
        for (var i = 0; i < physicalAddressInputs.length; i++) {

            if ($(physicalAddressInputs[i]).val() !== "") {
                physicalAddressFound = true;
                $("#buttonPhysicalAddress").addClass("active");
                $("#buttonPostalAddress").removeClass("active");
                $("#postalAddress").addClass("disabled");
                $("#physicalAddress").removeClass("disabled");
                $("#create_address").form("remove fields", ["pob", "pobZipcode", "pobCity"]);
                break;
            }
        }

    JSPC.app.deleteInputs = function (elems) {
      for (let i = 0; i < elems.length; i++) {
        $(elems[i]).val(null);
      }
    }

    JSPC.app.updateDropdown = function() {
        var dropdownRegion = $('#region');
        var selectedCountry = $("#country").val();

            dropdownRegion.empty();
            dropdownRegion.append('<option selected="true" disabled>${message(code: 'default.select.choose.label')}</option>');
            dropdownRegion.prop('selectedIndex', 0);

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getRegions"/>'
                + '?country=' + selectedCountry + '&format=json',
                success: function (data) {
                    $.each(data, function (key, entry) {
                        <g:if test="${addressInstance?.region}">
                            if(entry.id == ${addressInstance.region.id}){
                                dropdownRegion.append($('<option></option>').attr('value', entry.id).attr('selected', 'selected').text(entry.${"value_" + languageSuffix}));
                            }
                            else{
                                dropdownRegion.append($('<option></option>').attr('value', entry.id).text(entry.${"value_" + languageSuffix}));
                            }
                        </g:if>
                        <g:else>
                            dropdownRegion.append($('<option></option>').attr('value', entry.id).text(entry.${"value_" + languageSuffix}));
                        </g:else>
                     });
                }
            });
        }

        if($("#country").val()) { JSPC.app.updateDropdown(); }

        $("#country").change(function() { JSPC.app.updateDropdown(); });

    </laser:script>

    </ui:modalAddress>
