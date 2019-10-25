module DeleteMembersHelper
  # This method yields filename and organization id from command line arguments
  def parseArguments(number_of_args)
    case number_of_args
    when 0
      puts 'enter filename'
      filename = gets
      puts 'enter org_id'
      org_id = gets
      puts 'Warning: using default values'
    when 1
      filename = ARGV[0]
      org_id = '5572'
    when 2
      filename = ARGV[0]
      org_id = ARGV[1]
    end
    return filename,org_id
  end

  # This method signs in
  def sign_in(username,password,driver)
    driver.get('https://telefio.com/members/sign_in')

    username_input = driver.find_element(xpath:'//input[@id="member_email"]')
    username_input.send_keys(username)

    password_input = driver.find_element(xpath: '//*[@id="member_password"]')
    password_input.send_keys(password)

    submit =  driver.find_element(xpath: '//input[translate(@value, "LOGIN", "login") = "log in"]')
    submit.click()
  end

  # This method deletes a single member by phone number
  def delete_member(number,driver)
    # Search phone number
    query = driver.find_element(xpath:"//input[@name='query']")
    query.send_keys(number)
    query.send_keys(:enter)

    # Check for element row
    row = driver.find_element(xpath:"//*[@id='import-container']/following-sibling::table/tbody/tr[1][td[2][text()='#{number}']]")
    # Click delete on the first row
    delete = row.find_element(xpath:'.//a[@data-method="delete"]')
    delete.click()
    puts "Deleted member for tele: " + number
  end
end
