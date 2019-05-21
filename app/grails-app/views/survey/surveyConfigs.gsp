<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyConfigs.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>


<br>

<div class="ui icon info message">
    <i class="info icon"></i>

    ${message(code: 'surveyConfigs.info')}
</div>
<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyConfigs.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<div>

    <h3 class="ui left aligned icon header">${message(code: 'surveyConfigs.list.subscriptions')} <semui:totalNumber
            total="${surveyConfigs.findAll{it?.type == 'Subscription'}.size()}"/></h3>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">
                ${message(code: 'surveyConfig.configOrder.label')}
            </th>
            <th>${message(code: 'surveyProperty.subName')}</th>
            <th>${message(code: 'surveyProperty.subStatus')}</th>
            <th>${message(code: 'surveyProperty.subDate')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
            <th>${message(code: 'surveyProperty.plural.label')}</th>
            <th>${message(code: 'surveyConfig.documents.label')}</th>
            <th>${message(code: 'surveyConfig.orgs.label')}</th>
            <th></th>

        </tr>

        </thead>

        <g:each in="${surveyConfigs}" var="config" status="i">
            <g:if test="${config?.type == 'Subscription'}">
                <tr>
                    <td class="center aligned">
                        <semui:xEditable owner="${config}" field="configOrder"/>
                    </td>
                    <td>
                         <g:link controller="subscription" action="show"
                                    id="${config?.subscription?.id}">${config?.subscription?.name}</g:link>

                    </td>
                    <td>
                        ${config?.subscription?.status?.getI10n('value')}
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${config?.subscription?.startDate}"/><br>
                        <g:formatDate formatName="default.date.format.notime" date="${config?.subscription?.endDate}"/>
                    </td>
                    <td>
                        ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}

                        <g:if test="${config?.surveyProperty}">
                            <br>
                            <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(config?.surveyProperty?.type)}</b>

                            <g:if test="${config?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                <g:set var="refdataValues" value="${[]}"/>
                                <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(config?.surveyProperty?.refdataCategory)}"
                                        var="refdataValue">
                                    <g:set var="refdataValues"
                                           value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                </g:each>
                                <br>
                                (${refdataValues.join('/')})
                            </g:if>
                        </g:if>

                    </td>
                    <td>
                        <g:if test="${config?.type == 'Subscription'}">
                            <div class="ui circular label">${config?.surveyProperties?.size()}</div>
                        </g:if>

                    </td>
                    <td>
                        <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="">
                            ${config?.documents?.size()}
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="">
                            ${config?.orgs?.size() ?: 0}
                        </g:link>
                    </td>
                    <td>

                        <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="ui icon button"><i
                                class="write icon"></i></g:link>

                        <g:if test="${config?.getCurrentDocs()}">
                            <span data-position="top right"
                                  data-tooltip="${message(code: 'surveyConfigs.delete.existingDocs')}">
                                <button class="ui icon button negative" disabled="disabled">
                                    <i class="trash alternate icon"></i>
                                </button>
                            </span>
                        </g:if>
                        <g:elseif test="${editable}">
                            <g:link class="ui icon negative button js-open-confirm-modal"
                                    data-confirm-term-what="Abfrage-Elemente"
                                    data-confirm-term-what-detail="${config?.getConfigNameShort()}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyConfig"
                                    id="${config?.id}">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:elseif>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>

    <br>
    <br>
    <h3 class="ui left aligned icon header">${message(code: 'surveyConfigs.list.propertys')} <semui:totalNumber
            total="${surveyConfigs.findAll{it?.type == 'SurveyProperty'}.size()}"/></h3>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">
                ${message(code: 'surveyConfig.configOrder.label')}
            </th>
            <th>${message(code: 'surveyProperty.name.label')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
            <th>${message(code: 'surveyProperty.plural.label')}</th>
            <th>${message(code: 'surveyConfig.documents.label')}</th>
            <th>${message(code: 'surveyConfig.orgs.label')}</th>
            <th></th>

        </tr>

        </thead>

        <g:each in="${surveyConfigs}" var="config" status="i">
            <g:if test="${config?.type == 'SurveyProperty'}">
                <tr>
                    <td class="center aligned">
                        <semui:xEditable owner="${config}" field="configOrder"/>
                    </td>
                    <td>
                        <g:if test="${config?.type == 'Subscription'}">
                            <g:link controller="subscription" action="show"
                                    id="${config?.subscription?.id}">${config?.subscription?.dropdownNamingConvention()}</g:link>
                        </g:if>

                        <g:if test="${config?.type == 'SurveyProperty'}">
                            ${config?.surveyProperty?.getI10n('name')}
                        </g:if>

                    </td>
                    <td>
                        ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}

                        <g:if test="${config?.surveyProperty}">
                            <br>
                            <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(config?.surveyProperty?.type)}</b>

                            <g:if test="${config?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                <g:set var="refdataValues" value="${[]}"/>
                                <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(config?.surveyProperty?.refdataCategory)}"
                                        var="refdataValue">
                                    <g:set var="refdataValues"
                                           value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                </g:each>
                                <br>
                                (${refdataValues.join('/')})
                            </g:if>
                        </g:if>

                    </td>
                    <td>
                        <g:if test="${config?.type == 'Subscription'}">
                            <div class="ui circular label">${config?.surveyProperties?.size()}</div>
                        </g:if>

                    </td>
                    <td>
                        <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="">
                            ${config?.documents?.size()}
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="">
                            ${config?.orgs?.size() ?: 0}
                        </g:link>
                    </td>
                    <td>

                        <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="ui icon button"><i
                                class="write icon"></i></g:link>

                        <g:if test="${config?.getCurrentDocs()}">
                            <span data-position="top right"
                                  data-tooltip="${message(code: 'surveyConfigs.delete.existingDocs')}">
                                <button class="ui icon button negative" disabled="disabled">
                                    <i class="trash alternate icon"></i>
                                </button>
                            </span>
                        </g:if>
                        <g:elseif test="${editable}">
                            <g:link class="ui icon negative button js-open-confirm-modal"
                                    data-confirm-term-what="Abfrage-Elemente"
                                    data-confirm-term-what-detail="${config?.getConfigNameShort()}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyConfig"
                                    id="${config?.id}">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:elseif>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>

</div>

<br>
<br>

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
                                <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(property?.type)}</b>

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
