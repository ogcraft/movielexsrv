#!/usr/bin/env python
# -*- coding: utf-8 -*- 

import sys
import time
import smtplib

from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText


emails_file = "data/emails.clj"

def read_emails(fl):
    return open(fl).read().strip()[1:-1].split()

email_addresses = read_emails(emails_file)
#email_addresses = ["ogcraft@gmail.com", "aibolit6661@gmail.com"]

print "Loaded ", len(email_addresses), " emails"

#sys.exit(0)

fromaddr = 'movielex.com@gmail.com'
toaddrs  = 'ogcraft@gmail.com'

# Create the body of the message (a plain-text and an HTML version).
text = "Дорогие друзья,\n с большим опозданием, но все таки появилась русская озвучка к фильму \"Хоббит. битва пяти воинств\".\n Все кто не успел посмотреть фильм в кинотеатре без перевода (или в интернете с переводом) успевайте.\n Загляните на наш сайт www.movielex.com, установите приложение movielex и бежим в кино!\nВаш movielex.com"

html = """\
<html>
  <head></head>
  <body>
    <p>
    Дорогие друзья,<br>
    с большим опозданием, но все таки появилась русская озвучка к фильму «Хоббит - битва пяти воинств».<br> 
    Все кто не успел посмотреть фильм в кинотеатре без перевода (или в интернете с переводом) успевайте.<br> Загляните на наш сайт
     <a href="http://www.movielex.com">www.movielex.com</a>, установите приложение movielex и бежим в кино!"
    </p>
    <p>
    Ваш movielex.com
    </p>
   </body>
</html>
"""


def send_msg(fromaddr, toaddrs, text, html):
    print "Sending email to :", toaddrs 
    # Create message container - the correct MIME type is multipart/alternative.
    msg = MIMEMultipart('alternative')
    msg['Subject'] = "У MovieLex появилась русская озвучка к фильму «Хоббит - битва пяти воинств»"
    msg['From'] = fromaddr
    msg['To'] = toaddrs


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
    username = 'ogcraft@gmail.com'
    password = 'c20h25n3o'

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

count = 0
for a in email_addresses:
    count = count + 1
    print "----------", count, " of ", len(email_addresses), "----------"
    send_msg(fromaddr, a, text, html)
    time.sleep(2)


