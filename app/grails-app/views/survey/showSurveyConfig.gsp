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
                                  optionValue="${{it?.dropdownNamingConvention(contextService.getOrg())}}"
                                  value=""
                                  noSelection="${['': message(code: 'default.search_for.label', args: [message(code: 'surveyConfig.subscription.label')])]}"
                                  required=""/>
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
                                  noSelection="${['': message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])]}"
                                  required=""/>
                </div>
                <input type="submit" class="ui button"
                       value="${message(code: 'showSurveyConfig.add.button', default: 'Add')}"/>

            </g:form>

            <br>

            <input class="ui button" value="${message(code: 'surveyProperty.create_new')}"
                   data-semui="modal" data-href="#addSurveyPropertyModal" type="submit">
            <br>

            <g:if test="${surveyConfigs.size() > 0}">
                <br>
                
                <g:link controller="survey" action="showSurveyParticipants" id="${surveyInfo.id}" class="ui icon button">${message(code: 'showSurveyInfo.nextStep', default: 'Next Step')}</i></g:link>

            </g:if>

        </semui:form>


        <br>

        <h2 class="ui left aligned icon header">${message(code: 'showSurveyConfig.list')} <semui:totalNumber
                total="${surveyConfigs.size()}"/></h2>

        <div>
            <table class="ui celled sortable table la-table">
                <thead>
                <tr>
                    <th class="center aligned">
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
                                <g:link controller="subscription" action="show"
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
                        <g:if test="${config?.surveyProperties?.surveyProperty}">
                            <g:each in="${config?.surveyProperties ?}" var="prop" status="x">
                                <tr>
                                    <td style="background-color: #f4f8f9"></td>

                                    <td>
                                        ${prop?.surveyProperty?.getI10n('name')}
                                    </td>
                                    <td>
                                        ${message(code: 'showSurveyConfig.surveyPropToSub')}
                                        <br>
                                        <b>${com.k_int.kbplus.SurveyProperty.getLocalizedValue(prop?.surveyProperty?.type)}:</b>

                                        <g:if test="${prop?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                            <g:set var="refdataValues" value="${[]}"/>
                                            <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(prop?.surveyProperty?.refdataCategory)}"
                                                    var="refdataValue">
                                                <g:set var="refdataValues"
                                                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                            </g:each>
                                            <br>
                                            (${refdataValues.join('/')})
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${prop?.surveyProperty?.name != 'Continue to license'}">
                                            <g:link class="ui negative button"
                                                    controller="survey" action="deleteSurveyPropfromSub"
                                                    id="${prop?.id}">
                                                <i class="trash alternate icon"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </g:if>
                        <tr>
                            <td style="background-color: #f4f8f9"></td>
                            <td colspan="3">
                                <g:form action="addSurveyConfig" controller="survey" method="post"
                                        params="[id: surveyInfo?.id, surveyConfig: config?.id]" class="ui form">

                                    <laser:select class="ui dropdown search" name="propertytoSub"
                                                  from="${properties}"
                                                  optionKey="id"
                                                  optionValue="name"
                                                  value=""
                                                  noSelection="${['': message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])]}"
                                                  required=""/>

                                    <input type="submit" class="ui button"
                                           value="${message(code: 'showSurveyConfig.add.surveyPropToSub.button', default: 'Add Survey Property to this Subscription')}"/>

                                </g:form>
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

<semui:modal id="addSurveyPropertyModal" message="surveyProperty.create_new.label">
    <div class="scrolling content">

        <g:form class="ui form" action="addSurveyProperty" params="[surveyInfo: surveyInfo?.id]">

            <div class="field required">
                <label class="property-label"><g:message code="surveyProperty.name.label"/></label>
                <input type="text" name="name" required=""/>
            </div>

            <div class="two fields required">

                <div class="field five wide">
                    <label class="property-label"><g:message code="surveyProperty.type.label"/></label>
                    <g:select class="ui dropdown"
                              from="${SurveyProperty.validTypes.entrySet()}"
                              optionKey="key" optionValue="${{ SurveyProperty.getLocalizedValue(it.key) }}"
                              name="type"
                              id="cust_prop_modal_select"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" required=""/>
                </div>

                <div class="field six wide hide" id="cust_prop_ref_data_name">
                    <label class="property-label"><g:message code="refdataCategory.label"/></label>
                    <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
                </div>

            </div>

            <div class="three fields">
                <div class="field six wide">
                    <label class="property-label">${message(code: 'surveyProperty.explain.label', default: 'Explanation')}</label>
                    <textarea name="explain" class="ui textarea"></textarea>
                </div>

                <div class="field six wide">
                    <label class="property-label">${message(code: 'surveyProperty.introduction.label', default: 'Introduction')}</label>
                    <textarea name="introduction" class="ui textarea"></textarea>
                </div>

                <div class="field six wide">
                    <label class="property-label">${message(code: 'surveyProperty.comment.label', default: 'Comment')}</label>
                    <textarea name="comment" class="ui textarea"></textarea>
                </div>

            </div>

            <div>
                <h3 class="ui left aligned icon header">${message(code: 'surveyProperty.plural.label')} <semui:totalNumber
                        total="${properties.size()}"/></h3>
                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th class="center aligned">${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'surveyProperty.name.label')}</th>
                        <th>${message(code: 'surveyProperty.type.label')}</th>
                        <th>${message(code: 'surveyProperty.introduction.label')}</th>
                        <th>${message(code: 'surveyProperty.explain.label')}</th>
                        <th>${message(code: 'surveyProperty.comment.label')}</th>
                    </tr>
                    </thead>

                    <g:each in="${properties}" var="property" status="i">
                        <tr>
                            <td class="center aligned">
                                ${i + 1}
                            </td>
                            <td>
                                ${property?.getI10n('name')}
                            </td>
                            <td>
                                <b>${com.k_int.kbplus.SurveyProperty.getLocalizedValue(property?.type)}</b>

                                <g:if test="${property?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(property?.refdataCategory)}"
                                            var="refdataValue">
                                        <g:set var="refdataValues"
                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>
                                    <br>
                                    (${refdataValues.join('/')})
                                </g:if>

                            </td>
                            <td>
                                <g:if test="${property?.getI10n('introduction')}">
                                    <span data-tooltip="${property?.getI10n('introduction')}"
                                          data-position="top center">
                                        <i class="inverted circular info icon"></i>
                                    </span>
                                </g:if>
                            </td>

                            <td>
                                <g:if test="${property?.getI10n('explain')}">
                                    <span data-tooltip="${property?.getI10n('explain')}" data-position="top center">
                                        <i class="inverted circular info icon"></i>
                                    </span>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${property?.getI10n('comment')}">
                                    <span data-tooltip="${property?.getI10n('comment')}" data-position="top center">
                                        <i class="inverted circular info icon"></i>
                                    </span>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
        </g:form>
    </div>
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
