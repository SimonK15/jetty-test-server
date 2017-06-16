This is a sample project with jetty and bootique.

Purpose of this project is do deliver a sample webapp which can be used to reproduce a certain jetty issue.

Jetty Version: 9.3.6.v20151106

See: <add jetty bug issue url>
Refer to the [jetty-users] mailinglist topic 'Jetty LoadTests and no available selectors' for the discussion.

-----------------

<h2>The problem:</h2>

After putting Jetty under load the server ends up in a state where no further requests are processed.
New requests from the outside do not seem to reach Jetty anymore.

It can be observed that the opening of the connection is still happening on jetty side, but afterwards nothing happens.

The problem might be that the JDK selector has been closed, or some Jetty component has been stopped.

-----------------

<h2>To reproduce:</h2>

	0. Ideally add some additional parameters to the startup of the server so the server can be monitored via JMX.

		-Djava.net.preferIPv4Stack=true
		-Dcom.sun.management.jmxremote.port=7654
		-Dcom.sun.management.jmxremote
		-Dcom.sun.management.jmxremote.authenticate=false
		-Dcom.sun.management.jmxremote.ssl=false
		-Djava.rmi.server.hostname=[dns-name]"

	1. Start the server (see below)

		Check that POST requests can be send to the following url:
		http://[server]:10001/test/process
		Content does not matter, as it is not read by this sample application.

	2. Run the load test 

	3. If Jmx access is available check that all worker threads (filter by name: boot*) show a stracktrace similar to this one:

		"bootique-http-53" #53 prio=5 os_prio=0 tid=0x00007f4918033800 nid=0x4a5 waiting on condition [0x00007f48eeaf1000]
		java.lang.Thread.State: TIMED_WAITING (parking)
		at sun.misc.Unsafe.park(Native Method)
		- parking to wait for  <0x00000000fb879de0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
		at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
		at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
		at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:392)
		at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:546)
		at org.eclipse.jetty.util.thread.QueuedThreadPool.access$800(QueuedThreadPool.java:47)
		at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:609)
		at java.lang.Thread.run(Thread.java:745) 

	4. If you now send another request nothing happens till the client timeout is reached. The request does not seem to reach a worker 		thread.

-----------------

<h2>Starting the server.</h2>

The main class is: 
de.sk.jetty.App

When starting the program add the following parameters:
--config=config/server_config.yml --start

Note:
The server_config.xml contains the jetty configuration parameters.
The file provided in 'config/server_config.yml' contains a configuration
with a minimal thread pool.

The size of the thread pool does not seem to be relevant for this issue.

Note 2:
mvn clean package builds a runnable jar.

-----------------

<h2>Load Test:</h2>

We use a tsung master/client setup for generating enough load to 
get Jetty into the 'bad' state.

See 'etc/tsung.xml' for an example configuration.
(addresses for client and server need to be added). 

-----------------

