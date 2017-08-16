Name : Agamani Parida

Assignment name : Program Assignment1

Date : 02/08/2017

High-level description of the assignment and what your program(s) does - 

Its a functional web server which listens for connections on a socket ( bound to a specific port on a host machine). 
Client connects to this socket and retrieve index.html file from server and send response using multithreading.
-It retrieves GET request only. Supports index.html files and image files.
-It gives back response code to header (content type, content-length, date)
-It supports status code 200(HTTP OK) , 404 (file not found), 403 (access forbidden) , 400 (Bad Request)
-It also supports HTTP/1.1 persistent connection. Connection gets time out after 50000 millisecond.
-The response method handles all error handling and request from client
-If file is not present it will throw 404 file not found.
-If access permission changes to read only from read write, it will throw 403 forbidden error.
-If http protocol is other than 1.0 and 1.1, it will throw 400 bad request
-If everything is OK, it will show 200 HTTP OK.
-For HTTP/1.1 it also throws TIMED OUT error after 50000millisecond.


A list of submitted files - WebServer2.java, ReadMe.txt, Script.pdf , MakeFile, index.html, images files

Instructions for running your program: Run from command line -
 1) Compile :javac WebServer2.java
 2) Run: java WebServer2 -document_root "give directory path where scu.edu/index.html downloaded" -port 'Give your port number(e.g. 8002)'
 3) Compile from outside directory - javac “/give directory path where java file present/“ WebServer2.java
 4) Run from outside directory - java -cp "/give directory path where java file present/“ WebServer -document_root “/give directory path where scu.edu/index.html downloaded/“ -port 8002

Any other information you want us to know: I tried implementing part 2 of the question that is for extra credit, which I implemented in the same java file that is WebServer2.java. Script file has all the screenshots of main program implementation and error handling.