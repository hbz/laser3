<%@ page import="de.laser.interfaces.CalculatedType;de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />
<!doctype html>

<%-- r:require module="annotations" / --%>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser')} : ${message(code:'myinst.currentSubscriptions.label')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <g:if test="${filterSet || defaultSet}">
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[format:'csv']}">
                            ${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:if>
                <g:else>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                    </semui:exportDropdownItem>
                </g:else>
            </semui:exportDropdown>

            <g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ADMIN')}">
                <g:render template="actions" />
            </g:if>

        </semui:controlButtons>

        <semui:messages data="${flash}"/>

        <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'myinst.currentSubscriptions.label')}
            <semui:totalNumber total="${num_sub_rows}"/>
        </h1>

    <g:render template="/templates/subscription/subscriptionFilter"/>

    <g:render template="/templates/subscription/subscriptionTable"/>

    <%--
    <r:script>

        function availableTypesSelectUpdated(optionSelected) {

            var selectedOption = $( "#availablePropertyTypes option:selected" )
            var selectedValue = selectedOption.val()

            if (selectedValue) {
                //Set the value of the hidden input, to be passed on controller
                $('#propertyFilterType').val(selectedOption.text())

                updateInputType(selectedValue)
            }
        }

        function updateInputType(selectedValue) {
            //If we are working with RefdataValue, grab the values and create select box
            if(selectedValue.indexOf("RefdataValue") != -1) {
                var refdataType = selectedValue.split("&&")[1]
                $.ajax({
                    url:'<g:createLink controller="ajax" action="sel2RefdataSearch"/>'+'/'+refdataType+'?format=json',
                    success: function(data) {
                        var select = ' <select id="propertyFilter" name="propertyFilter" > '
                        //we need empty when we dont want to search by property
                        select += ' <option></option> '
                        for (var index=0; index < data.length; index++ ) {
                            var option = data[index]
                            select += ' <option value="'+option.text+'">'+option.text+'</option> '
                        }
                        select += '</select>'
                        $('#propertyFilter').replaceWith(select)
                    },async:false
                });
            }else{
                //If we dont have RefdataValues,create a simple text input
                $('#propertyFilter').replaceWith('<input id="propertyFilter" type="text" name="propertyFilter" placeholder="${message(code:'license.search.property.ph', default:'property value')}" />')
            }
        }

        function setTypeAndSearch(){
            var selectedType = $("#propertyFilterType").val()
            //Iterate the options, find the one with the text we want and select it
            var selectedOption = $("#availablePropertyTypes option").filter(function() {
                return $(this).text() == selectedType ;
            }).prop('selected', true); //This will trigger a change event as well.


            //Generate the correct select box
            availableTypesSelectUpdated(selectedOption)

            //Set selected value for the actual search
            var paramPropertyFilter = "${params.propertyFilter}";
            var propertyFilterElement = $("#propertyFilter");
            if(propertyFilterElement.is("input")){
                propertyFilterElement.val(paramPropertyFilter);
            }
            else {
                $("#propertyFilter option").filter(function() {
                    return $(this).text() == paramPropertyFilter ;
                }).prop('selected', true);
            }
        }

        $('#availablePropertyTypes').change(function(e) {
            var optionSelected = $("option:selected", this);
            availableTypesSelectUpdated(optionSelected);
        });

        window.onload = setTypeAndSearch()
    </r:script>
    --%>

    <semui:debugInfo>
        <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

  </body>
</html>
