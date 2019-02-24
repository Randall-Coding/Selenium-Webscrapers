package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
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

//City Council
//download link->  https://cityofplymouth.sharepoint.com/Public_Documents/_layouts/15/download.aspx?SourceUrl=/Public_Documents/Shared Documents/Council Meetings/Agenda/2019/February 14 2019 Reg Agenda Packet.pdf

//Planning Commission
//download link-> https://cityofplymouth.sharepoint.com/Public_Documents/_layouts/15/download.aspx?SourceUrl=/Public_Documents/Shared Documents/Planning Commission/Agenda/2017/FEB 2, 2017 CANCELLATION NOTICE.pdf

public class CA_plymouth_citycountycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    String cityDownloadBase = "https://cityofplymouth.sharepoint.com/Public_Documents/_layouts/15/download.aspx?SourceUrl=/Public_Documents/Shared Documents/Council Meetings/Agenda/"
    String planningDownloadBase = "https://cityofplymouth.sharepoint.com/Public_Documents/_layouts/15/download.aspx?SourceUrl=/Public_Documents/Shared Documents/Planning Commission/Agenda/"
    String cityAgendaPath = "//p[contains(.,\"Council\")]/following-sibling::table[1]//a[contains(.,\"Agenda\")]"
    String planningAgendaPath = "//p[contains(.,\"Planning Commission\")]/following-sibling::table[1]//a[contains(.,\"Agenda\")]"

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: "+baseUrl)

        driver.manage().timeouts()implicitlyWait(5, TimeUnit.SECONDS)
        driver.get(baseUrl)

        choosePath("City Council", cityAgendaPath, cityDownloadBase)
        choosePath("Planning Commission", planningAgendaPath, planningDownloadBase)

        driver.quit()
        return docList
    }

    public void choosePath(String name, String agendaPath, String downloadBase){
        try {
            System.out.println("Beginning " + name + " path")  //debug
            //FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver).ignoring(NoSuchElementException.class);
            //Click Agenda
            WebElement agenda = driver.findElementByXPath(agendaPath)
            String agendaUrl = agenda.getAttribute("href")
            driver.get(agendaUrl) //has target="_blank", can't use agenda.click()
            sleep(5000)
            //Cycle years
            for (int i=current_year-2;i<= current_year + 1; i++) {
                WebElement year = driver.findElementByXPath("//a[@title=\"${i}\"]")
                year.click()
                sleep(3000)
                //Grab list of pdfs
                List <WebElement> pdfs = driver.findElementsByXPath("//span[contains(@class,\"signalFieldValue\")]//a[contains(.,\"pdf\") or contains(.,\"doc\")]")
                //Cycle through pdfs
                for (WebElement pdf : pdfs){
                    //fill out doc data
                    DocumentWrapper doc = new DocumentWrapper()
                    doc.title = pdf.getText()
                    doc.dateStr = pdf.getText()
                    doc.link = downloadBase + i.toString() + "/" + pdf.getText()
                      //debugger
//                    System.out.println("\tTitle: ${doc.title}")
//                    System.out.println("\tDate: ${doc.dateStr}")
//                    System.out.println("\tUrl: ${doc.link}")

                    docList.add(doc)
                }
                driver.navigate().back()
            }
        }
        catch (Exception e) {
            log.debug("\nError along " + name + " path in " + this.class.name)
            log.debug("Selenium crawl error: "+e)
        }
        finally{ driver.navigate().back()}
    }

}
