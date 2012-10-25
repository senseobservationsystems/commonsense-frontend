<?php include('index.php'); 
/*ini_set("display_errors", "1");
error_reporting(E_ALL);*/

if(isset($_POST)) {
		$rod = new rod();
		
		//debug stuff
		$file = file_get_contents("php://input");
		$data = json_decode($file, true);
		
		/* The user wants to create or update his sensordata */
		
		if(isset($data['username']) && !isset($data['sensors']) && !isset($data['uuid']) && !isset($data['anonymous'])){
			$rod->getMyDataset();
		}
		
		elseif(isset($data['username']) && isset($data['sensors']) && isset($data['uuid']) && isset($data['anonymous'])){ 
			$rod->publishSensorData($_POST);
			$rod->getMyDataset();
		}
		
		/* The user just wants to get his dataset data */
		
} else {
	echo('no POST data');
}

?>
