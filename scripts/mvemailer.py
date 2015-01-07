#!/usr/bin/env python

import smtplib

from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText


fromaddr = 'ogcraft@gmail.com'
toaddrs  = 'olegg@waves.com'


# Create message container - the correct MIME type is multipart/alternative.
msg = MIMEMultipart('alternative')
msg['Subject'] = "Link"
msg['From'] = fromaddr
msg['To'] = toaddrs

# Create the body of the message (a plain-text and an HTML version).
text = "Hi!\nHow are you?\nHere is the link you wanted:\nhttps://www.python.org"
html = """\
<html>
  <head></head>
  <body>
    <p>Hi!<br>
       How are you?<br>
       Here is the <a href="https://www.python.org">link</a> you wanted.
    </p>
  </body>
</html>
"""

# Record the MIME types of both parts - text/plain and text/html.
part1 = MIMEText(text, 'plain')
part2 = MIMEText(html, 'html')

# Attach parts into message container.
# According to RFC 2046, the last part of a multipart message, in this case
# the HTML message, is best and preferred.
msg.attach(part1)
msg.attach(part2)

# Send the message via local SMTP server.
# Credentials (if needed)
username = ''
password = ''

# The actual mail send
server = smtplib.SMTP_SSL('smtp.gmail.com:465')
#server = smtplib.SMTP('smtp.gmail.com:587')
#server.starttls()

# sendmail function takes 3 arguments: sender's address, recipient's address
# and message to send - here it is sent as one string.

server.login(username,password)
server.sendmail(fromaddr, toaddrs, msg.as_string())
#server.sendmail(fromaddr, toaddrs, msg)
server.quit()




# import smtplib

# fromaddr = 'ogcraft@gmail.com'
# toaddrs  = 'olegg@waves.com'
# msg = 'There a test message!'


# # Credentials (if needed)
# username = 'ogcraft'
# password = 'c20h25n3o'

# # The actual mail send
# server = smtplib.SMTP_SSL('smtp.gmail.com:465')
# #server = smtplib.SMTP('smtp.gmail.com:587')
# #server.starttls()
# server.login(username,password)
# server.sendmail(fromaddr, toaddrs, msg)
# server.quit()

