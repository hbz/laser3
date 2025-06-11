<%@ page import="de.laser.RefdataValue;de.laser.storage.RDConstants" %>
<laser:htmlStart message="subscription.details.linkTitle.label.subscription" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.linkTitle.label.subscription')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${subscription.name}" />
<br/>
<br/>
<h2 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'subscription.details.linkTitle.heading.subscription')}</h2>

<ui:messages data="${flash}"/>

<laser:render template="/templates/filter/tipp_ieFilter" model="[notShow: true, dataAccess:'search', simple:'true']"/>

<h3 class="ui icon header la-clear-before">
    <ui:bubble count="${num_tipp_rows}" grey="true"/> <g:message code="title.found.result"/>
</h3>

<g:if test="${params.containsKey('filterSet')}">
    <div class="ui form">
        <div class="three wide fields">
            <div class="field">
                <laser:render template="/templates/titles/sorting_dropdown" model="${[sd_type: 2, sd_journalsOnly: journalsOnly, sd_sort: params.sort, sd_order: params.order]}" />
            </div>
        </div>
    </div>
    <div class="ui grid">
        <div class="row">
            <div class="column">
                <laser:render template="/templates/tipps/title_list" model="[tmplConfigShow: tmplConfigShow, tipps: titlesList, fixedSubscription: subscription]"/>
            </div>
        </div>
    </div>
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${num_tipp_rows}"/>
</g:if>
<g:else>
    <ui:msg class="info" showIcon="true" message="title.search.notice"/>
</g:else>

<laser:htmlEnd />

<%--

        <div class="two wide column">
            <g:if test="${showPackageLinking && editable && !isLinked}">
                <g:if test="${fixedSubscription}">
                    <a id="linkTitleToSubscription_${tipp.gokbId}" href="${createLink(action: 'linkTitleModal', controller: 'ajaxHtml', params: [tippID: tipp.gokbId, fixedSubscription: fixedSubscription.id, headerToken: "subscription.details.linkTitle.heading.title"])}" class="ui icon button"><g:message code="subscription.details.linkTitle.label.title"/></a>
                </g:if>
                <g:else>
                    <a id="linkTitleToSubscription_${tipp.gokbId}" href="${createLink(action: 'linkTitleModal', controller: 'ajaxHtml', params: [tippID: tipp.gokbId, headerToken: "subscription.details.linkTitle.heading.title"])}" class="ui icon button"><g:message code="subscription.details.linkTitle.label.title"/></a>
                </g:else>



                <laser:script file="${this.getGroovyPageFileName()}">
                    $('#linkTitleToSubscription_${tipp.gokbId}').on('click', function(e) {
                        e.preventDefault();

                        $.ajax({
                            url: $(this).attr('href')
                        }).done( function (data) {
                            $('.ui.dimmer.modals > #linkTitleModal').remove();
                            $('#dynamicModalContainer').empty().html(data);

                            $('#dynamicModalContainer .ui.modal').modal({
                               onShow: function () {
                                    r2d2.initDynamicUiStuff('#linkTitleModal');
                                    r2d2.initDynamicXEditableStuff('#linkTitleModal');
                                    $("html").css("cursor", "auto");
                                },
                                detachable: true,
                                autofocus: false,
                                transition: 'scale',
                                onApprove : function() {
                                    $(this).find('.ui.form').submit();
                                    return false;
                                }
                            }).modal('show');
                        })
                    });
                </laser:script>
            </g:if>
        </div>--%>