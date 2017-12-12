<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'license.current', default:'Current Licenses')}</title>
  </head>
  <body>

  <semui:breadcrumbs>
      <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
      <semui:crumb message="license.current" class="active" />

      <semui:exportDropdown>
          <semui:exportDropdownItem>
              <g:link action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
          </semui:exportDropdownItem>
          <g:each in="${transforms}" var="transkey,transval">
              <semui:exportDropdownItem>
                  <g:link action="currentLicenses" params="${params+[format:'xml',transformId:transkey,format_content:'subie']}">${transval.name}</g:link>
              </semui:exportDropdownItem>
          </g:each>
      </semui:exportDropdown>

  </semui:breadcrumbs>

  <g:if test="${is_inst_admin}">
      <semui:crumbAsBadge message="default.editable" class="orange" />
  </g:if>

  <semui:messages data="${flash}" />

  <h1 class="ui header">${institution?.name} - ${message(code:'license.plural', default:'Licenses')}</h1>

    <semui:subNav actionName="${actionName}">
        <semui:subNavItem controller="myInstitutions" action="currentLicenses" params="${[shortcode:params.shortcode]}" message="license.current" />
        <semui:subNavItem controller="myInstitutions" action="addLicense" params="${[shortcode:params.shortcode]}" message="license.copy" />
        <g:if test="${is_inst_admin}">
            <semui:subNavItem controller="myInstitutions" action="cleanLicense" params="${[shortcode:params.shortcode]}" message="license.add.blank" />
        </g:if>
    </semui:subNav>

    <semui:filter class="license-searches">
        <form class="form-inline">
          <div>
            <label>${message(code:'license.valid_on', default:'Valid On')}:</label>
            <input size="10" type="text"  id="datepicker-validOn" name="validOn" value="${validOn}">
            <label>${message(code:'license.search.by_ref', default:'Search by Reference')}:</label>
            <input type="text" name="keyword-search" placeholder="${message(code:'default.search.ph', default:'enter search term...')}" value="${params['keyword-search']?:''}" />
          </div>
          <div style="margin-top:10px;">
            <label>${message(code:'license.property.search')}:</label>
            <g:select id="availablePropertyTypes" name="availablePropertyTypes" from="${custom_prop_types}" optionKey="value" optionValue="key" value="${params.propertyFilterType}"/>
            <input id="selectVal" type="text" name="propertyFilter" placeholder="${message(code:'license.search.property.ph', default:'property value...')}" value="${params.propertyFilter?:''}" />
            <input type="hidden" id="propertyFilterType" name="propertyFilterType" value="${params.propertyFilterType}"/>
            <input type="submit" class="ui primary button" value="${message(code:'default.button.search.label', default:'Search')}" />
          </div>
        </form>
    </semui:filter>

        <div class="license-results">
        <g:if test="${licenseCount && licenseCount>0}">
          <span>${message(code:'license.current.showing', args:[licenseCount])}</span>
        </g:if>
          <table class="ui celled striped table">
            <thead>
              <tr>
                <g:sortableColumn params="${params}" property="reference" title="${message(code:'license.name')}" />
                <th>${message(code:'license.licensor.label', default:'Licensor')}</th>
                <g:sortableColumn params="${params}" property="startDate" title="${message(code:'license.start_date', default:'Start Date')}" />
                <g:sortableColumn params="${params}" property="endDate" title="${message(code:'license.end_date', default:'End Date')}" />
                <th>${message(code:'default.actions.label', default:'Action')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${licenses}" var="l">
                <tr>
                  <td>
                    <g:link action="index" controller="licenseDetails" id="${l.id}">
                      ${l.reference?:message(code:'missingLicenseReference', default:'** No License Reference Set **')}
                    </g:link>
                    <g:if test="${l.subscriptions && ( l.subscriptions.size() > 0 )}">
                      <ul>
                        <g:each in="${l.subscriptions}" var="sub">
                          <g:if test="${sub.status?.value != 'Deleted'}">
                            <li><g:link controller="subscriptionDetails" action="index" id="${sub.id}">${sub.id} (${sub.name})</g:link><br/></li>
                          </g:if>
                        </g:each>
                      </ul>
                    </g:if>
                    <g:else>
                      <br/>${message(code:'myinst.currentLicenses.no_subs', default:'No linked subscriptions.')}
                    </g:else>
                  </td>
                  <td>${l.licensor?.name}</td>
                  <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${l.startDate}"/></td>
                  <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${l.endDate}"/></td>
                  <td>
                    <g:link controller="myInstitutions" action="actionLicenses" params="${[shortcode:params.shortcode,baselicense:l.id,'copy-license':'Y']}" class="ui positive button">${message(code:'default.button.copy.label', default:'Copy')}</g:link>
                    <g:link controller="myInstitutions" action="actionLicenses" onclick="return confirm('${message(code:'license.delete.confirm', default:'Are you sure you want to delete')} ${l.reference?:message(code:'missingLicenseReference', default:'** No License Reference Set **')}?')" params="${[shortcode:params.shortcode,baselicense:l.id,'delete-license':'Y']}" class="ui negative button">${message(code:'default.button.delete.label', default:'Delete')}</g:link>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
       

          <semui:paginate action="currentLicenses" controller="myInstitutions" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${licenseCount}" />


    <r:script type="text/javascript">

        $("#datepicker-validOn").datepicker({
            format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
            language:"${message(code:'default.locale.label', default:'en')}",
            autoclose:true
        });

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
            $.ajax({ url:'<g:createLink controller="ajax" action="sel2RefdataSearch"/>'+'/'+refdataType+'?format=json',
                        success: function(data) {
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


  </body>
</html>
