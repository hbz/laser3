<%@ page import="com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.Combo" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
        </head>
        <body>
            <semui:breadcrumbs>
                <g:if test="${params.shortcode}">
                    <semui:crumb controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" text="${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
                </g:if>
                <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"  text="${subscriptionInstance.name}" />
                <semui:crumb class="active" text="${message(code:'subscription.details.addMembers.label', default:'Add Members')}" />
            </semui:breadcrumbs>

            <g:render template="actions" />

            <h1 class="ui header">
                <semui:editableLabel editable="${editable}" />
                <g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name" class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
            </h1>

            <g:render template="nav" contextPath="." />

            <!-- <semui:filter></semui:filter> -->

            <g:if test="${institution?.orgType?.value == 'Consortium'}">
                <g:form action="processAddMembers" params="${[shortcode:params.shortcode, id:params.id]}" controller="subscriptionDetails" method="post" class="ui form">

                    <div>
                        <div class="cons-options">

                            <input type="hidden" name="asOrgType" value="${institution?.orgType?.id}">

                            <div class="ui field">
                                <label>Teilnehmer hinzufügen</label>
                                <div class="ui checkbox">
                                    <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                                    <label>${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}</label>
                                </div>
                            </div>

                            <div class="ui field">
                                <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Subscription Status')}" var="rdcSubStatus"/>
                                <label>Status der neuen Instanzen</label>
                                <g:select from="${com.k_int.kbplus.RefdataValue.findAllByOwner(rdcSubStatus)}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="subStatus" />
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
                    <br/>
                    <input type="submit" class="ui button" value="${message(code:'default.button.create.label', default:'Create')}" />
                </g:form>
            </g:if>



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
        </r:script>

  </body>
</html>
