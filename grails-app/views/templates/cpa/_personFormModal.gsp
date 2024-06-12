<%@ page import="de.laser.properties.PropertyDefinition; de.laser.PersonRole; de.laser.Contact; de.laser.Person; de.laser.FormService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.RefdataCategory;de.laser.storage.RDConstants" %>
<laser:serviceInjection/>
<style>

</style>

<ui:modal  id="${modalID ?: 'personModal'}" formID="person_form"
           modalSize="big"
           text="${modalText ?: message(code: 'person.create_new.label')}"
           msgClose="${message(code: 'default.button.cancel')}"
           msgSave="${message(code: 'default.button.save.label')}">
    <g:form id="person_form" class="ui form" url="${url}" method="POST">
        <g:if test="${!personInstance}">
            <input name="tenant.id" type="hidden" value="${tenant.id}"/>
            <input name="isPublic" type="hidden" value="${personInstance?.isPublic ?: (isPublic ?: false)}"/>
            <g:if test="${org}">
                <input name="personRoleOrg" type="hidden" value="${org.id}"/>
            </g:if>
            <g:if test="${provider}">
                <input name="personRoleProvider" type="hidden" value="${provider.id}"/>
            </g:if>
            <g:if test="${vendor}">
                <input name="personRoleVendor" type="hidden" value="${vendor.id}"/>
            </g:if>
        </g:if>

    %{--Only for public contact person for Provider/Agency --}%
    <%--<g:if test="${contactPersonForProviderAgencyPublic && !personInstance}">
        <input name="personRoleOrg" type="hidden" value="${tenant.id}"/>
        <input name="functionType" type="hidden" value="${presetFunctionType.id}"/>
        <input name="last_name" type="hidden" value="${presetFunctionType.getI10n('value')}"/>
    </g:if>--%>

        <g:if test="${!contactPersonForProviderPublic && !contactPersonForVendorPublic}">

            <div class="field">
                <div class="two fields">
                    <g:if test="${!isPublic}">
                        <div class="field required">
                            <g:if test="${orgList}">
                                <label for="personRoleOrg">
                                    <g:message code="person.belongsTo"/> <g:message code="messageRequiredField" />
                                </label>
                                <g:select class="ui search dropdown"
                                          name="personRoleOrg"
                                          from="${orgList}"
                                          value="${org?.id}"
                                          optionKey="id"
                                          optionValue="${{ it.sortname ?: it.name }}"
                                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                            </g:if>
                            <g:if test="${provList}">
                                <label for="personRoleOrg">
                                    <g:message code="person.belongsTo"/> <g:message code="messageRequiredField" />
                                </label>
                                <g:select class="ui search dropdown"
                                          name="personRoleProvider"
                                          from="${provList}"
                                          value="${provider?.id}"
                                          optionKey="id"
                                          optionValue="${{ it.sortname ?: it.name }}"
                                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                            </g:if>
                            <g:if test="${venList}">
                                <label for="personRoleOrg">
                                    <g:message code="person.belongsTo"/> <g:message code="messageRequiredField" />
                                </label>
                                <g:select class="ui search dropdown"
                                          name="personRoleVendor"
                                          from="${venList}"
                                          value="${ven?.id}"
                                          optionKey="id"
                                          optionValue="${{ it.sortname ?: it.name }}"
                                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                            </g:if>
                            <g:if test="${org}">
                                <label for="personRoleOrg">
                                    <g:message code="person.belongsTo"/>
                                </label>
                                <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link>
                                <input name="personRoleOrg" type="hidden" value="${org.id}"/>
                            </g:if>
                            <g:elseif test="${provider}">
                                <label for="personRoleProvider">
                                    <g:message code="person.belongsTo"/>
                                </label>
                                <g:link controller="provider" action="show" id="${provider.id}">${provider.name}</g:link>
                                <input name="personRoleProvider" type="hidden" value="${provider.id}"/>
                            </g:elseif>
                            <g:elseif test="${vendor}">
                                <label for="personRoleVendor">
                                    <g:message code="person.belongsTo"/>
                                </label>
                                <g:link controller="vendor" action="show" id="${vendor.id}">${vendor.name}</g:link>
                                <input name="personRoleVendor" type="hidden" value="${vendor.id}"/>
                            </g:elseif>
                        %{--<g:else>
                            <label for="personRoleOrg">
                                <g:message code="contact.belongesTo.label"/>
                            </label>
                            <i class="icon university la-list-icon"></i>${org?.name}
                            <input id="personRoleOrg" name="personRoleOrg" type="hidden" value="${org?.id}"/>
                        </g:else>--}%
                        </div>
                    </g:if>

                %{-- <g:if test="${actionName != 'contacts'}">
                     <div class="field">
                         <g:if test="${institution}">
                             <label for="functionOrg">
                                 <g:message code="contact.belongesTo.label"/>
                             </label>
                             <g:select class="ui search dropdown"
                                       name="functionOrg"
                                       from="${orgList}"
                                       value="${org?.id}"
                                       optionKey="id"
                                       optionValue=""/>
                         </g:if>
                         <g:else>
                             <label for="functionOrg">
                                 <g:message code="contact.belongesTo.label"/>
                             </label>
                             <i class="icon university la-list-icon"></i>${org?.name}
                             <input id="functionOrg" name="functionOrg" type="hidden" value="${org?.id}"/>
                         </g:else>
                     </div>
                 </g:if>--}%

                %{--<g:if test="${actionName != 'contacts'}">
                    <div class="field">

                        <g:if test="${institution}">
                            <label for="positionOrg">
                                <g:message code="contact.belongesTo.label"/>
                            </label>
                            <g:select class="ui search dropdown"
                                      name="positionOrg"
                                      from="${orgList}"
                                      value="${org?.id}"
                                      optionKey="id"
                                      optionValue=""/>
                        </g:if>
                        <g:else>
                            <label for="positionOrg">
                                <g:message code="contact.belongesTo.label"/>
                            </label>
                            <i class="icon university la-list-icon"></i>${org?.name}
                            <input id="positionOrg" name="positionOrg" type="hidden" value="${org?.id}"/>
                        </g:else>
                    </div>
                </g:if>--}%

                </div>
            </div><!-- .field -->
                <!-- Alternating contact buttons START-->
            <div class="ui blue buttons" style="width: 100%">
                <button class="ui active button" id="la-js-buttonSurname">
                    <h2 class="ui icon inverted header">
                        <i class="address card outline icon"></i>
                        <div class="content">
                            Nachname
                        </div>
                    </h2>
                </button>
                <div class="or" data-text="<g:message code='search.advancedSearch.option.OR' />"></div>
                <button class="ui button" id="la-js-buttonFunction">
                    <h2 class="ui icon inverted header">
                        <i class="wrench icon"></i>
                        <div class="content">
                            Funktionsbezeichnung
                        </div>
                    </h2>
                </button>
            </div>
            <!-- Alternating contact buttons END-->
            <br><br>
            <div class="field">
                <div class="two fields">

                    <div class="field wide twelve ${hasErrors(bean: personInstance, field: 'last_name', 'error')} required">
                        <label id="la-js-nameOrFunction" for="last_name">
                            <g:message code="person.last_name.label"/> <g:message code="messageRequiredField" />
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: 'person.last_name.info')}">
                                <i class="question circle icon"></i>
                            </span>
                        </label>
                        <g:textField name="last_name" required="" value="${personInstance?.last_name}"/>
                    </div>



                    <div id="person_gender"
                         class="field wide four ${hasErrors(bean: personInstance, field: 'gender', 'error')} ">
                        <label for="gender">
                            <g:message code="person.gender.label"/>
                        </label>
                        <ui:select class="ui dropdown" id="gender" name="gender"
                                   from="${Person.getAllRefdataValues(RDConstants.GENDER)}"
                                   optionKey="id"
                                   optionValue="value"
                                   value="${personInstance?.gender?.id}"
                                   noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                    </div>

                </div>
            </div>

            <div class="ui accordion la-namedetails field">
                <div class="title">
                    <i class="icon dropdown"></i>
                    Namensdetails
                </div>
                <div class="content field">
                    <div class="three fields">
                        <div id="person_title"
                             class="field wide four ${hasErrors(bean: personInstance, field: 'title', 'error')}">
                            <label for="title">
                                <g:message code="person.title.label"/>
                            </label>
                            <g:textField name="title" value="${personInstance?.title}"/>
                        </div>
                        <div id="person_first_name"
                             class="field wide eight ${hasErrors(bean: personInstance, field: 'first_name', 'error')}">
                            <label for="first_name">
                                <g:message code="person.first_name.label"/>
                            </label>
                            <g:textField name="first_name" value="${personInstance?.first_name}"/>
                        </div>

                        <div id="person_middle_name"
                             class="field wide four ${hasErrors(bean: personInstance, field: 'middle_name', 'error')} ">
                            <label for="middle_name">
                                <g:message code="person.middle_name.label"/>
                            </label>
                            <g:textField name="middle_name" value="${personInstance?.middle_name}"/>
                        </div>
                    </div>
                </div>
            </div>

            <g:if test="${!tmplHideFunctions}">

                <div class="field">
                    <div class="two fields">
                        <div class="field">
                            <label for="functionType">
                                <g:message code="person.function.label"/>
                            </label>

                            <select name="functionType" id="functionType" multiple=""
                                    class="ui search selection dropdown sortable">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${functions ?: PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}"
                                        var="functionType">
                                    <option <%=(personInstance ? (functionType.id in personInstance.getPersonRoleByOrg(org ?: contextOrg).functionType?.id) : (presetFunctionType?.id == functionType.id)) ? 'selected="selected"' : ''%>
                                            value="${functionType.id}">
                                        ${functionType.getI10n('value')}
                                    </option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field">
                            <label for="positionType">
                                <g:message code="person.position.label"/>
                            </label>
                            <select name="positionType" id="positionType" multiple=""
                                    class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${positions ?: PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}"
                                        var="positionType">
                                    <option <%=(personInstance ? (positionType.id in personInstance.getPersonRoleByOrg(org ?: contextOrg).positionType?.id) : (presetPositionType?.id == positionType.id)) ? 'selected="selected"' : ''%>
                                            value="${positionType.id}">
                                        ${positionType.getI10n('value')}
                                    </option>
                                </g:each>
                            </select>

                        </div>

                    </div>
                </div><!-- .field -->

            </g:if>

        </g:if>
    %{-- Buttons for selection of kind of contact START --}%
    <div class="ui wrapping spaced buttons">
        <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_EMAIL.id}"><i class="icon  envelope outline"></i>E-Mail hinzufügen</a>
        <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_FAX.id}"><i class="tty circle icon"></i>Fax hinzufügen</a>
        <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_MOBILE.id}"><i class="mobile alternate circle icon"></i>Mobil hinzufügen</a>
        <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_PHONE.id}"><i class="phone circle icon"></i>Telefon hinzufügen</a>
        <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_URL.id}"><i class="globe circle icon"></i>Url hinzufügen</a>
    </div>
    %{-- Buttons for selection of kind of contact END --}%
    <g:if test="${showContacts}">
        <div class="field">
            <br />
            <label>
                <g:message code="person.contacts.label"/>:
            </label>

            <g:if test="${personInstance}">
                <g:each in="${personInstance.contacts?.toSorted()}" var="contact" status="i">
                    <div class="three fields contactField" id="contactFields${i}">
                        <div class="field wide four ">
                            <input type="text" readonly value="${contact.contentType.getI10n('value')}"/>
                        </div>

                        <div class="field wide four">
                            <ui:select class="ui search dropdown" name="contactLang${contact.id}"
                                       from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)}"
                                       optionKey="id"
                                       optionValue="value"
                                       value="${contact.language?.id}"
                                       noSelection="['null': '']"/>
                        </div>

                        <div class="field wide eight">
                            <g:textField name="content${contact.id}" value="${contact.content}"/>
                        </div>
                    </div>
                </g:each>
            </g:if>
        </div>
        <g:if test="${addContacts}">
            <button type="button" id="addContactElement" class="ui icon button">
                <i class="plus green circle icon"></i>
            </button>

            <button type="button" id="removeContactElement" class="ui icon button">
                <i class="minus red circle icon"></i>
            </button>

            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                  data-content="${message(code: 'person.contacts.add.button')}">
                <i class="question circle icon"></i>
            </span>

            <br />
            <br />
            <div class="field">
                <div class="three fields contactField" id="contactFields${personInstance?.contacts ? personInstance.contacts.size()+1 : 1}">
                    <div class="field one wide la-contactIconField">
                        <i class="icon large envelope outline la-js-contactIcon"></i>
                    </div>
                    <div class="field wide four">
                        <ui:select class="ui dropdown contentType" name="contentType.id"
                                   from="${[RDStore.CCT_EMAIL, RDStore.CCT_FAX, RDStore.CCT_MOBILE, RDStore.CCT_PHONE, RDStore.CCT_URL]}"
                                   optionKey="id"
                                   optionValue="value"
                                   value="${contactInstance?.contentType?.id}"/>
                    </div>



                    <div class="field wide four">
                        <ui:select class="ui search dropdown" name="contactLang.id"
                                   from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)}"
                                   optionKey="id"
                                   optionValue="value"
                                   value="${contactInstance?.language?.id}"
                                   noSelection="['null': message(code: 'person.contacts.selectLang.default')]"/>
                    </div>


                    <div class="field wide eight">
                        <g:textField id="content" name="content" value="${contactInstance?.content}"/>
                    </div>
                </div>
            </div>


            <div id="contactElements"></div>
        </g:if>

    </g:if>

