package pages
/**
 * Created by ryan@k-int.com
 */
class LicenseComparePage extends BasePage {
    static at = {
        browser.page.title.startsWith("KB+")
    }

    static content = {
        header { $("h1").text().trim() }

        addLicense{ val ->
            $("#select2-chosen-1").click()
            $("#s2id_autogen1_search").value(val)
            Thread.sleep(100)
            waitFor{$("div.select2-result-label")}
            $("div.select2-result-label").click()
            $("#addToList").click()
        }

        compare { lic1, lic2 ->
            addLicense(lic1)
            addLicense(lic2)
            $("input", type:"submit", value: "Compare").click() 
        }

        tableCount {$("table").size()}
    }
}
