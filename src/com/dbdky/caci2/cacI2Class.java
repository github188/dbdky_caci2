package com.dbdky.caci2;

import java.io.UnsupportedEncodingException;
import java.sql.*;

public class cacI2Class {
	public static void main(String[] args) throws UnsupportedEncodingException,
			SQLException {
		uploadService cac2Service = new uploadService();
		cac2Service.StartService();
	}
}