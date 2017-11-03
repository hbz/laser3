<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.emptySubscription.label', default:'Add New Subscription')}</title>
        </head>
    <body>

        <laser:breadcrumbs>
            <laser:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
            <laser:crumb message="myinst.addSubscription.label" class="active" />
        </laser:breadcrumbs>

        <div class="container">
            <h1>${institution?.name} - ${message(code:'myinst.addSubscription.label', default:'Add Subscripton')}</h1>
            <g:render template="subsNav" contextPath="." />
        </div>
        
    <div class="container">
      <p>${message(code:'myinst.emptySubscription.notice', default:'This form will create a new subscription not attached to any packages. You will need to add packages using the Add Package tab on the subscription details page')}</p>
      <g:form action="processEmptySubscription" params="${[shortcode:params.shortcode]}" controller="myInstitutions" method="post" class="form-inline"> 
        <dl>
          <dt><label>${message(code:'myinst.emptySubscription.name', default:'New Subscription Name')}: </label></dt><dd> <input type="text" name="newEmptySubName" placeholder="New Subscription Name"/>&nbsp;</dd>
          <dt><label>${message(code:'myinst.emptySubscription.identifier', default:'New Subscription Identifier')}: </label></dt><dd> <input type="text" name="newEmptySubId" value="${defaultSubIdentifier}"/>&nbsp;</dd>
          <dt><label>${message(code:'myinst.emptySubscription.valid_from', default:'Valid From')}: </label></dt><dd> <g:simpleHiddenValue id="valid_from" name="valid_from" type="date" value="${defaultStartYear}"/>&nbsp;</dd>
          <dt><label>${message(code:'myinst.emptySubscription.valid_to', default:'Valid To')}: </label></dt><dd> <g:simpleHiddenValue id="valid_to" name="valid_to" type="date" value="${defaultEndYear}"/>&nbsp;</dd>
          <g:if test="${orgType?.value == 'Consortium'}">
            <dt>
              <label></label>
            </dt>
            <dt>
              ${message(code:'myinst.emptySubscription.create_as', default:'Create with the role of')}:
              <g:select name="asOrgType"
                        from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.value <> ? and rdv.owner.desc = ?', ['Other', 'OrgType'])}"
                        optionKey="id"
                        optionValue="value"
                        class="input-medium"/>
            </dt>
            <dd>
              <div class="cons-options hidden">
                <div style="padding:10px;">${message(code:'myinst.emptySubscription.subscribe_members', default:'Also subscribe all Consortia Members to this Subscription')}: 
                  <g:checkBox name="linkToAll" 
                              value="Y" 
                              style="vertical-align:text-bottom;" 
                              checked="false" 
                              onchange="showGSS()"/>
                </div>
                <div style="padding:10px;" class="sep-sub-select hidden">${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}: 
                  <g:checkBox type="checkbox" 
                              name="generateSlavedSubs" 
                              value="Y" 
                              style="vertical-align:text-bottom;" />
                </div>
              </div>
            </dd>
          </g:if>
          <br/>
          <input type="submit" class="ui primary button" value="${message(code:'default.button.create.label', default:'Create')}" />
        </dl>
      </g:form>
    </div>       
    <r:script language="JavaScript">
      function showGSS(){
        if($(".sep-sub-select").hasClass("hidden")){
          $(".sep-sub-select").removeClass('hidden');
        }else{
          $(".sep-sub-select").addClass('hidden');
        }
      };
      $(document).ready(function(){
        var val = "${orgType?.value}";
        
        if(val == 'Consortium'){
          $(".cons-options").removeClass("hidden");
        }else{
          $(".cons-options").addClass("hidden");
        }
      });
      $("[name='asOrgType']").change(function(){
        var val = $(this)['context']['selectedOptions'][0]['label'];
        
        if(val == 'Consortium'){
          $(".cons-options").removeClass("hidden");
        }else{
          $(".cons-options").addClass("hidden");
        }
      })
    </r:script>
    </body>
</html>
