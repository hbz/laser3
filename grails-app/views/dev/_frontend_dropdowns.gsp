<h2 class="ui dividing header">Dropdowns<a class="anchor" id="form-elemente"></a></h2>
<div class="ui big form">
    <div class="three wide fields">
        <div class="field">
            <div class="ui selection icon dropdown">
                <i class="dropdown icon"></i>
                <div class="default text">Bitte auswählen</div>
                <div class="menu">

                    <div class="item">
                        <i class="sort alphabet down icon"></i>
                        Titel: aufsteigend
                    </div>
                    <div class="item">
                        <i class="sort alphabet up alternate icon"></i>
                        Titel: absteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-jan-dec icon"></i>
                        Print-Veröffentlichung: aufsteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-dec-jan icon"></i>
                        Print-Veröffentlichung: absteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-jan-dec icon"></i>
                        Online-Veröffentlichung: aufsteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-dec-jan icon"></i>
                        Online-Veröffentlichung: absteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-jan-dec icon"></i>
                        Mein Zugriff von: aufsteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-dec-jan icon"></i>
                        Mein Zugriff von: absteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-jan-dec icon"></i>
                        Mein Zugriff bis: aufsteigend
                    </div>
                    <div class="item">
                        <i class="la-sort-dec-jan icon"></i>
                        Mein Zugriff bis: absteigend
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- START Example with form selection -->
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ui>
            <li>selectOnKeydown: false <i class="ui icon arrow right"></i>nur wichtig für Tastaturbenutzung: eine Auswahl durch Tastaturbenutzung erfolgt NICHT</li>
            <li>clearable: true  <i class="ui icon arrow right"></i> die Auswahl kann durch das Kreuz gelöscht werden </li>
        </ui>
        <br />
        <div class="field">
            <label for="filterPropDef">
                Merkmal
                <i aria-hidden="true" class="question circle icon la-popup"></i>
                <div class="ui  popup ">
                    <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                </div>
            </label>
            <select class="ui dropdown">
                <option value="">Bitte auswählen</option>
                <option value="1">Option 1</option>
                <option value="0">Option 2</option>
            </select>
        </div>
        <div class="ui top attached label">Dropdown aus HTML-Select</div>
    </div>


    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;select class="ui dropdown">
            &ltoption value="">Bitte auswählen</option>
            &ltoption value="1">Option 1</option>
            &ltoption value="0">Option 2</option>
            &lt;/select>
        </pre>
    </div>

</div>
<!-- STOP Example with form selection -->

<!-- START Example search dropdown -->
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ui>
            <li>forceSelection: false <i class="ui icon arrow right"></i> Forsiert eine Auswahl NICHT bei blur,
                <ul><li>das heißt: ich suche etwas und bekomme etwas dazu vorgeschlagen, ich verlasse das input und der Vorschlag wird NICHT übernommen</li></ul></li>
            <li>selectOnKeydown: false <i class="ui icon arrow right"></i>nur wichtig für Tastaturbenutzung: eine Auswahl durch Tastaturbenutzung erfolgt NICHT</li>
            <li>clearable: true  <i class="ui icon arrow right"></i> die Auswahl kann durch das Kreuz gelöscht werden </li>
        </ui>
        <br />
        <div class="field">
                <div class="ui  popup ">
                    <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                </div>
            </label>
            <select class="ui search dropdown">
                <option value="">Bitte auswählen</option>
                <option value="1">Option 1</option>
                <option value="0">Option 2</option>
            </select>
        </div>
        <div class="ui top attached label">Such-Dropdown </div>
    </div>


    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;select class="ui <strong>search</strong>  dropdown">
            &ltoption value="">Bitte auswählen</option>
            &ltoption value="1">Option 1</option>
            &ltoption value="0">Option 2</option>
            &lt;/select>
        </pre>
    </div>

</div>
<!-- STOP Example search dropdown -->

