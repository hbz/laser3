<%@ page import="de.laser.utils.LocaleUtils; de.laser.RefdataCategory; de.laser.Address; de.laser.Org; de.laser.FormService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.storage.RDConstants; de.laser.I10nTranslation;" %>
<laser:serviceInjection />
<ui:modal modalSize="big" id="addressFormModal" text="${modalText ?: message(code: 'address.add.addressForPublic.label')}" msgClose="${message(code: 'default.button.cancel')}" msgSave="${modalMsgSave ?: message(code: 'default.button.create.label')}">
    <g:form id="create_address" class="ui form" url="${url}" method="POST">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <input type="hidden" name="tab" value="addresses"/>
        <g:if test="${addressInstance}">
            <input type="hidden" name="id" value="${addressInstance.id}"/>
        </g:if>
        <div class="fields">

            <g:if test="${orgId && orgId != contextOrg}">
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


            <div class="five wide required field ${hasErrors(bean: addressInstance, field: 'type', 'error')} ">
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
            <div class="six wide required field ${hasErrors(bean: addressInstance, field: 'name', 'error')} ">
                <label for="name">
                    <g:message code="address.name.label" />
                </label>
                <g:textField id="name" name="name" value="${addressInstance?.name}"/>
            </div>
        </div>

        <!-- Alternating address Buttons START-->
        <div class="ui blue buttons" style="width: 100%">
            <button class="ui active button" id="buttonPhysicalAddress">
                <h2 class="ui icon inverted header">
                    <i class="map marked alternate icon"></i>
                    <div class="content">
                        <g:message code="address.streetaddress.label" />
                    </div>
                </h2>
            </button>
            <div class="or" data-text="<g:message code='search.advancedSearch.option.OR' />"></div>
            <button class="ui button" id="buttonPostalAddress">
                <h2 class="ui icon inverted header">
                    <i class="inbox icon"></i>
                    <div class="content">
                        <g:message code="address.pob.label" />
                    </div>
                </h2>
            </button>
        </div>
        <!-- Alternating address Buttons END-->
        <!-- Alternating address Table START-->
        <div class="ui internally celled grid">
            <div class="row">
                <!-- Hausanschrift START -->
                <div class="ui grey inverted segment eight wide column" id="physicalAddress">
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
                <div class="ui grey inverted segment disabled eight wide column" id="postalAddress">
                    <h4 class="ui dividing header"><g:message code="address.pob.label" /></h4>

                    <div class="required wide field">
                        <label for="pob">
                            <g:message code="address.pob.label" />
                        </label>
                        <g:textField id="pob" name="pob" value="${addressInstance?.pob}"/>
                    </div>

                    <div class="required wide field">
                        <label for="pobZipcode">
                            <g:message code="address.zipcode.label" />
                        </label>
                        <g:textField id="pobZipcode" name="pobZipcode" value="${addressInstance?.pobZipcode}"/>
                    </div>

                    <div class="required wide field">
                        <label for="pobCity">
                            <g:message code="address.city.label" />
                        </label>
                        <g:textField id="pobCity" name="pobCity" value="${addressInstance?.pobCity}"/>
                    </div>

                </div>
                <!-- Postanschrift END -->
            </div>
        </div>
        <!-- Alternating address Table END-->






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

        /* Pflichtfelder wenn "Anlegen" geklickt wird*/
        $(".green.button").on("click", function () {

          if($("#buttonPhysicalAddress").hasClass('active')){
                        alert ("test");
            removePostalAddress()
          }
          if($("#buttonPostalAddress").hasClass('active')){
            console.log("buttonPostalAddress .hasClass active");
            removePhysicalAddress();
          }
          $("#create_address").form({
            //on: "blur",
            inline: true,
            fields: {
              org: {
                identifier: "org",
                rules: [
                  {
                    type: "empty",
                    prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      },
      type: {
        identifier: "type",
        rules: [
          {
            type   : 'minCount[1]',
            prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      },
      name: {
        identifier: "name",
        rules: [
          {
            type: "empty",
            prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      },
      /* physicalAddress  */
     /* street_1: {
        identifier: "street_1",
        rules: [
          {
            type: "empty",
            prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      },
      street_2: {
        identifier: "street_2",
        rules: [
          {
            type: "empty",
            prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      },
      zipcode: {
        identifier: "zipcode",
        rules: [
          {
            type: "empty",
            prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      },
      city: {
        identifier: "city",
        rules: [
          {
            type: "empty",
            prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
          }
        ]
      } */
    }
  });
});

/* Alternating address  */
function removePostalAddress() {

    // adding multiple at once from Hausanschrift
  console.log('adding multiple at once from Hausanschrift');
  console.log('removing multiple at once from Postanschrift');
  $("#create_address").form("add rule", "street_1", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte Straße eingeben"
      }
    ]
  });
  $("#create_address").form("add rule", "street_2", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte Nummer eingeben"
      }
    ]
  });
  $("#create_address").form("add rule", "zipcode", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte zipcode eingeben"
      }
    ]
  });
  $("#create_address").form("add rule", "city", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte city eingeben"
      }
    ]
  });
  // removing multiple at once from Postanschrift
  $("#create_address").form("remove fields", ["pob", "pobZipcode", "pobCity"]);
}

function removePhysicalAddress() {
    // adding multiple at once to Postanschrift

  console.log('adding multiple at once to Postanschrift');
  console.log('removing multiple at once from Hausanschrift');
  $("#create_address").form("add rule", "pob", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte pob eingeben"
      }
    ]
  });
  $("#create_address").form("add rule", "pobZipcode", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte pobZipcode eingeben"
      }
    ]
  });
  $("#create_address").form("add rule", "pobCity", {
    rules: [
      {
        type: "empty",
        prompt: "Bitte pobCity eingeben"
      }
    ]
  });
  // removing multiple at once from Hausanschrift
  $("#create_address").form("remove fields", [
    "street_1",
    "street_2",
    "zipcode",
    "city"
  ]);
}

$("#buttonPhysicalAddress").click(function () {
  $(this).addClass("active");
  $("#buttonPostalAddress").removeClass("active");
  deleteInputs(postalAddressInputs);
  $("#postalAddress").addClass("disabled");
  $("#physicalAddress").removeClass("disabled");
  removePostalAddress()
});

$("#buttonPostalAddress").click(function () {
  $(this).addClass("active");
  $("#buttonPhysicalAddress").removeClass("active");
  deleteInputs(physicalAddressInputs);
  $("#postalAddress").removeClass("disabled");
  $("#physicalAddress").addClass("disabled");
  removePhysicalAddress()
});

let postalAddressInputs = [
  $("#type"),
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

function deleteInputs(elems) {
  for (let i = 0; i < elems.length; i++) {
    $(elems[i]).val(null);
  }
}
    %{--

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
    --}%
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
    %{--
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
      --}%
        </laser:script>

    </ui:modal>
