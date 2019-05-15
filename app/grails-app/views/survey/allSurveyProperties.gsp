<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="myinst.currentSubscriptions.label" class="active"/>
</semui:breadcrumbs>

<br>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>



<g:render template="nav"/>


<semui:messages data="${flash}"/>

<br>

<input class="ui button" value="${message(code: 'surveyProperty.create_new')}"
       data-semui="modal" data-href="#addSurveyPropertyModal" type="submit">
<br>
<br>

<g:if test="${surveyConfig}">

    <h3><g:message code="allSurveyProperties.surveyConfig.info"/>

        <br>
        <g:if test="${surveyConfig?.type == 'Subscription'}">
            <g:link controller="subscription" action="show"
                    id="${surveyConfig?.subscription?.id}">${surveyConfig?.subscription?.dropdownNamingConvention()}

                ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(surveyConfig?.type)}
            </g:link>
        </g:if>

    </h3>
</g:if>

<br>

<div>

    <g:form action="addSurveyConfigs" controller="survey" method="post" class="ui form">
        <g:hiddenField name="id" value="${surveyInfo?.id}"/>
        <g:hiddenField name="configID" value="${surveyConfig?.id}"/>

        <h4 class="ui left aligned icon header">${message(code: 'surveyProperty.plural.label')} <semui:totalNumber
                total="${properties.size()}"/></h4>
        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="left aligned"></th>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.name.label')}</th>
                <th>${message(code: 'surveyProperty.introduction.label')}</th>
                <th>${message(code: 'surveyProperty.explain.label')}</th>
                <th>${message(code: 'surveyProperty.comment.label')}</th>
                <th>${message(code: 'surveyProperty.type.label')}</th>
            </tr>
            </thead>

            <g:each in="${properties}" var="property" status="i">
                <tr>
                    <td>
                        <g:if test="${com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, property)}">
                            <i class="check circle icon green"></i>
                        </g:if>
                        <g:else>
                            <g:checkBox name="selectedProperty" value="${property.id}" checked="false"/>
                        </g:else>
                    </td>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${property?.getI10n('name')}
                    </td>
                    <td>
                        <g:if test="${property?.getI10n('introduction')}">
                            ${property?.getI10n('introduction')}
                        </g:if>
                    </td>

                    <td>
                        <g:if test="${property?.getI10n('explain')}">
                            ${property?.getI10n('explain')}
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${property?.getI10n('comment')}">
                            ${property?.getI10n('comment')}
                        </g:if>
                    </td>
                    <td>
                        ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(property?.type)}

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
                </tr>
            </g:each>
        </table>

        <g:if test="${surveyConfig && surveyConfig?.type == 'Subscription'}">
            <input type="submit" class="ui button"
                   value="${message(code: 'allSurveyProperties.add.button')}"/>
        </g:if>

    </g:form>

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


