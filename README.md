# Multi-User-Chat-Application
Server client based multi user chat application in Java
Idea here is Server-Client
# To run on your machine download or clone it.
Step1 Since it is java project build and run it on your machine
Step2 Run server java main class
Step3 When server is up and running run client
# Functionalities provided by this project
1. User ---> server
    *login/logoff
    *status
2. Server ----> User
    *online / offline
3. User  ----> User
    *direct message
    *brodcast message / group message

Commands
    login <user> <password>
    logoff
    message <user> messageBody
        example: guest:"message John hello"      ---- sender
                 john: "Message From guest hello"-----recieved
    join #topic
        example: sender: "message #topic messageBody"
                 reciever: "Message #topic: from <sender> messagebody"
    leave #topic
