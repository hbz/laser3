<!doctype html>
<r:require module="annotations" />

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} - ${institution.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</title>
  </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:institution.shortcode]}" text="${institution.name}" />
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>
        <semui:modeSwitch controller="subscriptionDetails" action="index" params="${params}" />
        <g:render template="actions" />

        <semui:messages data="${flash}" />

        <h1 class="ui header">${institution?.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</h1>

      <semui:filter>

        <g:form action="currentSubscriptions" params="${[shortcode:institution.shortcode]}" controller="myInstitutions" method="get" class="form-inline ui form">
            <div class="fields">
                <div class="field">
                    <label>${message(code: 'default.search.text', default: 'Search text')}</label>
                    <div class="ui input">
                    <input type="text" name="q"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                           value="${params.q?.encodeAsHTML()}"/>
                    </div>
                </div>

                <semui:datepicker label="default.valid_on.label" name="validOn" placeholder="default.date.label" value="${validOn}" />

                <div class="field">
                    <label class="control-label">${message(code:'default.filter.label', default:'Filter')}</label>
                    <g:set var="noDate" value="${message(code:'default.filter.date.none', default:'-None-')}" />
                    <g:set var="renewalDate" value="${message(code:'default.renewalDate.label', default:'Renewal Date')}" />
                    <g:set var="endDate" value="${message(code:'default.endDate.label', default:'End Date')}" />
                    <g:select class="ui dropdown"
                              name="dateBeforeFilter"
                              value="${params.dateBeforeFilter}"
                              from="${['renewalDate' : renewalDate, 'endDate' : endDate]}"
                              noSelection="${['null' : noDate]}"
                              optionKey="key"
                              optionValue="value" />
                </div>

                <semui:datepicker label="myinst.currentSubscriptions.filter.before" name="dateBeforeVal" placeholder="default.date.label" value="${dateBeforeVal}" />

                <div class="field">
                    <label>&nbsp;</label>
                    <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label', default:'Search')}" />
                </div>
            </div>
            <div class="inline fields">
                <label>Filter by Role:</label>
                <div class="field">
                    <div class="ui radio checkbox">
                        <input name="orgRole" <g:if test="${params.orgRole == 'Subscriber'}">checked=""</g:if> class="hidden" type="radio" value="Subscriber">
                        <label>${message(code:'consortium.subscriber', default:'Subscriber')}</label>
                    </div>
                </div>
                <div class="field">
                    <div class="ui radio checkbox">
                        <input name="orgRole" <g:if test="${params.orgRole == 'Subscription Consortia'}">checked=""</g:if> class="hidden" type="radio" value="Subscription Consortia">
                        <label>${message(code:'consortium', default:'Consortia')}</label>
                    </div>
                </div>
            </div>
        </g:form>
      </semui:filter>


      <div class="subscription-results">
        <table class="ui celled sortable table table-tworow la-table">
          <thead>
            <tr>
                <g:sortableColumn params="${params}" property="s.name" title="${message(code:'license.slash.name')}" />
                <th><g:annotatedLabel owner="${institution}" property="linkedPackages">${message(code:'license.details.linked_pkg', default:'Linked Packages')}</g:annotatedLabel></th>
                <th>${message(code:'myinst.currentSubscriptions.subscription_type', default:'Subscription Type')}</th>

                <g:if test="${params.orgRole == 'Subscriber'}">
                    <th>${message(code:'consortium', default:'Consortia')}</th>
                </g:if>
                <g:if test="${params.orgRole == 'Subscription Consortia'}">
                    <th>${message(code:'consortium.subscriber', default:'Subscriber')}</th>
                </g:if>

                <g:sortableColumn params="${params}" property="s.startDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
                <g:sortableColumn params="${params}" property="s.endDate" title="${message(code:'default.endDate.label', default:'End Date')}" />

                <!--<g:sortableColumn params="${params}" property="s.manualCancellationDate" title="${message(code:'default.cancellationDate.label', default:'Cancellation Date')}" />-->
                <th></th>
            </tr>
          </thead>
          <g:each in="${subscriptions}" var="s">
              <g:if test="${true || ! s.instanceOf}">
                <tr>
                  <td>
                    <g:link controller="subscriptionDetails" action="details" params="${[shortcode:institution.shortcode]}" id="${s.id}">
                        <g:if test="${s.name}">${s.name}</g:if><g:else>-- ${message(code:'myinst.currentSubscriptions.name_not_set', default:'Name Not Set')}  --</g:else>
                        <g:if test="${s.instanceOf}">(${message(code:'subscription.isInstanceOf.label', default:'Dependent')}<g:if test="${s.consortia && s.consortia == institution}">: ${s.subscriber?.name}</g:if>)</g:if>
                    </g:link>
                    <g:if test="${s.owner}">
                        <g:link class="icon ico-object-link sub-link-icon law" controller="licenseDetails" action="index" id="${s.owner.id}">${s.owner?.reference}</g:link>
                    </g:if>
                  </td>
                    <td>
                        <!-- packages -->
                        <g:each in="${s.packages}" var="sp" status="ind">
                            <g:if test="${ind < 10}">
                                <g:link controller="packageDetails" action="show" id="${sp.pkg?.id}" title="${sp.pkg?.contentProvider?.name}">
                                    ${sp.pkg.name}
                                </g:link>
                            </g:if>
                        </g:each>
                        <g:if test="${s.packages.size() > 10}">
                            <div>${message(code:'myinst.currentSubscriptions.etc.label', args:[s.packages.size() - 10])}</div>
                        </g:if>
                        <g:if test="${editable && (s.packages==null || s.packages.size()==0)}">
                            <i>${message(code:'myinst.currentSubscriptions.no_links', default:'None currently, Add packages via')}
                                <g:link controller="subscriptionDetails" action="linkPackage" id="${s.id}">${message(code:'subscription.details.linkPackage.label', default:'Link Package')}</g:link>
                            </i>
                        </g:if>
                        <!-- packages -->
                    </td>
                    <td>
                        <!-- subscriptions type -->
                        <!-- subscriptions type -->
                    </td>
                    <g:if test="${params.orgRole == 'Subscriber'}">
                        <td>${s.consortia?.name}</td>
                    </g:if>
                    <g:if test="${params.orgRole == 'Subscription Consortia'}">
                        <td>
                            <g:each in="${s.allSubscribers}" var="subscriber">
                                <g:link controller="organisations" action="show" id="${subscriber.id}">${subscriber}</g:link>
                            </g:each>
                        </td>
                    </g:if>
                    <td><g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></td>

                    <td class="x">
                        <g:if test="${ editable && ( (institution in s.allSubscribers) || s.consortia == institution )}">
                            <g:link controller="myInstitutions" action="actionCurrentSubscriptions" class="ui icon basic negative button"
                                    params="${[shortcode:institution.shortcode,curInst:institution.id,basesubscription:s.id]}"
                                    onclick="return confirm('${message(code:'license.details.delete.confirm', args:[(s.name?:'this subscription')])}')">
                                <i class="trash icon"></i></g:link>
                        </g:if>
                    </td>
                </tr>
              </g:if>
          </g:each>
        </table>
      </div>

        <g:if test="${subscriptions}" >
          <semui:paginate  action="currentSubscriptions" controller="myInstitutions" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_sub_rows}" />
        </g:if>

    <r:script type="text/javascript">
        $(document).ready(function(){
            var val = "${params.dateBeforeFilter}";
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
