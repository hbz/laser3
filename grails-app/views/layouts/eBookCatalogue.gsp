<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>EBookCatalogue - DEMO</title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="eBookCatalogue.css"/><laser:javascript src="eBookCatalogue.js"/>%{-- dont move --}%

    <tmpl:/layouts/favicon />
</head>

<body>

<section id="custom-hero" class="hero is-info is-bold">
    <div class="hero-body custom-hero-body pt-6 pb-5">
        <div class="container">
            <div class="columns">
                <div class="column is-2">
                    <span id="custom-logo"></span>
                </div>
                <div class="column is-10">
                    <h1 class="title">EBookCatalogue</h1>
                    <h2 class="subtitle">
                        give us some time, but .. <strong>here</strong> we go
                    </h2>
                    <p>DEMO - DEMO - DEMO</p>
                </div>
            </div>

            <g:if test="${queryHistory?.size() > 1}">
                <div id="history">
                    <div class="dropdown is-right is-hoverable has-text-right">
                        <div class="dropdown-trigger">
                            <button class="button is-link" aria-haspopup="true" aria-controls="dropdown-menu">
                                <span>Meine letzten ${queryHistory.size()} Suchanfragen</span>
                            </button>
                        </div>
                        <div class="dropdown-menu" id="dropdown-menu" role="menu">
                            <div class="dropdown-content">
                                <g:each in="${queryHistory}" var="qq">
                                    <div>
                                        <a class="dropdown-item" href="<g:createLink controller='ebookCatalogue' action='index'/>${qq['queryString']}">
                                            ${qq['label'].join(' * ')}
                                            <g:if test="${qq['matches'] > 0}">
                                                <strong>(${qq['matches']} Treffer)</strong>
                                            </g:if>
                                            <g:else>
                                                (${qq['matches']} Treffer)
                                            </g:else>
                                        </a>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

        </div>
    </div>
</section><!-- #custom-hero -->

<section class="section custom-section-stats pt-5 pb-5">
    <div class="container">
        <div class="level">
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">EBooks</p>
                    <p class="title">${allTitles.size()}</p>
                </div>
            </div>
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Produkte</p>
                    <p class="title">${allSubscriptions.size()}</p>
                </div>
            </div>
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Anbieter</p>
                    <p class="title">${allProvider.size()}</p>
                </div>
            </div>
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Konsortialstellen</p>
                    <p class="title">${allConsortia.size()}</p>
                </div>
            </div>
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Suchenanfragen</p>
                    <p class="title">${hitCounter}</p>
                </div>
            </div>
        </div>
    </div>
</section><!-- .custom-section-stats -->

<g:layoutBody/>

<a id="scrollToTop" href="#">&#10148;</a>

<laser:script file="${this.getGroovyPageFileName()}">
        let $stt = $("#scrollToTop")

        $(window).scroll( function() {
            let height = $(window).scrollTop()
            if (height > 100) { $stt.fadeIn() } else { $stt.fadeOut() }
        });
        $stt.click( function(event) {
            event.preventDefault()
            $("html, body").animate({ scrollTop: 0 }, "slow")
            return false
        });
</laser:script>

<!--<footer class="footer">
    <div class="content has-text-centered">
        <p>give us some time, but <strong>here</strong> we go ..</p>
    </div>
</footer>-->

<laser:scriptBlock/>%{-- dont move --}%

<style>
    /* #custom-hero .hero-body {
        background-image: url("${resource(dir: 'images', file: 'eBookCatalogue/cubes.png')}");
    } */
    #custom-logo {
        display: inline-block;
        width: 150px;
        height: 150px;
        background-image: url("${resource(dir: 'images', file: 'eBookCatalogue/library-book.svg')}");
        background-position: center;
        background-repeat: no-repeat;
    }
    .custom-section-stats {
        background-image: linear-gradient(180deg, #c2cbda 0%, #d2dbea 100%);
    }
    .custom-section-stats p.heading {
        color: #333;
    }
    .custom-section-stats p.title {
        color: #444;
    }
    .custom-section-form {
        background-color: #fafafa;
    }
    .custom-section-result a > .icon {
        margin-right: 0.5em;
    }
    .custom-section-result a:hover {
        text-decoration: underline;
    }

    #history {
        position: absolute;
        top: 0.75rem;
        right: 0;
    }

    #scrollToTop {
        width: 40px;
        line-height: 40px;
        transform: rotate(270deg);
        position: fixed;
        bottom: 3rem;
        right: 0;
        text-align: center;
        font-size: 30px;
        text-decoration: none;
        background-color: #3298dc;
        color: #fff;
        overflow: hidden;
        z-index: 999;
        display: none;
    }
    #scrollToTop:hover {
        background-color: #2793da;
    }

    /* --- */

    .table thead td, .table thead th {
        padding-bottom: 1em;
        border-width: 0 0 1px 0;
    }

    a.dropdown-item, button.dropdown-item {
        padding-left: 1.5rem;
        padding-right: 1.5rem;
    }

</style>
</body>
</html>