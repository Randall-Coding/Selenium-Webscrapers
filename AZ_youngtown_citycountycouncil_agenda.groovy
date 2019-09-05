package com.agendadiscovery.crawlers

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit
import com.agendadiscovery.DocumentWrapper

//url: http://youngtownaz.hosted.civiclive.com//cms/One.aspx?pageId=13406575&portalId=12609077&objectId.259675=13424223&contextId.259675=13406577&parentId.259675=13406578
class AZ_youngtown_citycountycouncil_agenda extends BaseCrawler{
    private static final org.slf4j.Logger log = LoggerFactory.getLogger("com.agendadiscovery.groovy.AZ_youngtown_citycountycouncil_agenda")
    List <DocumentWrapper> docList = []
    int current_year = Year.now().getValue()
    Boolean running = true

    List getDocuments(String baseUrl, int maxCrawlRecords) throws Exception {
        log.info("Starting AZ Youngtown Selenium crawl")
        log.info("Requesting baseURL: "+baseUrl)

        try {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
            driver.get(baseUrl)

            // Wait for sort to be displayed
            By sortLink = By.xpath("//*[@id=\"listHeader\"]/div[1]/div[1]/a")
            wait.until(ExpectedConditions.presenceOfElementLocated(sortLink))

            // Click sort to avoid paging and put 2019 on top
            driver.findElement(sortLink).click()             // Asc sort
            driver.findElement(sortLink).click()             // Desc sort

            getDocumentsByYear(driver, 1, maxCrawlRecords)
        }
        catch (Exception e) {
            log.error("Unexpected error occurred: " +  e)
            log.debug("Stacktrace: ",  e)
        } finally{
            driver.quit()
            return docList
        }
    }

    // Originally planned on multiple years.  Can keep and modify later quite easily if desired
    void getDocumentsByYear(WebDriver driver, int years, int maxCrawlRecords){
        try{
            for(int offset:0..years-1){
                // Click the current year row
                By yearLink = By.xpath("/html/body/form/div[4]/div/div[2]/div[5]/div/div[1]/div[2]/div[2]/div/div/div[2]/div[2]/div[5]/ul/li/a[div/div/div[text()=${current_year - offset}]]")
                driver.findElement(yearLink).click()

                // Wait for the first PDF icon to show (ajax to finish)
                By firstPdfIcon = By.xpath("//li[2]/a[1]/div/div[2]/div/em[contains(@class,\"fa-file-pdf-o\")]")
                wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))

                paginateThrough(driver,maxCrawlRecords)
            }
        }
        catch(org.openqa.selenium.ElementNotInteractableException e){
            log.info("Skipping extra hidden pagination button: " + e)
        }
    }

    void paginateThrough(WebDriver driver, int maxCrawlRecords){
        // Iterate through the pages
        driver.findElementsByXPath("//div[@class=\"PO-paging\"]/ul[@class=\"PO-pageButton\"]/li/a[contains(@class,\"number\")]").each { WebElement we ->
            // Click page number
            we.click()
            // Grab documents on page
            getDocumentsByPage(driver, maxCrawlRecords)
        }
    }

    void getDocumentsByPage(WebDriver driver,  int maxCrawlRecords) throws Exception{
        // Grab the element rows
        By rowSelector = By.xpath("//ul[@id=\"documentList\"]/li[position()>1]")
        List<WebElement> webElements = driver.findElements(rowSelector)

        for(int i=0; i < webElements.size(); i++){

            try{
                DocumentWrapper doc = new DocumentWrapper()

                // Row element data will be relative to
                WebElement webElement = webElements.get(i)

                // Xpath Selectors
                By titleBy = By.xpath("./a/div/div/div[contains(@class,\"docTitle\")]")
                By dateBy = titleBy // same in this case
                By urlBy = By.xpath("./a")

                // Get data
                String title = webElement.findElement(titleBy).getText()
                String dateStr = webElement.findElement(dateBy).getText()  //match("[0-9]{2}.[0-9]{2}.[0-9]{2}",webElement.findElement(dateBy).getText() )

                // Modify date
                // Way without using match() function
                def m =  (dateStr =~ "[0-9]{2}.[0-9]{2}.[0-9]{2}")
                if(m.find()){
                    dateStr = m.group(0)
                }
                else{
                    throw new Exception("Date format has changed.  Please modify Selenium script for " + this.class.toString())
                }
                dateStr = dateStr[0..-3] + '20' + dateStr[-2..-1]

                String url = webElement.findElement(urlBy).getAttribute("href")

                doc.title = title
                doc.dateStr = dateStr
                doc.link = url

                log.debug("\tTitle: ${title}")
                log.debug("\tDate: ${dateStr}")
                log.debug("\tUrl: ${url}")

                docList.add(doc)
            }
            catch(org.openqa.selenium.NoSuchElementException e){
                log.debug("Item could not be located, skipping: " + this.class.name)
            }
            catch(Exception e){
                log.error("Unexpected error occurred: " +  e)
                log.debug("Stacktrace: ",  e)
            }
        }
    }
}
