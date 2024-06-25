<%@ page import="de.laser.properties.PropertyDefinition; de.laser.PersonRole; de.laser.Contact; de.laser.Person; de.laser.FormService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.RefdataCategory;de.laser.storage.RDConstants" %>
<laser:serviceInjection/>

<ui:modalAddress  id="${modalID ?: 'personModal'}" form="person_form"
           modalSize="big"
           text="${modalText ?: message(code: 'person.create_new.label')}"
           msgClose="${message(code: 'default.button.cancel')}"
           msgSave="${message(code: 'default.button.save.label')}">
    <g:form id="person_form" class="ui form" url="${url}" method="POST">
        <g:set var="personRole" value="${[]}"/>
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
        <g:elseif test="${personInstance}">
            <%
                personRole = personInstance.getPersonRoleByTarget([org: org, provider: provider, vendor: vendor])
            %>
        </g:elseif>

        <g:if test="${!contactPersonForProviderPublic && !contactPersonForVendorPublic}">
            <div class="field">
                <div class="two fields">
                    <g:if test="${!isPublic}">

                            <g:if test="${orgList}">
                                <div class="field required">
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
                                </div>
                            </g:if>
                            <g:if test="${provList}">
                                <div class="field required">
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
                                </div>
                            </g:if>
                            <g:if test="${venList}">
                                <div class="field required">
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
                                </div>
                            </g:if>
                            <g:if test="${org}">
                                <div class="field">
                                    <label for="personRoleOrg">
                                        <g:message code="person.belongsTo"/>
                                    </label>
                                    <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link>
                                    <input name="personRoleOrg" type="hidden" value="${org.id}"/>
                                </div>
                            </g:if>
                            <g:elseif test="${provider}">
                                <div class="field">
                                    <label for="personRoleProvider">
                                        <g:message code="person.belongsTo"/>
                                    </label>
                                    <g:link controller="provider" action="show" id="${provider.id}">${provider.name}</g:link>
                                    <input name="personRoleProvider" type="hidden" value="${provider.id}"/>
                                </div>
                            </g:elseif>
                            <g:elseif test="${vendor}">
                                <div class="field required">
                                    <label for="personRoleVendor">
                                        <g:message code="person.belongsTo"/>
                                    </label>
                                    <g:link controller="vendor" action="show" id="${vendor.id}">${vendor.name}</g:link>
                                    <input name="personRoleVendor" type="hidden" value="${vendor.id}"/>
                                </div>
                            </g:elseif>
                        %{--<g:else>
                            <label for="personRoleOrg">
                                <g:message code="contact.belongesTo.label"/>
                            </label>
                            <i class="icon university la-list-icon"></i>${org?.name}
                            <input id="personRoleOrg" name="personRoleOrg" type="hidden" value="${org?.id}"/>
                        </g:else>--}%

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
                <div class="ui active button" id="la-js-buttonSurname">
                    <h2 class="ui icon inverted header">
                        <i class="address card outline icon"></i>
                        <div class="content">
                            <g:message code='contact.surname' />
                        </div>
                    </h2>
                </div>
                <div class="or" data-text="<g:message code='search.advancedSearch.option.OR' />"></div>
                <div class="ui button" id="la-js-buttonFunction">
                    <h2 class="ui icon inverted header">
                        <i class="wrench icon"></i>
                        <div class="content">
                            <g:message code='contact.functionName' />
                        </div>
                    </h2>
                </div>
            </div>
            <!-- Alternating contact buttons END-->
            <br><br>
            <div class="field">
                <div class="two fields">

                    <div class="field wide twelve required">
                        <label id="la-js-nameOrFunction" for="last_name">
                            <g:message code="person.last_name.label"/> <g:message code="messageRequiredField" />
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: 'person.last_name.info')}">
                                <i class="question circle icon"></i>
                            </span>
                        </label>
                        <g:textField name="last_name"  value="${personInstance?.last_name}"/>
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
                                    <option <%=(personInstance ? (functionType.id in personRole.functionType?.id) : (presetFunctionType?.id == functionType.id)) ? 'selected="selected"' : ''%>
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
                                    <option <%=(personInstance ? (positionType.id in personRole.positionType?.id) : (presetPositionType?.id == positionType.id)) ? 'selected="selected"' : ''%>
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

    <g:if test="${showContacts}">
        <g:if test="${addContacts}">
            <div class="field">
                <label>
                    <g:message code="person.contacts.label"/>:
                </label>
                %{-- Buttons for selection of kind of contact START --}%
                <div class="ui wrapping spaced buttons">
                    <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_EMAIL.id}"><i class="icon  envelope outline"></i><g:message code="contact.addEmail"/></a>
                    <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_FAX.id}"><i class="tty circle icon"></i><g:message code="contact.addTty"/></a>
                    <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_MOBILE.id}"><i class="mobile alternate circle icon"></i><g:message code="contact.addMobil"/></a>
                    <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_PHONE.id}"><i class="phone circle icon"></i><g:message code="contact.addPhone"/></a>
                    <a class="ui blue button la-js-addContactElement" id="cct-${RDStore.CCT_URL.id}"><i class="globe circle icon"></i><g:message code="contact.addUrl"/></a>
                </div>
                %{-- Buttons for selection of kind of contact END --}%
            </div>


            <g:if test="${personInstance}">
                <g:each in="${personInstance.contacts?.toSorted()}" var="contact" status="i">
                    <div class="three fields contactField" id="contactFields${i}">
                        <div class="field one wide la-contactIconField">
                            <g:if test="${contact.contentType.id == RDStore.CCT_EMAIL.id}">
                                <i class="icon large envelope outline la-js-contactIcon"></i>
                            </g:if>
                            <g:elseif test="${contact.contentType.id == RDStore.CCT_FAX.id}">
                                <i class="icon large tty la-js-contactIcon"></i>
                            </g:elseif>
                            <g:elseif test="${contact.contentType.id == RDStore.CCT_MOBILE.id}">
                                <i class="icon large mobile alternate la-js-contactIcon"></i>
                            </g:elseif>
                            <g:elseif test="${contact.contentType.id == RDStore.CCT_PHONE.id}">
                                <i class="icon large phone la-js-contactIcon"></i>
                            </g:elseif>
                            <g:elseif test="${contact.contentType.id == RDStore.CCT_URL.id}">
                                <i class="icon large globe la-js-contactIcon"></i>
                            </g:elseif>
                            <g:else>
                                <i class="icon large envelope outline la-js-contactIcon"></i>
                            </g:else>



                        </div>
                        <div class="field wide four">
                            <input type="text" name="contact${contact.id}" readonly value="${contact.contentType.getI10n('value')}"/>
                        </div>

                        <div class="field four wide">
                            <ui:select class="ui search dropdown" name="contactLang${contact.id}"
                                       from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)}"
                                       optionKey="id"
                                       optionValue="value"
                                       value="${contact.language?.id}"
                                       noSelection="['null': '']"/>
                        </div>

                        <div class="field seven wide">
                            <g:if test="${contact.contentType.id == RDStore.CCT_EMAIL.id}">
                                <g:textField class="la-js-contactContent" data-validate="email" name="content${contact.id}" value="${contact.content}"/>
                            </g:if>
                            <g:else>
                                <g:textField class="la-js-contactContent" data-validate="contactContent" name="content${contact.id}" value="${contact.content}"/>
                            </g:else>
                        </div>
                        <div class="field one wide">
                            <button type="button"  class="ui icon negative button la-modern-button removeContactElement">
                                <i class="trash alternate outline icon"></i>
                            </button>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else >
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



                        <div class="field four wide">
                            <ui:select class="ui search dropdown" name="contactLang.id"
                                       from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)}"
                                       optionKey="id"
                                       optionValue="value"
                                       value="${contactInstance?.language?.id}"
                                       noSelection="['null': message(code: 'person.contacts.selectLang.default')]"/>
                        </div>


                        <div class="field eight wide">
                            <g:textField class="la-js-contactContent" data-validate="email" id="content" name="content" value="${contactInstance?.content}"/>
                        </div>
                    </div>
                </div>
            </g:else>
            <div id="contactElements"></div>
        </g:if>

    </g:if>

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

