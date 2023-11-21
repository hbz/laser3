<%@ page import="de.laser.*;de.laser.auth.Role" %>

<laser:htmlStart message="user.create_new.label" serviceInjection="true" />

    %{--<g:if test="${controllerName == 'myInstitution'}">
        // myInstitution has no breadcrumb yet
        <laser:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
    </g:if>
    <g:if test="${controllerName == 'organisation'}">
        <laser:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
    </g:if>--}%
    %{--<g:if test="${controllerName == 'user'}">--}%
        <laser:render template="/user/global/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
    %{--</g:if>--}%

        <ui:h1HeaderWithIcon message="user.create_new.label" type="user" />

        <ui:messages data="${flash}" />

    <g:if test="${editable}">
        <div class="ui grey segment">
            <g:form name="newUser" class="ui form" controller="${controllerName}" action="processCreateUser" method="post">
                <fieldset>
                    <div class="field required">
                        <label for="username">${message(code:'user.username.label')} ${message(code: 'messageRequiredField')}</label>
                        <input type="text" id="username" name="username" value="${params.username}"/>
                    </div>
                    <div class="field required">
                        <label for="displayName">${message(code:'user.displayName.label')} ${message(code: 'messageRequiredField')}</label>
                        <input class="validateNotEmpty" type="text" id="displayName" name="display" value="${params.display}"/>
                    </div>
                    <div class="field required">
                        <label for="password">${message(code:'user.password.label')} ${message(code: 'messageRequiredField')}</label>
                        <input class="validateNotEmpty" type="text" id="password" name="password" value="${params.password}"/>
                    </div>
                    <div class="field required">
                        <label for="email">${message(code:'user.email')} ${message(code: 'messageRequiredField')}</label>
                        <input class="validateMailAddress" type="text" id="email" name="email" value="${params.email}"/>
                    </div>

                    <div class="two fields">
                        <div class="field">
                            <label for="userOrg">${message(code:'user.org')}</label>
                            <g:select id="userOrg" name="org"
                                      from="${availableOrgs}"
                                      optionKey="id"
                                      optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                                      value="${params.org ?: orgInstance?.id}"
                                      class="ui fluid search dropdown la-not-clearable"/>
                        </div>
                        <div class="field">
                            <label for="userRole">${message(code:'default.role.label')}</label>
                            <g:select id="userRole" name="formalRole"
                                      from="${Role.findAllByRoleType('user')}"
                                      optionKey="id"
                                      optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                      value="${Role.findByAuthority('INST_EDITOR').id}"
                                      class="ui fluid dropdown la-not-clearable"/>
                        </div>
                    </div>

                    <div class="field">
                        <g:if test="${controllerName == 'organisation'}">
                            <input type="hidden" name="id" value="${orgInstance.id}" />
                        </g:if>
                        <input id="userSubmit" type="submit" value="${message(code:'user.create_new.label')}" class="ui button" disabled/>
                        <input type="button" class="ui button js-click-control" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
                    </div>

                </fieldset>
            </g:form>
        </div>
    </g:if>

<laser:script file="${this.getGroovyPageFileName()}">

    $("#username").keyup(function() {
        JSPC.app.checkUsername();
    });

    $(".validateMailAddress").keyup(function(){
        if($(this).val().length === 0) {
            JSPC.app.addError($(this),'<span id="'+$(this).attr('id')+'Error">'+$('[for="'+$(this).attr('id')+'"]').text()+' <g:message code="validation.needsToBeFilledOut"/></span>');
        }
        if($(this).val().length > 0 && !(/^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)+$/.test($(this).val()))) {
            JSPC.app.addError($(this),'<span id="'+$(this).attr('id')+'Error">'+$('[for="'+$(this).attr('id')+'"]').text()+' <g:message code="validation.mailAddress"/></span>');
        }
        else {
            JSPC.app.removeError($(this),$("#"+$(this).attr("id")+"Error"));
        }
    });

    $(".validateNotEmpty").keyup(function(){
        if($(this).val().length === 0) {
            JSPC.app.addError($(this),'<span id="'+$(this).attr('id')+'Error">'+$('[for="'+$(this).attr('id')+'"]').text()+' <g:message code="validation.needsToBeFilledOut"/></span>');
        }
        else {
            JSPC.app.removeError($(this),$("#"+$(this).attr("id")+"Error"));
        }
    });

        $("#newUser").submit(function(e){
            e.preventDefault();
            $(".validateNotEmpty").each(function(k) {
                if($(this).val().length === 0) {
                    JSPC.app.addError($(this),'<span id="'+$(this).attr('id')+'Error">'+$('[for="'+$(this).attr('id')+'"]').text()+' <g:message code="validation.needsToBeFilledOut"/></span>');
                }
            });
            JSPC.app.checkUsername();
            if($(".error").length === 0)
                $(this).unbind('submit').submit();
        });

        JSPC.app.checkUsername = function () {
            $.ajax({
                url: "<g:createLink controller="ajaxJson" action="checkExistingUser" />",
                data: {input: $("#username").val()},
                method: 'POST'
            }).done(function(response){
                if(response.result) {
                    JSPC.app.addError($("#username"),"<span id=\"usernameError\"><g:message code="user.not.created.message"/></span>");
                }
                else if($("#username").val().length > 0) {
                    JSPC.app.removeError($("#username"),$("#usernameError"));
                }
            }).fail(function(request,status,error){
                console.log("Error occurred, verify logs: "+status+", error: "+error);
            });
        }

        JSPC.app.addError = function (element,errorMessage) {
            if($("#"+element.attr("id")+"Error").length === 0) {
                element.after(errorMessage);
                element.parent("div").addClass("error");
                $("#userSubmit").attr("disabled",true);
            }
        }

        JSPC.app.removeError = function (element,errorSpan) {
            errorSpan.remove();
            element.parent("div").removeClass("error");
            if($(".error").length === 0)
                $("#userSubmit").removeAttr("disabled");
        }
        // dropdowns not wished to be clearable
        $('.ui.search.dropdown.la-not-clearable').dropdown({
            forceSelection: false,
            selectOnKeydown: false,
            fullTextSearch: 'exact',
            clearable: false,
        });
</laser:script>

<laser:htmlEnd />