<%@ page import="de.laser.storage.RDStore;" %>
<ui:modal id="modalAllTitleInfos" text="${message(code: 'title.details')}"
             hideSubmitButton="true">

    <laser:render template="/templates/title_long"
              model="${[ie: ie, tipp: tipp,
                        showPackage: showPackage, showPlattform: showPlattform, showCompact: showCompact, showEmptyFields: showEmptyFields]}"/>
    <br>
    <br>

    <g:if test="${ie}">

        <g:if test="${ie.tipp.titleType == 'Book'}">
            <div class="la-title">${message(code: 'tipp.print')} & ${message(code: 'tipp.online')}</div>
        </g:if>
        <g:elseif test="${ie.tipp.titleType == "Journal"}">
            <div class="la-title">${message(code: 'tipp.coverage')}</div>
        </g:elseif>
        <g:else>
            <div class="la-title">${message(code: 'tipp.online')}</div>
        </g:else>

        <div class="la-icon-list">
            <laser:render template="/templates/tipps/coverages" model="${[ie: ie, tipp: ie.tipp]}"/>
        </div>


        <br/>

        <div class="la-title">${message(code: 'subscription.details.access_dates')}</div>

        <div class="la-icon-list">
            <div class="item">
                <i class="grey calendar icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'subscription.details.access_start')}"></i>
                <g:if test="${editable}">
                    <ui:xEditable owner="${ie}" type="date"
                                     field="accessStartDate"/>
                    <i class="grey question circle icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                </g:if>
                <g:else>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${ie.accessStartDate}"/>
                </g:else>
            </div>

            <div class="item">
                <i class="grey calendar icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'subscription.details.access_end')}"></i>
                <g:if test="${editable}">
                    <ui:xEditable owner="${ie}" type="date" field="accessEndDate"/>
                    <i class="grey question circle icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                </g:if>
                <g:else>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${ie.accessEndDate}"/>
                </g:else>
            </div>
        </div>

        <br/>

        <div class="la-title">${message(code: 'subscription.details.prices')}</div>

        <div class="la-icon-list">
            <g:if test="${ie.priceItems}">
                <div class="ui cards">
                    <g:each in="${ie.priceItems}" var="priceItem" status="i">
                        <div class="item">
                            <div class="ui card">
                                <div class="content">
                                    <div class="la-card-column">
                                        <g:message code="tipp.price.listPrice"/>:
                                        <ui:xEditable field="listPrice"
                                                         owner="${priceItem}"/> <ui:xEditableRefData
                                                field="listCurrency" owner="${priceItem}" config="Currency"/>

                                        <br/>
                                        <g:message code="tipp.price.localPrice"/>: <ui:xEditable field="localPrice"
                                                                                                    owner="${priceItem}"/> <ui:xEditableRefData
                                                field="localCurrency" owner="${priceItem}" config="Currency"/>
                                        <%--<br/>
                                        (<g:message code="tipp.price.startDate"/> <ui:xEditable field="startDate"
                                                                                                  type="date"
                                                                                                  owner="${priceItem}"/>-
                                        <g:message code="tipp.price.endDate"/> <ui:xEditable field="endDate"
                                                                                               type="date"
                                                                                               owner="${priceItem}"/>)--%>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </g:each>
                </div>
            </g:if>
        </div>
        <br/>

        <div class="la-title">${message(code: 'subscription.details.ieGroups')}</div>

        <div class="la-icon-list">
            <g:if test="${ie.ieGroups}">
                <g:each in="${ie.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                    <div class="item">
                        <i class="grey icon object group la-popup-tooltip la-delay"
                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i>

                        <div class="content">
                            <g:link controller="subscription" action="index"
                                    id="${ie.subscription.id}"
                                    params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                        </div>
                    </div>
                </g:each>
            </g:if>
        </div>
    </g:if>

    <g:if test="${!ie && tipp}">
        <g:if test="${(tipp.titleType == 'Book')}">
            <div class="la-title">${message(code: 'tipp.print')} & ${message(code: 'tipp.online')}</div>
        </g:if>
        <g:elseif test="${tipp.titleType == "Journal"}">
            <div class="la-title">${message(code: 'tipp.coverage')}</div>
        </g:elseif>
        <g:else>
            <div class="la-title">${message(code: 'tipp.online')}</div>
        </g:else>

        <div class="la-icon-list">
            <laser:render template="/templates/tipps/coverages" model="${[ie: null, tipp: tipp]}"/>
        </div>
        <br/>

        <div class="la-title">${message(code: 'tipp.access_dates')}</div>

        <div class="la-icon-list">
            <div class="item">
                <i class="grey clipboard check clip icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.accessStartDate.tooltip')}"></i>

                <div class="content">
                    ${message(code: 'tipp.accessStartDate')}: <g:formatDate date="${tipp.accessStartDate}"
                                                                            format="${message(code: 'default.date.format.notime')}"/>
                </div>
            </div>

            <div class="item">
                <i class="grey clipboard check clip icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.accessEndDate.tooltip')}"></i>

                <div class="content">
                    ${message(code: 'tipp.accessEndDate')}: <g:formatDate date="${tipp.accessEndDate}"
                                                                          format="${message(code: 'default.date.format.notime')}"/>
                </div>
            </div>
        </div>

        <br/>

        <div class="la-title"><g:message code="tipp.price.plural"/></div>

        <div class="la-icon-list">
            <div class="ui cards">
                <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                    <div class="item">
                        <div class="ui card">
                            <div class="content">
                                <div class="la-card-column">
                                    <g:message code="tipp.price.listPrice"/>: <ui:xEditable field="listPrice"
                                                                                               owner="${priceItem}"
                                                                                               overwriteEditable="false"/> <ui:xEditableRefData
                                            field="listCurrency" owner="${priceItem}"
                                            config="Currency"
                                            overwriteEditable="false"/>
                                    <br />
                                    <%--(<g:message code="tipp.price.startDate"/> <ui:xEditable field="startDate"
                                                                                              type="date"
                                                                                              owner="${priceItem}"
                                                                                              overwriteEditable="false"/>-
                                    <g:message code="tipp.price.endDate"/> <ui:xEditable field="endDate"
                                                                                           type="date"
                                                                                           owner="${priceItem}"
                                                                                           overwriteEditable="false"/>)--%>
                                </div>
                            </div>
                        </div>
                    </div>

                </g:each>
            </div>
        </div>
    </g:if>


    <br>
    <br>

</ui:modal>
