<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>${message(code: 'laser', default: 'LAS:eR')}</title>

</head>

<div class="landingpage">
    <!-- NAVIGATION FIX -->
    <div class="ui container ">
        <div class="ui top fixed hidden inverted  menu la-fixed-menu">
            <div class="ui container">
                <img class="logo" src="images/laser.svg"/>
                <a href="https://wiki1.hbz-nrw.de/display/LAS/Projekthintergrund" class="item" target="_blank">${message(code: 'landingpage.menu.about', default: 'About')}</a>
                <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" class="item" target="_blank">Wiki</a>

                <div class="right item">
                    <g:link controller="home" action="index" class="ui button blue">
                        ${message(code: 'landingpage.login', default: 'Login')}
                    </g:link>
                </div>
            </div>
        </div>
    </div>

    <!--Page Contents-->
    <div class="pusher">
        <div class="ui inverted menu la-top-menu">
            <div class="ui container">
                <img class="logo" src="images/laser.svg"/>
                <a href="https://wiki1.hbz-nrw.de/display/LAS/Projekthintergrund" class="item" target="_blank">${message(code: 'landingpage.menu.about', default: 'About')}</a>
                <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" class="item" target="_blank">Wiki</a>

                <div class="right item">
                    <g:link controller="home" action="index" class="ui button blue">
                        ${message(code: 'landingpage.login', default: 'Login')}
                    </g:link>
                </div>
            </div>
        </div>
        <!-- HERO -->
        <div class="ui  masthead center aligned segment">
            <div class="ui container">

                <div class="ui grid ">
                    <div class="five wide column la-hero left aligned ">
                        <h1 class="ui inverted header ">
                            ${message(code: 'landingpage.hero.h1')}
                        </h1>
                        <h2 style="padding-bottom: 1rem;">
                            ${message(code: 'landingpage.hero.h2')}
                        </h2>

                        <div class="ui huge button">
                            ${message(code: 'landingpage.hero.button')}<i class="right arrow icon"></i>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- NEWS -->
        <div class="ui segment la-eye-catcher">
            <div class="ui container">
                <div class="ui labeled button" tabindex="0" >
                    <div class="ui blue button la-eye-catcher-header">
                        <h1>NEWS</h1>
                    </div>

                    <a class="ui basic blue left pointing  label la-eye-catcher-txt">
                        ${message(code: 'landingpage.news')}
                    </a>
                </div>

            </div>

        </div>

        <!-- segment -->
        <div class="ui segment">
            <div class="ui container">
                <div class="ui grid">
                    <div class="four wide column">
                        <img src="http://placehold.it/250x150/eeeeee/a1a1a1">

                        <h3 class="ui header">${message(code: 'landingpage.feature.1.head')}</h3>

                        <p><span
                                class="la-lead">${message(code: 'landingpage.feature.1.lead')}</span> ${message(code: 'landingpage.feature.1.bodycopy')}
                        </p>
                    </div>

                    <div class="four wide column">
                        <img src="http://placehold.it/250x150/eeeeee/a1a1a1">

                        <h3 class="ui header">${message(code: 'landingpage.feature.2.head')}</h3>

                        <p><span
                                class="la-lead">${message(code: 'landingpage.feature.2.lead')}</span> ${message(code: 'landingpage.feature.2.bodycopy')}
                        </p>
                    </div>

                    <div class="four wide column">
                        <img src="http://placehold.it/250x150/eeeeee/a1a1a1">

                        <h3 class="ui header">${message(code: 'landingpage.feature.3.head')}</h3>

                        <p><span
                                class="la-lead">${message(code: 'landingpage.feature.3.lead')}</span> ${message(code: 'landingpage.feature.3.bodycopy')}
                        </p>
                    </div>

                    <div class="four wide column">
                        <img src="http://placehold.it/250x150/eeeeee/a1a1a1">

                        <h3 class="ui header">${message(code: 'landingpage.feature.4.head')}</h3>

                        <p><span
                                class="la-lead">${message(code: 'landingpage.feature.4.lead')}</span> ${message(code: 'landingpage.feature.4.bodycopy')}
                        </p>
                    </div>
                </div>

                <div class="ui center aligned segment"
                     style="background: transparent!important; border: none!important;box-shadow: none!important; webkit-box-shadow: none;">
                    <div class="ui huge blue button">
                        ${message(code: 'landingpage.feature.button')}<i class="right arrow icon"></i>
                    </div>
                </div>
            </div>
        </div>



        <div class="ui inverted vertical footer segment ">
            <div class="ui container">
                <div class="ui stackable inverted divided equal height stackable grid center aligned">

                        <div class="three wide column left aligned">
                            <h4 class="ui inverted header">
                                ${message(code: 'landingpage.footer.1.head')}
                            </h4>

                            <div class="ui inverted link list">
                                <a class="item" href="homepage.html#">${message(code: 'landingpage.footer.1.link1')}</a>
                                <a class="item" href="homepage.html#">${message(code: 'landingpage.footer.1.link2')}</a>
                                <a class="item" href="homepage.html#">${message(code: 'landingpage.footer.1.link3')}</a>
                            </div>
                        </div>

                        <div class="three wide column left aligned">
                            <h4 class="ui inverted header">
                            ${message(code: 'landingpage.footer.2.head')}</a>
                            </h4>

                            <div class="ui inverted link list">
                                <a class="item" href="homepage.html#">${message(code: 'landingpage.footer.2.link1')}</a></a>
                                <a class="item" href="homepage.html#">${message(code: 'landingpage.footer.2.link2')}</a></a>
                                <a class="item" href="homepage.html#">${message(code: 'landingpage.footer.2.link3')}</a></a>
                            </div>
                        </div>

                        <div class="three wide column left aligned">
                            <h4 class="ui inverted header">${message(code: 'landingpage.footer.3.head')}</a></h4>
                            <div class="ui inverted link list">
                                <a class="item" href="https://github.com/hbz/laser">
                                Version ${grailsApplication.metadata['app.version']}
                                <!-- (${grailsApplication.metadata['app.buildNumber']}) -->
                                // ${grailsApplication.metadata['app.buildDate']}
                                </a>
                                <a class="item" href="https://github.com/hbz/laser">${message(code: 'landingpage.footer.3.link1')}</a>

                                </a>
                            </div>
                        </div>

                </div>

                <div class="ui center aligned segment inverted">
                    <p class="" >
                        © 2017 Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz) Jülicher Straße 6 50674 Köln +49 221 400 75-0
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>



