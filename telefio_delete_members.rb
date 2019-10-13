# Summary: Delete_members.rb deletes members from an organization based on an excel spreadsheet.
# Spreadsheet: Must contain column to_number
# Target Site: Telefio.com
# Specs: Ruby 2.5.1 ChromeDriver 77, Selenium 3.14 (Gecko seems to suffer from same issue as Firefox which won't load on my laptop)

require "selenium-webdriver"
require 'csv'

# Parse command arguments
case ARGV.size
when 0
  puts 'filename?'
  filename = gets
  puts 'org_id?'
  org_id = gets
  puts 'Warning: using default values'
when 1
  filename = ARGV[0]
  org_id = '5572'
when 2
  filename = ARGV[0]
  org_id = ARGV[1]
end

numbers_list = []

puts 'Deleting customers listed in "' + filename + '" from organization ' + org_id

# CSV to list
CSV.foreach(filename, {headers:true}) do |row|
  numbers_list << row['To_number']
end

options = Selenium::WebDriver::Chrome::Options.new
## Uncomment for headless browser
options.headless!
driver = Selenium::WebDriver.for(:chrome, options:options)

# Sign in
driver.get('https://telefio.com/members/sign_in')

username = driver.find_element(xpath:'//input[@id="member_email"]')
username.send_keys(ENV['TELEFIO_ADMIN_USER'])

password = driver.find_element(xpath: '//*[@id="member_password"]')
password.send_keys(ENV['TELEFIO_ADMIN_PASS'])

submit =  driver.find_element(xpath: '//input[translate(@value, "LOGIN", "login") = "log in"]')
submit.click()

# Visit member list page
driver.get('https://telefio.com/account/5572/members')

# Process each number
numbers_list.each do |number|
  # Search phone number
  query = driver.find_element(xpath:"//input[@name='query']")
  query.send_keys(number)
  query.send_keys(:enter)

  begin
    # Check for element row
    row = driver.find_element(xpath:"//*[@id='import-container']/following-sibling::table/tbody/tr[1][td[2][text()='#{number}']]")
    # Click delete on the first row
    delete = row.find_element(xpath:'.//a[@data-method="delete"]')
    delete.click()
    puts "Deleted one for " + number
    # sleep(1)
  rescue Selenium::WebDriver::Error::NoSuchElementError => e
    puts 'WARNING: Row not found in ' + caller[0]
    puts e
  rescue Exception => e
     puts "ERROR Unexpected exception in " + caller[0]
     puts e
     puts e.class
  end

  # Back to members list
  driver.get('https://telefio.com/account/5572/members')
  # sleep(2)
end #end Numbers Loop

puts "DONE"