<h2>Jetty dump - after it got stuck</h2>

	org.eclipse.jetty.server.Server@24c22fe - STARTED
	 += bootique-http{STARTED,1<=21<=21,i=19,q=0} - STARTED
	 |   +- 50 bootique-http-50 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 18 bootique-http-18 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 26 bootique-http-26 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 35 bootique-http-35 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 24 bootique-http-24 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 49 bootique-http-49 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 46 bootique-http-46 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 48 bootique-http-48 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 41 bootique-http-41 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 44 bootique-http-44 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 19 bootique-http-19 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 21 bootique-http-21-acceptor-0@3b1bb3ab-ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001} BLOCKED @ sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:233) prio=3
	 |   +- 36 bootique-http-36 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 20 bootique-http-20 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 37 bootique-http-37 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 39 bootique-http-39 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 40 bootique-http-40 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 22 bootique-http-22-acceptor-1@5a4bef8-ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001} RUNNABLE @ sun.nio.ch.ServerSocketChannelImpl.accept0(Native Method) prio=3
	 |   +- 17 bootique-http-17 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 43 bootique-http-43 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 |   +- 47 bootique-http-47 TIMED_WAITING @ sun.misc.Unsafe.park(Native Method) IDLE
	 += o.e.j.s.ServletContextHandler@495ee280{/,null,AVAILABLE} - STARTED
	 |   += org.eclipse.jetty.servlet.ServletHandler@39d76cb5 - STARTED
	 |   |   += jersey@baa68670==org.glassfish.jersey.servlet.ServletContainer,-1,true - STARTED
	 |   |   +- [/*]=>jersey
	 |   |   +~ org.eclipse.jetty.jmx.MBeanContainer@12299890
	 |   |   += org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-68034211@2dd4e877==org.eclipse.jetty.servlet.ServletHandler$Default404Servlet,-1,false - STARTED
	 |   |   +- [/]=>org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-68034211
	 |   +~ org.eclipse.jetty.jmx.MBeanContainer@12299890
	 |   |
	 |   +> No ClassLoader
	 |   +> Handler attributes o.e.j.s.ServletContextHandler@495ee280{/,null,AVAILABLE}
	 |   |   +- org.eclipse.jetty.server.Executor=bootique-http{STARTED,1<=21<=21,i=19,q=0}
	 |   +> Context attributes o.e.j.s.ServletContextHandler@495ee280{/,null,AVAILABLE}
	 |   |   +- org.eclipse.jetty.util.DecoratedObjectFactory=org.eclipse.jetty.util.DecoratedObjectFactory[decorators=0]
	 |   +> Initparams o.e.j.s.ServletContextHandler@495ee280{/,null,AVAILABLE}
	 += ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001} - STARTED
	 |   +~ org.eclipse.jetty.server.Server@24c22fe - STARTED
	 |   +~ bootique-http{STARTED,1<=21<=21,i=19,q=0} - STARTED
	 |   += org.eclipse.jetty.util.thread.ScheduledExecutorScheduler@6f8e8894 - STARTED
	 |   |   +- sun.misc.Unsafe.park(Native Method)
	 |   |   +- java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
	 |   |   +- java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
	 |   |   +- java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
	 |   |   +- java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
	 |   |   +- java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1067)
	 |   |   +- java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1127)
	 |   |   +- java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	 |   |   +- java.lang.Thread.run(Thread.java:745)
	 |   +- org.eclipse.jetty.io.ArrayByteBufferPool@41394595
	 |   += HttpConnectionFactory@6531a794[HTTP/1.1] - STARTED
	 |   |   +- HttpConfiguration@40f33492{32768/8192,8192/8192,https://:0,[ForwardedRequestCustomizer@10e26e25]}
	 |   |   +~ org.eclipse.jetty.jmx.MBeanContainer@12299890
	 |   += org.eclipse.jetty.server.ServerConnector$ServerConnectorManager@30b34287 - STARTED
	 |   |   +- org.eclipse.jetty.io.ManagedSelector@251bd1d8 id=0 keys=497 selected=0 id=0
	 |   |   +- org.eclipse.jetty.io.ManagedSelector@1f6cf02f id=1 keys=266 selected=0 id=1
	 |   |   +- org.eclipse.jetty.io.ManagedSelector@6402770b id=2 keys=273 selected=0 id=2
	 |   |   +- org.eclipse.jetty.io.ManagedSelector@7c89f880 id=3 keys=1042 selected=1 id=3
	 |   +~ org.eclipse.jetty.jmx.MBeanContainer@12299890
	 |   +- sun.nio.ch.ServerSocketChannelImpl[/0.0.0.0:10001]
	 |   +- bootique-http-21-acceptor-0@3b1bb3ab-ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001}
	 |   +- bootique-http-22-acceptor-1@5a4bef8-ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001}
	 +- org.eclipse.jetty.jmx.MBeanContainer@12299890
	 |   +- org.eclipse.jetty.servlet.ServletHandler@39d76cb5=org.eclipse.jetty.servlet:context=ROOT,type=servlethandler,id=0
	 |   +- [/]=>org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-68034211=org.eclipse.jetty.servlet:context=ROOT,type=servletmapping,name=org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-68034211,id=0
	 |   +- bootique-http-21-acceptor-0@3b1bb3ab-ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001}=org.eclipse.jetty.server:context=HTTP/1.1@2fba3fc4,type=abstractconnector$acceptor,id=0
	 |   +- ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001}=org.eclipse.jetty.server:context=HTTP/1.1@2fba3fc4,type=serverconnector,id=0
	 |   +- org.eclipse.jetty.io.ArrayByteBufferPool@41394595=org.eclipse.jetty.io:context=HTTP/1.1@2fba3fc4,type=arraybytebufferpool,id=0
	 |   +- HttpConnectionFactory@6531a794[HTTP/1.1]=org.eclipse.jetty.server:context=HTTP/1.1@2fba3fc4,type=httpconnectionfactory,id=0
	 |   +- org.eclipse.jetty.server.Server@24c22fe=org.eclipse.jetty.server:type=server,id=0
	 |   +- org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-68034211@2dd4e877==org.eclipse.jetty.servlet.ServletHandler$Default404Servlet,-1,false=org.eclipse.jetty.servlet:context=ROOT,type=servletholder,name=org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-68034211,id=0
	 |   +- org.eclipse.jetty.jmx.MBeanContainer@12299890=org.eclipse.jetty.jmx:context=ROOT,type=mbeancontainer,id=0
	 |   +- bootique-http-22-acceptor-1@5a4bef8-ServerConnector@2fba3fc4{HTTP/1.1,[http/1.1]}{0.0.0.0:10001}=org.eclipse.jetty.server:context=HTTP/1.1@2fba3fc4,type=abstractconnector$acceptor,id=1
	 |   +- sun.nio.ch.ServerSocketChannelImpl[/0.0.0.0:10001]=sun.nio.ch:context=HTTP/1.1@2fba3fc4,type=serversocketchannelimpl,id=0
	 |   +- HttpConfiguration@40f33492{32768/8192,8192/8192,https://:0,[ForwardedRequestCustomizer@10e26e25]}=org.eclipse.jetty.server:context=HTTP/1.1@2fba3fc4,type=httpconfiguration,id=0
	 |   +- bootique-http{STARTED,1<=20<=21,i=18,q=0}=org.eclipse.jetty.util.thread:type=queuedthreadpool,id=0
	 |   +- org.eclipse.jetty.server.ServerConnector$ServerConnectorManager@30b34287=org.eclipse.jetty.server:context=HTTP/1.1@2fba3fc4,type=serverconnector$serverconnectormanager,id=0
	 |   +- jersey@baa68670==org.glassfish.jersey.servlet.ServletContainer,-1,true=org.eclipse.jetty.servlet:context=ROOT,type=servletholder,name=jersey,id=0
	 |   +- o.e.j.s.ServletContextHandler@495ee280{/,null,AVAILABLE}=org.eclipse.jetty.servlet:context=ROOT,type=servletcontexthandler,id=0
	 |   +- [/*]=>jersey=org.eclipse.jetty.servlet:context=ROOT,type=servletmapping,name=jersey,id=0
	 |   +- org.eclipse.jetty.util.thread.ScheduledExecutorScheduler@6f8e8894=org.eclipse.jetty.util.thread:context=HTTP/1.1@2fba3fc4,type=scheduledexecutorscheduler,id=0
	 |
	 +> sun.misc.Launcher$AppClassLoader@764c12b6
     +- file:/home/user/logger-server/jetty-test-server-0.0.1-SNAPSHOT.jar
     +- sun.misc.Launcher$ExtClassLoader@17f052a3
