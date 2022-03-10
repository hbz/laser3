<%@ page import="de.laser.helper.ConfigUtils" %>
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
        }
        .pushable>.pusher {
            overflow-y: visible!important;
            overflow-x: visible!important;
            min-height: auto!important;
        }
        .la-js-verticalNavi {
            display: block!important;
        }

</style>
<footer class="ui inverted vertical footer segment la-footer">
    <div class="ui container">

       <div class="ui stackable inverted divided equal height stackable grid center aligned">

            <div class="five wide column left aligned" style="opacity: 0.7">
                <g:link controller="home" aria-label="${message(code:'default.home.label')}" action="index" class="header item la-logo-item">
                    <svg width="75.333344"
                            height="26.040001"
                            viewBox="0 0 75.33334 26.04">
                            <defs
                            id="defs6" /><g
                            transform="matrix(0.24321284,0,0,0.24321284,0.552501,0.9140142)"
                            style="fill:#ffffff"
                            id="g3388"><g
                                style="fill:#ffffff"
                                id="g3380"><path
                                    d="m 144.652,69.753601 0,-38.699999 7.45333,0 0,32.162932 12.556,0 0,6.537067 -20.00933,0"
                                    style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                    id="path14" /><path
                                    d="m 185.06,45.787868 c -0.74667,-2.121333 -1.32,-4.013467 -1.77734,-6.192133 l -0.0573,0 c -0.45867,2.0068 -1.09067,4.242666 -1.83467,6.478666 l -3.49733,9.861467 10.77867,0 z m 8.25466,23.965733 -2.69333,-7.913067 -14.62,0 -2.696,7.913067 -7.51067,0 13.532,-38.699999 8.42667,0 13.81733,38.699999 -8.256,0"
                                    style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                    id="path16" /><path
                                    d="m 212.68266,70.383201 c -2.752,0 -5.504,-0.285867 -7.912,-0.916667 l 0.45867,-6.936933 c 2.17733,0.7448 5.27466,1.376 8.084,1.376 3.784,0 6.70666,-1.547867 6.70666,-5.102533 0,-7.395867 -16.22533,-4.0136 -16.22533,-16.741733 0,-6.880133 5.44667,-11.639067 14.736,-11.639067 2.17867,0 4.70133,0.287067 6.99333,0.688534 l -0.4,6.536 c -2.17866,-0.631334 -4.58666,-1.032267 -6.93866,-1.032267 -4.29867,0 -6.47734,1.949467 -6.47734,4.701466 0,6.9376 16.28267,4.357334 16.28267,16.454267 0,7.2812 -5.79067,12.612933 -15.308,12.612933"
                                    style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                    id="path18" /><path
                                    d="m 242.89599,65.740534 c 0,2.5792 -2.12133,4.700533 -4.70133,4.700533 -2.52267,0 -4.644,-2.121333 -4.644,-4.700533 0,-2.5808 2.12133,-4.702133 4.644,-4.702133 2.58,0 4.70133,2.121333 4.70133,4.702133 z m 0,-16.225999 c 0,2.580133 -2.12133,4.700933 -4.70133,4.700933 -2.52267,0 -4.644,-2.1208 -4.644,-4.700933 0,-2.580267 2.12133,-4.701067 4.644,-4.701067 2.58,0 4.70133,2.1208 4.70133,4.701067"
                                    style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                    id="path20" /><path
                                    d="m 261.18399,46.017068 c -3.03733,0 -5.15866,2.236533 -5.56,5.676533 l 10.492,0 c 0.11467,-3.555733 -1.83466,-5.676533 -4.932,-5.676533 z m 11.984,10.664533 -17.65866,0 c -0.0573,5.3324 2.58,7.911467 7.85466,7.911467 2.80934,0 5.848,-0.630134 8.31334,-1.777067 l 0.688,5.6756 c -3.03867,1.204666 -6.65067,1.8348 -10.20534,1.8348 -9.05866,0 -14.104,-4.528533 -14.104,-14.5624 0,-8.7152 4.816,-15.136533 13.35867,-15.136533 8.31333,0 11.98267,5.676133 11.98267,12.728667 0,0.974 -0.0573,2.121333 -0.22934,3.325466"
                                    style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                    id="path22" /><path
                                    d="m 289.96533,36.900935 c -1.08934,0 -2.17734,0.05733 -2.924,0.171867 l 0,11.180799 c 0.51733,0.0572 1.548,0.114534 2.696,0.114534 4.47066,0 7.22266,-2.293734 7.22266,-5.905734 0,-3.726533 -2.292,-5.561466 -6.99466,-5.561466 z m 8.14266,32.852666 -10.77866,-15.9964 -0.288,0 0,15.9964 -7.45334,0 0,-38.699999 c 3.03867,-0.172 6.65067,-0.2292 11.12267,-0.2292 8.54267,0 14.04667,2.9812 14.04667,10.892133 0,5.733866 -4.01334,9.862533 -9.86,10.7792 1.088,1.433333 2.17733,2.924533 3.152,4.243199 l 9.28933,13.014667 -9.23067,0"
                                    style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                    id="path24" /></g><path
                                style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:none"
                                d="M 0,0.00390625 0,101.03906 l 101.0332,0 0,-101.03515375 -101.0332,0 z M 44.603516,5.0878906 C 59.878491,5.236396 74.290109,14.402313 80.380859,29.445312 88.464192,49.407313 78.834513,72.143228 58.873047,80.226562 41.336514,87.326961 21.662822,80.754864 11.660156,65.519531 21.678956,79.477797 40.272078,85.330456 56.902344,78.597656 76.40901,70.697656 85.818455,48.481942 77.919922,28.974609 71.779389,13.81011 56.985462,4.7515542 41.539062,5.1796875 42.562854,5.1084958 43.585184,5.0779903 44.603516,5.0878906 Z M 26.556641,12.056641 c 0.545233,0.0034 0.763331,0.244791 0.8125,0.421875 0.07867,0.283334 -0.377743,1.547315 -2.59961,2.978515 0.320267,5.639067 0.617777,8.15623 1.03711,11.351563 2.3276,-0.652667 4.118901,-0.379671 5.990234,0.914062 4.1416,-3.736933 6.074937,-5.213067 11.710937,-8.927734 -2.145333,-3.146266 1.99368,-5.360955 5.330079,-4.554688 0.512,0.123466 1.175227,0.38735 1.621093,0.666016 2.7412,1.7136 1.940017,4.913281 -1.40625,5.613281 -1.631866,0.341067 -3.201482,0.03814 -4.666015,-0.90039 -4.644267,2.9552 -7.110639,4.750387 -11.701172,8.939453 0.886933,1.0588 1.277822,1.843536 1.607422,3.230468 7.7276,-0.364 13.416197,-0.283559 21.341797,1.181641 3.164534,-8.690666 13.10911,-5.092158 16.964843,3.734375 5.3532,12.256266 -6.268628,9.118355 -9.673828,7.607422 -5.806266,-2.576533 -6.933556,-6.835853 -7.357422,-8.439453 -5.7896,-0.844263 -10.70565,-1.391427 -21.28125,-0.746094 -0.333867,1.474 -0.707023,2.35544 -1.535156,3.628906 3.558266,3.521867 7.237883,7.164197 12.416016,11.341797 6.107733,-4.347867 14.065965,1.424337 13.384765,6.408203 -0.769333,5.6188 -11.17697,8.331949 -14.970703,2.363282 -0.7536,-1.186 -1.772892,-3.643478 -0.140625,-6.777344 -3.124,-2.496934 -6.354763,-5.078051 -12.601562,-11.28125 -1.602667,1.212533 -2.771166,1.686819 -4.634766,1.876953 -0.304667,4.1708 -0.592243,8.110631 -0.162109,13.779297 7.279733,2.5224 8.075452,10.405533 3.939453,12.197265 -4.879734,2.116135 -11.514902,-4.555998 -9.595703,-9.644531 0.8412,-2.2308 2.841801,-2.563856 3.802734,-2.722656 -0.216667,-3.945333 -0.404305,-7.354058 0.173828,-13.775391 -1.2948,-0.3468 -2.022274,-0.76479 -2.99414,-1.722656 -2.276133,2.715733 -4.628623,5.52298 -8.28125,11.248047 1.98756,4.535866 1.203723,8.685547 -0.845703,8.185547 C 9.0630279,59.457488 6.5835988,49.923617 9.2617188,48.771484 c 0.5869867,-0.2536 1.5742192,-0.163667 2.9492192,1.697266 2.493212,-3.907867 4.408232,-6.794531 8.134765,-11.144531 -0.689599,-1.467733 -0.893314,-2.490364 -0.833984,-4.207031 -2.904667,-0.127603 -2.903911,-0.127839 -4.464844,-0.167969 -1.89744,4.789067 -4.349079,4.972245 -5.3183594,3.267578 -1.8140533,-3.189067 1.5098384,-10.257624 4.3906254,-9.337891 1.505197,0.4812 1.464465,2.720049 1.447265,3.675782 2.576533,-0.03277 2.785749,-0.0364 4.488282,0 1.055733,-2.551601 2.253951,-3.928033 4.574218,-5.259766 -0.437467,-3.5912 -0.71809,-6.056039 -0.96289,-11.253906 -1.562,0.6364 -2.160055,0.564259 -2.419922,0.183593 -0.5448,-1.645867 3.155053,-3.543162 3.939453,-3.851562 0.5988,-0.23435 1.043953,-0.318438 1.371094,-0.316406 z"
                                id="path26" /></g></svg>
                </g:link>
                <laser:script file="${this.getGroovyPageFileName()}">
                    $('#spanYear').html(new Date().getFullYear());
                </laser:script>
                <p>
                    ©<span id="spanYear"></span>&nbsp;Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz) &#8231; Jülicher Straße 6 &#8231; 50674 Köln &#8231; +49 221 400 75-0
                </p>
            </div>

            <div class="three wide column left aligned">
                <h2 class="ui inverted header">
                    ${message(code: 'landingpage.footer.1.head')}
                </h2>

                <div class="ui inverted link list">
                    <div class="item">
                        <i class="envelope icon"></i><a class="content" href="mailto:laser@hbz-nrw.de">${message(code: 'landingpage.footer.1.link1')}</a>
                    </div>
                    <div class="item">
                        <i class="map signs icon"></i><a target="_blank" class="content" href="https://www.hbz-nrw.de/ueber-uns/kontakt/anreise">${message(code: 'landingpage.footer.1.link2')}</a>
                    </div>
                    <div class="item">
                        <i class="file alternate icon"></i><a target="_blank" class="content" href="https://www.hbz-nrw.de/impressum">${message(code: 'landingpage.footer.1.link3')}</a>
                    </div>
                    <div class="item">
                        <i class="lock icon"></i><a target="_blank" class="content" href="https://www.hbz-nrw.de/datenschutz">${message(code:'dse')}</a>
                    </div>
                </div>
            </div>

            <div class="four wide column left aligned">
               <h2 class="ui inverted header">
                   ${message(code: 'landingpage.footer.4.head')}
               </h2>

               <div class="ui inverted link list">
                   <div class="item">
                       <i class="universal access icon"></i>
                       <a target="_blank" class="content" href="https://www.hbz-nrw.de/barrierefreiheit">
                            ${message(code: 'landingpage.footer.4.link1')}
                       </a>
                   </div>
                <g:if test="${ConfigUtils.getLaserSystemId() == 'LAS:eR-Productive' || ConfigUtils.getLaserSystemId() == 'local'}">
                   <div class="item">
                        <i class="universal access icon"></i>
                       <g:link controller="public" action="wcagFeedbackForm" class="content">
                           ${message(code: 'landingpage.footer.4.link2')}
                       </g:link>
                   </div>
                </g:if>
                   <div class="item">
                       <i class="universal access icon"></i>
                        <g:link controller="public" action="wcagEasyLanguage" class="content">
                            ${message(code: 'landingpage.footer.4.link4')}
                        </g:link>
                   </div>
               </div>
           </div>
            <div class="four wide column left aligned">
                <h2 class="ui inverted header">${message(code: 'landingpage.footer.3.head')}</h2>
                <div class="ui inverted link list">

                    <a target="_blank" class="item" href="https://github.com/hbz/laser2/releases">
                        Version: ${grailsApplication.metadata['info.app.version']}
                    </a>

                    <g:if test="${grailsApplication.metadata['git.branch']}">
                        <a target="_blank" class="item" href="https://github.com/hbz/laser2/tree/${grailsApplication.metadata['git.branch']}">
                            Branch: ${grailsApplication.metadata['git.branch']}
                        </a>
                    </g:if>

                    <div class="item">
                        <g:if test="${grailsApplication.metadata['git.commit.id']}">
                            <a target="_blank" class="item" href="https://github.com/hbz/laser2/tree/${grailsApplication.metadata['git.commit.id']}">
                                Build: ${grailsApplication.metadata['info.app.build.date']}
                            </a>
                        </g:if>
                        <g:else>
                            Build: ${grailsApplication.metadata['info.app.build.date']}
                        </g:else>
                    </div>

                    <!-- (${grailsApplication.metadata['info']}) -->
                </div>
            </div>

        </div>
    </div>
</footer>