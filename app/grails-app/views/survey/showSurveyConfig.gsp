<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'createSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="createSurvey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'createSurvey.label')}</h1>


<g:render template="steps"/>
<br>

<semui:messages data="${flash}"/>

<br>
<br>


<div class="ui grid">

    <div class="eleven wide column">

        <semui:form>

            <p><b>${message(code: 'showSurveyConfig.add.info')}</b></p>
            <g:if test="${true}">
                <g:form action="addSurveyConfig" controller="survey" method="post" class="ui form">
                    <g:hiddenField name="id" value="${surveyInfo?.id}"/>


                    <div class="field required">
                        <label>${message(code: 'showSurveyConfig.subscription')}</label>
                        <g:select class="ui dropdown search" name="subscription"
                                  from="${subscriptions}"
                                  optionKey="id"
                                  optionValue="name"
                                  value=""
                                  noSelection="${['': message(code: 'default.select.choose.label')]}" required="true"/>
                    </div>
                    <input type="submit" class="ui button"
                           value="${message(code: 'showSurveyConfig.add.button', default: 'Add')}"/>

                </g:form>
            </g:if>

            <g:form action="addSurveyConfig" controller="survey" method="post" class="ui form">
                <g:hiddenField name="id" value="${surveyInfo?.id}"/>


                <div class="field required">
                    <label>${message(code: 'showSurveyConfig.property')}</label>
                    <laser:select class="ui dropdown search" name="property"
                                  from="${properties}"
                                  optionKey="id"
                                  optionValue="name"
                                  value=""
                                  noSelection="${['': message(code: 'default.select.choose.label')]}" required="true"/>
                </div>
                <input type="submit" class="ui button"
                       value="${message(code: 'showSurveyConfig.add.button', default: 'Add')}"/>

            </g:form>

            <br>

            <input class="ui button" value="${message(code: 'surveyProperty.create_new')}"
                   data-semui="modal" href="#addSurveyPropertyModal" type="submit">
            <br>
        </semui:form>




        <h3></h3>

        <div>
            <table class="ui celled sortable table la-table">
                <thead>
                <tr>
                    <th rowspan="2" class="center aligned">
                        ${message(code: 'surveyConfig.configOrder.label')}
                    </th>
                    <th>${message(code: 'surveyProperty.name.label')}</th>
                    <th>${message(code: 'surveyProperty.type.label')}</th>
                    <th></th>
                    <th></th>

                </tr>

                </thead>
                <g:each in="${surveyConfigs}" var="config" status="i">
                    <tr>
                        <td class="center aligned">
                            ${config?.configOrder}
                        </td>
                        <td>
                            <g:if test="${config?.type == 'Subscription'}">
                                <g:link controller="subscriptionDetails" action="show"
                                        id="${config?.subscription?.id}">${config?.subscription?.name}</g:link>
                                <br>${config?.subscription?.startDate ? '(' : ''}
                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${config?.subscription?.startDate}"/>
                                ${config?.subscription?.endDate ? '-' : ''}
                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${config?.subscription?.endDate}"/>
                                ${config?.subscription?.startDate ? ')' : ''}
                            </g:if>

                            <g:if test="${config?.type == 'SurveyProperty'}">
                                ${config?.surveyProperty?.getI10n('name')}
                            </g:if>

                        </td>
                        <td>
                            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}
                        </td>
                        <td>
                            <g:link class="ui negative button"
                                    controller="survey" action="deleteSurveyConfig" id="${config?.id}">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </td>
                    </tr>

                    <g:if test="${config.type == 'Subscription'}">
                        <tr>
                            <td class="two wide wide">
                            </td>
                            <td class="center aligned fourteen wide">
                                <g:if test="${config?.surveyProperties?.surveyProperty}">
                                    <table class="ui celled sortable table la-table">
                                        <thead>
                                        <tr>
                                            <th>${message(code: 'surveyProperty.name.label')}</th>
                                            <th>${message(code: 'surveyProperty.type.label')}</th>
                                            <th></th>
                                            <th></th>

                                        </tr>

                                        </thead>
                                        <g:each in="${config?.surveyProperties?.surveyProperty}" var="prop" status="x">
                                            <tr>

                                                <td>
                                                    ${prop?.getI10n('name')}
                                                </td>
                                            </tr>
                                        </g:each>
                                    </table>
                                </g:if>
                            </td>
                        </tr>
                    </g:if>

                </g:each>
            </table>
        </div>

        <br>
        <br>

    </div>

    <aside class="five wide column la-sidekick">
        <div class="la-inline-lists">

            <g:render template="infoArea"/>

        </div>
    </aside>

</div>

<semui:modal id="addSurveyPropertyModal" message="propertyDefinition.create_new.label">

    <g:form class="ui form" action="">
        <g:field type="hidden" name="cmd" value="add"/>

        <div class="field">
            <label class="property-label">Name</label>
            <input type="text" name="pd_name"/>
        </div>

        <div class="fields">

            <div class="field five wide">
                <label class="property-label"><g:message code="propertyDefinition.type.label"/></label>
                <g:select class="ui dropdown"
                          from="${SurveyProperty.validTypes.entrySet()}"
                          optionKey="key" optionValue="${{ SurveyProperty.getLocalizedValue(it.key) }}"
                          name="pd_type"
                          id="cust_prop_modal_select"/>
            </div>

            <div class="field five wide">
                <label class="property-label">${message(code: 'propertyDefinition.expl.label', default: 'Explanation')}</label>
                <textarea name="pd_expl" id="pd_expl" class="ui textarea"></textarea>
            </div>

            <div class="field six wide hide" id="cust_prop_ref_data_name">
                <label class="property-label"><g:message code="refdataCategory.label"/></label>
                <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
            </div>
        </div>

    </g:form>
</semui:modal>

<g:javascript>

       if( $( "#cust_prop_modal_select option:selected" ).val() == "class com.k_int.kbplus.RefdataValue") {
            $("#cust_prop_ref_data_name").show();
       } else {
            $("#cust_prop_ref_data_name").hide();
       }

    $('#cust_prop_modal_select').change(function() {
        var selectedText = $( "#cust_prop_modal_select option:selected" ).val();
        if( selectedText == "class com.k_int.kbplus.RefdataValue") {
            $("#cust_prop_ref_data_name").show();
        }else{
            $("#cust_prop_ref_data_name").hide();
        }
    });

    $("#cust_prop_refdatacatsearch").select2({
        placeholder: "Kategorie eintippen...",
        minimumInputLength: 1,

        formatInputTooShort: function () {
            return "${message(code: 'select2.minChars.note', default: 'Please enter 1 or more character')}";
        },
        formatNoMatches: function() {
            return "${message(code: 'select2.noMatchesFound')}";
        },
        formatSearching:  function() {
            return "${message(code: 'select2.formatSearching')}";
        },
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
            url: '${createLink(controller: 'ajax', action: 'lookup')}',
            dataType: 'json',
            data: function (term, page) {
                return {
                    q: term, // search term
                    page_limit: 10,
                    baseClass:'com.k_int.kbplus.RefdataCategory'
                };
            },
            results: function (data, page) {
                return {results: data.values};
            }
        }
    });

</g:javascript>

</body>
</html>
