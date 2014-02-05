JAVAC=javac
sources = $(wildcard cs455/overlay/*/*.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f cs455/overlay/*/*.class
%.class : %.java
	$(JAVAC) -cp . $<
