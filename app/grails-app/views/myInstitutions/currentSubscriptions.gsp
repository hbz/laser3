<!doctype html>
<r:require module="annotations" />

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${institution.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</title>
  </head>
  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:institution.shortcode]}" text="${institution.name}" />
      <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
      <g:if test="${editable}">
        <semui:crumbAsBadge message="default.editable" class="orange" />
      </g:if>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

      <h1 class="ui header">${institution?.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</h1>

      <g:render template="subsNav" contextPath="." />


    <div>
      <div class="well">
      <g:form action="currentSubscriptions" params="${[shortcode:institution.shortcode]}" controller="myInstitutions" method="get" class="form-inline">



        <label class="control-label">${message(code:'default.search.text', default:'Search text')}: </label>
        <input style="margin-top:10px" type="text" name="q" placeholder="${message(code:'default.search.ph', default:'enter search term...')}" value="${params.q?.encodeAsHTML()}"/>
            
        <label class="control-label">${message(code:'default.valid_on.label', default:'Valid On')}: </label>
        <div class="input-append date">
          <input class="span2 datepicker-class" size="16" type="text" name="validOn" value="${validOn}">
        </div>

        <label class="control-label">${message(code:'default.filter.label', default:'Filter')}: </label>
        <g:set var="noDate" value="${message(code:'default.filter.date.none', default:'-None-')}" />
        <g:set var="renewalDate" value="${message(code:'default.renewalDate.label', default:'Renewal Date')}" />
        <g:set var="endDate" value="${message(code:'default.endDate.label', default:'End Date')}" />
        <g:select style="margin-top:10px;padding:0px 6px;"
                  name="dateBeforeFilter"
                  value="${params.dateBeforeFilter}"
                  from="${['renewalDate' : renewalDate, 'endDate' : endDate]}"
                  noSelection="${['null' : noDate]}"
                  optionKey="key"
                  optionValue="value" />

        <span class="dateBefore hidden">
          <label class="control-label" style="margin-right:10px;">${message(code:'myinst.currentSubscriptions.filter.before', default:'before')}</label>
          <div class="input-append date">
              <input class="span2 datepicker-class" size="16" type="text" name="dateBeforeVal" value="${params.dateBeforeVal}">
          </div>
        <input type="submit" class="ui primary button" value="${message(code:'default.button.search.label', default:'Search')}" />
      </g:form>
      </div>
    </div>


      <div class="subscription-results">
        <table class="ui celled striped table table-tworow">
          <tr>
            <g:sortableColumn colspan="7" params="${params}" property="s.name" title="${message(code:'license.slash.name')}" />
            <th rowspan="2">${message(code:'default.action.label', default:'Action')}</th>
          </tr>

          <tr>
            <th><g:annotatedLabel owner="${institution}" property="linkedPackages">${message(code:'license.details.linked_pkg', default:'Linked Packages')}</g:annotatedLabel></th>
            <th>${message(code:'consortium.plural', default:'Consortia')}</th>
            <g:sortableColumn params="${params}" property="s.startDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
            <g:sortableColumn params="${params}" property="s.endDate" title="${message(code:'default.endDate.label', default:'End Date')}" />
            <g:sortableColumn params="${params}" property="s.manualRenewalDate" title="${message(code:'default.renewalDate.label', default:'Renewal Date')}" />
            <th>${message(code:'tipp.platform', default:'Platform')}</th>
          </tr>
          <g:each in="${subscriptions}" var="s">
            <tr>
              <td colspan="7">
                <g:link controller="subscriptionDetails" action="index" params="${[shortcode:institution.shortcode]}" id="${s.id}">
                  <g:if test="${s.name}">${s.name}</g:if><g:else>-- ${message(code:'myinst.currentSubscriptions.name_not_set', default:'Name Not Set')}  --</g:else>
                  <g:if test="${s.instanceOf}">(${message(code:'subscription.isInstanceOf.label', default:'Dependent')}<g:if test="${s.consortia && s.consortia == institution}">: ${s.subscriber?.name}</g:if>)</g:if>
                </g:link>
                <g:if test="${s.owner}"> 
                  <span class="pull-right">${message(code:'license')} : <g:link controller="licenseDetails" action="index" id="${s.owner.id}">${s.owner?.reference}</g:link></span>
                </g:if>
              </td>
              <td rowspan="2">
                <g:if test="${ editable && ( (institution in s.allSubscribers) || s.consortia == institution )}">
                    <g:link controller="myInstitutions" action="actionCurrentSubscriptions" params="${[shortcode:institution.shortcode,curInst:institution.id,basesubscription:s.id]}" onclick="return confirm($message(code:'licence.details.delete.confirm', args:[(s.name?:'this subscription')})" class="ui negative button">${message(code:'default.button.delete.label', default:'Delete')}</g:link>
                </g:if>
              </td>
            </tr>
            <tr>
              <td>
                <ul>
                  <g:each in="${s.packages}" var="sp" status="ind">
                    <g:if test="${ind < 10}">
                      <li>
                        <g:link controller="packageDetails" action="show" id="${sp.pkg?.id}" title="${sp.pkg?.contentProvider?.name}">${sp.pkg.name}</g:link>
                      </li>
                    </g:if>
                  </g:each>
                  <g:if test="${s.packages.size() > 10}">
                    <div>${message(code:'myinst.currentSubscriptions.etc.label', args:[s.packages.size() - 10])}</div>
                  </g:if>
                </ul>
                <g:if test="${editable && (s.packages==null || s.packages.size()==0)}">
                  <i>${message(code:'myinst.currentSubscriptions.no_links', default:'None currently, Add packages via')} <g:link controller="subscriptionDetails" action="linkPackage" id="${s.id}">${message(code:'subscription.details.linkPackage.label', default:'Link Package')}</g:link></i>
                </g:if>
                &nbsp;<br/>
                &nbsp;<br/>
              </td>
              <td>${s.consortia?.name}</td>
              <td><g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/></td>
              <td><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></td>
              <td><g:formatDate formatName="default.date.format.notime" date="${s.renewalDate}"/></td>
              <td>
                <g:each in="${s.instanceOf?.packages}" var="sp">
                  ${sp.pkg?.nominalPlatform?.name}<br/>
                </g:each>
              </td>
            </tr>
          </g:each>
        </table>
      </div>

  
      <div class="pagination" style="text-align:center">
        <g:if test="${subscriptions}" >
          <bootstrap:paginate  action="currentSubscriptions" controller="myInstitutions" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_sub_rows}" />
        </g:if>
      </div>

    <r:script type="text/javascript">

        $(".datepicker-class").datepicker({
            format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
            language:"${message(code:'default.locale.label', default:'en')}",
            autoclose:true
        });

        $(document).ready(function(){
          var val = "${params.dateBeforeFilter}";
          console.log(val);
          if(val == "null"){
            $(".dateBefore").addClass("hidden");
          }else{
            $(".dateBefore").removeClass("hidden");
          }
        });

        $("[name='dateBeforeFilter']").change(function(){
          var val = $(this)['context']['selectedOptions'][0]['label'];

          if(val != "${message(code:'default.filter.date.none', default:'-None-')}"){
            $(".dateBefore").removeClass("hidden");
          }else{
            $(".dateBefore").addClass("hidden");
          }
        })
    </r:script>

  </body>
</html>
