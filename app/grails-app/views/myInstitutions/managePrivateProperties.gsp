<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition; de.laser.domain.I10nTranslation" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
        <semui:crumb message="menu.institutions.manage_props" class="active" />
    </semui:breadcrumbs>

    <h1 class="ui header">${institution?.name} - ${message(code: 'menu.institutions.manage_props')}</h1>

    <semui:messages data="${flash}" />

    <div class="ui grid">
        <div class="twelve wide column">

            <g:if test="${privatePropertyDefinitions}">

                <g:form class="ui form" params="${['shortcode':params.shortcode]}" action="managePrivateProperties" method="post">
                    <table class="ui celled striped table">
                        <thead>
                            <tr>
                                <th>${message(code:'propertyDefinition.descr.label', default:'Description')}</th>
                                <th>${message(code:'propertyDefinition.name.label', default:'Name')}</th>
                                <th>Name (DE)</th>
                                <th>Name (EN)</th>
                                <th>Count</th>
                                <th>${message(code:'default.button.delete.label', default:'Delete')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${privatePropertyDefinitions}" var="ppd">
                                <g:set var="pdI10nName" value="${I10nTranslation.createI10nOnTheFly(ppd, 'name')}" />
                                <tr>
                                    <td>${ppd.getI10n('descr')}</td>
                                    <td>
                                        ${ppd.getI10n('name')}
                                        <g:if test="${ppd.softData}">
                                            <span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
                                        </g:if>
                                        <g:if test="${ppd.mandatory}">
                                            <span  class="badge badge-warning" title="${message(code: 'default.mandatory.tooltip')}"> &#8252; </span>
                                        </g:if>
                                        <g:if test="${ppd.multipleOccurrence}">
                                            <span class="badge badge-info" title="${message(code:'default.multipleOccurrence.tooltip')}"> &#9733; </span>
                                        </g:if>
                                    </td>
                                    <td><g:xEditable owner="${pdI10nName}" field="valueDe" /></td>
                                    <td><g:xEditable owner="${pdI10nName}" field="valueEn" /></td>
                                    <td>${ppd.countUsages()}</td>
                                    <td>
                                        <g:if test="${ppd.countUsages()==0}">
                                            <g:checkBox name="deleteIds" value="${ppd?.id}" checked="false" />
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>

                    <p>${message(code:'propertyDefinition.private.info')}</p>

                    <g:field type="hidden" name="cmd" value="delete" />
                    <button type="submit" class="ui primary button">${message(code:'default.button.delete.label', default:'Delete')}</button>
                </g:form>
            </g:if>
        </div>
        <div class="four wide column">
            <semui:card class="card-grey">
                <input class="ui primary button" value="${message(code:'propertyDefinition.create_new.label')}"
                       data-semui="modal" href="#addPropertyDefinitionModal" type="submit">
            </semui:card>
        </div>
    </div><!-- .grid -->


    <semui:modal id="addPropertyDefinitionModal" message="propertyDefinition.create_new.label">

        <g:form params="${['shortcode':params.shortcode]}" action="managePrivateProperties" >
            <g:field type="hidden" name="cmd" value="add" />

            <dl>
                <dd>
                    <label class="property-label">Name:</label> <input type="text" name="pd_name"/>
                </dd>

                <dd>
                    <label class="property-label">${message(code:'propertyDefinition.descr.label', default:'Description')}</label>
                    <g:select name="pd_descr" from="${PropertyDefinition.AVAILABLE_PRIVATE_DESCR}"/>
                </dd>

                <dd>
                    <label class="property-label">Type:</label> <g:select
                        from="${PropertyDefinition.validTypes.entrySet()}"
                        optionKey="value" optionValue="key"
                        name="pd_type"
                        id="cust_prop_modal_select" />
                </dd>

                <div class="hide" id="cust_prop_ref_data_name">
                    <dd>
                        <label class="property-label">Refdata Kategory:</label>
                        <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
                    </dd>
                </div>

                <dd>
                    <label class="property-label">${message(code:'default.mandatory.tooltip')}:</label>
                    <g:checkBox type="text" name="pd_mandatory" />
                </dd>
                <dd>
                    <label class="property-label">${message(code:'default.multipleOccurrence.tooltip')}:</label>
                    <g:checkBox type="text" name="pd_multiple_occurrence" />
                </dd>

            </dl>
        </g:form>
    </semui:modal>

    <g:javascript>

       if( $( "#cust_prop_modal_select option:selected" ).val() == "class com.k_int.kbplus.RefdataValue") {
            $("#cust_prop_ref_data_name").show();
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
