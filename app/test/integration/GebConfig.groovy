import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

baseUrl = "http://localhost:8080/laser"

waiting {
    timeout = 10
    retryInterval = 0.5
}

environments {

    htmlUnit {
        driver = { new HtmlUnitDriver() }
    }

    firefox {
        driver = { new FirefoxDriver() }
    }
}