=== Import the DB Data

1. Install MySql 5.6+.
2. Unpack all the *.dump and *.sql files from *employees.zip* directly into the *data* folder.
3. Be sure that mysql.exe is in your PATH (eg. on Windows default path to MySql is: c:\Program Files\MySQL\MySQL Server 5.5\bin).
4. Open a command prompt in the *data* folder and launch the following command: mysql.exe -uroot -proot Warning: without those username/password params it may throw error.
5. At the mysql command prompt type: source employees.sql and wait until the dumps are imported.
6. At this point you can connect to the freshly created database with the jdbc connection url: jdbc:mysql://localhost:3306/employees

=== Build the project with Maven

1. Open a command prompt in project's root folder and run the command *mvn clean install*

=== Run the war file in a web container

For example you may import the project into Eclipse (as a Maven project) and add a Tomcat server instance to the servers tab, add the project to it, run it and open it in your web browser.   
