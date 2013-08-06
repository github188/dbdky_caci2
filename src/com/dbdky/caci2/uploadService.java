package com.dbdky.caci2;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import org.apache.axis2.AxisFault;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dk.caci2.CAGAccessServicePortTypeClient;
import com.dk.caci2.uploadService.HeartbeatTask;
import com.dk.caci2.uploadService.UploadDataTask;
import com.dk.caci2.IniClass;


import javax.xml.parsers.*;

public class uploadService {
	
	private String _Path ="c:\\config.ini";	//Path need declaring
	private String _Logpath = "c:\\log.txt";	//Path need declaring
	private String _UploadTime = "";
	private String ConnectionString = "";
    private Timer heartbeatTimer = null;
    private Timer UploadTimer = null;
    private DBConnection cCACDBCon = null;
    
    //�ȴ�����ӿ�
    private CAGAccessServicePortTypeClient cac = new CAGAccessServicePortTypeClient();   
    
    public uploadService()
    {
    	//Init Service
    	InitializeService();
    	//C#�����ַ���
    	//ConnectionString = "server=NEPRISERVER;uid=sms;pwd=~1q;database=OMSDB";
    	
    	//cCACDBCon = new DBConnection("jdbc:sqlserver://192.168.0.1:8080; DatabaseName=OMSDB", "sms", "~1q");
    	//cCACDBCon = new DBConnection("jdbc:sqlserver://localhost:1433; DatabaseName=OMSDB", "SMS", "~1q");
    	cCACDBCon = new DBConnection("jdbc:sqlserver://localhost\\SQLEXPRESS; DatabaseName=OMSDB; Integrated Security=True;Connect Timeout=30;User Instance=True ", "SMS", "~1q");

    	String sDate = GetIniInfo("LastUpLoadTime");
		//sDate = String.format("{0:yyyy-MM-dd HH:mm:ss}", sDate);
    	if (sDate.equals(""))
			sDate = getNowDate("yyyy-MM-dd HH:mm:ss");
		UpLoadMoniData(sDate);
    	 
    }
    
    private String getNowDate(String format) {
		Date date = new Date();
		String str = null;
		SimpleDateFormat df = new SimpleDateFormat(format);
		str = df.format(date);
		return str;
	}

	//�����ʼ��
    private void InitializeService()
    {
    	//heartbeatTimer = new Timer();
		//heartbeatTimer.schedule(new HeartbeatTask(), 0, 100000);
		
		//UploadTimer = new Timer();
		//UploadTimer.schedule(new UploadDataTask(), 0, 100000);
    }
    
