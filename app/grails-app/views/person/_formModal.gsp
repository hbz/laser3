<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>
<laser:serviceInjection/>


<g:set var="modalId" value="${modalId ?: 'personFormModal'}"/>
<semui:modal id="${modalId}" text="${message(code: (modalId))}">

    <g:form class="ui form" id="create_person" url="[controller: 'person', action: 'create', params: [org_id: org.id]]"
            method="POST">

        <div class="field">
            <div class="two fields">

                <div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'contactType', 'error')} ">
                    <label for="contactType">
                        ${com.k_int.kbplus.RefdataCategory.findByDesc('Person Contact Type')?.getI10n('desc')}
                    </label>
                    <laser:select class="ui dropdown" id="contactType" name="contactType"
                                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Person Contact Type')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${personInstance?.contactType?.id ?: com.k_int.kbplus.RefdataValue.getByValueAndCategory('Personal contact', 'Person Contact Type')?.id}"
                                  noSelection="['': '']"/>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field wide twelve ${hasErrors(bean: personInstance, field: 'last_name', 'error')} required">
                    <label for="last_name">
                        <g:message code="person.last_name.label" default="Lastname"/>
                    </label>
                    <g:textField name="last_name" required="" value="${personInstance?.last_name}"/>
                </div>

                <div id="person_title"
                     class="field wide four ${hasErrors(bean: personInstance, field: 'title', 'error')}">
                    <label for="title">
                        <g:message code="person.title.label" default="Title"/>
                    </label>
                    <g:textField name="title" required="" value="${personInstance?.title}"/>
                </div>

            </div>
        </div>

        <div class="field">
            <div class="three fields">

                <div id="person_first_name"
                     class="field wide eight ${hasErrors(bean: personInstance, field: 'first_name', 'error')}">
                    <label for="first_name">
                        <g:message code="person.first_name.label" default="Firstname"/>
                    </label>
                    <g:textField name="first_name" required="" value="${personInstance?.first_name}"/>
                </div>

                <div id="person_middle_name"
                     class="field wide four ${hasErrors(bean: personInstance, field: 'middle_name', 'error')} ">
                    <label for="middle_name">
                        <g:message code="person.middle_name.label" default="Middlename"/>
                    </label>
                    <g:textField name="middle_name" value="${personInstance?.middle_name}"/>
                </div>

                <div id="person_gender"
                     class="field wide four ${hasErrors(bean: personInstance, field: 'gender', 'error')} ">
                    <label for="gender">
                        <g:message code="person.gender.label" default="Gender"/>
                    </label>
                    <laser:select class="ui dropdown" id="gender" name="gender"
                                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Gender').sort { a, b -> a.value.compareTo(b.value) }}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${personInstance?.gender?.id}"
                                  noSelection="['': '']"/>
                </div>
            </div>
        </div>


        <g:if test="${contextService.getOrg()}">
            <input type="hidden" name="tenant.id" value="${contextService.getOrg().id}"/>
            <input id="isPublic" name="isPublic" type="hidden" value="${isPublic?.id}"/>
        </g:if>
        <g:else>
            <div class="field">
                <div class="two fields">

                    <div class="field wide twelve fieldcontain ${hasErrors(bean: personInstance, field: 'tenant', 'error')} required">
                        <label for="tenant">
                            <g:message code="person.tenant.label"
                                       default="Tenant (Permissions to edit this person and depending addresses and contacts)"/>
                        </label>
                        <g:select id="tenant" name="tenant.id" from="${contextService.getMemberships()}" optionKey="id"
                                  value="${contextService.getOrg()?.id}"/>
                    </div>

                    <div class="field wide four fieldcontain ${hasErrors(bean: personInstance, field: 'isPublic', 'error')} required">
                        <label for="isPublic">
                            <g:message code="person.isPublic.label" default="IsPublic"/>
                        </label>
                        ${isPublic?.getI10n('value')}
                        <input id="isPublic" name="isPublic" type="hidden" value="${isPublic?.id}"/>
                    </div>
                </div>
            </div>
            <hr/>
        </g:else>

        <div id="person-role-manager">

            <g:if test="${!tmplHideFunctions}">
                <div class="person-role-function-manager">

                    <g:if test="${hideFunctionTypeAndPositionAndOrg}">

                        <input name="functionType" type="hidden" value="${presetFunctionType?.id}"/>
                        <input name="functionOrg" type="hidden" value="${org?.id}"/>

                    </g:if>
                    <g:else>
                        <div class="field">
                            <div class="two fields">
                                <div class="field">
                                    <label for="functionOrg">
                                        <g:message code="person.function.label" default="Function"/>
                                    </label>
                                    <laser:select class="ui dropdown values"
                                                  name="functionType"
                                                  from="${PersonRole.getAllRefdataValues('Person Function')}"
                                                  optionKey="id"
                                                  value="${presetFunctionType?.id}"
                                                  optionValue="value"/>
                                </div>

                                <div class="field">
                                    <g:if test="${institution}">
                                        <label for="functionOrg">
                                            <g:message code="contact.belongesTo.label"/>
                                        </label>
                                        <g:select class="ui search dropdown"
                                                  name="functionOrg"
                                                  from="${Org.getAll()}"
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
                            </div>
                        </div><!-- .field -->


                    <div class="field js-positionTypeWrapper">
                        <div class="two fields">
                            <div class="field">
                                <label for="positionOrg">
                                    <g:message code="person.position.label" default="Position"/>
                                </label>
                                <laser:select class="ui dropdown values"
                                              name="positionType"
                                              from="${PersonRole.getAllRefdataValues('Person Position')}"
                                              optionKey="id"
                                              value="${presetPositionType?.id}"
                                              optionValue="value"
                                              noSelection="${['': '']}"/>
                            </div>

                            <div class="field">
                                <g:if test="${institution}">
                                    <label for="positionOrg">
                                        <g:message code="contact.belongesTo.label"/>
                                    </label>
                                    <g:select class="ui search dropdown"
                                              name="positionOrg"
                                              from="${Org.getAll()}"
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
                        </div>
                    </div><!-- .field -->
                    </g:else>

                </div>

            </g:if>
            <g:else>
            <%-- DEFAULT --%>
                <input type="hidden" name="functionOrg.default" value="${org?.id}"/>
                <input type="hidden" name="functionType.default" value="${presetFunctionType?.id}"/><%-- not used ? --%>
            </g:else>
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
                            prompt : '{name} <g:message code="validation.needsToBeFilledOut"
                                                        default=" muss ausgefüllt werden"/>'
                        }
                    ]
                }
             }
        });
        var fc = "${com.k_int.kbplus.RefdataValue.getByValueAndCategory('Functional contact', 'Person Contact Type')?.getI10n('value')}";

        $(document).ready( function(){
            changeForm( ($("#${modalId} #contactType option:selected").text() == fc), "${modalId}")
        });

        $("#${modalId} #contactType").on('change', function() {
            changeForm( ($("#${modalId} #contactType option:selected").text() == fc), "${modalId}")
        })

        function changeForm(hide, mId) {
            console.log(mId+"Test1");
            var group1 = $("#"+mId+" #person_middle_name, #"+mId+" #person_first_name, #"+mId+" #person_title, #"+mId+" #person_gender, #"+mId+" .js-positionTypeWrapper")
            var group2 = $("#"+mId+" #person_gender .dropdown,#"+mId+" #person_gender select")
            var group3 = $("#"+mId+" #positionType,#"+mId+" #positionOrg")
            var group4 = $("#"+mId+" #person_middle_name input, #"+mId+" #person_first_name input,#"+mId+" #person_title input")

            if (hide) {
                group1.hide()
                group2.addClass('disabled')
                group3.attr('disabled', 'disabled')
                group4.attr('disabled', 'disabled')

                $("label[for='last_name']").text("Benenner")
                ! $("#"+mId+"#last_name").val() ? $("#"+mId+"#last_name").val("Allgemeiner Funktionskontakt") : false

            }
            else {
                group1.show()
                group2.removeClass('disabled')
                group3.removeAttr('disabled')
                group4.removeAttr('disabled')

                $("label[for='last_name']").text("Nachname")
                $("#"+mId+"#last_name").val() == "Allgemeiner Funktionskontakt" ? $("#"+mId+"#last_name").val("") : false
            }
        }

        changeForm(false) // init
    </r:script>

</semui:modal>