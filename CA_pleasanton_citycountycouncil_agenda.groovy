package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait

import java.util.concurrent.TimeUnit

public class CA_pleasanton_citycountycouncil_agenda extends BaseCrawler{

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        //QUESTION ABOUT AGENDA.  CAN WE ADD NEW ATTRIBUTE TO DOCUMENTWRAPPER OR OTHER OPTION?
        List docList = []
        try {
            driver.manage().timeouts()implicitlyWait(10, TimeUnit.SECONDS)
            driver.get(baseUrl);
            // Wait for years to be displayed
            By yearLink = By.xpath("//nobr[span[contains(.,'2018')]]")
            wait.until(ExpectedConditions.presenceOfElementLocated(yearLink))

            // Click the 2018 row
            driver.findElement(yearLink).click()
            sleep(2000)

            //get folder web elements
            List <WebElement> folders = driver.findElementsByXPath("//div[@id=\"TheRightPanel\"]//tbody//tr/td[1]/a")
            //get text from each for loop (cannot loop with web elements after DOM refresh)
            List folderNames = []
            for(WebElement folder:folders){
                folderNames.add(folder.getText())
                //debug  System.out.println(folder.getText()) //debug
            }

            //Iterate through the dated folders
            folderNames.each{ String folderName ->
                //grab link and refresh element (new DOM each time)
                System.out.println(folderName) //debug
                By folderPath = By.xpath("//a[contains(. ,'" + folderName + "')]")
                WebElement folder = driver.findElement(folderPath)
                folder.click()
                sleep(1200) //debug

                try {
                    DocumentWrapper doc = new DocumentWrapper();
                    // grab date
                    By datePath = By.xpath("//div[@class=\"FolderNameHeader\"]")
                    doc.dateStr = driver.findElement(datePath).getText()
                    //debug  System.out.println("\tDate: ${doc.dateStr}")

                    //grab title
                    By titlePath = By.xpath("//div[@title=\"Path\"]/following-sibling::div[@class=\"FolderDataValue\"][1]")
                    doc.title = driver.findElement(titlePath).getText()
                    //debug  System.out.println("\tTitle: ${doc.title}")

                    //grab agenda url
                    By agendaPath = By.xpath("//div[@id=\"TheRightPanel\"]//tr/td[a]/*[contains(.,\"AGENDA\")]");
                    doc.link = driver.findElement(agendaPath).getAttribute("href")
                    //debug System.out.println("\tUrl: ${doc.link}")
                    docList = docList + doc
                }
                catch (Exception e) {
                    System.out.println(e.message)
                    //continue crawling the rest
                }
                //navigate back again before loop
                driver.navigate().back()
            }
        } catch (Exception e) {
            System.out.println(e.message)
            System.out.println("Continuing with next link")
        } finally{
            driver.quit()
            return docList
        }
    }

    //not used
    public List getDocumentsByPage(WebDriver driver) throws Exception{
        List docList = []
        //*[@id="ViewTextLink"]
        return docList
    }
}
