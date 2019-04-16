<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="survey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>


<g:render template="steps"/>
<br>

<semui:messages data="${flash}"/>

<br>
<semui:form>
    <div class="ui grid">
        <div class="middle aligned row">
            <div class="two wide column">

                <g:link controller="survey" action="showSurveyInfo" id="${surveyInfo.id}"
                        class="ui huge button"><i class="angle left aligned icon"></i></g:link>

            </div>

            <div class="twelve wide column">

                <div class="la-inline-lists">
                    <div class="ui card">
                        <div class="content">

                            <div class="header">
                                <div class="ui grid">
                                    <div class="twelve wide column">
                                        ${message(code: 'showSurveyInfo.step.first.title')}
                                    </div>
                                </div>
                            </div>
                            <dl>
                                <dt>${message(code: 'surveyInfo.status.label', default: 'Survey Status')}</dt>
                                <dd>${surveyInfo.status?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</dt>
                                <dd>${surveyInfo.name}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.startDate.label')}</dt>
                                <dd><g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyInfo.startDate ?: null}"/></dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.endDate.label')}</dt>
                                <dd><g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyInfo.endDate ?: null}"/></dd>
                            </dl>

                            <dl>
                                <dt>${message(code: 'surveyInfo.type.label')}</dt>
                                <dd>${com.k_int.kbplus.RefdataValue.get(surveyInfo?.type?.id)?.getI10n('value')}</dd>
                            </dl>

                        </div>
                    </div>
                </div>
            </div>

            <div class="two wide column">
                <g:if test="${surveyConfigs.size() > 0}">

                    <g:link controller="survey" action="showSurveyConfigDocs" id="${surveyInfo.id}"
                            class="ui huge button"><i class="angle right icon"></i></g:link>

                </g:if>
            </div>
        </div>

    </div>

</semui:form>
<br>



<g:if test="${editable}">
    <semui:form>

        <p><b>${message(code: 'showSurveyConfig.add.info')}</b></p>

        <g:form action="addSurveyConfig" controller="survey" method="post" class="ui form">
            <g:hiddenField name="id" value="${surveyInfo?.id}"/>


            <div class="field required">
                <label>${message(code: 'showSurveyConfig.subscription')}</label>
                <g:select class="ui dropdown search" name="subscription"
                          from="${subscriptions}"
                          optionKey="id"
                          optionValue="${{ it?.dropdownNamingConvention() }}"
                          value=""
                          noSelection="${['': message(code: 'default.search_for.label', args: [message(code: 'surveyConfig.subscription.label')])]}"
                          required=""/>
            </div>
            <input type="submit" class="ui button"
                   value="${message(code: 'showSurveyConfig.add.button', default: 'Add')}"/>

        </g:form>

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

            <input type="submit" name="addtoallSubs" class="ui button"
                   value="${message(code: 'showSurveyConfig.add.toAllSub.button', default: 'Add')}"/>

        </g:form>

        <br>

        <input class="ui button" value="${message(code: 'surveyProperty.create_new')}"
               data-semui="modal" data-href="#addSurveyPropertyModal" type="submit">
        <br>

    </semui:form>
</g:if>


<br>

<h2 class="ui left aligned icon header">${message(code: 'showSurveyConfig.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<div class="ui grid">
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
                </td>
                <td>
                    <g:if test="${config?.getCurrentDocs()}">
                        <span data-position="top right"
                              data-tooltip="${message(code: 'showSurveyConfig.delete.existingDocs')}">
                            <button class="ui icon button negative" disabled="disabled">
                                <i class="trash alternate icon"></i>
                            </button>
                        </span>
                    </g:if>
                    <g:elseif test="${editable}">
                        <g:link class="ui icon negative button js-open-confirm-modal"
                                data-confirm-term-what="Umfrage"
                                data-confirm-term-what-detail="${config?.getConfigNameShort()}"
                                data-confirm-term-how="delete"
                                controller="survey" action="deleteSurveyConfig"
                                id="${config?.id}">
                            <i class="trash alternate icon"></i>
                        </g:link>
                    </g:elseif>
                </td>
            </tr>

            <g:if test="${config?.type == 'Subscription'}">
                <g:if test="${config?.surveyProperties?.surveyProperty}">
                    <g:each in="${config?.surveyProperties.sort { it?.surveyProperty?.getI10n('name') }}" var="prop"
                            status="x">
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
                                <g:if test="${editable}">
                                    <g:link class="ui icon negative button"
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
                        <g:if test="${editable}">
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
