 package com.belkast.soap;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Map.Entry;
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLSession;
 
 import com.beust.jcommander.*;
 
 public class Webservice
 {
     private static Boolean varDebug;
     private static String varPassword;
     private static String programStatus;

     private static String NEW_STATUS = null;
     
   public static void main(String[] args) throws Exception
   {
       
       try
       {
           soapResponse(args);      
       }
       catch(Exception ex)
       {
           throw new RuntimeException(ex);
       }
     
   }
 
   public static String soapResponse(String[] args) throws Exception
   { 
       
     Parser commandLine = new Parser();
     JCommander jct = new JCommander (commandLine);
     File configFilePath = new File("");

    Date dateStart = new Date();
    
    SimpleDateFormat varFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    System.out.println("\n<debug>Start Time: " + varFormat.format(dateStart) + "</debug>");

     try
     {
         jct.parse(args);
     }
     catch (ParameterException ex)
     {
         System.out.println(ex.getMessage());
         jct.usage();
     }

    if (commandLine.varKey != null && commandLine.varEncrypt != null)
        {
            varPassword = encryptPassword(commandLine.varKey, commandLine.varEncrypt);
            return "";
        }
    
    if (commandLine.varPropertiesFile == null)
        {
//  System.out.println("Nothing to do");            
        programStatus = "Properties file not valid";
        return programStatus;
        }
    
    varDebug = Boolean.parseBoolean(commandLine.debug);
    System.out.println("<debug>Debug is: " + varDebug.toString() + "</debug>");

    try
        {
            configFilePath = new File(commandLine.varPropertiesFile);
            FileReader fileReader = new FileReader(configFilePath);
        }
    catch (IOException e)
        {
//  e.printStackTrace();
//  throw new RuntimeException("Properties file was not found", e);
        programStatus = e.toString();
        return programStatus;
        }

    String varDelimiter = readProperty(varDebug, configFilePath, "ARGUMENT_DELIMITER");
          
    HashMap hashMap = generateHashMap(commandLine.varTokens.size(), varDelimiter, commandLine.varTokens);

    System.out.println();
    String varURL = readProperty(varDebug, configFilePath, "SHIM_URL");
    String varRequestType = readProperty(varDebug, configFilePath, "SOAP_ACTION");
    String varInputFile = readProperty(varDebug, configFilePath, "INPUT_FILE");
    String varUsername = readProperty(varDebug, configFilePath, "USERNAME");
    String varPasswordTemp = readProperty(varDebug, configFilePath, "PASSWORD");

    try
    {
      varPassword = Protector.decrypt(commandLine.varKey.getBytes(), varPasswordTemp);
    }
    catch (Exception e)
    {
//        e.printStackTrace();
//      throw new Exception(e);
        programStatus = e.toString();
        return programStatus;
    }

    String varUseSSL = readProperty(varDebug, configFilePath, "SSL");

    if ((varURL.equalsIgnoreCase("")) || (varInputFile.equalsIgnoreCase("")))
    {
        programStatus = "Properties file " + configFilePath.toString() + " is not valid";
        return programStatus;
//      throw new Exception("Properties file is corrupt: " + configFilePath.toString());
    }

    System.out.println();

    String varDocument = readTextFile(varInputFile, hashMap);
    if (varDocument.length() > 0)
    {
        String response = writeMessage(varUsername, varPassword, varURL, varRequestType, varInputFile, varUseSSL, hashMap, varDocument.getBytes());
        System.out.println();
        System.out.println("<debug>Response from Web Service: " + response + "</debug>");
        
        if (programStatus == null)
        {
                return "WS response: " + response;
        }            
    }

    return programStatus;

   }

  private static String debugMessage(Boolean varLevel, String varMessage)
  {
    if (varLevel.booleanValue() == true)
    {
      System.out.println(varMessage);
    }
    return "OK";
  }

  private static HashMap generateHashMap(Integer size, String delimiter, List<String> tokens) throws Exception
  {

    System.out.println("\n## Generating a HashMap based on " + tokens.toString() + " ##");
    String[] argumentsArray = new String[size];
    HashMap hashMap = new HashMap();

    System.out.println("<debug>Token size " + size.toString() + "</debug>");


      for (int i = 0; i < size; i++)
      {
        argumentsArray[i] = tokens.get(i);
        System.out.println("<debug>Token [" + i + "]: " + argumentsArray[i] + "</debug>");

        String words[] = argumentsArray[i].split(delimiter);

        if (words.length != 2)
          continue;
        System.out.println("<debug>hashMap: " + words[0] + " and " + words[1] + "</debug>");
        hashMap.put(words[0], words[1]);
      }
      debugMessage(varDebug, "<debug>Array Size " + argumentsArray.length + "</debug>");
      debugMessage(varDebug, "<debug>HashMap contains " + hashMap.size() + " key value pairs</debug>");
      
    return hashMap;
  }
  
  private static String encryptPassword(String tempKey, String varPassword) throws Exception
  {
      char[] varPasswordIn = new char[0];
      Integer validKeyLength = 15;

      System.out.println("\n## Generating an encrypted password ##");
      try
      {
      tempKey.charAt(validKeyLength);
      byte[] thisKey = tempKey.getBytes();
      System.out.println("Key to use: " + new String(thisKey));
      System.out.println("Value to encrypt: " + varPassword);
      varPasswordIn = Protector.encrypt(thisKey, varPassword);
      System.out.println("Encrypted value: " + new String(varPasswordIn));
      String varPasswordOut = Protector.decrypt(thisKey, new String(varPasswordIn));
      System.out.println("Test Decrypted value: " + varPasswordOut);
      }
      catch(Exception e)
      {
          e.printStackTrace();
          //          throw (e);
      }
  
      return varPasswordIn.toString();          
 }

  public static String readProperty(Boolean varDebug, File MyFile, String varProperty) throws Exception
  {
    Properties props = new Properties();
    String propertyValue = "";

    if (varProperty.equals("SHOW_DEBUG"))
    {
      varDebug = true;
    }
    try
    {
      props.load(new FileInputStream(MyFile));
      propertyValue = props.getProperty(varProperty);
      debugMessage(varDebug, "<debug>Reading properties file for " + varProperty + ", got " + propertyValue + "</debug>");
    }
    catch (Exception e)
    {
        e.printStackTrace();
//        throw (e);
    }
    return propertyValue;
  }

  public static String readTextFile(String fullPathFilename, HashMap hashMap) throws IOException
  {
    System.out.println("## Preparing the SOAP document ##");
    StringBuilder sb = new StringBuilder(43);
    try
    {
      File myFile = new File(fullPathFilename);
      FileReader fileReader = new FileReader(myFile);
      BufferedReader reader = new BufferedReader(fileReader);
      String line;
      while ((line = reader.readLine()) != null)
      {
        sb.append(line);
      }
      reader.close();
    }
    catch (IOException e)
    {
        // throw (e);
        e.printStackTrace();
        return ("");
//      System.out.println("Exception: " + e.toString());
    }

    String returnString = sb.toString();

    Set set = hashMap.entrySet();
    Iterator i = set.iterator();
    while (i.hasNext())
    {
      Map.Entry me = (Map.Entry)i.next();
      System.out.println("<debug>Replacing " + me.getKey().toString() + " with " + me.getValue().toString() + "</debug>");
      returnString = returnString.replaceAll(me.getKey().toString(), me.getValue().toString());
    }

    System.out.println("<debug>SOAP Document to send: " + returnString + "</debug>");
    return returnString;
  }

  public static String writeMessage(String username, String password, String varURL, String varRequestType, String varInputFile, String varUseSSL, HashMap hashMap, byte[] document) throws Exception
    {
        programStatus = NEW_STATUS;

    StringBuilder response = new StringBuilder(500);
    String SOAPAction = varRequestType;
    String login = username + ":" + password;
    String encoding = Base64.encodeString(login);

    URL url = new URL(varURL);

    try
    {
        System.out.println("\n## Sending SOAP Document ##");
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + encoding);
        HttpURLConnection httpConn = (HttpURLConnection)connection;
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();

        httpConn.setRequestProperty("Content-Length", String.valueOf(document.length));
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setRequestProperty("SOAPAction", SOAPAction);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        OutputStream out = httpConn.getOutputStream();
        out.write(document);
        out.close();

        InputStreamReader ireader = new InputStreamReader(httpConn.getInputStream());

        BufferedReader in = new BufferedReader(ireader);
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }

        in.close();
        httpConn.disconnect();
    }
    catch (Exception e)
        {
//            throw (e);
        e.printStackTrace();
        programStatus = e.toString();
        return programStatus;
        }

    return response.toString();
  }

  static
  {
    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
    {
      public boolean verify(String hostname, SSLSession sslSession)
      {
        System.out.println("<debug>Hostname: " + hostname + "</debug>");
        return true;
      }
    });
  }
}