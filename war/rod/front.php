<?php

ini_set("display_errors","1");
error_reporting(-1);

$json = '{
 "username": "steven.foo@sense-os.nl",
 "uuid": "c87bfec6-0d5f-11e2-94c1-da0007d04f45",
 "anonymous": false,
 "sensors": [
   {
     "id": 180655,
     "name": "light",
     "description": "GP2A Light sensor"
   },
   {
     "id": 180658,
     "name": "pressure",
     "description": "BMP180 Pressure sensor"
   },
   {
     "id": 180658,
     "name": "pressure",
     "description": "BMP180 Pressure sensorsss"
   },
   {
     "id": 180658,
     "name": "pressure",
     "description": "BMP180 Pressure sensorssss"
   },
   {
     "id": 180668,
     "name": "pressure",
     "description": "BMP180 Pressure sensorssss"
   }
 ]
}';

function curl($json){
	$ch = curl_init();
	$api_key = '199fe36c-8baf-455d-9f7a-aeefb209025c';
	$ch_headers = array('Authorization: ' . ''.$api_key.'');
	curl_setopt($ch, CURLOPT_HTTPHEADER, $ch_headers);
	curl_setopt($ch, CURLOPT_URL, 'http://common.dev.sense-os.nl/rod/cs.php');
	curl_setopt($ch, CURLOPT_POST, $json);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

	var_dump(curl_exec($ch));
	
	$httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
	curl_close($ch);
	
	return $httpcode;
};


echo curl($json);




?>
