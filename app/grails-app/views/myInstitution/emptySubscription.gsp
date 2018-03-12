<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<%@ page import="com.k_int.kbplus.Combo" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} - ${institution?.name} - ${message(code:'myinst.addSubscription.label')}</title>
        </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <semui:crumb message="myinst.addSubscription.label" class="active" />
        </semui:breadcrumbs>

        <g:render template="actions" />

        <h1 class="ui header">${institution?.name} - ${message(code:'myinst.addSubscription.label')}</h1>

        <semui:messages data="${flash}"/>

        <g:form action="processEmptySubscription" controller="myInstitution" method="post" class="ui form">

            <p>${message(code:'myinst.emptySubscription.notice', default:'This form will create a new subscription not attached to any packages. You will need to add packages using the Add Package tab on the subscription details page')}</p>

            <div class="field">
                <label>${message(code:'myinst.emptySubscription.name', default:'New Subscription Name')}</label>
                <input type="text" name="newEmptySubName" placeholder="New Subscription Name"/>
             </div>

            <div class="field hidden">
                <label>${message(code:'myinst.emptySubscription.identifier', default:'New Subscription Identifier')}</label>
                <input type="text" name="newEmptySubId" value="${defaultSubIdentifier}"/>
            </div>

            <semui:datepicker label="subscription.startDate.label" name="valid_from" value="${defaultStartYear}" />

            <semui:datepicker label="subscription.endDate.label" name="valid_to" value="${defaultEndYear}" />

            <g:if test="${orgType?.value == 'Consortium'}">
                <div class="field">
                    <label>${message(code:'myinst.emptySubscription.create_as', default:'Create with the role of')}</label>

                    <select id="asOrgType" name="asOrgType" class="ui dropdown">
                        <g:each in="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.value <> ? and rdv.owner.desc = ?', ['Other', 'OrgType'])}" var="opt">
                            <option value="${opt.id}" data-value="${opt.value}">${opt.getI10n('value')}</option>
                        </g:each>
                    </select>

                </div>
            </g:if>

            <br/>
            <div id="dynHiddenValues"></div>

            <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
            <input id="submitterFallback" type="submit" class="ui button" value="${message(code:'default.button.create.label', default:'Create')}" />
        </g:form>

        <g:if test="${orgType?.value == 'Consortium'}">

            <div class="cons-options">
                <semui:filter>
                    <g:formRemote name="x" url="[controller:'MyInstitution', action:'ajaxEmptySubscription', params:[shortcode:contextService.getOrg()?.shortcode]]" update="orgListTable" class="ui form">
                        <g:render template="/templates/filter/orgFilter" />
                    </g:formRemote>
                </semui:filter>

                <div id="orgListTable">
                    <g:render template="/templates/filter/orgFilterTable" model="[orgList: cons_members, tmplShowCheckbox: true]" />
                </div>

                <div class="ui checkbox">
                    <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                    <label>${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}</label>
                </div>

            </div><!-- .cons-options -->

            <r:script language="JavaScript">
                $('#submitterFallback').click(function(e){
                    e.preventDefault()
                    $('#dynHiddenValues').empty()
                    $('input[name=selectedOrgs]:checked').each(function(index, elem){
                        var newElem = $('<input type="hidden" name="selectedOrgs" value="' + $(elem).attr('value') + '">')
                        $('#dynHiddenValues').append(newElem)
                    })
                    $(this).parents('form').submit()
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
            </r:script>

        </g:if>
    </body>
</html>
