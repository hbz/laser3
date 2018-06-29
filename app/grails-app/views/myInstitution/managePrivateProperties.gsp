<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition; de.laser.domain.I10nTranslation" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.institutions.manage_props')}</title>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
        <semui:crumb message="menu.institutions.manage_props" class="active" />
    </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code: 'menu.institutions.manage_props')}</h1>

    <semui:messages data="${flash}" />

    <semui:card>
        <div class="content ui form">
            <input class="ui button" value="${message(code:'menu.institutions.manage_props.create_new')}"
                   data-semui="modal" href="#addPropertyDefinitionModal" type="submit">
        </div>
    </semui:card>

            <g:if test="${privatePropertyDefinitions}">

                <g:form class="ui form" action="managePrivateProperties" method="post">
                    <table class="ui celled la-table table">
                        <thead>
                            <tr>
                                <th>${message(code:'propertyDefinition.descr.label', default:'Description')}</th>
                                <th>${message(code:'propertyDefinition.name.label', default:'Name')}</th>
                                <th>Name (DE)</th>
                                <th>Name (EN)</th>
                                <th>${message(code:'propertyDefinition.count.label', default:'Count in Use')}</th>
                                <th>${message(code:'default.actions')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${privatePropertyDefinitions}" var="ppd">
                                <g:set var="pdI10nName" value="${I10nTranslation.createI10nOnTheFly(ppd, 'name')}" />
                                <tr>
                                    <td><g:message code="propertyDefinition.${ppd.descr}.label" default="${ppd.descr}" /></td>
                                    <td>
                                        ${ppd.name}
                                        <g:if test="${ppd.softData}">
                                            <span data-position="top right" data-tooltip="${message(code:'default.softData.tooltip')}">
                                                <i class="tint icon teal"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${ppd.mandatory}">
                                            <span data-position="top right" data-tooltip="${message(code:'default.mandatory.tooltip')}">
                                                <i class="star icon yellow"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${ppd.multipleOccurrence}">
                                            <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                                                <i class="redo icon orange"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td><semui:xEditable owner="${pdI10nName}" field="valueDe" /></td>
                                    <td><semui:xEditable owner="${pdI10nName}" field="valueEn" /></td>
                                    <td>${ppd.countUsages()}</td>
                                    <td class="x">
                                        <g:if test="${ppd.countUsages()==0}">
                                            <g:link action="managePrivateProperties" params="[cmd:'delete', deleteIds: ppd?.id]" class="ui icon negative button">
                                            <i class="trash alternate icon"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>

                    <p>${message(code:'propertyDefinition.private.info')}</p>
                </g:form>
            </g:if>


    <semui:modal id="addPropertyDefinitionModal" message="propertyDefinition.create_new.label">

        <g:form class="ui form" action="managePrivateProperties" >
            <g:field type="hidden" name="cmd" value="add" />

            <div class="field">
                <label class="property-label">Name</label>
                <input type="text" name="pd_name"/>
            </div>

            <div class="fields">

                <div class="field five wide">
                    <label class="property-label">${message(code:'propertyDefinition.descr.label', default:'Description')}</label>
                    <%--<g:select name="pd_descr" from="${PropertyDefinition.AVAILABLE_PRIVATE_DESCR}"/>--%>
                    <select name="pd_descr" id="pd_descr" class="ui dropdown">
                        <g:each in="${PropertyDefinition.AVAILABLE_PRIVATE_DESCR}" var="pd">
                            <option value="${pd}"><g:message code="propertyDefinition.${pd}.label" default="${pd}"/></option>
                        </g:each>
                    </select>
                </div>

                <div class="field five wide">
                    <label class="property-label">Type</label>
                    <g:select  class="ui dropdown"
                        from="${PropertyDefinition.validTypes.entrySet()}"
                        optionKey="value" optionValue="key"
                        name="pd_type"
                        id="cust_prop_modal_select" />
                </div>

                <div class="field six wide hide" id="cust_prop_ref_data_name">
                    <label class="property-label">Kategorie</label>
                    <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
                </div>
            </div>

            <div class="fields">
                <div class="field five wide">
                    <label class="property-label">${message(code:'default.mandatory.tooltip')}</label>
                        <g:checkBox type="text" name="pd_mandatory" />
                </div>
                <div class="field five wide">
                    <label class="property-label">${message(code:'default.multipleOccurrence.tooltip')}</label>
                    <g:checkBox type="text" name="pd_multiple_occurrence" />
                </div>
            </div>

        </g:form>
    </semui:modal>

    <g:javascript>

       if( $( "#cust_prop_modal_select option:selected" ).val() == "class com.k_int.kbplus.RefdataValue") {
            $("#cust_prop_ref_data_name").show();
       }else {
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
        placeholder: "Type category...",
        minimumInputLength: 1,
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
            url: '${createLink(controller:'ajax', action:'lookup')}',
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
