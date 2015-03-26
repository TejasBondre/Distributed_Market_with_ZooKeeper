JFLAGS = -g -cp zookeeper-3.4.6/zookeeper-3.4.6.jar:zookeeper-3.4.6/lib/*
JC = javac#~/../../usr/lib/jvm/java-6-openjdk/bin/javac
LIB = 
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        *.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

