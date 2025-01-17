<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.remote.Wekb" %>
<laser:serviceInjection />

<g:if test="${tmplView == 'info' && wekbNews.counts.all > 0}">

    <div id="wekb-menu">
        <div id="wekb-menu-content">

            <div class="ui fluid card">
                <div class="content">
                    <div class="header">${message(code: 'marker.WEKB_CHANGES')}</div>
                </div>

            <div class="content" style="padding-bottom: 0">
            <div class="ui grid">
%{--                <div class="one wide column"></div>--}%
%{--                <div class="fourteen wide column" style="padding-top:0; padding-bottom:0">--}%
                <div class="ten wide column">

                    <table class="ui basic compact table" style="border: none">
%{--                        <thead>--}%
%{--                        <tr>--}%
%{--                            <th>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,all">--}%
%{--                                    <i class="${Icon.PROVIDER}"></i> &nbsp; ${message(code: 'provider.label')}--}%%{--: ${wekbNews.provider.count}--}%
%{--                                </a>--}%
%{--                            </th>--}%
%{--                            <th>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,all">--}%
%{--                                    <i class="${Icon.VENDOR}"></i> &nbsp; ${message(code: 'vendor.plural')}--}%%{--: ${wekbNews.vendor.count}--}%
%{--                                </a>--}%
%{--                            </th>--}%
%{--                            <th>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">--}%
%{--                                    <i class="${Icon.PLATFORM}"></i> &nbsp; ${message(code: 'platform.plural')}--}%%{--: ${wekbNews.platform.count}--}%
%{--                                </a>--}%
%{--                            </th>--}%
%{--                            <th>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,all">--}%
%{--                                    <i class="${Icon.PACKAGE}"></i> &nbsp; ${message(code: 'package.plural')}--}%%{--: ${wekbNews.package.count}--}%
%{--                                </a>--}%
%{--                            </th>--}%
%{--                        </tr>--}%
%{--                        </thead>--}%
                        <thead>
                            <tr>
                                <th>Betrifft</th>
                                <th>Neu</th>
                                <th>Geändert</th>
                                <th>Gelöscht</th>
                                <th>
                                    <span class="la-popup-tooltip" data-content="${message(code: 'menu.my')}" data-position="top right"><i class="${Icon.SIG.MY_OBJECT}"></i></span>
                                </th>
                                <th>
                                    <span class="la-popup-tooltip" data-content="${message(code: 'marker.label')}" data-position="top right"><i class="${Icon.MARKER}"></i></span>
                                </th>
%{--                                <th></th>--}%
                            </tr>
                        </thead>
                        <tbody>
%{--                        <tr style="border-bottom: none">--}%
%{--                            <td></td>--}%
%{--                            <td>Neu</td>--}%
%{--                            <td>Geändert</td>--}%
%{--                            <td>Gelöscht</td>--}%
%{--                            <td><i class="${Icon.SIG.MY_OBJECT}"></i></td>--}%
%{--                            <td><i class="${Icon.MARKER}"></i></td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,all">--}%
%{--                                    <i class="${Icon.PROVIDER} la-list-icon"></i> ${message(code: 'provider.label')}--}%%{--: ${wekbNews.provider.count}--}%
%{--                                </a>--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,all">--}%
%{--                                    <i class="${Icon.VENDOR} la-list-icon"></i> ${message(code: 'vendor.plural')}--}%%{--: ${wekbNews.vendor.count}--}%
%{--                                </a>--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">--}%
%{--                                    <i class="${Icon.PLATFORM} la-list-icon"></i> ${message(code: 'platform.plural')}--}%%{--: ${wekbNews.platform.count}--}%
%{--                                </a>--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,all">--}%
%{--                                    <i class="${Icon.PACKAGE} la-list-icon"></i> ${message(code: 'package.plural')}--}%%{--: ${wekbNews.package.count}--}%
%{--                                </a>--}%
%{--                            </td>--}%
%{--                        </tr>--}%
%{--                        <tr style="border-bottom: none">--}%
%{--                            <td>--}%
%{--                                Neu <br />--}%
%{--                                Geändert <br />--}%
%{--                                Gelöscht <br />--}%
%{--                                <i class="${Icon.SIG.MY_OBJECT}"></i> <br />--}%
%{--                                <i class="${Icon.MARKER}"></i> <br />--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,created">${wekbNews.provider.created.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,updated">${wekbNews.provider.countUpdated}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,deleted">${wekbNews.provider.deleted.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,my">${wekbNews.provider.my.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,marker">${wekbNews.provider.marker.size()}</a>--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,created">${wekbNews.vendor.created.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,updated">${wekbNews.vendor.countUpdated}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,deleted">${wekbNews.vendor.deleted.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,my">${wekbNews.vendor.my.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,marker">${wekbNews.vendor.marker.size()}</a>--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,created">${wekbNews.platform.created.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,updated">${wekbNews.platform.countUpdated}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,deleted">${wekbNews.platform.deleted.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,my">${wekbNews.platform.my.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,marker">${wekbNews.platform.marker.size()}</a>--}%
%{--                            </td>--}%
%{--                            <td>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,created">${wekbNews.package.created.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,updated">${wekbNews.package.countUpdated}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,deleted">${wekbNews.package.deleted.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,my">${wekbNews.package.my.size()}</a> <br/>--}%
%{--                                <a href="#" class="wekb-flyout-trigger" data-preset="package,marker">${wekbNews.package.marker.size()}</a>--}%
%{--                            </td>--}%

                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,all">
                                    <i class="${Icon.PROVIDER} la-list-icon"></i> ${message(code: 'provider.label')}%{--: ${wekbNews.provider.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.created.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,created">${wekbNews.provider.created.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.countUpdated}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,updated">${wekbNews.provider.countUpdated}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.deleted.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,deleted">${wekbNews.provider.deleted.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.my.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,my">${wekbNews.provider.my.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.marker.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,marker">${wekbNews.provider.marker.size()}</a>
                                </g:if>
                            </td>
%{--                            <td rowspan="4">--}%
%{--                                <div class="ui vertical secondary menu right floated">--}%
%{--                                    <a href="#" id="wekb-menu-trigger" class="ui item la-popup-tooltip"--}%
%{--                                       data-content="Alle Änderungen anzeigen" data-position="top right">--}%
%{--                                        <i class="${Icon.WEKB} blue"></i> We:kb-News--}%
%{--                                    </a>--}%
%{--                                    <a href="#" class="ui icon item la-popup-tooltip wekb-flyout-trigger" data-preset="all,my"--}%
%{--                                       data-content="${message(code: 'menu.my')}" data-position="top right">--}%
%{--                                        <i class="${Icon.SIG.MY_OBJECT} yellow"></i> ${wekbNews.counts.my}--}%
%{--                                    </a>--}%
%{--                                    <a href="#" class="ui icon item la-popup-tooltip wekb-flyout-trigger" data-preset="all,marker"--}%
%{--                                       data-content="${message(code: 'marker.label')}" data-position="top right">--}%
%{--                                        <i class="${Icon.MARKER} purple"></i> ${wekbNews.counts.marker}--}%
%{--                                    </a>--}%
%{--                                </div>--}%
%{--                            </td>--}%
                        </tr>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,all">
                                    <i class="${Icon.VENDOR} la-list-icon"></i> ${message(code: 'vendor.plural')}%{--: ${wekbNews.vendor.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.created.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,created">${wekbNews.vendor.created.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.countUpdated}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,updated">${wekbNews.vendor.countUpdated}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.deleted.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,deleted">${wekbNews.vendor.deleted.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.my.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,my">${wekbNews.vendor.my.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.marker.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,marker">${wekbNews.vendor.marker.size()}</a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">
                                    <i class="${Icon.PLATFORM} la-list-icon"></i> ${message(code: 'platform.plural')}%{--: ${wekbNews.platform.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.created.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,created">${wekbNews.platform.created.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.countUpdated}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,updated">${wekbNews.platform.countUpdated}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.deleted.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,deleted">${wekbNews.platform.deleted.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.my.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,my">${wekbNews.platform.my.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.marker.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,marker">${wekbNews.platform.marker.size()}</a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="package,all">
                                    <i class="${Icon.PACKAGE} la-list-icon"></i> ${message(code: 'package.plural')}%{--: ${wekbNews.package.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.created.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,created">${wekbNews.package.created.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.countUpdated}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,updated">${wekbNews.package.countUpdated}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.deleted.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,deleted">${wekbNews.package.deleted.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.my.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,my">${wekbNews.package.my.size()}</a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.marker.size()}">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,marker">${wekbNews.package.marker.size()}</a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
%{--                        <td>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="provider,all">--}%
%{--                                <i class="${Icon.PROVIDER} la-list-icon"></i> ${message(code: 'provider.label')}--}%%{--: ${wekbNews.provider.count}--}%
%{--                            </a>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="provider,created">Neu: ${wekbNews.provider.created.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="provider,updated">Geändert: ${wekbNews.provider.countUpdated}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="provider,deleted">Gelöscht: ${wekbNews.provider.deleted.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="provider,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.provider.my.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="provider,marker"><i class="${Icon.MARKER}"></i>${wekbNews.provider.marker.size()}</a>--}%
%{--                        </td>--}%
%{--                        <td>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="vendor,all">--}%
%{--                                <i class="${Icon.VENDOR} la-list-icon"></i> ${message(code: 'vendor.plural')}--}%%{--: ${wekbNews.vendor.count}--}%
%{--                            </a>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="vendor,created">Neu: ${wekbNews.vendor.created.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="vendor,updated">Geändert: ${wekbNews.vendor.countUpdated}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="vendor,deleted">Gelöscht: ${wekbNews.vendor.deleted.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="vendor,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.vendor.my.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="vendor,marker"><i class="${Icon.MARKER}"></i>${wekbNews.vendor.marker.size()}</a>--}%
%{--                        </td>--}%
%{--                        <td>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">--}%
%{--                                <i class="${Icon.PLATFORM} la-list-icon"></i> ${message(code: 'platform.plural')}--}%%{--: ${wekbNews.platform.count}--}%
%{--                            </a>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="platform,created">Neu: ${wekbNews.platform.created.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="platform,updated">Geändert: ${wekbNews.platform.countUpdated}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="platform,deleted">Gelöscht: ${wekbNews.platform.deleted.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="platform,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.platform.my.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="platform,marker"><i class="${Icon.MARKER}"></i>${wekbNews.platform.marker.size()}</a>--}%
%{--                        </td>--}%
%{--                        <td>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="package,all">--}%
%{--                                <i class="${Icon.PACKAGE} la-list-icon"></i> ${message(code: 'package.plural')}--}%%{--: ${wekbNews.package.count}--}%
%{--                            </a>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="package,created">Neu: ${wekbNews.package.created.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="package,updated">Geändert: ${wekbNews.package.countUpdated}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="package,deleted">Gelöscht: ${wekbNews.package.deleted.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="package,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.package.my.size()}</a> <br/>--}%
%{--                            <a href="#" class="wekb-flyout-trigger" data-preset="package,marker"><i class="${Icon.MARKER}"></i>${wekbNews.package.marker.size()}</a>--}%
%{--                        </td>--}%
%{--                            <td>--}%
%{--                                <div class="ui vertical secondary menu right floated">--}%
%{--                                    <a href="#" id="wekb-menu-trigger" class="ui item la-popup-tooltip"--}%
%{--                                       data-content="Alle Änderungen anzeigen" data-position="top right">--}%
%{--                                        <i class="${Icon.WEKB} blue"></i> We:kb-News--}%
%{--                                    </a>--}%
%{--                                    <a href="#" class="ui icon item la-popup-tooltip wekb-flyout-trigger" data-preset="all,my"--}%
%{--                                       data-content="${message(code: 'menu.my')}" data-position="top right">--}%
%{--                                        <i class="${Icon.SIG.MY_OBJECT} yellow"></i> ${wekbNews.counts.my}--}%
%{--                                    </a>--}%
%{--                                    <a href="#" class="ui icon item la-popup-tooltip wekb-flyout-trigger" data-preset="all,marker"--}%
%{--                                       data-content="${message(code: 'marker.label')}" data-position="top right">--}%
%{--                                        <i class="${Icon.MARKER} purple"></i> ${wekbNews.counts.marker}--}%
%{--                                    </a>--}%
%{--                                </div>--}%
%{--                            </td>--}%
                        </tr>
                        </tbody>
                    </table>

                </div>
%{--                <div class="one wide column"></div>--}%
                <div class="six wide column">

                    <div class="ui vertical secondary menu right floated">
                        <a href="#" id="wekb-menu-trigger" class="ui item la-popup-tooltip"
                           data-content="Alle Änderungen anzeigen" data-position="top right">
                            <i class="${Icon.WEKB} blue"></i> We:kb-News
                        </a>
                        <a href="#" class="ui icon item la-popup-tooltip wekb-flyout-trigger" data-preset="all,my"
                           data-content="${message(code: 'menu.my')}" data-position="top right">
                            <i class="${Icon.SIG.MY_OBJECT} yellow"></i> ${wekbNews.counts.my}
                        </a>
                        <a href="#" class="ui icon item la-popup-tooltip wekb-flyout-trigger" data-preset="all,marker"
                           data-content="${message(code: 'marker.label')}" data-position="top right">
                            <i class="${Icon.MARKER} purple"></i> ${wekbNews.counts.marker}
                        </a>
                    </div>
                </div>
            </div><!-- .column -->

%{--                <div class="extra content">--}%
%{--                    <div class="right floated">--}%
%{--                        <div class="ui large labels">--}%
%{--                            <a href="#" id="wekb-menu-trigger" class="ui label"><i class="${Icon.WEKB} blue"></i>&nbsp;We:kb-News</a>--}%
%{--                            <a href="#" class="ui icon label la-popup-tooltip wekb-flyout-trigger" data-preset="all,my"--}%
%{--                               data-content="${message(code: 'menu.my')}" data-position="top right">--}%
%{--                                <i class="${Icon.SIG.MY_OBJECT} yellow"></i> ${wekbNews.counts.my}--}%
%{--                            </a>--}%
%{--                            <a href="#" class="ui icon label la-popup-tooltip wekb-flyout-trigger" data-preset="all,marker"--}%
%{--                               data-content="${message(code: 'marker.WEKB_CHANGES')}" data-position="top right">--}%
%{--                                <i class="${Icon.MARKER} purple"></i> ${wekbNews.counts.marker}--}%
%{--                            </a>--}%
%{--                        </div>--}%
%{--                    </div>--}%
%{--                </div>--}%
            </div>
        </div>
    </div>

    <div id="wekbFlyout" class="ui ten wide flyout"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('#wekb-menu-trigger').on ('click', function () {
            $('#wekb-menu-content').slideToggle (200);
        });

        if (!JSPC.app.dashboard) { JSPC.app.dashboard = {} }

        JSPC.app.dashboard.wekbNews = {
            preset: {
                obj: 'all', filter: 'all'
            },
            dataLoaded: false
        }

        $('a.wekb-flyout-trigger').on ('click', function(e) {
            e.preventDefault()
            let preset = $(this).attr ('data-preset').split(',')
            JSPC.app.dashboard.wekbNews.preset.obj = preset[0]
            JSPC.app.dashboard.wekbNews.preset.filter = preset[1]

            $('#globalLoadingIndicator').show()

            if (JSPC.app.dashboard.wekbNews.dataLoaded) {
                JSPC.app.dashboard.wekbNews.flyout (preset[0], preset[1])

                $('#wekbFlyout').flyout('show')
                $('#globalLoadingIndicator').hide()
            }
            else {
                $.ajax ({
                    url: "<g:createLink controller="ajaxHtml" action="wekbNewsFlyout"/>"
                }).done (function (response) {
                    JSPC.app.dashboard.wekbNews.dataLoaded = true

                    $('#wekbFlyout').html (response)
                    JSPC.app.dashboard.wekbNews.flyout (preset[0], preset[1])

                    $('#wekbFlyout').flyout('show')
                    $('#globalLoadingIndicator').hide()

                    r2d2.initDynamicUiStuff ('#wekbFlyout')
                    r2d2.initDynamicXEditableStuff ('#wekbFlyout')
                })
            }

        });

        JSPC.app.dashboard.wekbNews.flyout  = function(obj, filter) {
            console.log(obj,filter)

            if (! obj)    { obj    = JSPC.app.dashboard.wekbNews.preset.obj }     else { JSPC.app.dashboard.wekbNews.preset.obj = obj }
            if (! filter) { filter = JSPC.app.dashboard.wekbNews.preset.filter }  else { JSPC.app.dashboard.wekbNews.preset.filter = filter }

            $('#wekbFlyout .filterWrapper .button').removeClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-obj=' + obj + ']').addClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-filter=' + filter + ']').addClass ('active')

            let cats = '#wekbFlyout .dataWrapper'
            if (obj == 'all') {
                $(cats).removeClass('hidden')
            }
            else {
                $(cats).addClass('hidden')
                $('#wekbFlyout .dataWrapper[data-obj=' + obj + ']').removeClass('hidden')
            }

            let rows = '#wekbFlyout .dataWrapper .row'
            if (filter == 'all') {
                $(rows).removeClass('hidden')
            }
            else {
                $(rows).addClass('hidden')
                if (filter == 'created') {
                    $(rows + '[data-f1=created]').removeClass('hidden')
                }
                else if (filter == 'updated') {
                    $(rows + '[data-f1=updated]').removeClass('hidden')
                }
                else if (filter == 'deleted') {
                    $(rows + '[data-f1=deleted]').removeClass('hidden')
                }
                else if (filter == 'my') {
                    $(rows + '[data-f2=true]').removeClass('hidden')
                }
                else if (filter == 'marker') {
                    $(rows + '[data-f3=true]').removeClass('hidden')
                }
            }
        }
        </laser:script>

</g:if>

<g:if test="${tmplView == 'details'}">

    <%
        tmplConfig = [
                ['provider', wekbNews.provider,   'provider.label',     "${Icon.PROVIDER}",    'menu.my.providers'],
                ['vendor',   wekbNews.vendor,     'vendor.plural',      "${Icon.VENDOR}",      'menu.my.vendors'],
                ['platform', wekbNews.platform,   'platform.plural',    "${Icon.PLATFORM}",    'menu.my.platforms'],
                ['package',  wekbNews.package,    'package.plural',     "${Icon.PACKAGE}",     'menu.my.packages']
        ]
    %>

    <div class="filterWrapper" style="margin:2em">
        <p style="margin:0 2em 2em">
            Änderungen der letzten <strong>${wekbNews.query.days}</strong> Tage: <strong>${wekbNews.counts.all}</strong> Datensätze.<br />
            Letzter Datenabgleich: <strong>${wekbNews.query.call}</strong>
            <span style="float:right">
                <a href="${Wekb.getURL()}" target="_blank"><i class="${Icon.WEKB} large"></i></a>
            </span>
        </p>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <div class="ui buttons mini">
                <span class="${Btn.SIMPLE}" data-obj="provider">${message(code: 'provider.label')}: ${wekbNews.provider.count}</span>
                <span class="${Btn.SIMPLE}" data-obj="vendor">${message(code: 'vendor.plural')}: ${wekbNews.vendor.count}</span>
                <span class="${Btn.SIMPLE}" data-obj="platform">${message(code: 'platform.plural')}: ${wekbNews.platform.count}</span>
                <span class="${Btn.SIMPLE}" data-obj="package">${message(code: 'package.plural')}: ${wekbNews.package.count}</span>
                <span class="${Btn.SIMPLE_TOOLTIP} la-long-tooltip" data-obj="all"
                      data-content="Alle anzeigen: ${message(code: 'provider.label')}, ${message(code: 'vendor.plural')}, ${message(code: 'platform.plural')}, ${message(code: 'package.plural')}">Alle</span>
            </div>
        </div>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <div class="ui buttons mini">
                <span class="${Btn.SIMPLE}" data-filter="created">Neue Objekte: ${wekbNews.counts.created}</span>
                <span class="${Btn.SIMPLE}" data-filter="updated">Geänderte Objekte: ${wekbNews.counts.updated}</span>
                <span class="${Btn.SIMPLE}" data-filter="deleted">Gelöschte Objekte: ${wekbNews.counts.deleted}</span>
                <span class="${Btn.SIMPLE}" data-filter="my"><i class="${Icon.SIG.MY_OBJECT}"></i> ${wekbNews.counts.my}</span>
                <span class="${Btn.SIMPLE}" data-filter="marker"><i class="${Icon.MARKER}"></i> ${wekbNews.counts.marker}</span>
                <span class="${Btn.SIMPLE_TOOLTIP} la-long-tooltip" data-filter="all"
                      data-content="Alle anzeigen: Neue Objekte, Geänderte Objekte, Gelöschte Objekte, Meine Objekte, Meine Beobachtungsliste">Alle</span>
            </div>
        </div>
    </div>

    <style>
        .filterWrapper > .filter .button {
            color: #54575b;
            background-color: #d3dae3;
        }
        .filterWrapper > .filter .button:hover {
            background-color: #c3cad3;
        }
        .filterWrapper > .filter .button.active {
            color: #ffffff;
            background-color: #004678;
        }
        .dataWrapper .grid .row .column .label {
            margin-left: 1em;
        }
    </style>

    <g:each in="${tmplConfig}" var="cfg">
        <div class="dataWrapper" data-obj="${cfg[0]}" style="margin:2em">
            <div class="ui header">
                <i class="icon circular grey ${cfg[3]}"></i>
                <div class="content"> ${message(code: "${cfg[2]}")} </div>
            </div>
            <div class="ui vertically divided very compact grid" style="margin-top: 1.5em;">
                <g:each in="${cfg[1].all}" var="obj">
                    <g:set var="isObjCreated" value="${obj.uuid in cfg[1].created}" /> %{-- uuid --}%
                    <g:set var="isObjDeleted" value="${obj.uuid in cfg[1].deleted}" /> %{-- uuid --}%
                    <g:set var="isObjMy" value="${obj.id in cfg[1].my}" />  %{-- id --}%
                    <g:set var="isObjMarker" value="${obj.id in cfg[1].marker}" /> %{-- id --}%

                    <div class="row"
                         data-f1="${isObjCreated ? 'created' : (isObjDeleted ? 'deleted' : 'updated')}"
                         data-f2="${isObjMy ? 'true' : 'false'}"
                         data-f3="${isObjMarker ? 'true' : 'false'}"
                    >
                        <div class="column one wide center aligned">
                            <ui:wekbIconLink type="${cfg[0]}" gokbId="${obj.uuid}" />
                        </div>
                        <div class="column nine wide">
                            <g:if test="${obj.globalUID}">
                                <g:link controller="${cfg[0]}" action="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                            </g:if>
                            <g:else>
                                ${obj.name}
                            </g:else>
                            <g:if test="${isObjCreated}"><span class="ui green mini label">NEU</span></g:if>
                            <g:if test="${isObjDeleted}"><span class="ui red mini label">Gelöscht</span></g:if>
                        </div>
                        <div class="column two wide center aligned">
                            <g:if test="${isObjMy}">
                                <ui:myXIcon tooltip="${message(code: "${cfg[4]}")}" color="yellow"/>
                            </g:if>
                            <g:else>
                                <icon:placeholder />
                            </g:else>
                            <g:if test="${isObjMarker}">
                                <ui:markerIcon type="WEKB_CHANGES" color="purple" />
                            </g:if>
                            <g:else>
                                <icon:placeholder />
                            </g:else>
                        </div>
                        <div class="column four wide center aligned">${obj.lastUpdatedDisplay}</div>
                    </div>
                </g:each>
            </div>
        </div>
    </g:each>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#wekbFlyout .filterWrapper .button').on ('click', function(e) {
            JSPC.app.dashboard.wekbNews.flyout ($(this).attr('data-obj'), $(this).attr('data-filter'))
        });
    </laser:script>
</g:if>