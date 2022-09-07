# Doxis4Helpers
## Hello everyone!
#### This is the source project of Doxis4Helpers. Initially it was created by: Leonid Iashin, Konstantin Razinkov and Valentin Drazdov.
#### We are using these libs in all our projects we take part in. The main goal of these libs is to make coding agents, webCube scripting and mobileCube scripting easier!
#### All in all, these libs allow to economy your time and reduce your code lines. How is it done? Simply. These libs contain a lot of pre-compiled methods that are almost always the same, but you need to define them in every script. Using these libs you can simply call the method in 1 line and that's it!
#### For example: you do not need to create a Descriptor object, create ValueDescriptor etc. Just call SetDescriptorValue() method and that's it! And this is just one method out of dozens of them.
## Which libs does this project contain:
## 1) Archiver
#### With this lib you can easily work with ZIP files (create, add, archive etc), check Hash and produce ISO
## 2) Commons
#### This is one of the most popular libs. Using it you can easily (in one line) do: 
#### 2.1) Classes - work with Doxis4 Archive Classes. 
#### 2.2) Connector - make easy connections to Doxis4.
#### 2.3) Descriptors - do almost everything with descriptors and their values (actually one of the most popular classes).
#### 2.4) GlobalValueLists - do almost everything with Global Value Lists (GVL)
#### 2.5) InformationObjects - do almost everything with Information Objects (search for documents, add content, init)
#### 2.6) Other classes provide some options to: download documents from web (from link), translate text, work with semaphores
## 3) Documents
#### This library allows you to easily work with such document types like: Doxis4Template, EXCEL, PDF and Word files
#### 3.1) doxis4template - with it you can load template document, see its XML, analyse it, see mapping and edit it
#### 3.2) excel - this is one of the biggest parts of this class. With it, you can generate/edit EXCEL file, add cells, edit cells, set values, set parameters (align, merge, wrap etc.)
#### 3.3) pdf - allows you to convert any document type into PDF
#### 3.4) word - allows you to get word file from Doxis4 as ZIP-archive. This gives you an ability to edit mappings inside of word.
## 4) EDM
#### This is a huge library for integration of Doxis4 and Diadoc platform. Diadoc is not used in most of the projects, so it is not up-to-date. Maybe one day...
## 5) Notifications
#### The main goal of this lib is to simplify your work with Doxis4 email notifications
#### 5.1) Connector - allows you to easily connect to mail SMTP server.
#### 5.2) Sender - using this class you can send an email notification to recipient with subject, body and attachments in almost 1 line of code.
#### 5.3) 'locale' - here is a pre-defined body and subject of the notification email. Currently, there are EN and RU locale files. For sure, we can add more locales.
#### 5.4) TaskNotificationBuilder - with this class you can send notification, get recipients, generate links to InformationObject to webCube/winCube/mobileCube.
## 6) SQL
#### This lib was created to simplify SQL connections and allow executing SQL scripts
#### 6.1) datatypes - this module "describes" all required SQL data. You can get SQL columns or update them.
#### 6.2) SqlConnector - this class allows you to set/close connection, execute and query SQL scripts.
## 7) WebCube
#### This library was developed to simplify your code in webCube scripting. With it, you can easily operate with Dialogs, fields, values etc. 
#### 7.1) Dialogs - the most popular class in this lib. Using it, you can easily get fields, their values, parameters, set and edit them. You can also copy values from InformationObjects into current Dialogs and vice-verse as well as check if all mandatory fields are filled in.
#### 7.2) ImageComparator - allows you to compare images and make their copies.
#### 7.3) RibbonProducer - with this class you can get and add ribbon buttons.
#### Mostly, this is all basic information about this project and libs. FOr sure, you're welcome to use it in your projects. I'd like to add that I am 100% sure that is you spend a little of your time checking methods from these libs (you can use javadoc for that) and test them in your code - it will economy you a lot of time in future (as well as it will reduce the amount of lines in your scripts)!
#### This project as well as compiled libs were build on the last versions of Doxis4 Java API (v.8.2.2) and using Azul Java 11.0.16 so it will work on the latest Doxis4 CSB versions as well as on webCube v9.
#### You can find source project and all latest releases on [Doxis4Helpers GitHub](https://github.com/KonstantinRazinkov/Doxis4Helpers)
#### If you have any ideas/complaints/questions, or you've discovered some bugs and errors - please let me know by creating a ticket/card in [Trello](https://trello.com/invite/b/rEeQdkYV/6bc336eb6e93beb6903db72a445dc8b1/doxis4helpers)
## Best regards and many thanks for using Doxis4Helpers, Konstantin and Leonid
