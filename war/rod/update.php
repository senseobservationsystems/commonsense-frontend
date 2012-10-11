<?php
// create a new cURL resource
$ch = curl_init();

// Data
$dataset 				= "http://data.rotterdamopendata.nl:9090/nl/api/rest/group/test";
$data 					= json_decode(file_get_contents($dataset));

// Update
$data->packages			= array_merge($data->packages, array('a59d12ff-6a3f-478b-9f05-7e720897053d'));

// set URL and other appropriate options
$options 	= array(
				CURLOPT_URL			=> $dataset,
				CURLOPT_HEADER		=> false,
				CURLOPT_POSTFIELDS	=> json_encode($data),
				CURLOPT_HTTPHEADER	=> array('Authorization: 199fe36c-8baf-455d-9f7a-aeefb209025c'),
				);

curl_setopt_array($ch, $options);

// grab URL and pass it to the browser
curl_exec($ch);

// close cURL resource, and free up system resources
curl_close($ch);
?>