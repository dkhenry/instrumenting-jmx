
import scala.collection.JavaConversions._
import java.lang.management.ManagementFactory
import javax.management.openmbean.OpenType
import javax.management.openmbean.SimpleType

object Main extends App { 
	   println { "Starting JMX Monitoring Tool" } 
	   var mbs = ManagementFactory.getPlatformMBeanServer()
	   
	   var data = new BasicData { 
			val attributes = List[(String,String,OpenType[_ <: AnyRef], () => AnyRef)] (
					("epoch","This is the current epoch in seconds",SimpleType.LONG,{ () =>
                                        Long.box(System.currentTimeMillis() / 1000) })
            )
            val dataName = "ExampleJMXMBean"
			override def typ = "ExampleJMXMBean"
	   }
	   
	   data.register(mbs)
	   
	   while ( 1 == 1 ) { 
	   		Thread.sleep(1000)
		}
}