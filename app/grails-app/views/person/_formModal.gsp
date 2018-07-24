<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>
<% def cService = grailsApplication.mainContext.getBean("contextService") %>

<semui:modal id="personFormModal" text="${message(code: 'person.create_new.contactPerson.label')}">

    <g:form class="ui form" id="create_person" url="[controller: 'person', action: 'create', params: [org_id: org.id]]" method="POST">

        <div class="field">
            <div class="two fields">

                <div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'contactType', 'error')} ">
                    <label for="contactType">
                        ${com.k_int.kbplus.RefdataCategory.findByDesc('Person Contact Type').getI10n('desc')}


                    </label>
                    <laser:select class="ui dropdown" id="contactType" name="contactType"
                                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Person Contact Type')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${personInstance?.contactType?.id}"
                                  />
                </div>
                <div id="roleType" class="field fieldcontain ${hasErrors(bean: personInstance, field: 'roleType', 'error')}">
                    <label for="roleType">
                        ${com.k_int.kbplus.RefdataCategory.findByDesc('Person Position').getI10n('desc')}

                    </label>
                    <laser:select class="ui dropdown" id="roleType" name="roleType"
                                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Person Position')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${personInstance?.roleType?.id}"
                                  noSelection="['': '']"/>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field wide twelve ${hasErrors(bean: personInstance, field: 'last_name', 'error')} required">
                    <label for="last_name">
                        <g:message code="person.last_name.label" default="Lastname" />
                    </label>
                    <g:textField name="last_name" required="" value="${personInstance?.last_name}"/>

                </div>

                <div id="person_middle_name" class="field wide four ${hasErrors(bean: personInstance, field: 'middle_name', 'error')} ">
                    <label for="middle_name">
                        <g:message code="person.middle_name.label" default="Middlename" />

                    </label>
                    <g:textField name="middle_name" value="${personInstance?.middle_name}"/>

                </div>

            </div>
        </div>

        <div class="field">
            <div class="three fields">

                <div id="person_first_name" class="field wide eight ${hasErrors(bean: personInstance, field: 'first_name', 'error')}">
                    <label for="first_name">
                        <g:message code="person.first_name.label" default="Firstname" />
                    </label>
                    <g:textField name="first_name" required="" value="${personInstance?.first_name}"/>
                </div>

                <div id="person_title" class="field wide four ${hasErrors(bean: personInstance, field: 'title', 'error')}">
                    <label for="title">
                        <g:message code="person.title.label" default="Title" />
                    </label>
                    <g:textField name="title" required="" value="${personInstance?.title}"/>
                </div>

                <div id="person_gender" class="field wide four ${hasErrors(bean: personInstance, field: 'gender', 'error')} ">
                    <label for="gender">
                        <g:message code="person.gender.label" default="Gender" />
                    </label>
                    <laser:select class="ui dropdown" id="gender" name="gender"
                                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Gender').sort{ a, b -> a.value.compareTo(b.value) }}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${personInstance?.gender?.id}"
                                  noSelection="['': '']"
                    />
                </div>
            </div>
        </div>


        <g:if test="${cService.getOrg()}">
            <input type="hidden" name="tenant.id" value="${cService.getOrg().id}" />
            <input id="isPublic" name="isPublic" type="hidden" value="${isPublic?.id}" />
        </g:if>
        <g:else>
            <div class="field">
                <div class="two fields">

                    <div class="field wide twelve fieldcontain ${hasErrors(bean: personInstance, field: 'tenant', 'error')} required">
                        <label for="tenant">
                            <g:message code="person.tenant.label" default="Tenant (Permissions to edit this person and depending addresses and contacts)" />
                        </label>
                        <g:select id="tenant" name="tenant.id" from="${cService.getMemberships()}" optionKey="id" value="${cService.getOrg()?.id}" />
                    </div>

                    <div class="field wide four fieldcontain ${hasErrors(bean: personInstance, field: 'isPublic', 'error')} required">
                        <label for="isPublic">
                            <g:message code="person.isPublic.label" default="IsPublic" />
                        </label>
                        ${isPublic.getI10n('value')}
                        <input id="isPublic" name="isPublic" type="hidden" value="${isPublic?.id}" />
                    </div>
                </div>
            </div>
            <hr />
        </g:else>

        <div id="person-role-manager">

            <g:if test="${! tmplHideFunctions}">
                <h4 class="ui header"><g:message code="person.functions.label" default="Functions" /></h4>
                <div class="ui segment person-role-function-manager">

                    <div class="workspace">
                        <h5 class="ui header">
                            <g:message code="default.button.create_new.label" default="Adding"/>
                        </h5>
                        <div class="field">
                            <div class="two fields">
                                <div class="field wide ten">
                                    <laser:select class="ui dropdown values"
                                                  name="ignore-functionType-selector"
                                                  from="${PersonRole.getAllRefdataValues('Person Function')}"
                                                  optionKey="id"
                                                  optionValue="value" />
                                </div>
                                <div class="field wide six">
                                    <button class="ui button add-person-role" type="button">${message('code':'default.button.add.label')}</button>
                                </div>
                            </div>
                        </div>

                        <div class="adding"></div>
                        <h5 class="ui header" data-attr="removeIfEmpty">
                            <g:message code="default.button.delete.label" default="Delete"/>
                        </h5>
                        <div class="existing" data-attr="removeIfEmpty"></div>
                    </div>
                </div>
                <script>
                    $.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&roleType=func').done(function(data){
                        $('.person-role-function-manager .workspace .existing').append(data);
                        if(data == 'No Data found.') {
                            $('.person-role-function-manager .workspace [data-attr=removeIfEmpty]').remove()
                        }
                    });
                    $('.person-role-function-manager .add-person-role').click(function(){
                        var tt = $('.person-role-function-manager select').val()

                        $.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&roleType=func&roleTypeId=' + tt + '&org=${org?.id}').done(function(data){
                            $('.person-role-function-manager .workspace .adding').append(data);
                        });
                    })
                </script>
            </g:if>
            <g:else>
                <%-- DEFAULT --%>
                <input type="hidden" name="org.default" value="${org?.id}" />
                <input type="hidden" name="functionType.default" value="${presetFunctionType?.id}" />
            </g:else>

            <g:if test="${! tmplHideResponsibilities}">
                <h4 class="ui header"><g:message code="person.responsibilites.label" default="Responsibilites" /></h4>

                <div class="ui segment person-role-responsibility-manager">

                    <div class="workspace">
                        <h5 class="ui header">
                            <g:message code="default.button.create_new.label" default="Adding"/>
                        </h5>
                        <div class="field">
                            <div class="two fields">
                                <div class="field wide ten">
                                    <laser:select class="ui dropdown values"
                                                  name="ignore-responsibilityType-selector"
                                                  from="${PersonRole.getAllRefdataValues('Person Responsibility')}"
                                                  optionKey="id"
                                                  optionValue="value" />
                                </div>
                                <div class="field wide six">
                                    <button class="ui button add-person-role" type="button">${message('code':'default.button.add.label')}</button>
                                </div>
                            </div>
                        </div>

                        <div class="adding"></div>
                        <h5 class="ui header" data-attr="removeIfEmpty">
                            <g:message code="default.button.delete.label" default="Delete"/>
                        </h5>
                        <div class="existing" data-attr="removeIfEmpty"></div>
                    </div>
                </div>
                <script>
                    $.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&roleType=resp').done(function(data){
                        $('.person-role-responsibility-manager .workspace .existing').append(data);
                        if(data == 'No Data found.') {
                            $('.person-role-responsibility-manager .workspace [data-attr=removeIfEmpty]').remove()
                        }
                    });
                    $('.person-role-responsibility-manager .add-person-role').click(function(){
                        var tt = $('.person-role-responsibility-manager select').val()

                        $.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&roleType=resp&roleTypeId=' + tt + '&org=${org?.id}').done(function(data){
                            $('.person-role-responsibility-manager .workspace .adding').append(data);
                        });
                    })
                </script>
            </g:if>
        </div>

    </g:form>

    <r:script>

        $('#create_person')
                .form({
            on: 'blur',
            inline: true,
            fields: {
                last_name: {
                    identifier  : 'last_name',
                    rules: [
                        {
                            type   : 'empty',
                            prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                        }
                    ]
                }
             }
        });
        var fc = "${com.k_int.kbplus.RefdataValue.getByValueAndCategory('Functional contact', 'Person Contact Type').getI10n('value')}";

        $("#contactType").on('change', function() {
            changeForm( $("#contactType option:selected").text() == fc )
        })

        function changeForm(hide) {
            var group1 = $("#roleType, #person_middle_name, #person_first_name, #person_title, #person_gender")
            var group2 = $("#roleType .dropdown, #person_gender .dropdown")
            var group3 = $("#roleType select, #person_gender select")
            var group4 = $("#person_middle_name input, #person_first_name input, #person_title input")

            if (hide) {
                group1.hide()
                group2.addClass('disabled')
                group3.attr('disabled', 'disabled')
                group4.attr('disabled', 'disabled')

                $("label[for='last_name']").text("Benenner")
            }
            else {
                group1.show()
                group2.removeClass('disabled')
                group3.removeAttr('disabled')
                group4.removeAttr('disabled')

                $("label[for='last_name']").text("Nachname")
            }
        }

        changeForm(true) // init
    </r:script>

</semui:modal>