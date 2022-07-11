<%@ page import="de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.Platform; de.laser.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="subscription.details.linkPackage.heading" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}"
                 text="${subscription.name}"/>
    <ui:crumb class="active"
                 text="${message(code: 'subscription.details.linkPackage.heading')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${subscription.name}" />
<br>
<br>

<h2 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'subscription.details.linkPackage.heading')}</h2>
<br>
<br>

<h3 class="ui left floated aligned icon header la-clear-before">${message(code: 'package.plural')}
<ui:totalNumber total="${recordsCount}"/>
</h3>


<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter"/>
</g:if>

<ui:messages data="${flash}"/>

<div class="ui icon message" id="durationAlert" style="display: none">
    <i class="notched circle loading icon"></i>

    <div class="content">
        <div class="header">
            <g:message code="globalDataSync.requestProcessing"/>
        </div>
        <g:message code="globalDataSync.requestProcessingInfo"/>

    </div>
</div>



<g:if test="${records}">

    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <g:sortableColumn property="name"
                              title="${message(code: 'package.show.pkg_name')}"
                              params="${params}"/>
            <g:sortableColumn property="titleCount"
                              title="${message(code: 'package.compare.overview.tipps')}"
                              params="${params}"/>
            <g:sortableColumn property="providerName" title="${message(code: 'package.content_provider')}"
                              params="${params}"/>
            <g:sortableColumn property="nominalPlatformName"
                              title="${message(code: 'platform.label')}"
                              params="${params}"/>
            <th>${message(code: 'package.curatoryGroup.label')}</th>
            <th>${message(code: 'package.source.label')}</th>
            <g:sortableColumn property="lastUpdatedDisplay"
                              title="${message(code: 'package.lastUpdated.label')}"
                              params="${params}"/>
            <th>${message(code: 'default.action.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${records}" var="record" status="jj">
            <tr>
                <g:set var="pkg" value="${Package.findByGokbId(record.uuid)}"/>
                <g:set var="org" value="${Org.findByGokbId(record.providerUuid)}"/>
                <g:set var="plat" value="${Platform.findByGokbId(record.nominalPlatformUuid)}"/>
                <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                <td>
                <%--UUID: ${record.uuid} --%>
                <%--Package: ${Package.findByGokbId(record.uuid)} --%>
                    <g:if test="${pkg}">
                        <g:link controller="package" action="show"
                                id="${pkg.id}">${record.name}</g:link>
                    </g:if>
                    <g:else>
                        ${record.name} <a target="_blank"
                                          href="${editUrl ? editUrl + '/public/packageContent/?id=' + record.uuid : '#'}"><i
                                title="we:kb Link" class="external alternate icon"></i></a>
                    </g:else>
                </td>
                <td>
                    <g:if test="${record.titleCount}">
                        ${record.titleCount}
                    </g:if>
                    <g:else>
                        0
                    </g:else>
                </td>
                <td><g:if test="${org}"><g:link
                        controller="organisation" action="show"
                        id="${org.id}">${record.providerName}</g:link></g:if>
                <g:else>${record.providerName}</g:else>
                </td>
                <td><g:if test="${plat}"><g:link
                        controller="platform" action="show"
                        id="${plat.id}">${record.nominalPlatformName}</g:link></g:if>
                    <g:else>${record.nominalPlatformName}</g:else></td>
                <td>
                    <div class="ui bulleted list">
                        <g:each in="${record.curatoryGroups}" var="curatoryGroup">
                            <div class="item"><g:link url="${editUrl.endsWith('/') ? editUrl : editUrl+'/'}resource/show/${curatoryGroup.curatoryGroup}">${curatoryGroup.name}</g:link></div>
                        </g:each>
                    </div>
                </td>
                <td>
                    <g:if test="${record.source?.automaticUpdates}">
                        <g:message code="package.index.result.automaticUpdates"/>
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${record.source.frequency}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                    <g:else>
                        <g:message code="package.index.result.noAutomaticUpdates"/>
                    </g:else>
                </td>
                <td>
                    <g:if test="${record.lastUpdatedDisplay}">
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${DateUtils.parseDateGeneric(record.lastUpdatedDisplay)}"/>
                    </g:if>
                </td>
                <td class="right aligned">
                    <g:if test="${editable && (!pkgs || !(record.uuid in pkgs))}">
                        <g:set var="disabled" value="${bulkProcessRunning ? 'disabled' : ''}" />
                        <button type="button" class="ui icon button la-popup-tooltip la-delay ${disabled}"
                                data-addUUID="${record.uuid}"
                                data-packageName="${record.name}"
                                data-ui="modal"
                                data-href="#linkPackageModal"
                                data-content="${message(code: 'subscription.details.linkPackage.button', args: [record.name])}"><g:message
                                code="subscription.details.linkPackage.label"/></button>

                    </g:if>
                %{--<g:else>
                    <g:set var="hasCostItems"
                           value="${CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.pkg.gokbId = :record and ci.subPkg.subscription = :sub', [record: record.uuid, sub: subscription])}"/>
                    <g:if test="${editable && !hasCostItems}">
                        <div class="ui icon negative buttons">
                            <button class="ui button la-selectable-button"
                                    onclick="JSPC.app.unlinkPackage(${Package.findByGokbId(record.uuid)?.id})">
                                <i class="unlink icon"></i>
                            </button>
                        </div>
                    </g:if>
                    <g:elseif test="${editable && hasCostItems}">
                        <div class="ui icon negative buttons la-popup-tooltip"
                             data-content="${message(code: 'subscription.delete.existingCostItems')}">
                            <button class="ui disabled button la-selectable-button">
                                <i class="unlink icon"></i>
                            </button>
                        </div>
                    </g:elseif>
                    <br/>
                </g:else>--}%
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>


    <ui:paginate action="linkPackage" controller="subscription" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${recordsCount}"/>

</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:else>
</g:else>




<div id="magicArea"></div>

<ui:modal contentClass="scrolling" id="linkPackageModal" message="myinst.currentSubscriptions.link_pkg"
             msgSave="${message(code: 'default.button.link.label')}">

    <g:form class="ui form" id="linkPackageForm" url="[controller: 'subscription', action: 'processLinkPackage', id: params.id]">
        <input type="hidden" name="addUUID" value=""/>
        <div class="field">
            <label for="pkgName">${message(code: 'package.label')}</label>
            <input type="text" id="pkgName" name="pkgName" value="" readonly/>
        </div>
        <div class="ui divided grid">
            <g:set var="colCount" value="${institution.getCustomerType() == 'ORG_CONSORTIUM' ? 'eight' : 'sixteen'}"/>
            <div class="${colCount} wide column">
                <div class="grouped required fields">
                    <label for="With">${message(code: 'subscription.details.linkPackage.label')}</label>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input type="radio" name="addType" id="With" value="With" tabindex="0" class="hidden">
                            <label>${message(code: 'subscription.details.link.with_ents')}</label>
                        </div>
                    </div>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input type="radio" name="addType" id="Without" value="Without" tabindex="0" class="hidden">
                            <label>${message(code: 'subscription.details.link.no_ents')}</label>
                        </div>
                    </div>
                </div>

                <br>
                <br>

                <div class="field">
                    <h5 class="ui dividing header">
                        <g:message code="subscription.packages.config.label" args="${[""]}"/>
                    </h5>

                    <table class="ui table compact la-table-height53px">
                        <tr>
                            <th class="control-label"><g:message code="subscription.packages.changeType.label"/></th>
                            <th class="control-label">
                                <g:message code="subscription.packages.setting.label"/>
                            </th>
                            <th class="control-label la-popup-tooltip la-delay"
                                data-content="${message(code: "subscription.packages.notification.label")}">
                                <i class="ui large icon bullhorn"></i>
                            </th>
                        </tr>
                        <g:set var="excludes"
                               value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                         PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                        <g:each in="${PendingChangeConfiguration.SETTING_KEYS}" var="settingKey">
                            <tr>
                                <td class="control-label">
                                    <g:message code="subscription.packages.${settingKey}"/>
                                </td>
                                <td>
                                    <g:if test="${!(settingKey in excludes)}">
                                        <g:if test="${editable}">
                                            <ui:select class="ui dropdown"
                                                          name="${settingKey}!§!setting"
                                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)}"
                                                          optionKey="id" optionValue="value"
                                                          value="${RDStore.PENDING_CHANGE_CONFIG_PROMPT.id}"/>
                                        </g:if>
                                        <g:else>
                                            ${RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n("value")}
                                        </g:else>
                                    </g:if>
                                </td>
                                <td>
                                    <g:if test="${editable}">
                                        <g:checkBox class="ui checkbox" name="${settingKey}!§!notification"
                                                    checked="${false}"/>
                                    </g:if>
                                    <g:else>
                                        ${RDStore.YN_NO.getI10n("value")}
                                    </g:else>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
                <div class="inline field">
                    <label for="freezeHolding"><g:message code="subscription.packages.freezeHolding"/> <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.packages.freezeHolding.expl')}"><i class="ui question circle icon"></i></span></label>
                    <g:checkBox class="ui checkbox" name="freezeHolding" checked="${false}"/>
                </div>
            </div>
            <g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM'}">
                <div class="${colCount} wide column">
                    <div class="grouped fields">
                        <label for="WithForChildren">${message(code: 'subscription.details.linkPackage.children.label')}</label>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input type="radio" name="addTypeChildren" id="WithForChildren" value="WithForChildren" tabindex="0" class="hidden">
                                <label>${message(code: 'subscription.details.link.with_ents')}</label>
                            </div>
                        </div>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input type="radio" name="addTypeChildren" id="WithoutForChildren" value="WithoutForChildren" tabindex="0" class="hidden">
                                <label>${message(code: 'subscription.details.link.no_ents')}</label>
                            </div>
                        </div>
                    </div>

                    <br>
                    <br>

                    <div class="field">
                        <h5 class="ui dividing header">
                            <g:message code="subscription.packages.config.children.label" args="${[""]}"/>
                        </h5>

                        <table class="ui table compact la-table-height53px">
                            <tr>
                                <th class="control-label la-popup-tooltip la-delay" data-contet="${message(code: "subscription.packages.auditable")}">
                                    <i class="ui large icon thumbtack"></i>
                                </th>
                                <th class="control-label la-popup-tooltip la-delay" data-content="${message(code: "subscription.packages.notification.auditable")}">
                                    <i class="ui large icon bullhorn"></i>
                                </th>
                            </tr>
                            <g:set var="excludes"
                                   value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                             PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                            <g:each in="${PendingChangeConfiguration.SETTING_KEYS}" var="settingKey">
                                <tr>
                                    <td>
                                        <g:if test="${!(settingKey in excludes)}">
                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!auditable"
                                                        checked="${false}"/>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${editable}">
                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!notificationAudit"
                                                        checked="${false}"/>
                                        </g:if>
                                        <g:else>
                                            ${RDStore.YN_NO.getI10n("value")}
                                        </g:else>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div class="inline field">
                        <label for="freezeHoldingAudit"><g:message code="subscription.packages.freezeHolding"/> <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.packages.freezeHolding.expl')}"><i class="ui question circle icon"></i></span></label>
                        <g:checkBox class="ui checkbox" name="freezeHoldingAudit" checked="${false}"/>
                    </div>
                </div>
            </g:if>
        </div>

    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.callbacks.modal.show.linkPackageModal = function(trigger) {
            $('#linkPackageModal #pkgName').attr('value', $(trigger).attr('data-packageName'))
            $('#linkPackageModal input[name=addUUID]').attr('value', $(trigger).attr('data-addUUID'))
        }

        $('#linkPackageForm').submit(function(e){
                e.preventDefault();
                if($('#With').prop('checked') == false && $('#Without').prop('checked') == false) {
                    alert("${message(code:'subscription.details.linkPackage.error.withORWithoutIEs')}");
                }else{
                    $('#linkPackageForm').unbind('submit').submit();
                }
                });
    </laser:script>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
%{--    JSPC.app.unlinkPackage = function (pkg_id){
      var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage', params: [subscription: subscription.id])}&package="+pkg_id

        $.ajax({url: req_url,
          success: function(result){
             $("#unlinkPackageModal").remove();
             $('#magicArea').html(result);
          },
          complete: function(){
            $("#unlinkPackageModal").modal("show");
          }
        });
      }--}%
    JSPC.app.toggleAlert = function() {
      $('#durationAlert').toggle();
    }


      $(".packageLink").click(function(evt) {
          evt.preventDefault();

          var check = confirm('${message(code: 'subscription.details.link.with_ents.confirm')}');
            console.log(check)
            if (check == true) {
                JSPC.app.toggleAlert();
                window.open($(this).attr('href'), "_self");
            }
        });

        $(".packageLinkWithoutIE").click(function(evt) {
            evt.preventDefault();

            var check = confirm('${message(code: 'subscription.details.link.no_ents.confirm')}');
            console.log(check)
            if (check == true) {
                JSPC.app.toggleAlert();
                window.open($(this).attr('href'), "_self");
            }
        });
</laser:script>

<laser:htmlEnd />
