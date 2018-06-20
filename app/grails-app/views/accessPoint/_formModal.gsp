<%@ page import="com.k_int.kbplus.OrgAccessPoint" %>

<semui:modal id="accessPointFormModal" text="${message(code: 'default.add.label', args: [message(code: 'accessPoint.label', default: 'Access Point')])}">

    <g:form class="ui form" url="[controller: 'accessPoint', action: 'create']" method="POST">
                
        <div class="inline-lists">
            <dl>                
                <dt><label for="name">
                        <g:message code="accessPoint.name" default="Name" />
                    </label>
                </dt>
                <dd> 
                    <g:select id="type" name='name'
                        noSelection="${['null':'Select...']}"
                        from="${['campus','wlan','vpn']}"></g:select>
                </dd>
                <br /><br />
                <dt>
                    <label for="accessMethod">
                        <g:message code="accessMethod.label" default="Access Method" />
                    </label>
                </dt>
                <dd>
                    <laser:select class="values"
                        name="accessMethod"
                        from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues('Access Method')}"
                        optionKey="id"
                        optionValue="value" />
                </dd>
                <br /><br />
                <dt>
                    <label for="authorizationMethod">
                        <g:message code="accessMethod.type" default="Type" />
                    </label>
                </dt>
                <dd>
                    <laser:select class="values"
                        name="authorizationMethod"
                        from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues('Authorization Method')}"
                        optionKey="id"
                        optionValue="value" />
                </dd>
                <br /><br />
                <dt>
                    <label for="first_name">
                        <g:message code="accessMethod.data" default="Data" />
                    </label>
                </dt>
                <dd>
                    <div class="two fields">
                        <div class="field wide twelve fieldcontain">
                            <g:textField name="modeDetails" value="${params.modeDetails}"/>
                        </div>
                        <div class="field wide five fieldcontain">
                            <g:select id="iptype" name='iptype'
                        noSelection="${['null':'CIDR']}"
                        from="${['CIDR','IP-Range','IP']}"></g:select>
                        </div>
                    </div>
                </dd>
                <br /><br />
                <dt>
                    <label>
                        <g:message code="accessPoint.validityRange" default="Gültigkeit" />
                    </label>
                </dt>
                <dd>
                    
                    <semui:datepicker label ="Von:" name="validFrom" placeholder ="default.date.label" value ="${params.validFrom}">
                    </semui:datepicker>
                    <semui:datepicker label ="Bis:" name="validTo" placeholder ="default.date.label" value ="${params.validTo}">
                    </semui:datepicker>
                </dd>
                <br /><br />
                <hr />
                <dt>
                    <label for="first_name">
                        <g:message code="accessMethod.data" default="Data" />
                    </label>
                </dt>
                <dd>
                    <div class="two fields">
                        <g:select id="availablePropertyTypes" name="availablePropertyTypes" from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues('Access Method')}" value="${params.propertyFilterType}"/>
                        <input id="selectVal" type="text" name="propertyFilter" placeholder="${message(code:'license.search.property.ph', default:'property value...')}" value="${params.propertyFilter?:''}" />
                        <input type="hidden" id="propertyFilterType" name="propertyFilterType" value="${params.propertyFilterType}"/>
                    </div>
                </dd>
                <br /><br />
                
            </dl>
        </div>
        
    </g:form>
        
        <r:script type="text/javascript">

        $('.license-results input[type="radio"]').click(function () {
            $('.license-options').slideDown('fast');
        });

        function availableTypesSelectUpdated(optionSelected){

          var selectedOption = $( "#availablePropertyTypes option:selected" )

          var selectedValue = selectedOption.val()

          //Set the value of the hidden input, to be passed on controller
          $('#propertyFilterType').val(selectedOption.text())
          
          updateInputType(selectedValue)  
        }

        function updateInputType(selectedValue){
          //If we are working with RefdataValue, grab the values and create select box
          if(selectedValue.indexOf("RefdataValue") != -1){
            var refdataType = selectedValue.split("&&")[1]
            refdataType="Access Method";
            
            $.ajax({ url:'<g:createLink controller="ajax" action="sel2RefdataSearch"/>'+'/'+refdataType+'?format=json',
                        success: function(data) {
                          alert(data);
                          var select = ' <select id="selectVal" name="propertyFilter" > '
                          //we need empty when we dont want to search by property
                          select += ' <option></option> '
                          for(var index=0; index < data.length; index++ ){
                            var option = data[index]
                            select += ' <option value="'+option.text+'">'+option.text+'</option> '
                          }
                          select += '</select>'
                          $('#selectVal').replaceWith(select)
                        },async:false
            });
          }else{
            //If we dont have RefdataValues,create a simple text input
            $('#selectVal').replaceWith('<input id="selectVal" type="text" name="propertyFilter" placeholder="${message(code:'license.search.property.ph', default:'property value')}" />')
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
          var propertyFilterElement = $("#selectVal");
          if(propertyFilterElement.is("input")){
            propertyFilterElement.val(paramPropertyFilter);
          }else{
              $("#selectVal option").filter(function() {
                return $(this).text() == paramPropertyFilter ;
              }).prop('selected', true);
          }
        }

        $('#availablePropertyTypes').change(function(e) {
          var optionSelected = $("option:selected", this);
          availableTypesSelectUpdated(optionSelected);
        });

        $('.license-options .delete-license').click(function () {
            $('.license-results input:checked').each(function () {
                $(this).parent().parent().fadeOut('slow');
                $('.license-options').slideUp('fast');
            })
        })
        window.onload = setTypeAndSearch()
    </r:script>
    
</semui:modal>