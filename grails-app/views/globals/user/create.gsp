<%@ page import="com.k_int.kbplus.*;de.laser.*;de.laser.auth.Role" %>
<laser:serviceInjection />
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'user.create_new.label')}</title>
    </head>
    <body>

        <g:render template="${breadcrumb}" model="${[inContextOrg: inContextOrg, orgInstance: orgInstance, departmentalView: departmentalView, institutionalView: institutionalView,  params:params ]}"/>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'user.create_new.label')}</h1>

        <semui:messages data="${flash}" />

        <g:if test="${editable}">
            <g:form name="newUser" class="ui form" controller="${controllerName}" action="processCreateUser" method="post">
                <fieldset>
                    <div class="field required">
                        <label for="username">${message(code:'user.username.label')}</label>
                        <input type="text" id="username" name="username" value="${params.username}"/>
                    </div>
                    <div class="field required">
                        <label for="displayName">${message(code:'user.displayName.label')}</label>
                        <input class="validateNotEmpty" type="text" id="displayName" name="display" value="${params.display}"/>
                    </div>
                    <div class="field required">
                        <label for="password">${message(code:'user.password.label')}</label>
                        <input class="validateNotEmpty" type="text" id="password" name="password" value="${params.password}"/>
                    </div>
                    <div class="field required">
                        <label for="email">${message(code:'user.email')}</label>
                        <input class="validateNotEmpty" type="text" id="email" name="email" value="${params.email}"/>
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
                            <label for="userRole">${message(code:'user.role')}</label>
                            <g:select id="userRole" name="formalRole"
                                      from="${availableOrgRoles}"
                                      optionKey="id"
                                      optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                      value="${Role.findByAuthority('INST_EDITOR').id}"
                                      class="ui fluid dropdown"/>
                        </div>
                    </div>

                    <div class="field">
                        <input id="userSubmit" type="submit" value="${message(code:'user.create_new.label')}" class="ui button" disabled/>
                        <input type="button" class="ui button js-click-control" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
                    </div>

                </fieldset>
            </g:form>
        </g:if>
    </body>
</html>
<laser:script file="${this.getGroovyPageFileName()}">

    $("#username").keyup(function() {
        JSPC.app.checkUsername();
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
            console.log("eee");
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