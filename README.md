loganalyzer
===========
mongod -  start mongo daemon
mong -   start mongo console

use <dbname> to switch to database

database is log_analyzer, collection of this database is called feedbacks
collection has index: db.feedbacks.ensureIndex({PraktijkId: 1, foutcode: 1}, {unique: true});
to ensure uniques over the combined collumns.

start sbt with command:   sbt
than just type run if you are in the application dir.
you can compile with the compile command

In idea the loganalyzer has to be started with SBT because of dependencies on the mongodb drivers
Both the writer and analyzer source files have to be in the sbt dir, if you want to use sbt to build and run the app.



To start debugging a play application use command: 
play debug
nohup play start --> production mode and will continue to run after shell is closed.
play run --> is development mode : In this mode, the server will be launched with the auto-reload feature enabled, meaning that for each request Play will check your project and recompile required sources. If needed the application will restart automatically.
play clean-all -> If something goes wrong and you think that the sbt cache is corrupted, use the clean-all command for your OS command line to clean all generated directories.




Using sbt features

The Play console is just a normal sbt console, so you can use sbt features such as triggered execution.

For example, using ~ compile

[My first application] $ ~ compile

The compilation will be triggered each time you change a source file.

If you are using ~ run

[My first application] $ ~ run

The triggered compilation will be enabled while a development server is running.

You can also do the same for ~ test, to continuously test your project each time you modify a source file:

[My first application] $ ~ test

usefull tips
http://www.playframework.org/documentation/2.0/PlayConsole

to start app in prod mode:
play
start





