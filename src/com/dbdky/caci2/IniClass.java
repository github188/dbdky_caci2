package com.dbdky.caci2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//@@@@@@import java.util.logging.Level;
//@@@@@@import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//����CAG
public class IniClass {
	public String inipath;
	public IniClass(String path)
	{
		inipath = path;
	}
	/**
	����* ��ini�����ļ��ж�ȡ�ַ�������ֵ
	����* @param section Ҫ��ȡ�ı������ڶ�����
	����* @param variable Ҫ��ȡ�ı�������
	����* @param defaultValue �������Ʋ�����ʱ��Ĭ��ֵ
	����* @return �������ַ���ֵ
	*/
	public String GetPrivateProfileString(String section,String variable,String defaultValue, String filePath)
	{
		BufferedReader bufferedReader = null;

		try 
		{
			String strLine;
			String value = "";

			bufferedReader = new BufferedReader(new FileReader(filePath));

			boolean isInSection = false;

			while ((strLine = bufferedReader.readLine()) != null)
			{
				strLine = strLine.trim();
				strLine = strLine.split("[;]")[0];
				Pattern p;
				Matcher m;

				p = Pattern.compile("\\[\\s*.*\\s*\\]");
				m = p.matcher(strLine);

				if (m.matches()) 
				{
					p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
					m = p.matcher(strLine);

					if (m.matches()) 
					{
						isInSection = true;
					} 
					else 
					{
						isInSection = false;
					}
				}

				if (isInSection == true) 
				{
					strLine = strLine.trim();
					String[] strArray = strLine.split("=");

					if (strArray.length == 1) 
					{
						value = strArray[0].trim();

						if (value.equalsIgnoreCase(variable)) 
						{
							value = "";

							return value;
						}
					} 
					else if (strArray.length == 2) 
					{
						value = strArray[0].trim();
						
						if (value.equalsIgnoreCase(variable)) 
						{
							value = strArray[1].trim();

							return value;
						}
					}
					else if (strArray.length > 2) 
					{
						value = strArray[0].trim();

						if (value.equalsIgnoreCase(variable))
						{
							value = strLine.substring(strLine.indexOf("=") + 1).trim();

							return value;
						}
					}
				}
			}
		} 
		catch (FileNotFoundException ex) 
		{
			//@@@@@@Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);
		} 
		catch (IOException ex) 
		{
			//@@@@@@Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);
		} 
		finally 
		{
			try 
			{
				bufferedReader.close();
			}
			catch (IOException ex) 
			{
				//@@@@@@Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		return defaultValue;
	}
	
	/**
	����* �޸�ini�����ļ����ַ�������ֵ
	����* @param section Ҫ�޸ĵı������ڶ�����
	����* @param variable Ҫ�޸ĵı�������
	����* @param value ��������ֵ
	����* @return �����Ƿ�ɹ�
	 */

	public boolean  WritePrivateProfileString(String section, String variable, String value, String filePath) 
	{
		BufferedReader bufferedReader = null;
		
		try 
		{
			boolean isInSection = false;
			bufferedReader = new BufferedReader(new FileReader(filePath));
			String allLine = "";
			String fileContent = "";
			while ((allLine = bufferedReader.readLine()) != null) 
			{

				String remarkStr = "";

				String strLine = "";
				allLine = allLine.trim();
				if (allLine.split("[;]").length > 1) {
					remarkStr = ";" + allLine.split(";")[1];
				}
				else 
				{
					remarkStr = "";
				}

				strLine = allLine.split(";")[0];
				
				Pattern p;
				Matcher m;

				p = Pattern.compile("\\[\\s*.*\\s*\\]");
				m = p.matcher(strLine);
				if (m.matches()) 
				{
					p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
					m = p.matcher(strLine);

					if (m.matches()) 
					{
						isInSection = true;
					}
					else 
					{
						isInSection = false;
					}
				}
				
				if (isInSection == true)
				{
					strLine = strLine.trim();
					String[] strArray = strLine.split("=");
					String getValue = strArray[0].trim();
					
					if (getValue.equalsIgnoreCase(variable)) 
					{
						String newLine = getValue + " = " + value + " " + remarkStr;
						fileContent += newLine + "\r\n";

						while ((allLine = bufferedReader.readLine()) != null) {
							fileContent += allLine + "\r\n";
						}

						bufferedReader.close();

	 					BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, false));
	                    bufferedWriter.write(fileContent);

	                    bufferedWriter.flush();
	                    bufferedWriter.close();

	                    return true;

					}

				}

				fileContent += allLine + "\r\n";
			}

		}
		catch (FileNotFoundException ex)
		{

			//@@@@@@Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {

        	//@@@@@@Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);

        } 
		finally {

			try 
			{
					bufferedReader.close();
			} 
			catch (IOException ex) 
			{
				//@@@@@@Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		return false;
	}
	
	 public void IniWriteValue(String Section, String Key, String Value)
     {
         WritePrivateProfileString(Section, Key, Value, this.inipath);           
     }


     public String IniReadValue(String Section, String Key)
     {
         String retVal = GetPrivateProfileString(Section, Key, "",  this.inipath); //@@@@@@ Ҫ����
         return retVal;  

     }

     public boolean ExistINIFile()
     {
         return new File(this.inipath).exists();
     } 
}