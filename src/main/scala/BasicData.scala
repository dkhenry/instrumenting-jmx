
import java.lang.management.ManagementFactory
import javax.management._
import javax.management.openmbean._
import javax.management.remote._
import javax.security.auth.Subject
import javax.management.remote.JMXPrincipal
import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._
import java.rmi.registry.LocateRegistry
import java.net.InetAddress
import java.rmi.registry.Registry

/** 
 * This trait wraps all the DynamicMBean boilerplate
 * If you provide a list of attributes it will construct the mbean for you
 */ 
abstract trait BasicData extends DynamicMBean {
	val attributes: List[(String,String,OpenType[_ <: AnyRef], () => AnyRef)]	
	val functions : List[(String,String,Array[OpenMBeanParameterInfo],OpenType[_ <: AnyRef], Int, () => AnyRef)]
	val dataName: String 
	def typ: String
	def name: String = dataName

	override def getAttribute(arg0: String) :Object = {
		println {" Getting result for " + arg0 }
		attributes.filter( _._1 == arg0) match {
			case x :: xs => x._4()
			case _ => throw new Exception("Attribute " + arg0 + " not defined")								
		}
	}

	override def getAttributes(arg0 :Array[String]) : AttributeList = {
			println {" Getting result for " + arg0.mkString(",") }
			attributes.filter( x => arg0.contains(x._1) ).map { 
				x => new Attribute(x._1,x._4())
			}.foldLeft( new AttributeList() )( (b,a) => {b.add(a) ; b} ) 
	}

	override def getMBeanInfo(): MBeanInfo =  { 
			var arr :Array[OpenMBeanAttributeInfo] = new Array[OpenMBeanAttributeInfo](attributes.length)
			val a = attributes.map { x=> new OpenMBeanAttributeInfoSupport(x._1, x._2, x._3, true, false, false) }
			val f = functions.map { x=> new OpenMBeanOperationInfoSupport(x._1,x._2,x._3,x._4,x._5)}
			new OpenMBeanInfoSupport(this.getClass.getName,dataName , a.toArray , null, f.toArray, null)
	}

	override def invoke(arg0: String, arg1: Array[Object], arg2: Array[String]): Object = {
		println {"Invoking " + arg0 }
		functions.filter( _._1 == arg0) match {
			case x :: xs => x._6()
			case _ => throw new ReflectionException(new NoSuchMethodException(arg0),"Cannot find the operation " + arg0) ;				
		}
		  
	}

	override def setAttribute(arg0: Attribute): Unit = { 
			throw new AttributeNotFoundException("No attributes can be set on this MBean") ;
	}

	override def setAttributes(arg0: AttributeList): AttributeList = {
			throw new AttributeNotFoundException("No attributes can be set on this MBean") ;  
	} 

	def register(mbs: MBeanServer) = { 
		var name = new ObjectName("com.dkhenry:type=" + typ + ",name=" + dataName + "") ; 
		mbs.registerMBean(this, name) ;    
	}

}