<%--<g:if test="${showAddresses}">
    <div class="field">
        <br />
        <label>
            <g:message code="person.addresses.label"/>:
        </label>
        <g:if test="${personInstance}">
            <div class="ui divided middle aligned list la-flex-list addressField">
                <g:each in="${personInstance.addresses.sort { it.type.each { it?.getI10n('value') } }}"
                        var="address">
                    <laser:render template="/templates/cpa/address"
                              model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton, editable: editable]}"/>
                </g:each>
            </div>
        </g:if>
    </div>
    <g:if test="${addAddresses}">
        <button type="button" id="addAddressElement" class="ui icon button">
            <i class="plus green circle icon"></i>
        </button>

        <button type="button" id="removeAddressElement" class="ui icon button">
            <i class="minus red circle icon"></i>
        </button>

        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
              data-content="${message(code: 'person.addresses.add.button')}">
            <i class="question circle icon"></i>
        </span>

        <br />
        <br />
        <laser:render template="/templates/cpa/addressFields" model="[multipleAddresses: true]"/>

        <div id="addressElements"></div>
    </g:if>

</g:if>--%>

</g:form>

<g:if test="${personInstance && !contactPersonForProviderAgencyPublic}">
    <div class="ui grid">
        <div class="sixteen wide column">
            <div class="la-inline-lists">
                <div class="ui card">
                    <div class="content">
                        <g:set var="propertyWrapper" value="private-property-wrapper-${contextOrg.id}" />
                        <h2 class="ui header">${message(code: 'org.properties.private')} ${contextOrg.name}</h2>
                        <div id="${propertyWrapper}">
                            <laser:render template="/templates/properties/private" model="${[
                                    prop_desc       : PropertyDefinition.PRS_PROP,
                                    ownobj          : personInstance,
                                    propertyWrapper: "${propertyWrapper}",
                                    tenant          : contextOrg]}"/>

                            <laser:script file="${this.getGroovyPageFileName()}">
                                r2d2.initDynamicUiStuff('#${propertyWrapper}');
                                c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextOrg.id});
                            </laser:script>
                        </div>
                    </div>
                </div><!-- .card -->
            </div>
        </div>
    </div>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    $.fn.form.settings.rules.functionOrPosition = function() {
        return $('#functionType').dropdown('get value').length > 0
    };
    $('#person_form').form({
        on: 'submit',
        inline: true,
        fields: {
    <g:if test="${orgList}">
        personRoleOrg: {
          identifier: 'personRoleOrg',
          rules: [
            {
              type: 'empty',
              prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                }
              ]
            },
    </g:if>
    last_name: {
        identifier: 'last_name',
        rules: [
            {
                type: 'empty',
                prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                    }
                ]
            },
            functionType: {
                identifier: 'functionType',
                rules: [
                    {
                        type: 'functionOrPosition',
                        prompt: '<g:message code="person.create.missing_function"/>'
                    }
                ]
            }
        }
    });

    tooltip.init("#${modalID ?: 'personModal'}");

    JSPC.app.addressElementCount = $(".addressField").length;
    JSPC.app.contactElementCount = $(".contactField").length;

    JSPC.app.addressContainer = $(document.createElement('div'));
    JSPC.app.contactContainer = $(document.createElement('div'));

    $(JSPC.app.addressContainer).attr('id', 'addressElementsContainer');
    $(JSPC.app.contactContainer).attr('id', 'contactElementsContainer');

    /* CONTACT START */



    $('.la-js-addContactElement').click(function () {
        let buttonClicked =    $(this);
        $.ajax({
            url: "<g:createLink controller="ajaxHtml" action="contactFields"/>",
            type: "POST",
            success: function (data) {
                if (JSPC.app.contactElementCount <= 3) {

                    JSPC.app.contactElementCount = JSPC.app.contactElementCount + 1;
                    $(JSPC.app.contactContainer).append(data);
                    $('#contactFields').attr('id', 'contactFields' + JSPC.app.contactElementCount);

                    $('#contactElements').after(JSPC.app.contactContainer);

                      let iconType = buttonClicked.attr("id");
                      alert(iconType);
                      let icon = $(".la-js-contactIcon");

                      deleteIconClass();
                      changeIcon(iconType.split('cct-')[1], icon);
                      $('.contactField  option[value="' + iconType + '"]').prop("selected", true);
                      $(".dropdown").dropdown();





                    $('.removeContactElement').click(function () {

                        if (JSPC.app.contactElementCount != 0) {
                            // $('.contactField').last().remove();
                            $(this).parents('.contactField').remove();

                        }
                        JSPC.app.contactElementCount = $(".contactField").length;

                        if (JSPC.app.contactElementCount == 0) {
                            $(JSPC.app.contactContainer).empty().remove();
                            $('#addContactElement').removeAttr('disabled').attr('class', 'ui icon button');
                        }
                    });


                } else {
                    $('#addContactElement').attr('class', 'ui icon button disable');
                    $('#addContactElement').attr('disabled', 'disabled');
                }
                r2d2.initDynamicUiStuff('#contactElementsContainer');
            },
            error: function (j, status, eThrown) {
                console.log('Error ' + eThrown)
            }
        });
    });

