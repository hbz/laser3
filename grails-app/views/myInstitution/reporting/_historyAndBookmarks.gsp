<%@page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.ReportingFilter;de.laser.reporting.export.GlobalExportHelper;de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.reporting.report.ReportingCache;" %>
<laser:serviceInjection/>

<g:if test="${filterHistory}">
    <div id="history-content" class="ui segment <g:if test="${! params.get('cmd').equals('addBookmark')}">hidden</g:if>">
        <span class="ui top attached label" style="border-radius: 0; text-align: center">
            <i class="icon history large"></i>${message(code:'reporting.ui.global.history')}
        </span>
        <div style="margin-top: 3em !important;">
            <table class="ui single line table compact">
                <g:each in="${filterHistory}" var="fh">
                    <g:set var="fhRCache" value="${new ReportingCache(ReportingCache.CTX_GLOBAL, fh.split('/').last() as String)}" />
                    <g:set var="meta" value="${fhRCache.readMeta()}" />
                    <g:set var="filterCache" value="${fhRCache.readFilterCache()}" />
                    <tr>
                        <td>
                            <g:link controller="myInstitution" action="reporting" class="${Btn.MODERN.SIMPLE} large reporting-callLink"
                                    params="${[filter: meta.filter /*, token: fhRCache.token*/ ] + filterCache.map}">
                                <i class="${BaseConfig.getIcon(meta.filter)}" aria-hidden="true"></i>
                            </g:link>
                        </td>
                        <td>
                            <div class="content">
                                <div class="header">
                                    <strong>${BaseConfig.getFilterLabel(meta.filter.toString())}</strong> - ${DateUtils.getSDF_onlyTime().format(meta.timestamp)}
                                </div>
                                <div class="description">
                                    <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: GlobalExportHelper.getCachedFilterLabels(fhRCache.token), simple: true]}" />
                                </div>
                                <div class="footer sc_grey">
                                    <%= filterCache.result %>
                                </div>
                            </div>
                        </td>
                        <td>
                            <g:if test="${ReportingFilter.findByToken(fhRCache.token)}">
                            %{--
                            <g:link controller="ajaxHtml" action="reporting" params="${[context: BaseConfig.KEY_MYINST, cmd: 'deleteBookmark', token: "${fhRCache.token}", tab: 'history']}"
                                    class="${Btn.MODERN.NEGATIVE} small right floated"><i class="${Icon.CMD.DELETE}"></i></g:link>
                                    --}%
                            </g:if>
                            <g:else>
                                <g:link controller="ajaxHtml" action="reporting" params="${[context: BaseConfig.KEY_MYINST, cmd: 'addBookmark', token: "${fhRCache.token}", tab: 'history']}"
                                        class="${Btn.MODERN.POSITIVE} small right floated"><i class="${Icon.CMD.ADD}"></i></g:link>
                            </g:else>
                        </td>
                    </tr>
                </g:each>
            </table>
        </div>
        <div style="margin-top: 1em">
            <g:link controller="ajaxHtml" action="reporting" params="${[context: BaseConfig.KEY_MYINST, cmd: 'deleteHistory']}"
                    elementId="history-delete" class="${Btn.SIMPLE}">${message(code:'reporting.ui.global.history.delete')}</g:link>
        </div>
    </div>
</g:if>
<g:if test="${bookmarks}">
    <div id="bookmark-content" class="ui segment <g:if test="${! params.get('cmd').equals('deleteBookmark')}">hidden</g:if>">
        <span class="ui top attached label" style="border-radius: 0; text-align: center">
            <i class="icon teal bookmark large"></i>${message(code:'reporting.ui.global.bookmarks')}
        </span>
        <div style="margin-top: 3em !important;">
            <table class="ui single line table compact">
                <g:each in="${bookmarks}" var="fav">
                    <tr>
                        <td>
                            <g:link controller="myInstitution" action="reporting" class="${Btn.MODERN.SIMPLE} large reporting-callLink"
                                params="${[filter: fav.filter /*, token: fhRCache.token*/ ] + fav.getParsedFilterMap()}">
                                <i class="${BaseConfig.getIcon(fav.filter)}" aria-hidden="true"></i>
                            </g:link>
                        </td>
                        <td>
                            <div class="content">
                                <div class="header">
                                    <strong><ui:xEditable owner="${fav}" field="title" overwriteEditable="true" /></strong>
                                    <g:if test="${fav.id == lastAddedBookmarkId}">
                                        <i id="last-added-bookmark" class="ui icon bookmark small teal"></i>
                                    </g:if>
                                </div>
                                <div class="description">
                                    <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: fav.getParsedLabels(), simple: true]}" />
                                </div>
                                <div class="footer">
                                    <ui:xEditable owner="${fav}" field="description" overwriteEditable="true" />
                                </div>
                            </div>
                        </td>
                        <td>
                            <g:link controller="ajaxHtml" action="reporting" params="${[context: BaseConfig.KEY_MYINST, cmd: 'deleteBookmark', token: "${fav.token}", tab: 'bookmark']}"
                                    class="${Btn.MODERN.NEGATIVE} small right floated"><i class="${Icon.CMD.DELETE}"></i></g:link>
                        </td>
                    </tr>
                </g:each>
            </table>
        </div>
    </div>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    r2d2.initDynamicXEditableStuff('#hab-wrapper');

    <g:if test="${filterHistory}">
        $('#history-toggle').removeClass('disabled');
    </g:if>
    <g:else>
        $('#history-toggle').addClass('disabled').removeClass('blue');
    </g:else>
    <g:if test="${bookmarks}">
        $('#bookmark-toggle').removeClass('disabled');
    </g:if>
    <g:else>
        $('#bookmark-toggle').addClass('disabled').removeClass('blue');
    </g:else>

    $('.reporting-callLink').on( 'click', function() {
        $('#globalLoadingIndicator').show();
    })
    $('#hab-wrapper a.positive, #hab-wrapper a.negative, #history-delete').on( 'click', function(e) {
        e.preventDefault();
        $('#hab-wrapper').load( $(this).attr('href'), function() {});
    })
</laser:script>