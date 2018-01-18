<%@ page import="com.k_int.kbplus.Combo" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} - ${institution?.name} - ${message(code:'myinst.addSubscription.label')}</title>
        </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
            <semui:crumb controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" message="myinst.currentSubscriptions.label" />
            <semui:crumb message="myinst.addSubscription.label" class="active" />
        </semui:breadcrumbs>

        <g:render template="actions" />

        <h1 class="ui header">${institution?.name} - ${message(code:'myinst.addSubscription.label')}</h1>

        <semui:messages data="${flash}"/>

    <div>
      <p>${message(code:'myinst.emptySubscription.notice', default:'This form will create a new subscription not attached to any packages. You will need to add packages using the Add Package tab on the subscription details page')}</p>
      <g:form action="processEmptySubscription" params="${[shortcode:params.shortcode]}" controller="myInstitutions" method="post" class="ui form">

          <div class="field">
            <label>${message(code:'myinst.emptySubscription.name', default:'New Subscription Name')}</label>
            <input type="text" name="newEmptySubName" placeholder="New Subscription Name"/>
          </div>

          <div class="field">
            <label>${message(code:'myinst.emptySubscription.identifier', default:'New Subscription Identifier')}</label>
            <input type="text" name="newEmptySubId" value="${defaultSubIdentifier}"/>
          </div>

          <semui:datepicker label="myinst.emptySubscription.valid_from" name="valid_from" value="${defaultStartYear}" />

          <semui:datepicker label="myinst.emptySubscription.valid_to" name="valid_to" value="${defaultEndYear}" />

          <g:if test="${orgType?.value == 'Consortium'}">
            <div class="field">
                <label>${message(code:'myinst.emptySubscription.create_as', default:'Create with the role of')}</label>

                <select id="asOrgType" name="asOrgType" class="input-medium">
                    <g:each in="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.value <> ? and rdv.owner.desc = ?', ['Other', 'OrgType'])}" var="opt">
                        <option value="${opt.id}" data-value="${opt.value}">${opt.getI10n('value')}</option>
                    </g:each>
                </select>

                <br />

                <div class="cons-options">

                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                        <label>${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}</label>
                    </div>

                <table class="ui celled striped table">
                    <thead>
                        <tr>
                            <th>
                                <g:checkBox name="cmToggler" id="cmToggler" checked="false"/>
                            </th>
                            <th>Alle Konsortialteilnehmer der Subskription hinzufügen</th>
                            <th>Ausgehende Verknüpfung</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${cons_members}" var="cm">
                            <tr>
                                <td>
                                    <g:checkBox type="text" name="selectedConsortiaMembers" value="${cm.id}" checked="false" />
                                </td>
                                <td>
                                    <g:link controller="organisations" action="show" id="${cm.id}">${cm}</g:link>
                                </td>
                                <td>
                                    ${Combo.findByFromOrgAndToOrg(cm, institution)?.type?.getI10n('value')}
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
                </div>
            </div>
          </g:if>
          <br/>
          <input type="submit" class="ui button" value="${message(code:'default.button.create.label', default:'Create')}" />

      </g:form>
    </div>       
    <r:script language="JavaScript">
        $('#cmToggler').click(function() {
            if($(this).prop('checked')) {
                $("input[name=selectedConsortiaMembers]").prop('checked', true)
            }
            else {
                $("input[name=selectedConsortiaMembers]").prop('checked', false)
            }
        })
        $('#asOrgType').change(function() {
            var selVal = $(this).find('option:selected').attr('data-value')
            if ('Consortium' == selVal) {
                $('.cons-options').show()
            }
            else {
                $('.cons-options').hide()
            }
        })
        $('#asOrgType').trigger('change') // init
    </r:script>
    </body>
</html>
