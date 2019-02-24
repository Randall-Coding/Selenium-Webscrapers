package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import java.time.Year

//weird DocumentCenter
public class KS_lyons_citycountycouncil_agenda extends BaseCrawler{

    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []
    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting ${this.class.name} Selenium crawl")
        log.info("Requesting baseURL: "+baseUrl)
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
        try {
            driver.get(baseUrl)
            //Click "City Council"
            driver.findElementByXPath('//*[@id="AjaxTreeView"]//span[text() = "City Council"]').click()
            //Click "Agenda"
            driver.findElementByXPath('//*[@id="AjaxTreeView"]//span[text() = "Agendas"]').click()
            //Grab years
            List <WebElement> years = driver.findElementsByXPath("//*[@id=\"AjaxTreeView\"]//span[text() = \"Agendas\"]/../following-sibling::ul//span")
            //Cycle through years
            for (WebElement year : years){
                    year.click()
                    sleep(500)
                    //Grab months
                    String monthPath = "//*[@id=\"AjaxTreeView\"]//span[text() = \"" + year.getText() + "\"]/../following-sibling::ul//span"
                    List <WebElement> months
                    try {months = driver.findElementsByXPath(monthPath)}
                    catch (Exception e){System.out.println(e.message); continue}
                    //Cycle through months
                    for (WebElement month : months){
                        month.click()
                        DocumentWrapper doc = new DocumentWrapper()
                        sleep(500)
                        //Grab pdf link
                        WebElement pdf
                        String pdfPath = "//div[@id=\"Grid\"]//tbody//a[contains(translate(.,\"CITY COUNCIL\",\"city council\"), \"city council\" ) or contains(translate(. , \"AGEND\",\"agend\") , \"agenda\")]"
                        try {
                            pdf = driver.findElementByXPath(pdfPath)
                        }
                        catch (Exception e){
                            System.out.println(e.message)
                            log.debug(e.message)
                            System.out.println("!!!An entry failed for " + month.getText() )
                            continue;
                        }
                        //set
                        doc.title = pdf.getText()
                        doc.dateStr = "" //debug inconsistent titles / dates pdf.getText()
                        doc.link = pdf.getAttribute("href")

                        //debug
                        //System.out.println(doc.title)
                        //System.out.println(doc.dateStr)
                        //System.out.println(doc.link)

                        docList.add(doc)
                    }
                }
        } catch (Exception e) {
            log.debug("Selenium crawl error: "+e)
            System.out.println(e.message)
        }
        finally{
            driver.quit()
            return docList
        }
    }

}





//debug List<WebElement> rowElements = driver.findElements(rowSelector)
// Cheap way to get rid of stale element and refresh original folders list
//debug       rowElements = driver.findElements(rowSelector)