%{--    --}%
    $('.ui.form').form('remove fields', ['contactContent']);

    $("#la-js-buttonFunction").on("click", function () {
        $("#la-js-nameOrFunction").text("${message(code:'contact.functionName')}");
        $(this).parent('.buttons').find('#la-js-buttonSurname').removeClass('active');
        $(this).addClass('active');
    });
    $("#la-js-buttonSurname").on("click", function () {
        $("#la-js-nameOrFunction").text("${message(code:'contact.surname')}");
         $(this).parent('.buttons').find('#la-js-buttonFunction').removeClass('active');
        $(this).addClass('active');
    });


    $.fn.form.settings.rules.isMinimalOneContactFilled = function() {
        return $(".la-js-contactContent").val() ?  true : false ;
    };


    JSPC.app.formValidation = function () {
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
        <g:if test="${provList}">
            personRoleProvider: {
              identifier: 'personRoleProvider',
              rules: [
                {
                  type: 'empty',
                  prompt: '{name} <g:message code="validation.needsToBeFilledOut"/>'
                        }


                      ]
                    },
        </g:if>
        <g:if test="${venList}">
            personRoleVendor: {
              identifier: 'personRoleVendor',
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
                            type: 'empty',
                            prompt: '<g:message code="person.create.missing_function"/>'
                        }
                    ]
                },
                contactContent: {
                    identifier: 'contactContent',
                    rules: [
                        {
                            type: 'isMinimalOneContactFilled',
                            prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                        }
                    ]
                },
                email: {
                    identifier: 'email',
                    rules: [
                        {
                            type   : 'email',
                            prompt: '<g:message code="contact.create.email.error" />'
                        }
                    ]
                }
            }
        });
    }