<!-- START Example dropdown in filters -->
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ui>
            <li>forceSelection: false <i class="ui icon arrow right"></i> Forsiert eine Auswahl NICHT bei blur,
                <ul><li>das heißt: ich suche etwas und bekomme etwas dazu vorgeschlagen, ich verlasse das input und der Vorschlag wird NICHT übernommen</li></ul></li>
            <li>selectOnKeydown: false <i class="ui icon arrow right"></i>nur wichtig für Tastaturbenutzung: eine Auswahl durch Tastaturbenutzung erfolgt NICHT</li>
            <li>clearable: true  <i class="ui icon arrow right"></i> die Auswahl kann durch das Kreuz gelöscht werden </li>
            <li>onChange: function(value, text, $choice){
            (value !== '') ? _addFilterDropdown(this) : _removeFilterDropdown(this);
            }  <i class="ui icon arrow right"></i> bei einer Auswahl wird die Klasse des Dropdowns geändert und damit farblich markiert </li>
        </ui>
        <br />
        <div class="field">
                <div class="ui  popup ">
                    <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                </div>
            </label>
            <div class="la-filter">
                <select class="ui dropdown">
                    <option value="">Bitte auswählen</option>
                    <option value="1">Option 1</option>
                    <option value="0">Option 2</option>
                </select>
                <select class="ui search dropdown">
                    <option value="">Bitte auswählen</option>
                    <option value="1">Option 1</option>
                    <option value="0">Option 2</option>
                </select>
            </div>
        </div>
        <div class="ui top attached label">Such-Dropdown und einfaches Dropdown innerhalb Filter</div>
    </div>


    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;select class="ui dropdown">
            &ltoption value="">Bitte auswählen</option>
            &ltoption value="1">Option 1</option>
            &ltoption value="0">Option 2</option>
            &lt;/select>

            &lt;select class="ui <strong>search</strong> dropdown">
            &ltoption value="">Bitte auswählen</option>
            &ltoption value="1">Option 1</option>
            &ltoption value="0">Option 2</option>
            &lt;/select>
        </pre>
    </div>

</div>
<!-- STOP Example dropdown in filters -->

<!-- START Example with no form selection -->
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ui>
            <li>versehen mit Label, das mit Extra-Inputdfeld verbunden ist </li>
            <li><g:link controller="myInstitution" action="currentSubscriptions">zum Beispiel hier verwendet (Merkmal innerhalb Filter)</g:link></li>
        </ui>
        <br />
        <div class="field">
            <label for="filterPropDef">
                Merkmal
                <i aria-hidden="true" class="question circle icon la-popup"></i>
                <div class="ui  popup ">
                    <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                </div>
            </label>
            <div class="ui search selection dropdown ">
                <input type="hidden" name="filterPropDef">
                <i aria-hidden="true" class="dropdown icon"></i>
                <input class="search" id="filterPropDef">
                <div class="default text">Bitte auswählen</div>
                <div class="menu">
                    <div class="item selected" data-value="1">Abbestellgrund</div>
                    <div class="item" data-value="2">Abbestellquote</div>
                    <div class="item" data-value="3">AGB <i aria-hidden="true" class="shield alternate icon"></i></div>
                    <div class="item" data-value="4">Alternativname <i aria-hidden="true" class="shield alternate icon"></i></div>
                    <div class="item" data-value="5" >Archivzugriff</div>
                    <div class="item" data-value="6" >Bei hbz Aufnahme der Metadaten nachfragen <i aria-hidden="true" class="shield alternate icon"></i></div>
                    <div class="item" data-value="7">Bestellnummer im Erwerbungssystem</div>
                    <div class="item" data-value="8">Bundesweit offen</div>
                    <div class="item" data-value="9">DBIS-Eintrag</div>
                    <div class="item" data-value="10">DBIS-Link</div>
                </div>
            </div>
        </div>

        <div class="ui top attached label">Dropdown aus verschachtelten Div-Elementen</div>
    </div>


    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;div class=&quot;field&quot;&gt;
            &lt;label <strong>for=&quot;filterPropDef&quot;</strong>&gt;
        Merkmal
        &lt;i class=&quot;question circle icon la-popup&quot;&gt;&lt;/i&gt;
        &lt;div class=&quot;ui  popup &quot;&gt;
        &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt; = Meine Merkmal
        &lt;/div&gt;
        &lt;/label&gt;
        &lt;div class=&quot;ui search selection dropdown la-filterPropDef&quot;&gt;
        &lt;input type=&quot;hidden&quot; name=&quot;filterPropDef&quot;&gt; &lt;i class=&quot;dropdown icon&quot;&gt;&lt;/i&gt;
        &lt;input class=&quot;search&quot; <strong>id=&quot;filterPropDef&quot;</strong> &gt;
        &lt;div class=&quot;default text&quot;&gt;Bitte ausw&auml;hlen&lt;/div&gt;
        &lt;div class=&quot;menu&quot;&gt;
        &lt;div class=&quot;item selected&quot; data-value=&quot;linkurl&quot;&gt;Abbestellgrund&lt;/div&gt;
        &lt;div class=&quot;item&quot; data-value=&quot;linkurl&quot;&gt;Abbestellquote&lt;/div&gt;
        &lt;div class=&quot;item&quot; data-value=&quot;linkurl&quot;&gt;AGB &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt;&lt;/div&gt;
        &lt;/div&gt;
        &lt;/div&gt;
        &lt;/div&gt;
        </pre>
    </div>

</div>
<!-- STOP Example with no form selection -->