%{--$('#removeContactElement').click(function () {
    alert("test");
    if (JSPC.app.contactElementCount != 0) {
        // $('.contactField').last().remove();
        $(this).parents('.contactField').remove();

    }
    JSPC.app.contactElementCount = $(".contactField").length;

    if (JSPC.app.contactElementCount == 0) {
        $(JSPC.app.contactContainer).empty().remove();
        $('#addContactElement').removeAttr('disabled').attr('class', 'ui icon button');
    }
});--}%
        let test = function() {

        }
        /* CONTACT END */

    %{--        /* ADDRESS START */
            $('#addAddressElement').click(function () {
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="addressFields" params="[multipleAddresses: true]"/>",
                    type: "POST",
                    success: function (data) {
                        if (JSPC.app.addressElementCount <= 3) {

                            JSPC.app.addressElementCount = JSPC.app.addressElementCount + 1;
                            $(JSPC.app.addressContainer).append(data);
                            $('#addressFields').attr('id', 'addressFields' + JSPC.app.addressElementCount);

                            $('#addressElements').after(JSPC.app.addressContainer);
                        } else {
                            $('#addAddressElement').attr('class', 'ui icon button disable');
                            $('#addAddressElement').attr('disabled', 'disabled');
                        }
                        r2d2.initDynamicUiStuff('#addressElementsContainer');
                    },
                    error: function (j, status, eThrown) {
                        console.log('Error ' + eThrown)
                    }
                });
            });

            $('#removeAddressElement').click(function () {
                if (JSPC.app.addressElementCount != 0) {
                    $('.addressField').remove();
                }
                JSPC.app.addressElementCount = $(".addressField").length;

                if (JSPC.app.addressElementCount == 0) {
                    $(JSPC.app.addressContainer).empty().remove();
                    $('#addAddressElement').removeAttr('disabled').attr('class', 'ui icon button');
                }
            });
            /* ADDRESS END */--}%
        $('#cust_prop_add_value_private').submit(function(e) {
            e.preventDefault();
            console.log("redirect obstructed, continue implementing!");
            bb8.ajax4remoteForm($(this));
        });

        $('#person_form').submit(function(e) {
            e.preventDefault();
            JSPC.app.addressElementCount = $(".addressField").length;
            JSPC.app.contactElementCount = $(".contactField").length;
            if($.fn.form.settings.rules.functionOrPosition() && $('#last_name').val().length > 0) {
                let addressElements = null, contactElements = null;
                if(JSPC.app.contactElementCount == 1) {
                    contactElements = [$('#'+$.escapeSelector('contactLang.id')), $('#content')];
                }
                if(JSPC.app.addressElementCount == 1) {
                    addressElements = [$('#type'), $('#name'), $('#additionFirst'), $('#additionSecond'), $('#street_1'), $('#street_2'), $('#zipcode'), $('#city'), $('#pob'), $('#pobZipcode'), $('#pobCity'), $('#country'), $('#region')];
                }
                if((JSPC.app.addressElementCount == 0 || !JSPC.app.areElementsFilledOut(addressElements)) &&
                   (JSPC.app.contactElementCount == 0 || !JSPC.app.areElementsFilledOut(contactElements))) {
                    if(confirm("${message(code:'person.create.noAddressConfirm')}")) {
                        $('#person_form').unbind('submit').submit();
                    }
                }
                else $('#person_form').unbind('submit').submit();
            }
            else if($('#functionType').length === 0 && $('#positionType').length === 0) {
                $('#person_form').unbind('submit').submit();
            }
        });

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

        /* Deal with accordion in case already any input */

        $(".accordion").accordion();
        let input = [$("#title"), $("#first_name"), $("#middle_name")];

        let found = false;
        for (var i = 0; i < input.length; i++) {
          if ($(input[i]).val().trim() !== "") {
            found = true;
            console.log("Non empty");
            $(".title").addClass("active");
            $(".content").addClass("active");
            break;
          }
        }



        function deleteIconClass() {
          $(".la-js-contactIcon").removeAttr("class");
        }
        $(".dropdown.contentType select").on("change", function () {

          let icon = $(".la-js-contactIcon");
          let value = $(this).val();

          deleteIconClass();
          changeIcon(value, icon)
        });

        function changeIcon(value, icon) {
            console.log("value: " + value);
            console.log("icon: " + icon);
           deleteIconClass();
           switch (value) {
            case "${RDStore.CCT_EMAIL.id}":
              icon.addClass("icon large la-js-contactIcon envelope outline");
              break;
            case "${RDStore.CCT_FAX.id}":
              icon.addClass("icon large la-js-contactIcon tty");
              break;
            case "${RDStore.CCT_MOBILE.id}":
              icon.addClass("icon large la-js-contactIcon mobile alternate");
              break;
            case "${RDStore.CCT_PHONE.id}":
              icon.addClass("icon large la-js-contactIcon phone");
              break;
            case "${RDStore.CCT_PHONE.id}":
              icon.addClass("icon large la-js-contactIcon phone");
              break;
            case "${RDStore.CCT_URL.id}":
              icon.addClass("icon large la-js-contactIcon globe");
              break;
          }
        }


</laser:script>

</ui:modal>
