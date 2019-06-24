<%@ page import="de.laser.domain.I10nTranslation; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.SurveyProperty" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'menu.admin.manageSurveyPropertyDefinitions')}</title>
	</head>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.manageI10n" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/><g:message
        code="menu.admin.manageSurveyPropertyDefinitions"/></h1>

<h3 class="ui header">${message(code: 'surveyProperty.label')}</h3>

<semui:messages data="${flash}"/>


<br>

<input class="ui button" value="${message(code: 'surveyProperty.create_new')}"
       data-semui="modal" data-href="#addSurveyPropertyModal" type="submit">
<br>

<br>

<div>
    <h4 class="ui left aligned icon header">${message(code: 'surveyProperty.all.label')}

        <i class="question circle icon la-popup"></i>

        <div class="ui popup">
            <i class="shield alternate icon"></i> = ${message(code: 'subscription.properties.my')}
        </div>
        <semui:totalNumber total="${surveyPropertyDefinitions.size()}"/>

    </h4>
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.name.label')}</th>
            <th>DE</th>
            <th>EN</th>
            <th>DE ${message(code: 'surveyProperty.introduction.label')}</th>
            <th>EN ${message(code: 'surveyProperty.introduction.label')}</th>
            <th>DE ${message(code: 'surveyProperty.explain.label')}</th>
            <th>EN ${message(code: 'surveyProperty.explain.label')}</th>
            <th>${message(code: 'surveyProperty.comment.label')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
        </tr>
        </thead>

        <g:each in="${surveyPropertyDefinitions}" var="property" status="i">
            <tr>

                <g:set var="pdI10nName" value="${I10nTranslation.createI10nOnTheFly(property, 'name')}"/>
                <g:set var="pdI10nIntroduction"
                       value="${I10nTranslation.createI10nOnTheFly(property, 'introduction')}"/>
                <g:set var="pdI10nExplain" value="${I10nTranslation.createI10nOnTheFly(property, 'explain')}"/>
                <td class="center aligned">

                    <g:if test="${property.hardData}">
                        <span data-position="top left" data-tooltip="${message(code: 'default.hardData.tooltip')}">
                            <i class="check circle icon green"></i>
                        </span>
                    </g:if>

                    <g:if test="${usedPdList?.contains(property.id)}">
                        <span data-position="top left"
                              data-tooltip="${message(code: 'default.dataIsUsed.tooltip', args: [property.id])}">
                            <i class="info circle icon blue"></i>
                        </span>
                    </g:if>

                </td>
                <td>
                    <g:if test="${property.isUsedForLogic}">
                        <span style="color:orange">${fieldValue(bean: property, field: "name")}</span>
                    </g:if>
                    <g:else>
                        ${fieldValue(bean: property, field: "name")}
                    </g:else>

                    <g:if test="${property?.owner?.id == institution?.id}">
                        <i class='shield alternate icon'></i>
                    </g:if>
                </td>
                <td>
                    <g:if test="${!property.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                        <semui:xEditable owner="${pdI10nName}" field="valueDe"/>
                    </g:if>
                    <g:else>
                        ${pdI10nName?.valueDe}
                    </g:else>
                </td>
                <td>
                    <g:if test="${!property.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                        <semui:xEditable owner="${pdI10nName}" field="valueEn"/>
                    </g:if>
                    <g:else>
                        ${pdI10nName?.valueEn}
                    </g:else>
                </td>
                <td>
                    <g:if test="${!property.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                        <semui:xEditable owner="${pdI10nIntroduction}" field="valueDe"/>
                    </g:if>
                    <g:else>
                        ${pdI10nIntroduction?.valueDe}
                    </g:else>
                </td>
                <td>
                    <g:if test="${!property.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                        <semui:xEditable owner="${pdI10nIntroduction}" field="valueEn"/>
                    </g:if>
                    <g:else>
                        ${pdI10nIntroduction?.valueEn}
                    </g:else>
                </td>
                <td>
                    <g:if test="${!property.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                        <semui:xEditable owner="${pdI10nExplain}" field="valueDe" type="textarea"/>
                    </g:if>
                    <g:else>
                        ${pdI10nExplain?.valueDe}
                    </g:else>
                </td>
                <td>
                    <g:if test="${!property.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                        <semui:xEditable owner="${pdI10nExplain}" field="valueEn" type="textarea"/>
                    </g:if>
                    <g:else>
                        ${pdI10nExplain?.valueEn}
                    </g:else>
                </td>
                <td>
                    <semui:xEditable owner="${property}" field="comment" type="textarea"/>
                </td>
                <td>

                    <g:set var="pdRdc" value="${property.type?.split('\\.').last()}"/>
                    <g:if test="${'RefdataValue'.equals(pdRdc)}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(property?.refdataCategory)}"
                                var="refdataValue">
                            <g:set var="refdataValues"
                                   value="${refdataValues + refdataValue?.getI10n('value')}"/>
                        </g:each>

                        <span data-position="top right" data-tooltip="${refdataValues.join('/')}">
                            <small>
                                ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(property?.type)}
                            </small>
                        </span>
                    </g:if>
                    <g:else>
                        <small>${com.k_int.kbplus.SurveyProperty.getLocalizedValue(property?.type)}</small>
                    </g:else>

                </td>
            </tr>
        </g:each>
    </table>

</div>

<semui:modal id="addSurveyPropertyModal" message="surveyProperty.create_new.label">
    <div class=" content">

        <g:form class="ui form" action="addSurveyProperty" params="[surveyInfo: surveyInfo?.id]">

            <div class="field required">
                <label class="property-label"><g:message code="surveyProperty.name"/></label>
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

        $(".la-popup").popup({
    });
</g:javascript>


</body>
</html>
