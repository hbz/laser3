<%@ page import="de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.config.ConfigMapper" %>

<footer class="ui inverted vertical footer segment la-footer" style="background: #425259;background: linear-gradient(90deg,rgba(66, 82, 89, 1) 0%, rgba(66, 82, 89, 1) 39%, rgba(245, 237, 234, 1) 100%);">
    <div class="ui container" >

       <div class="ui stackable inverted divided equal height stackable grid center aligned">
            <div class="twelve wide column left aligned">
                <div class="sixteen wide column left aligned">
                    <div class="ui horizontal inverted divided link list">
                        <div class="item">
                            <a class="content" href="mailto:laser@hbz-nrw.de">${message(code: 'landingpage.footer.1.link1')}</a>
                        </div>
                        <div class="item">
                            <a target="_blank" class="content" href="https://www.hbz-nrw.de/impressum">${message(code: 'landingpage.footer.1.link3')}</a>
                        </div>
                        <div class="item">
                            <a target="_blank" class="content" href="https://www.hbz-nrw.de/datenschutz">${message(code:'dse')}</a>
                        </div>
                        <div class="item">
                            <a target="_blank" class="content" href="https://www.hbz-nrw.de/barrierefreiheit">
                                ${message(code: 'landingpage.footer.4.link1')}
                            </a>
                        </div>
                        <g:if test="${ConfigMapper.getLaserSystemId() == 'LAS:eR-Productive' || ConfigMapper.getLaserSystemId() == 'local'}">
                            <div class="item">
                                <g:link controller="public" action="wcagFeedbackForm" class="content">
                                    ${message(code: 'landingpage.footer.4.link2')}
                                </g:link>
                            </div>
                        </g:if>
                    </div>
                </div>
                <p>
                    ©<span id="spanYear">${java.time.Year.now()}</span>
                    Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz) &#8231; Jülicher Straße 6 &#8231; 50674 Köln +49 221 400 75-0
                </p>
                <em>powered by</em>
                <g:link controller="home" aria-label="${message(code:'default.home.label')}" action="index" class="ui inverted icon button">
                    <i class="icon la-laser"></i>
                    LAS:eR
                    <i class="arrow right icon"></i>
                </g:link>
            </div>



            <div class="four wide column left aligned" style="box-shadow: -1px 0 0 0 rgba(34, 36, 38, .15);">
                <g:link  controller="home" action="index" class="ui red right floated button">
                    Zum LAS:eR-System
                    <i class="arrow right icon"></i>
                </g:link>
            </div>

        </div>
    </div>
</footer>
<style>
/** inline style here with intention:
    flex layout helps footer to stick at bottom when main high not high enough
    but would make a bug when using the sidebars at all the other sides without the footer
 */
.pusher {
    flex: 1;
}
body {
    display: flex;
    min-height: 100vh;
    flex-direction: column;
}

main {
    flex: 1;
    display: flex!important;
    flex-direction: column;
}


.pushable>.pusher {
    overflow-y: visible!important;
    overflow-x: visible!important;
    min-height: auto!important;
}

</style>