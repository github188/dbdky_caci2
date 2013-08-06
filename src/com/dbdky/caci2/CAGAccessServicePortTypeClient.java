package com.dbdky.caci2;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
d
public class CAGAccessServicePortTypeClient {

	private static String EndPointUrl;
	private static String QUrl = "http://ws.apache.org/axis2";
	// private static String QUrl = "http://tempuri.org/";
	private QName opAddEntry;
	public String WSUrl = "http://172.168.1.33:8099/CACService.asmx";

	// public String WSUrl = "http://localhost:3597/CAGService.asmx";

	public RPCServiceClient setOption() throws AxisFault {
		RPCServiceClient serviceClient = new RPCServiceClient();
		Options options = serviceClient.getOptions();
		options.setProperty(
				org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
				Boolean.FALSE);
		EndpointReference targetEPR = new EndpointReference(WSUrl);

		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		// options.setAction("http://ws.apache.org/axis2/uploadCMAHeartbeatInfo");

		// options.setProperty(propertyKey, property)

		// enabling MTOM in the client side
		// options.setProperty(Constants.Configuration.ENABLE_MTOM,
		// Constants.VALUE_TRUE);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setTo(targetEPR);

		return serviceClient;
	}

	public QName getQname(String Option) {

		return new QName(QUrl, Option);
	}

	// 返回String
	public String getStr(String Option, String strXMLParams) throws AxisFault {
		RPCServiceClient serviceClient = this.setOption();

		opAddEntry = this.getQname(Option);

		String str = (String) serviceClient.invokeBlocking(opAddEntry,
				new String[] { strXMLParams }, new Class[] { String.class })[0];

		return str;
	}

	// 返回一维String数组
	public String[] getArray(String Option) throws AxisFault {
		RPCServiceClient serviceClient = this.setOption();

		opAddEntry = this.getQname(Option);

		String[] strArray = (String[]) serviceClient.invokeBlocking(opAddEntry,
				new Object[] {}, new Class[] { String[].class })[0];
		return strArray;
	}

	// 从WebService中返回一个对象的实例
	public Object getObject(String Option, Object o) throws AxisFault {
		RPCServiceClient serviceClient = this.setOption();
		QName qname = this.getQname(Option);
		Object object = serviceClient.invokeBlocking(qname, new Object[] {},
				new Class[] { o.getClass() })[0];
		return object;
	}

	public String uploadCACData(String sTempXml) throws AxisFault {
		return getStr("uploadCMAData", sTempXml);
	}

	public String uploadCACHeartbeatInfo(String sTempXml) throws AxisFault {
		return getStr("uploadCMAHeartbeatInfo", sTempXml);
	}

}
