JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java
	
CLASSES = \
	server/ServerManager.java \
	client/ClientManager.java
	
default: classes

classes: $(CLASSES:.java=.class)

clean: 
	$(RM) server/*.class
	$(RM) client/*.class
	
start:
	rmiregistry &
	sleep 3
	xterm -e "java server.ServerManager Razorback1" &
	sleep 3
	xterm -e "java server.ServerManager Razorback2" &
	sleep 5
	xterm -e "java client.ClientManager C1 Razorback1 3 A 1 B 4 C 6" &
	xterm -e "java client.ClientManager C2 Razorback2 3 A 1 D 2" &
	xterm -e "java client.ClientManager C3 Razorback2 3 E 2" &
	
stop:
	killall -q rmiregistry &
	sleep 1
	killall -q xterm &
