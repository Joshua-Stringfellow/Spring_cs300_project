#!/usr/bin/env bash
javac edu/cs300/*java
javac CtCILibrary/*.java
javac -h . edu/cs300/MessageJNI.java
export JAVA_HOME=/usr/lib/jvm/default-java
gcc -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux system5_msg.c -o edu_cs300_MessageJNI.o
gcc -shared -o libsystem5msg.so edu_cs300_MessageJNI.o -lc
gcc -std=c99 -D_GNU_SOURCE search_manager.c -o msgsnd
#gcc -std=c99 -D_GNU_SOURCE msgrcv_lwr.c -o msgrcv
./msgsnd con &
#java -cp . -Djava.library.path=. edu.cs300.MessageJNI
#./msgrcv
java -cp . -Djava.library.path=. edu.cs300.PassageProcessor
#java -cp . -Djava.library.path=. edu.cs300.ParallelTextSearch con
#java edu.cs300.PassageProcessor
