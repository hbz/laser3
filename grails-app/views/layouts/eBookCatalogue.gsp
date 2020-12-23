<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>EBookCatalogue</title>
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
                <div class="column is-7">
                    <h1 class="title">EBookCatalogue</h1>
                    <h2 class="subtitle">
                        give us some time, but .. <strong>here</strong> we go
                    </h2>
                </div>
                <div class="column is-3">
                    <span class="tag is-large is-danger">DEMO - DEMO - DEMO</span>
                </div>
            </div>
        </div>
    </div>

</section>

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
</section>

<g:layoutBody/>


<!--<footer class="footer">
    <div class="content has-text-centered">
        <p>give us some time, but <strong>here</strong> we go ..</p>
    </div>
</footer>-->

<laser:scriptBlock/>%{-- dont move --}%

<style>
/*
#9CA0A9
#C2CBDA
 */
    #custom-hero {
    }
    #custom-logo {
        display: inline-block;
        width: 150px;
        height: 150px;
        background-image: url("${resource(dir: 'images', file: 'eBookCatalogue/library-book.svg')}");
        background-position: center;
        background-repeat: no-repeat;
    }
    .custom-section-stats {
        background-color: #D2DBEA;
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

</style>
</body>
</html>