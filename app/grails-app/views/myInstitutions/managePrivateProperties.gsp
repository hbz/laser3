<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition; de.laser.domain.I10nTranslation" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>

    </head>
    <body>

    <laser:breadcrumbs>
        <laser:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
        <laser:crumb message="menu.institutions.manage_private_properties" class="active" />
    </laser:breadcrumbs>

    <laser:flash data="${flash}" />

    <div class="container">
        <h1>${institution?.name} - ${message(code: 'menu.institutions.manage_private_properties')}</h1>
    </div>

    <div class="container">
        <div class="row">
            <div class="span12">
                <laser:card class="card-grey">
                    <input class="btn btn-primary" value="${message(code:'propertyDefinition.create_new.label')}"
                           data-toggle="modal" href="#addPropertyDefinitionModal" type="submit">
                </laser:card>
            </div>
        </div>
    </div>

    <div class="container">
        <div class="row">
            <div class="span12">

                <p>${message(code:'propertyDefinition.private.info')}</p>

                <g:if test="${privatePropertyDefinitions}">
                    <fieldset>
                        <g:form class="form-horizontal" params="${['shortcode':params.shortcode]}" action="managePrivateProperties" method="post">
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

                            <g:field type="hidden" name="cmd" value="delete" />
                            <button type="submit" class="btn btn-primary">${message(code:'default.button.delete.label', default:'Delete')}</button>
                        </g:form>
                    </fieldset>
                </g:if>
            </div>
        </div>
    </div>

    <div id="addPropertyDefinitionModal" class="modal hide">

        <g:form params="${['shortcode':params.shortcode]}" action="managePrivateProperties" >
            <g:field type="hidden" name="cmd" value="add" />
            <div class="modal-body">
                <dl>
                    <dt>
                        <label class="control-label">${message(code:'propertyDefinition.create_new.label', default:'Create new property definition')}</label>
                    </dt>
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
            </div>

            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal">${message(code:'default.button.close.label', default:'Close')}</a>
                <input class="btn btn-success" name="SavePropertyDefinition" value="${message(code:'default.button.create_new.label', default:'Create New')}" type="submit">
            </div>
        </g:form>
    </div>

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
