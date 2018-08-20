<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<% def securityService = grailsApplication.mainContext.getBean("springSecurityService") %>

<%@ page import="com.k_int.kbplus.Combo" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.addSubscription.label')}</title>
        </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <semui:crumb message="myinst.addSubscription.label" class="active" />
        </semui:breadcrumbs>

        <g:render template="actions" />

        <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code:'myinst.addSubscription.label')}</h1>

        <semui:messages data="${flash}"/>

        <semui:form>
            <g:form action="processEmptySubscription" controller="myInstitution" method="post" class="ui form newLicence">
                <input type="hidden" name="newEmptySubId" value="${defaultSubIdentifier}"/>

                <p>${message(code:'myinst.emptySubscription.notice', default:'This form will create a new subscription not attached to any packages. You will need to add packages using the Add Package tab on the subscription details page')}</p>

                <div class="field required">
                    <label>${message(code:'myinst.emptySubscription.name', default:'New Subscription Name')}</label>
                    <input required type="text" name="newEmptySubName" placeholder=""/>
                 </div>

                <div class="two fields">
                    <semui:datepicker label="subscription.startDate.label" name="valid_from" value="${defaultStartYear}" />

                    <semui:datepicker label="subscription.endDate.label" name="valid_to" value="${defaultEndYear}" />
                </div>

                <g:if test="${orgType?.value == 'Consortium'}">
                    <div class="field">
                        <label>${message(code:'myinst.emptySubscription.create_as', default:'Create with the role of')}</label>

                        <select id="asOrgType" name="asOrgType" class="ui dropdown">
                            <g:each in="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.value in (:wl) and rdv.owner.desc = :ot', [wl:['Consortium', 'Institution'], ot:'OrgType'])}" var="opt">
                                <option value="${opt.id}" data-value="${opt.value}">${opt.getI10n('value')}</option>
                            </g:each>
                        </select>

                    </div>
                </g:if>

                <br/>
                <div id="dynHiddenValues"></div>

                <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                <input id="submitterFallback" type="submit" class="ui button js-click-control" value="${message(code:'default.button.create.label', default:'Create')}" />
            </g:form>
        </semui:form>

    <hr>

        <g:if test="${orgType?.value == 'Consortium'}">

            <g:if test="${! cons_members}">
                <g:if test="${securityService.getCurrentUser().hasAffiliation("INST_ADM")}">
                    <hr />

                    <div class="ui info message">
                        <div class="header">Konsorten verwalten</div>
                        <p>
                            Sie können bei Bedarf über
                            <g:link controller="myInstitution" action="addConsortiaMembers">diesen Link</g:link>
                            Ihre Konsorten verwalten ..
                        </p>
                    </div>
                </g:if>
            </g:if>
            <g:else>
                <div class="cons-options">
                    <semui:filter>
                        <g:formRemote name="x" url="[controller:'MyInstitution', action:'ajaxEmptySubscription', params:[shortcode:contextService.getOrg()?.shortcode]]" update="orgListTable" class="ui form">
                            <g:render template="/templates/filter/orgFilter"
                                      model="[tmplConfigShow: ['name']]" />
                        </g:formRemote>
                    </semui:filter>

                    <div id="orgListTable">
                        <g:render template="/templates/filter/orgFilterTable" model="[orgList: cons_members, tmplShowCheckbox: true]" />
                    </div>

                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" checked="checked" readonly="readonly">
                        <label>${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}</label>
                    </div>

                </div><!-- .cons-options -->
            </g:else>

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
        <r:script language="JavaScript">

                    $('.newLicence')
                            .form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            newEmptySubName: {
                                identifier  : 'newEmptySubName',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                    }
                                ]
                            }
                         }
                    });
        </r:script>
    </body>
</html>
