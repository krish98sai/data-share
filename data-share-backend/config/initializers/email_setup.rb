if Rails.env.development?

  ActionMailer::Base.delivery_method = :smtp
  ActionMailer::Base.smtp_settings = {
    address:              'smtp.gmail.com',
    port:                 587,
    domain:               '192.168.99.100',
    user_name:            ENV["GMAIL_USERNAME"],
    password:             ENV["GMAIL_PASSWORD"],
    authentication:       'plain',
    enable_starttls_auto: true
  }

elsif Rails.env.production?

  ActionMailer::Base.delivery_method = :smtp
  ActionMailer::Base.smtp_settings = {
    address:              'smtp.sendgrid.net',
    port:                 587,
    domain:               'heroku.com',
    user_name:            ENV["SENDGRID_USERNAME"],
    password:             ENV["SENDGRID_PASSWORD"],
    authentication:       'plain',
    enable_starttls_auto: true
  }

end