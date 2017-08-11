<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.emptySubscription.label', default:'Add New Subscription')}</title>
  </head>
  <body>

    <div class="container">
      <ul class="breadcrumb">
         <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="myInstitutions"  action="addSubscription" params="${[shortcode:params.shortcode]}">${institution.name} ${message(code:'myinst.addSubscription.label', default:'Add Subscripton')}</g:link> </li>
      </ul>
    </div>


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
              Create with the role of:
              <g:select name="asOrgType"
                        from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.value <> ? and rdv.owner.desc = ?', ['Other', 'OrgType'])}"
                        optionKey="id"
                        optionValue="value"
                        class="input-medium"/>
            </dt>
            <dd>
              <div style="padding:10px;">Also subscribe all Consortia Members to this Subscription: <input type="checkbox" name="linkToAll" value="Y" style="vertical-align:text-bottom;" /></div>
              <div style="padding:10px;">Generate seperate Subscriptions for all Consortia Members: <input type="checkbox" name="generateSlavedSubs" value="Y" style="vertical-align:text-bottom;"/></div>
            </dd>
          </g:if>
          <br/>
          <input type="submit" class="btn btn-primary" value="${message(code:'default.button.create.label', default:'Create')}" />
        </dl>
      </g:form>
    </div>       
    
  </body>
</html>
