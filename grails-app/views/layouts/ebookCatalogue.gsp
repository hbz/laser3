<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>ebookCatalogue</title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="ebookCatalogue.css"/><asset:javascript src="ebookCatalogue.js"/>%{-- dont move --}%

    <tmpl:/layouts/favicon />
</head>

<body>

<section id="custom-hero" class="hero is-info is-bold">
    <div class="hero-body custom-hero-body">
        <div class="container">
            <div class="columns">
                <div class="column is-2">
                    <span id="custom-logo"></span>
                </div>
                <div class="column is-7">
                    <h1 class="title">ebookCatalogue (working title)</h1>
                    <h2 class="subtitle">
                        give us some time, but <strong>here</strong> we go ..
                    </h2>
                    <p>DEMO - DEMO - DEMO</p>
                </div>
                <div class="column is-3">
                    <div id="custom-statistics">
                        <div class="has-text-right">
                            <p class="subtitle"><strong class="has-text-weight-semibold">123456</strong> EBooks,</p>
                            <p class="subtitle"><strong class="has-text-weight-semibold">6543</strong> Produkte,</p>
                            <p class="subtitle"><strong class="has-text-weight-semibold">123</strong> Anbieter,</p>
                            <p class="subtitle"><strong class="has-text-weight-semibold">999999</strong> Suchanfragen,</p>
                            <p class="subtitle">davon heute <strong class="has-text-weight-semibold">789</strong></p>
                        </div>
                    </div>
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


<style>

    #custom-hero {
    }
    #custom-logo {
        display: inline-block;
        width: 150px;
        height: 150px;
        background-image: url("${resource(dir: 'images', file: 'ebookCatalogue/library-book.svg')}");
        background-position: center;
        background-repeat: no-repeat;
    }
    #custom-statistics .subtitle {
        margin-bottom: 0.5rem;
    }
    #custom-statistics .subtitle:last-of-type {
        margin-bottom: 0;
    }
    .custom-section-form {
        background-color: #fafafa;
    }
    .custom-section-result {
        border-top: 1px solid #3298dc;
    }

</style>
</body>
</html>