
import scala.collection.JavaConversions._
import java.lang.management.ManagementFactory
import javax.management.openmbean.OpenType
import javax.management.openmbean.SimpleType
import java.rmi.registry.LocateRegistry
import java.net.InetAddress
import java.rmi.registry.Registry
import javax.management.MBeanServerFactory
import javax.management.MBeanServer
import javax.management.remote.JMXServiceURL
import javax.management.remote.JMXConnectorServerFactory
import scala.collection.immutable.HashMap
import javax.management.MBeanOperationInfo
import javax.management.openmbean.OpenMBeanParameterInfo

object Main extends App { 
	var reg: Registry = null //! The registery hold the open port. This _must_ remain in scope for the program to work 
	var stop: Boolean = false;
	var counter: Long = 0;
	/**
	 * This function wraps enabling the JMX system programatically 
	 */
	def enableJmx(address: String, port: Int) = { 
		val mbs = ManagementFactory.getPlatformMBeanServer() match {  
		  case null => { 
		    println { "Platform MBean Server is null creating a new mbs" } ;
			MBeanServerFactory.createMBeanServer();
		  }
		  case m: MBeanServer => m
		}
		
		println { "Platform MBean Server Created: " + mbs }
		println { "JMX enabled on " + "service:jmx:rmi:///jndi/rmi://" + address + ":" + port + "/jmxrmi" }
		  
		val url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + address + ":" + port + "/jmxrmi")						
		val cs = JMXConnectorServerFactory.newJMXConnectorServer(url,new HashMap[String,AnyRef](), mbs)
		
		reg = LocateRegistry.createRegistry(port)
		cs.start() 
		mbs
	}
	
	   println { "Starting JMX Monitoring Tool" } 
	   val addr = InetAddress.getLocalHost().getHostAddress()
	   println { "Enabling JMX on " + addr }
	   val mbs = enableJmx(addr,7009)
	   
	   var data = new BasicData { 
			val attributes = List[(String,String,OpenType[_ <: AnyRef], () => AnyRef)] (
					("epoch","This is the current epoch in seconds",SimpleType.LONG,{ () =>
                                        Long.box(System.currentTimeMillis() / 1000) }),
                    ("counter","This is the current value of the counter",SimpleType.LONG,{ () =>
                                        Long.box(counter) })                    
            )
            val functions: List[(String,String,Array[OpenMBeanParameterInfo],OpenType[_ <: AnyRef], Int, () => AnyRef)] = List(
            		("terminate","Calling this function will terminate the daemon",Array(),SimpleType.VOID,MBeanOperationInfo.ACTION,{ () =>
            			stop = true
            			null
            		}),
            		("increment","Increment the counter",Array(),SimpleType.VOID,MBeanOperationInfo.ACTION,{ () =>
            			counter = counter + 1 
            			null
            		})
            )
            val dataName = "ExampleJMXMBean"
			override def typ = "ExampleJMXMBean"
	   }
	   
	   data.register(mbs)
	   
	   while ( ! stop ) { 
	   		Thread.sleep(1000)
		}
}