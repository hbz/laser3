<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>${message(code: 'laser', default: 'LAS:eR')}</title>
</head>
<body>
<!-- NAVIGATION FIX -->
<div class="ui container ">
    <div class="ui top fixed hidden menu la-fixed-menu">
        <div class="ui container">
            <img src="images/laser.svg"/>
            <a href="https://wiki1.hbz-nrw.de/display/LAS/Projekthintergrund" class="item">Über LAS:eR</a>
            <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" class="item">Wiki</a>
            <div class="right item">
                <g:link controller="home" action="index" class="ui button blue" >
                    ${message(code:'template.loginDiv.label', default:'LAS:eR Member Login')}
                </g:link>
            </div>
        </div>
    </div>
</div>

<!--Page Contents-->
<div class="pusher">
    <div class="ui inverted menu la-top-menu">
        <div class="ui container">
            <img src="images/laser.svg"/>
            <a href="https://wiki1.hbz-nrw.de/display/LAS/Projekthintergrund" class="item">Über LAS:eR</a>
            <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" class="item">Wiki</a>
            <div class="right item">
                <g:link controller="home" action="index" class="ui button blue" >
                    ${message(code:'template.loginDiv.label', default:'LAS:eR Member Login')}
                </g:link>
            </div>
        </div>
    </div>
    <!-- HERO -->
    <div class="ui  masthead center aligned segment landing-image">
        <div class="ui container">

            <div class="ui grid ">
                    <div class="seven wide column la-hero" >
                        <h3 class="ui inverted header">
                            Lizenz-Administrationssystem <br>für e-Ressourcen
                        </h3>
                        <h4>
                            Lorem ipsum
                        </h4>
                        <div class="ui huge orange button">
                            Start<i class="right arrow icon"></i>
                        </div>
                    </div>

                    </div>
                </div>
            </div>

    </div>

    <!-- SEQUMENT -->

        <div class="ui container">
            <div class="ui grid">
                <div class="four wide column">
                    <h3 class="ui header">Optimierte Lizenzverwaltung</h3>
                    <p><span class="">Endlich alle Informationen an einem Ort</span> LAs:eR ermöglicht die vollumfängliche Verwaltung von eRessourcen in einem System inklusive Konsortial- und Nationallizenzen. Alle benötigten Vertragsinformationen wie Laufzeit, Preis, Zugriffsbeschränkungen und Fernleihinformationen werden vorgehalten und für Konsortial- und Nationallizenzen zentral gepflegt.</p>
                </div>
                <div class="four wide column">
                    <h3 class="ui header">Integrierte Zugriffsstatistiken</h3>
                    <p><span class="">Behalten Sie den Überblick über die Nutzung Ihrer eRessourcen</span> LAS:eR bietet durch die Integration des Nationalen Statistikservers Zugriff auf die Statistiken der lizenzierten Angebote und ermöglicht weiterführende Auswertungen wie die Cost-per-Download-Analyse.
                    </p>
                </div>
                <div class="four wide column">
                    <h3 class="ui header">Standardisierte Exportschnittstellen</h3>
                    <p><span class="">Nutzen Sie LAs:eR als qualitativ hochwertige Quelle für Ihre konsortialen Lizenzteilnahmen</span> Offene Schnittstellen ermöglichen die Übernahme von Daten aus LAS:eR in cloudbasierte Bibliothekssysteme mit ERM-Modul und andere Drittsysteme.</p>
                </div>
                <div class="four wide column">
                    <h3 class="ui header">Effiziente Interaktionsmöglichkeiten</h3>
                    <p><span class="">Gemeinsam mehr erreichen durch bessere Abstimmung</span> LAS:eR bietet zahlreiche Funktionen für die Kommunikation zwischen Konsortialführer und Teilnehmern: Renewals, Produktumfragen und Testwünsche lassen sich einfach organisieren und können im Anschluss direkt für die Übermittlung an den Anbieter aufbereitet werden.</p>
                </div>
            </div>
        </div>




    <div class="ui inverted vertical footer segment">
        <div class="ui container">
            <div class="ui stackable inverted divided equal height stackable grid">
                <div class="three wide column">
                    <h4 class="ui inverted header">
                        About
                    </h4>
                    <div class="ui inverted link list">
                        <a class="item" href="homepage.html#"> Sitemap</a><a class="item" href="homepage.html#"> Contact Us</a><a class="item" href="homepage.html#"> Religious Ceremonies</a><a class="item" href="homepage.html#"> Gazebo Plans</a>
                    </div>
                </div>
                <div class="three wide column">
                    <h4 class="ui inverted header">
                        Services
                    </h4>
                    <div class="ui inverted link list">
                        <a class="item" href="homepage.html#"> Banana Pre-Order</a><a class="item" href="homepage.html#"> DNA FAQ</a><a class="item" href="homepage.html#"> How To Access</a><a class="item" href="homepage.html#"> Favorite X-Men</a>
                    </div>
                </div>
                <div class="seven wide column">
                    <h4 class="ui inverted header">
                        Footer Header
                    </h4>
                    <p>
                        Extra space for a call to action inside the footer that could help re-engage users.
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<style type="text/css">
    body{
        background-color: #fae2c1;
    }
.hidden.menu {
    display: none;
}
.ui.inverted.menu.la-top-menu {
    height: 70px;
    margin:0!important;
    border-radius: none !important;
    border: none!important;
}
.landing-image {
    background-image: url('images/landingpage/hero.png')!important;
    background-repeat: no-repeat!important;
    background-position: top center!important;
    border: none!important;
}
.la-fixed-menu {
    height: 70px;
}
.la-hero {
    padding-top: 7rem!important;
}
.masthead.segment {
    min-height: 480px;
    margin: 0;
    padding: 0;
}
.masthead .logo.item img {
    margin-right: 1em;
}
.masthead .ui.menu .ui.button {
    margin-left: 0.5em;
}
.masthead h1.ui.header {
    margin-top: 2em;
    margin-bottom: 0em;
    font-size: 4em;
    font-weight: normal;
}
.masthead h2 {
    font-size: 1.7em;
    font-weight: normal;
}

.ui.vertical.stripe {
    padding: 8em 0em;
}
.ui.vertical.stripe h3 {
    font-size: 2em;
}
.ui.vertical.stripe .button + h3,
.ui.vertical.stripe p + h3 {
    margin-top: 3em;
}
.ui.vertical.stripe .floated.image {
    clear: both;
}
.ui.vertical.stripe p {
    font-size: 1.33em;
}
.ui.vertical.stripe .horizontal.divider {
    margin: 3em 0em;
}

.quote.stripe.segment {
    padding: 0em;
}
.quote.stripe.segment .grid .column {
    padding-top: 5em;
    padding-bottom: 5em;
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
        .ready(function() {
            // fix menu when passed
            $('.masthead')
                .visibility({
                    once: false,
                    onBottomPassed: function() {
                        $('.fixed.menu').transition('fade in');
                    },
                    onBottomPassedReverse: function() {
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
