<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>
        <r:require module="annotations" />
    </head>
    <body>

    <laser:breadcrumbs>
        <laser:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
        <laser:crumb message="menu.institutions.manage_props" class="active" />
    </laser:breadcrumbs>

    <g:if test="${flash.message}">
        <div class="container">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </div>
    </g:if>

    <g:if test="${flash.error}">
        <div class="container">
            <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
        </div>
    </g:if>

    <div class="container">
        <h1>${institution?.name} - ${message(code: 'menu.institutions.manage_props', default: 'Manage Property Rules')}</h1>
    </div>

    <div class="container">
        <div class="row">
            <div class="span8">
                <br />
                <p>${message(code:'propertyRules.info', default:'Managing properties that are mandatory for members of this organisations.')}</p>

                <h4>${message(code:'propertyRules.add_new.label', default:'Add new rules')}</h4>

                <g:each in="${pprPropertyDescriptions}" var="pdesc">
                    <fieldset>
                        <g:form class="form-horizontal" params="${['shortcode':params.shortcode]}" action="propertyRules" method="post">

                            <g:select name="privatePropertyRule.propertyOwnerType"
                                      from="${pdesc}"
                                      optionValue="value"
                                      optionKey="key" />

                            <laser:select name="privatePropertyRule.propertyDefinition.id"
                                      from="${PropertyDefinition.findAllByDescr(pdesc.value)}"
                                      optionValue="name"
                                      optionKey="id" />

                            <g:field type="hidden" name="privatePropertyRule.propertyTenant" value="${institution.id}" />

                            <g:field type="hidden" name="cmd" value="add" />
                            <button type="submit" class="btn btn-primary">${message(code:'default.button.add.label', default:'Add')}</button>
                        </g:form>
                    </fieldset>
                </g:each>

                <g:if test="${ppRules}">
                    <h4>${message(code:'propertyRules.existing.label', default:'Existing rules')}</h4>
                    <fieldset>
                        <g:form class="form-horizontal" params="${['shortcode':params.shortcode]}" action="propertyRules" method="post">
                            <table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                        <th>${message(code:'propertyDefinition.descr.label', default:'Description')}</th>
                                        <th>${message(code:'propertyDefinition.name.label', default:'Name')}</th>
                                        <th>${message(code:'default.button.delete.label', default:'Delete')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${ppRules}" var="ppr">
                                        <tr>
                                            <td>${ppr.propertyDefinition.getI10n('descr')}</td>
                                            <td>${ppr.propertyDefinition.getI10n('name')}</td>
                                            <td>
                                                <g:checkBox name="propertyRuleDeleteIds" value="${ppr?.id}" checked="false" />
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
            <div class="span4">
                <br />
                <laser:card title="propertyDefinition.create_new.label" class="card-grey">
                    <input class="btn btn-primary" value="${message(code:'default.button.create_new.label')}"
                           data-toggle="modal" href="#addPropertyDefinitionModal" type="submit">
                </laser:card>
            </div>
        </div>
    </div>

    <div id="addPropertyDefinitionModal" class="modal hide">

        <g:form id="create_cust_prop" url="[controller: 'ajax', action: 'addCustomPropertyType']" >
            <input type="hidden" name="reloadReferer" value="/myInstitutions/${params.shortcode}/propertyRules"/>
            <input type="hidden" name="ownerClass" value="${this.class}"/>

            <div class="modal-body">
                <dl>
                    <dt>
                        <label class="control-label">${message(code:'propertyDefinition.create_new.label', default:'Create new property definition')}</label>
                    </dt>
                    <dd>
                        <label class="property-label">Name:</label> <input type="text" name="cust_prop_name"/>
                    </dd>
                    <dd>
                        <label class="property-label">Type:</label> <g:select
                            from="${PropertyDefinition.validTypes.entrySet()}"
                            optionKey="value" optionValue="key"
                            name="cust_prop_type"
                            id="cust_prop_modal_select" />
                    </dd>

                    <div class="hide" id="cust_prop_ref_data_name">
                        <dd>
                            <label class="property-label">Refdata Category:</label>
                            <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
                        </dd>
                    </div>
                    <dd>
                        <label class="property-label">Context:</label>
                        <g:select name="cust_prop_desc"  from="${PropertyDefinition.AVAILABLE_DESCR}"/>

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