<style type="text/css">

    .hidden.menu {
        display: none;
    }

    .ui.inverted.menu.la-top-menu {
        height: 70px;
        margin: 0 !important;
        border-radius: none !important;
        border: none !important;
    }

    .la-fixed-menu {
        height: 70px;
    }

    .la-hero {
        padding-top: 7rem !important;
    }

    .masthead.segment {
        background-image: url('images/landingpage/hero.png') !important;
        background-repeat: no-repeat !important;
        background-position: top center !important;
        border: none !important;
        min-height: 450px;
        margin: 0;
        padding: 0;
    }
    .masthead h1{
        font-size: 28px!important;
        font-weight: 900!important;
        text-align: left;
    }
    .masthead h2{
        font-size: 1.3rem!important;
        font-weight: normal;
        text-align: left;
    }
    h3 {
        color: #2d6697 !important;
    }



    .footer.segment {
        padding: 5em 0em;
    }

    .secondary.pointing.menu .toc.item {
        display: none;
    }

    @media only screen and (max-width: 700px) {
        .ui.fixed.menu {
            display: none !important;
        }

        .secondary.pointing.menu .item,
        .secondary.pointing.menu .menu {
            display: none;
        }

        .secondary.pointing.menu .toc.item {
            display: block;
        }

        .masthead.segment {
            min-height: 350px;
        }

        .masthead h1.ui.header {
            font-size: 2em;
            margin-top: 1.5em;
        }

        .masthead h2 {
            margin-top: 0.5em;
            font-size: 1.5em;
        }
    }
    </style>
    <script>
        $(document)
            .ready(function () {
                // fix menu when passed
                $('.masthead')
                    .visibility({
                        once: false,
                        onBottomPassed: function () {
                            $('.fixed.menu').transition('fade in');
                        },
                        onBottomPassedReverse: function () {
                            $('.fixed.menu').transition('fade out');
                        }
                    })
                ;

                // create sidebar and attach to menu open
                $('.ui.sidebar')
                    .sidebar('attach events', '.toc.item')
                ;
            })
        ;
    </script>
</body>
</html>
