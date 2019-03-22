<?php
include('./helperfuncs.php');
//user-agent check is not implemented here bcoz this script is also called by web front-end besides the app
$outputjsonobject = array('ForceWifiOnForRecordUpload'=>0, 'ServerTalkInterval'=>0,'message'=>'error','statut'=>0);
if(isset($_POST['uniqueid']) && !empty($_POST['uniqueid']))
{
    addLog("ENTREE: ".json_encode($_POST),"setting");
    $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
    if($conn->connect_errno==0){
        addLog("NIVEAU 1: ".json_encode($conn),"setting");
        CreateSettingsTableIfNotExists($conn);
        addLog("NIVEAU 2: ","setting");
        $uniqueid=$conn->escape_string($_POST['uniqueid']);
        $getsettingsquery="SELECT * FROM settings WHERE DeviceUniqueId='$uniqueid'";
        $response=$conn->query($getsettingsquery);
        addLog("NIVEAU 3: ".$getsettingsquery,"setting");
        if($response->num_rows>0)
        {
            if($row=$response->fetch_assoc()){
                $forceWifiOnForRecordUpload = $row['ForceWifiOnForRecordUpload'];
                $serverTalkInterval = $row['ServerTalkInterval'];
                
                $outputjsonobject['ForceWifiOnForRecordUpload'] = $forceWifiOnForRecordUpload;
                $outputjsonobject['ServerTalkInterval'] = $serverTalkInterval;
                $outputjsonobject['message'] = "Succes";
                $outputjsonobject['statut'] = 1;
            }
            else
            {
                $outputjsonobject['message'] = "No row in settings table";
            }
            
        }else{
            $outputjsonobject['message'] = "No row in settings table (2)";
        }
    }
    else
    {
        $outputjsonobject['message'] = "Error connecting to database! $conn->connect_errno";
    }
}else{
    $outputjsonobject['message'] = "requete incorrecte !";
}
echo (json_encode($outputjsonobject));

exit();
?>