    //����Task
    public class HeartbeatTask extends java.util.TimerTask
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			UpLoadHeartbeat();
		}
    	
    }
    
    
    //�����ϴ�����Task
    public class UploadDataTask extends java.util.TimerTask 
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			String sDate = GetIniInfo("LastUpLoadTime");
			sDate = String.format("{0:yyyy-MM-dd HH:mm:ss}", sDate);
			
			Date dateNow = new Date();
	    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	String sLastTime =  simpleDateFormat.format(dateNow);
			
	    	if("" == sDate)
	    	{
	    		sDate = sLastTime;
	    	}
	    	
	    	String s = UpLoadMoniData(sDate);
	    	
	    	if (s.equals("")) {
				TxtWrite("null--" + sLastTime, _Logpath);
				SetIniInfo("LastUpLoadTime", sLastTime);
			} else {
				StringReader read = new StringReader(s);
				//�����µ�����ԴSAX ��������ʹ�� InputSource ������ȷ����ζ�ȡ XML ����
				InputSource source = new InputSource(read);
				//����һ���µ�SAXBuilder
				SAXBuilder sb = new SAXBuilder();
				
				try {
				    //ͨ������Դ����һ��Document
				    Document doc = (Document) sb.build(source);

		            //ȡ�ĸ�Ԫ��
		            Element root = ((org.jdom2.Document) doc).getRootElement();
		            //System.out.println("tasktypename:"+root.getAttributeValue("tasktypename"));
		            //System.out.println("perfrenceNum:"+root.getAttributeValue("perfrenceNum"));
		            System.out.println(root.getName());//�����Ԫ�ص����ƣ����ԣ�
		            //�õ���Ԫ��������Ԫ�صļ���
		            List jiedian = root.getChildren();

				    sLastTime = String.format("{0:yyyy-MM-dd HH:mm:ss}",
						sLastTime);
				    int n = jiedian.size();
				    Element et = null;
		            for(int i=0;i<jiedian.size();i++){
		                et = (Element) jiedian.get(i);//ѭ�����εõ���Ԫ��
		                
		                if(et.getAttributeValue("code").equals("0")){
		                	SetIniInfo("LastUpLoadTime", sLastTime);
					    	TxtWrite("0--" + sLastTime, _Logpath);
		                }
		                else if(et.getAttributeValue("code").equals("1")){
		                	SetIniInfo("LastUpLoadTime", sLastTime);
					    	TxtWrite("1--" + sLastTime, _Logpath);
		                }
		                
		            }
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
    }
	/*
	 * Description: timer for heartbeat and uploading data
	 */
	public void StartService() {
		if(null == cac)
    	{
    		//�ȴ�����ӿ�
    		cac = new CAGAccessServicePortTypeClient();
    	}
    	else
    	{
    		//�ȴ�����ӿ�
    		//@@@@@?????????????????cac.close();
    		cac = new CAGAccessServicePortTypeClient();
    	}
    	
    	
    	int iUploadElapsed = GetUpLoadRound();
    	String sHeartBeatElapsed = GetIniInfo("HearBeatTime");
    	int iHeartBeatElapsed = Integer.parseInt(sHeartBeatElapsed);
    	
    	
    	if(heartbeatTimer != null)
    	{//�ǳ�������,��ͣ��֮ǰ��Timer
    		heartbeatTimer.cancel();
    	}
    	
    	//���ò���������Timer
    	heartbeatTimer = new Timer();
		heartbeatTimer.schedule(new HeartbeatTask(), 0, iHeartBeatElapsed);
		
		if(UploadTimer != null)
		{//�ǳ�������,��ͣ��֮ǰ��Timer
			UploadTimer.cancel();
		}
		
		//���ò������ϴ�����Timer
		UploadTimer = new Timer();
		UploadTimer.schedule(new UploadDataTask(), 0, 100000);  	//@@@@@@ open UploadTimer.schedule(new UploadDataTask(), 100000);
	}
    //��������
    public void OnStart()
    {
    	if(null == cac)
    	{
    		//�ȴ�����ӿ�
    		cac = new CAGAccessServicePortTypeClient();
    	}
    	else
    	{
    		//�ȴ�����ӿ�
    		//@@@@@?????????????????cac.close();
    		cac = new CAGAccessServicePortTypeClient();
    	}
    	
    	//C#�����ַ���
    	ConnectionString = "server=NEPRISERVER;uid=sms;pwd=~1q;database=OMSDB";
    	
    	cCACDBCon = new DBConnection("jdbc:sqlserver://192.168.0.1:8080; DatabaseName=OMSDB", "sms", "~1q");
    	
    	int iUploadElapsed = GetUpLoadRound();
    	String sHeartBeatElapsed = GetIniInfo("HearBeatTime");
    	int iHeartBeatElapsed = Integer.parseInt(sHeartBeatElapsed);
    	
    	
    	if(heartbeatTimer != null)
    	{//�ǳ�������,��ͣ��֮ǰ��Timer
    		heartbeatTimer.cancel();
    	}
    	
    	//���ò���������Timer
    	heartbeatTimer = new Timer();
		heartbeatTimer.schedule(new HeartbeatTask(), 0, iHeartBeatElapsed);
		
		if(UploadTimer != null)
		{//�ǳ�������,��ͣ��֮ǰ��Timer
			UploadTimer.cancel();
		}
		
		//���ò������ϴ�����Timer
		UploadTimer = new Timer();
		UploadTimer.schedule(new UploadDataTask(), 0, 100000);    	
    }
    
    //����ֹͣ
    public void OnStop()
    {
    	cCACDBCon = null;
    	
    	heartbeatTimer.cancel();
    	UploadTimer.cancel();
    	
    }
        
    //ע��CAC��Ϣ
    //�˴�C#���벻�Ծ�
    private void RegisterCACInfo()
    {
    	//CAGAccessServicePortTypeClient web = new CAGAccessServicePortTypeClient();
	    	
    	StringBuilder strbTemp =  new StringBuilder();
    	strbTemp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        strbTemp.append("<request><cac id=\"22M00000022376016\">");
        strbTemp.append("<ip>10.163.141.247</ip>");
        strbTemp.append("<station id=\"111\">�Ѻӱ��վ</station>");
        strbTemp.append("</cac>");
        strbTemp.append("</request>");
    	
        String sXML = strbTemp.toString();
        /*
		try 
		{//ʹ��DOM����XML
			
			//����DOM����������ʵ��
			DocumentBuilderFactory XMLDBFactory = DocumentBuilderFactory.newInstance();
			
			//��DOM�������DOM������
			DocumentBuilder XMLBuilder = XMLDBFactory.newDocumentBuilder();
			
			//�õ�XML�ĵ�
			Document XMLDoc = XMLBuilder.parse(sXML);
			
			Element eXMLRoot = XMLDoc.getDocumentElement();
			
			NodeList NLReqContent = eXMLRoot.getChildNodes();
			
			if(NLReqContent != null)
			{
				NodeList NLResult = eXMLRoot.getElementsByTagName("result");
				String sRe = NLResult.item(0).getAttributes().item(0).getTextContent();
				
				if("0" == sRe)
				{
					String sIPName = XMLDoc.getElementsByTagName("action").item(0).getAttributes().item(0).getTextContent();
					String sIP = XMLDoc.getElementsByTagName("action").item(0).getAttributes().item(1).getTextContent();

					//��ǰʱ��
					String sCurtimeName = XMLDoc.getElementsByTagName("action").item(1).getAttributes().item(0).getTextContent();
					String sCurtime = XMLDoc.getElementsByTagName("action").item(1).getAttributes().item(1).getTextContent();
					
	                //SetSystemTime(sCurtime);// ��������ѹ�������ķ�����ʱ����һСʱ��ʱ�䲻��ȷ���ݲ������ʱ��
	                //sCurtime = Convert.ToString(DateTime.Now);
					
					SetIniInfo("LastUpLoadTime", sCurtime);
					
					//�ϴ����� ��λΪ����
					String sRoundValue = XMLDoc.getElementsByTagName("action").item(2).getAttributes().item(1).getTextContent();
					SetRoundTime(sRoundValue);
				}
				else if ("1" == sRe)
				{
					//MessageBox.Show("ע��ʧ��");
				}
			}
			
		} 
		//catch (ParserConfigurationException | SAXException | IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */
    }
    
    //����������Ϣ
    private void SetIniInfo(String sKeyName, String sValue)
    {
    	String sPath = _Path;
    	IniClass cIni = new IniClass(sPath);
    	
    	if (true == cIni.ExistINIFile())
    	{
    		cIni.IniWriteValue("Config", sKeyName, sValue);
    	}
    	
    	return;
    }
    
    //ȡ��������Ϣ
   /* private String GetIniInfo(String sKeyName)
    {
    	String sPath = _Path;
    	String sConfig = "";
    	IniClass cIni = new IniClass(sPath);
    	
    	if (true == cIni.ExistINIFile())
    	{
    		sConfig = cIni.IniReadValue("Config", sKeyName);
    	}
    	
    	return sConfig;
    }*/
	private String GetIniInfo(String skeyName) {
		String spath = _Path;
		IniClass ini = new IniClass(spath);

		if (ini.ExistINIFile() == false) {
			return "";
		}
		String s = "";
		s = ini.IniReadValue("Config", skeyName);
		ini = null;
		return s;
	}

    
    //�����ϴ�����
    private void SetRoundTime(String sRound)
    {
    	long lRound = Integer.parseInt(sRound) * 60 * 1000;
    	_UploadTime =  String.valueOf(lRound);
    	SetIniInfo("upLoadTime", _UploadTime);
    	
    	UploadTimer.cancel();
    	
    	UploadTimer = new Timer();
    	UploadTimer.schedule(new UploadDataTask(), 0, lRound);    			
    }
    
    //����ϵͳʱ��
    private void SetSystemTime(String sTime)
    {
    
    	//Linuxϵͳ�����÷���
    	sTime = String.format("{0:yyyy-MM-dd HH:mm:ss}", sTime);
    	String sCmd = "/bin/date -s '" + sTime +"'";
    	String[] sCommands = new String[]{"/bin/sh", "-c", sCmd}; 
    	
    	try 
    	{
    		//ִ������
			Process p = Runtime.getRuntime().exec(sCommands);
		}
    	catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // ��ü�����ͱ���
    private String GetCACCdType(String str)
    {
    	String sCdType = "";
    	
    	if(str.equals(DBhelper._JBFD))
    	{
    		sCdType = "021001";
    	}
    	else if(str.equals(DBhelper._Yzrj))
    	{
    		sCdType = "021002";
    	}
    	else if(str.equals(DBhelper._WS))
    	{
    		sCdType = "021003";
    	}
    	else if(str.equals(DBhelper._TXXL))
    	{
    		sCdType = "021004";
    	}
    	else if(str.equals(DBhelper._DCYW))
    	{
    		sCdType = "021005";
    	}
    	else if(str.equals(DBhelper._RZCW))
    	{
    		sCdType = "021006";
    	}
    	else if(str.equals(DBhelper._DRJC))
    	{
    		sCdType = "022001";
    	}
    	else if(str.equals(DBhelper._BLQJYJC))
    	{
    		sCdType = "023001";
    	}
    	else if(str.equals(DBhelper._DLQJBFD))
    	{
    		sCdType = "024001";
    	}
    	else if(str.equals(DBhelper._FHZXQWave))
    	{
    		sCdType = "024002";
    	}
    	else if(str.equals(DBhelper._FHDLWave))
    	{
    		sCdType = "024003";
    	}
    	else if(str.equals(DBhelper._SF6))
    	{
    		sCdType = "024004";
    	}
    	else if(str.equals(DBhelper._SF6WS))
    	{
    		sCdType = "024005";
    	}
    	else if(str.equals(DBhelper._CNDJ))
    	{
    		sCdType = "024006";
    	}
    	
    	return sCdType;
    }
    
    //�ϴ�������Ϣ
    private String UpLoadHeartbeat()
    {
    	String sReturnInfo = "";
    	String sSQL = "select OBJID,LinkedDevice,DeviceCode,ZIEDID,IEDID,Phase,jclxbm from BD_CD";
    	String sJczzid = "";
    	
    	cCACDBCon.setSql(sSQL);
    	
    	try 
    	{
			cCACDBCon.setPreStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	cCACDBCon.selectSql();
    	
    	//ȡ�ü������
    	ResultSet Rs = cCACDBCon.getResult();
    	
    	StringBuilder sTmp = new StringBuilder();
    	StringBuilder sBulider = new StringBuilder();
    	
    	sTmp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	sTmp.append("<request><cac id=\"22M00000022376016\">");
    	sTmp.append("<ip>10.163.141.247</ip>");
    	
    	Date dateNow = new Date();
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String sTime =  simpleDateFormat.format(dateNow);
    	sTmp.append("<curtime>" + sTime + "</curtime>");
    	sTmp.append("<operationtemperature>15.00</operationtemperature></cac>");
    	sTmp.append("<sensors>");
    	
    	try 
    	{
			while(Rs.next())
			{
				sJczzid = Rs.getString("DeviceCode");
				sTmp.append(" <sensor id = \"" + sJczzid + "\">");
                sTmp.append("<status>NORMAL</status>");
                sTmp.append("<operationtemperature>15.00</operationtemperature>");
                sTmp.append("</sensor>");				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	sTmp.append("</sensors></request>");
    	String sTempXML = sTmp.toString();
    	
    	//�ȴ�����ӿ�
    	try {
			sReturnInfo = cac.uploadCACHeartbeatInfo(sTempXML);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return sReturnInfo;
    }
    
    //�ϴ�����
    private String UpLoadMoniData(String sd1)
    {
    	String sSQL = "select OBJID,LinkedDevice,DeviceCode,ZIEDID,IEDID,Phase,jclxbm from BD_CD";
    	
    	cCACDBCon.setSql(sSQL);
    	
    	try 
    	{
			cCACDBCon.setPreStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	cCACDBCon.selectSql();
    	
    	//ȡ�ü������
    	ResultSet Rs = cCACDBCon.getResult();
    	
        String sSbbm;
        String sCdid;
        String sJczzid;
        String sJclxbm;
        String sCdType;
        String sZIED;
        String sIED;
        String sBjcsbxb;
        String sRows = "init_rowcount";
        String sJcsj = null;
        
        StringBuilder sTmp = new StringBuilder();
        StringBuilder sbuilder = new StringBuilder();
        
        sTmp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sTmp.append("<request><monitordata cacid=\"22M00000022376016\" datanodenum=\"" + sRows + "\">");

        int iCount = 0;
        
        try {
			while(Rs.next())
			{
				sSbbm = Rs.getString("LinkedDevice");
				sCdid = Rs.getString("OBJID");
				sJczzid = Rs.getString("DeviceCode");
					
				sZIED = Rs.getString("ZIEDID");
				sZIED = sJczzid;	//��IEDID ��ʱδ��,��DeviceCode���
					
				sIED = Rs.getString("IEDID");
				sIED = sJczzid;		//��IEDID ��ʱδ��,��DeviceCode���
					
				sJclxbm = Rs.getString("JCLXBM");
				sBjcsbxb = Rs.getString("Phase");
				sCdType = GetCACCdType(sJclxbm);
				sSQL = "select * from %1$s where cdid = '%2$S' and AcquisitionTime > '%3$S'";

				sSQL = String.format(sSQL, sJclxbm, sCdid, sd1);
					
				ResultSet rsInside;
				cCACDBCon.setSql(sSQL);
				cCACDBCon.setPreStatement();
				cCACDBCon.selectSql();
					
				rsInside = cCACDBCon.getResult();

				while(rsInside.next())
				{
						
					try {
						Date d = DateFormat.getDateTimeInstance().parse(rsInside.getString("AcquisitionTime"));
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						sJcsj = sdf.format(d);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sTmp.append("    <datanode sensorid=\"" + sJczzid + "\"> ");
					sTmp.append("<type>" + sCdType + "</type>");
	                sTmp.append("<equipmentid>" + sSbbm + "</equipmentid>");
	                sTmp.append("<timestamp>" + sJcsj + "</timestamp>");//timestamp ���ݲɼ�ʱ��?
	                sTmp.append("<attrs>");//�汾ȡ�����ļ�
	                sTmp.append("<attr name=\"Phase\" value=\"" + sBjcsbxb + "\" alarm=\"FALSE\" />");
	                    
	                if(sJclxbm.equals(DBhelper._Yzrj))
	                {
	                	String sQingqi = rsInside.getString("H2");
	                    String sJiawan = rsInside.getString("CH4");
	                    String syiwan = rsInside.getString("C2H6");
	                    String syixi = rsInside.getString("C2H4");
	                    String syique = rsInside.getString("C2H2");

	                    String sYYHT = rsInside.getString("CO");
	                    String sEYHT = rsInside.getString("CO2");

	                    String sYangqi = rsInside.getString("O2");
	                    String sDanqi = rsInside.getString("N2");
	                    String sZt = rsInside.getString("TotalHydrocarbon");
	                    	
	                    //������׼
	                    sTmp.append("<attr name=\"H2\" value=\"" + sQingqi + "\"   />");
	                    sTmp.append("<attr name=\"CO\" value=\"" + sYYHT + "\"/>");
	                    sTmp.append("<attr name=\"CO2\" value=\"" + sEYHT + "\"/>");
	                    sTmp.append("<attr name=\"CH4\" value=\"" + sJiawan + "\"/>");
	                    sTmp.append("<attr name=\"C2H4\" value=\"" + syixi + "\"/>");
	                    sTmp.append("<attr name=\"C2H2\" value=\"" + syique + "\"/>");
	                    sTmp.append("<attr name=\"C2H6\" value=\"" + syiwan + "\"/>");
	                    sTmp.append("<attr name=\"O2\" value=\"" + sYangqi + "\"/>");
	                    sTmp.append("<attr name=\"N2\" value=\"" + sDanqi + "\"/>");
	                    sTmp.append("<attr name=\"TotalHydrocarbon\" value=\"" + sZt + "\"/>");
	                }
	                else if(sJclxbm.equals(DBhelper._JBFD))
	                {
	                	String sFdl = rsInside.getString("DischargeCapacity");
	                	String sWeiZhi = rsInside.getString("DischargePosition");
	                	String sMcgs = rsInside.getString("PulseCount");
	                    sTmp.append("<attr name=\"DischargeCapacity\" value=\"" + sFdl + "\"/>");
	                    sTmp.append("<attr name=\"DischargePosition\" value=\"" + sWeiZhi + "\"/>");///��������
	                    sTmp.append("<attr name=\"PulseCount\" value=\"" + sMcgs + "\"/>");
	                    String sWave = "";
	                    String sPath = "";
	                         
	                    //C#�汾 ���ȡ����·���µ�һ�������ļ�
	                    //string spath = Application.StartupPath + "\\waveformTest.txt";
	                    //sWave = TxtRead(spath);

	                    //sTmp.Append("<attr name=\"DischargeWaveform\" value=\"" + wave + "\"/>");
	                }
	                else if(sJclxbm.equals(DBhelper._TXXL))
	                {
	                	String sTxxlDl = rsInside.getString("TotalCoreCurrent");
	                    sTmp.append("<attr name=\"TotalCoreCurrent\" value=\"" + sTxxlDl + "\"/>");
	                }
	                else if(sJclxbm.equals(DBhelper._SF6))
	                {
	                    String sWendu = rsInside.getString("Temperature");
	                    String sJdyl = rsInside.getString("AbsolutePressure");
	                    String sMidu = rsInside.getString("Density");
	                    String sYl20 = rsInside.getString("Pressure20C");

	                    sTmp.append("<attr name=\"Temperature\" value=\"" + sWendu + "\" alarm=\"TRUE\" />");
	                    sTmp.append("<attr name=\"AbsolutePressure\" value=\"" + sJdyl + "\" alarm=\"TRUE\" />");
	                    sTmp.append("<attr name=\"Density\" value=\"" + sMidu + "\" alarm=\"TRUE\" />");
	                    sTmp.append("<attr name=\"Pressure20C\" value=\"" + sYl20 + "\" alarm=\"TRUE\" />");

	                }
	                else if(sJclxbm.equals(DBhelper._SF6WS))
	                {
	                    String sWendu = rsInside.getString("Temperature");
	                    String sSf = rsInside.getString("Moisture");
	                    sTmp.append("<attr name=\"Temperature\" value=\"" + sWendu + "\" alarm=\"TRUE\" />");
	                    sTmp.append("<attr name=\"Moisture\" value=\"" + sSf + "\" alarm=\"TRUE\" />");
	                }
	                else if(sJclxbm.equals(DBhelper._WS))
	                {
	                    String sSf = rsInside.getString("Moisture");
	                    sTmp.append("<attr name=\"Moisture\" value=\"" + sSf + "\" alarm=\"TRUE\" />");
	                }
	                else if(sJclxbm.equals(DBhelper._DCYW))
	                {
	                    String sDcyw = rsInside.getString("OilTemperature");
	                    sTmp.append("<attr name=\"OilTemperature\" value=\"" + sDcyw + "\"/>");

	                }
	                else if(sJclxbm.equals(DBhelper._RZCW))
	                {
	                    String sRzwd1 = rsInside.getString("RZWD");
	                    sTmp.append("<attr name=\"RZWD\" value=\"" + sRzwd1 + "\"/>");

	                }
	                else if(sJclxbm.equals(DBhelper._BLQJYJC))
	                {
	                	String sdy = rsInside.getString("SystemVoltage");
	                    String sQdl = rsInside.getString("TotalCurrent");
	                    String sZxdl = rsInside.getString("ResistiveCurrent");
	                    String sJsqcs = rsInside.getString("ActionCount");
	                    String sZhTime = String.format("{0:yyyy-MM-dd HH:mm:ss}", rsInside.getString("LastActionTime"));

	                    sTmp.append("<attr name=\"SystemVoltage\" value=\"" + sdy + "\"/>");
	                    sTmp.append("<attr name=\"TotalCurrent\" value=\"" + sQdl + "\"/>");
	                    sTmp.append("<attr name=\"ResistiveCurrent\" value=\"" + sZxdl + "\"/>");
	                    sTmp.append("<attr name=\"ActionCount\" value=\"" + sJsqcs + "\"/>");
	                    sTmp.append("<attr name=\"LastActionTime\" value=\"" + sZhTime + "\"/>");
	                }
	                else if(sJclxbm.equals(DBhelper._DRJC))
	                {
	                    String sDr = rsInside.getString("Capacitance");
	                    String sJieSun = rsInside.getString("LossFactor");
	                    String sXldl = rsInside.getString("TotalCurrent");
	                    String sXtdy = rsInside.getString("SystemVoltage");
	                        
	                    String sDL = rsInside.getString("UnbalanceCurrent");
	                    String sDY = rsInside.getString("UnbalanceVoltage");
	                    sTmp.append("<attr name=\"Capacitance\" value=\"" + sDr + "\"/>");
	                    sTmp.append("<attr name=\"LossFactor\" value=\"" + sJieSun + "\"/>");
	                    sTmp.append("<attr name=\"TotalCurrent\" value=\"" + sXldl + "\"/>");
	                    sTmp.append("<attr name=\"SystemVoltage\" value=\"" + sXtdy + "\"/>");
	                    sTmp.append("<attr name=\"UnbalanceCurrent\" value=\"" + sDL + "\"/>");
	                    sTmp.append("<attr name=\"UnbalanceVoltage\" value=\"" + sDY + "\"/>");
	                }
	                else if(sJclxbm.equals(DBhelper._FHDLWave))
	                {
	                    String sAction = rsInside.getString("Action");
	                    sTmp.append("<attr name=\"Action\" value=\"" + sAction + "\"/>");
	                        
	                    //C#�汾 ���ȡ����·���µ�һ�������ļ�
	                    //string wave = "";
	                    //string spath = Application.StartupPath + "\\waveformTest.txt";
	                    //wave = TxtRead(spath);

	                    //sTmp.append("<attr name=\"LoadWaveform\" value=\"" + wave + "\"/>");
	                }
	                else if(sJclxbm.equals(DBhelper._FHZXQWave))
	                {
	                    String action = rsInside.getString("Action");
	                    sTmp.append("<attr name=\"Action\" value=\"" + action + "\"/>");
	                        
	                    //C#�汾 ���ȡ����·���µ�һ�������ļ�
	                    //string wave = "";
	                    //string spath = Application.StartupPath + "\\waveformTest.txt";
	                    //wave = TxtRead(spath);

	                    //sTmp.Append("<attr name=\"CoilWaveform\" value=\"" + wave + "\"/>");     //��淶��һ��    

	                }
	                else if(sJclxbm.equals(DBhelper._DLQJBFD))
	                {
	                    String sFdl = rsInside.getString("DischargeCapacity");
	                    String sWeiZhi = rsInside.getString("DischargePosition");
	                    String sMcgs = rsInside.getString("PulseCount");
	                    sTmp.append("<attr name=\"DischargeCapacity\" value=\"" + sFdl + "\"/>");
	                    sTmp.append("<attr name=\"DischargePosition\" value=\"" + sWeiZhi + "\"/>");///��������
	                    sTmp.append("<attr name=\"PulseCount\" value=\"" + sMcgs + "\"/>");
	                        
	                    //C#�汾 ���ȡ����·���µ�һ�������ļ�
	                    //string wave = "";
	                    //string spath = Application.StartupPath + "\\waveformTest.txt";
	                    //wave = TxtRead(spath);

	                    //sTmp.Append("<attr name=\"DischargeWaveform\" value=\"" + wave + "\"/>");

	                }
	                sTmp.append("</attrs></datanode>");
	                    	                    
			    }//while(rsInside.next())
					
				try {
					rsInside.last();
					iCount += rsInside.getRow();	//****�˴��Ƿ����1��Ҫ�ڵ���ʱȷ��****
					rsInside.beforeFirst();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
        catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        sTmp.append("</monitordata></request>");
        
        String sTempXML = sTmp.toString();
        sTempXML = sTempXML.replace("init_rowcount", String.valueOf(iCount));
        

        String sTmpWeb = "";
            
        //�ȴ�����ӿ�
        try {
			sTmpWeb = cac.uploadCACData(sTempXML);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return sTmpWeb;    
    }
    
    //д�ļ� ����CAG
	private void TxtWrite(String strToWrite, String sFileName) {
		try {
			// ��һ����������ļ���������д��ʽ
			RandomAccessFile randomFile = new RandomAccessFile(sFileName, "rw");
			// �ļ����ȣ��ֽ���
			long fileLength = randomFile.length();
			// ��д�ļ�ָ���Ƶ��ļ�β��
			randomFile.seek(fileLength);
			strToWrite += "\r\n";
			randomFile.writeBytes(strToWrite);
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	//��ȡ�ļ�
	private String TxtRead(String sFileName)
	{
		String sReadData = "";
		
		try
		{
			RandomAccessFile randomFile = new RandomAccessFile(sFileName, "r");
			byte[] abTempBuffer = new byte[(int)randomFile.length()]; 
			
			randomFile.read(abTempBuffer);
			sReadData = abTempBuffer.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return sReadData;
	}
	
	// �õ���������ʱ��  ����CAG
	private int GetUpLoadRound() {
		String sPath = _Path;

		IniClass ini = new IniClass(sPath);
		if (ini.ExistINIFile() == false) {
			return 0;
		}

		String sVal = ini.IniReadValue("Config", "UpLoadTime");
		return Integer.valueOf(sVal);
	}
	

}