%{--    JSPC.app.addressElementCount = $(".addressField").length;--}%
    JSPC.app.contactElementCount = $(".contactField").length;

%{--    JSPC.app.addressContainer = $(document.createElement('div'));--}%
    JSPC.app.contactContainer = $(document.createElement('div'));

%{--    $(JSPC.app.addressContainer).attr('id', 'addressElementsContainer');--}%
    $(JSPC.app.contactContainer).attr('id', 'contactElementsContainer');



    JSPC.app.isNamesDetailsAlreadyFilled = function () {
        let input = [$("#title"), $("#first_name"), $("#middle_name")];

        let found = false;
        for (var i = 0; i < input.length; i++) {
            if ($(input[i]).val() !== "") {
            found = true;
            $(".title").addClass("active");
            $(".content").addClass("active");
            break;
            }
        }
    }

    JSPC.app.removeContactElement = function () {
        $('.removeContactElement').click(function () {
            JSPC.app.checkIfMoreThanFourContactElements();
            if (JSPC.app.contactElementCount != 1) {
                $(this).parents('.contactField').remove();
            }
            else {
            }
        });
    }

    JSPC.app.checkIfMoreThanFourContactElements = function() {
        JSPC.app.contactElementCount = $(".contactField").length;
        if (JSPC.app.contactElementCount <= 3) {
            $('.la-js-addContactElement').removeClass('disabled');
        }
    }

    $('.la-js-addContactElement').click(function () {

        let buttonClicked =    $(this);
        $.ajax({
            url: "<g:createLink controller="ajaxHtml" action="contactFields"/>",
            type: "POST",
            success: function (data) {
                if (JSPC.app.contactElementCount <= 3) {

                    JSPC.app.contactElementCount = JSPC.app.contactElementCount + 1;
                    $(JSPC.app.contactContainer).append(data);

                    let lastRowIcon = $(JSPC.app.contactContainer).find('.la-js-contactIcon').last();

                    $('#contactFields').attr('id', 'contactFields' + JSPC.app.contactElementCount);

                    $('#contactElements').after(JSPC.app.contactContainer);

                    let iconType = buttonClicked.attr("id").split('cct-')[1];
                    let icon = $(".la-js-contactIcon");

                    JSPC.app.changeIcon(iconType, lastRowIcon);

                    $('.contactField  option[value="' + iconType + '"]').last().prop("selected", true);
                    $(".dropdown").dropdown();
                    JSPC.app.checkIfMoreThanFourContactElements();
                    JSPC.app.changeIconRegardingDropdown();
                    JSPC.app.removeContactElement();
                    JSPC.app.dataValidateProperty(lastRowIcon);
                } else {
                    $('.la-js-addContactElement').addClass( 'disabled');
                }
                r2d2.initDynamicUiStuff('#contactElementsContainer');
                JSPC.app.formValidation();
            },
            error: function (j, status, eThrown) {
                console.log('Error ' + eThrown)
            }
        });
    });


%{--   Delete the icon classes before adding new one        --}%
    JSPC.app.deleteIconClass = function (icon) {
        icon.removeAttr("class");
    }
%{--  Change data-validate property when contact dropdown is changed          --}%
    JSPC.app.dataValidateProperty = function(elem) {
      let contactField = elem.parents('.contactField').find('.la-js-contactContent');
      if ( elem.parents('.contactField').find('.contentType select').val() == ${RDStore.CCT_EMAIL.id} ){
        contactField.attr('data-validate','email');
        $('.ui.form').form('remove fields', ['contactContent']);
      }
      else {
        contactField.attr('data-validate','contactContent');
        //$('.ui.form').form('remove fields', ['email']);
      }
    }


%{--  Change icon when contact dropdown is changed          --}%
    JSPC.app.changeIconRegardingDropdown = function() {
        $('.dropdown').dropdown();
        $(".dropdown.contentType select").on("change", function () {


          JSPC.app.dataValidateProperty($(this))
          let icon = $(this).parents('.contactField').find('.la-js-contactIcon');
          let value = $(this).val();

          JSPC.app.deleteIconClass(icon);
          JSPC.app.changeIcon(value, icon);

        });
    }

%{--   Change the icon classes        --}%
    JSPC.app.changeIcon = function (value, icon) {
       JSPC.app.deleteIconClass(icon);
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

    $('#cust_prop_add_value_private').submit(function(e) {
        e.preventDefault();
        console.log("redirect obstructed, continue implementing!");
        bb8.ajax4remoteForm($(this));
    });

    tooltip.init("#${modalID ?: 'personModal'}");

    JSPC.app.changeIconRegardingDropdown();

    JSPC.app.formValidation();
    $('.ui.form').form('remove fields', ['contactContent']);

%{--    Deal with accordion in case already any input--}%
    $(".accordion").accordion();

     JSPC.app.isNamesDetailsAlreadyFilled();

     JSPC.app.removeContactElement();



    </laser:script>

</ui:modalAddress>
