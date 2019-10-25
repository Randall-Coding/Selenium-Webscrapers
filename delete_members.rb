# Summary: Delete_members.rb deletes members from an organization based on an excel spreadsheet.
# Spreadsheet: Must contain column to_number
# Target Site: Telefio.com
# Specs: Ruby 2.5.1 ChromeDriver 77, Selenium 3.14 (Gecko seems to suffer from same issue as Firefox which won't load on my laptop)
require "selenium-webdriver"
require 'csv'
require './delete_members_helper.rb'
include DeleteMembersHelper

# Parse command line arguments: <number list csv> and <organization id>
filename, org_id = parseArguments(ARGV.size)

# Initial messages
puts 'Deleting customers listed in "' + filename + '" from organization ' + org_id

# CSV to list
numbers_list = []
CSV.foreach(filename, {headers:true}) do |row|
  numbers_list << row['To_number']
end

# Initialize driver
options = Selenium::WebDriver::Chrome::Options.new
# options.headless! ## Uncomment for headless browser
driver = Selenium::WebDriver.for(:chrome, options:options)

# Sign in
sign_in(ENV['TELEFIO_ADMIN_USER'],ENV['TELEFIO_ADMIN_PASS'],driver)

# Visit member list page
driver.get("https://telefio.com/account/#{org_id}/members")

# Process each number
numbers_list.each do |number|
  begin
    delete_member(number,driver)
  rescue Selenium::WebDriver::Error::NoSuchElementError => e
    puts 'WARNING: Row not found in ' + caller[0]
    puts e
  rescue Exception => e
     puts "ERROR Unexpected exception in " + caller[0]
     puts e
     puts e.class
  end

  # Back to members list
  driver.get("https://telefio.com/account/#{org_id}/members")
end #end Numbers Loop

puts "delete_members.rb is DONE"
