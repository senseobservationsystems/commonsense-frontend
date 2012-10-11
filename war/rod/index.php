<?php

include_once('Ckan_client.php');

class rod{
	private $ckan;
	private $name;
	private $title;
	private $group;
	private $data = array();
	private $baseurl;

	public function __construct(){	
		$this->ckan = new Ckan_client('27b900c8-cd20-4647-b8be-bdf3446b32b3');
		$file = file_get_contents("php://input");
		$data = json_decode($file, true);
		$this->data = $data;
		$this->group = 'commonsense-data';
		$this->name = 'mydata'.md5($data['username']).'';
		
		if(isset($data['anonymous'])){
			if($data['anonymous'] == false){
				$this->title = $data['username'].'';
			}else{
				$this->title = 'user'.md5($data['username']).'';
			}
		}
		
		$this->baseurl = 'http://data.rotterdamopendata.nl:9090/dataset/';
	}

	private function getStatus(){
		$url = 'http://data.rotterdamopendata.nl:9090/nl/api/rest/dataset/'.$this->name;
		
		if(@file_get_contents($url)){
			$file = file_get_contents($url);
			$json = json_decode($file, true);
			
			if(is_array($json)){
				if(isset($json['id'])){
					return $json['id'];
				}
			}
		}
		return false;
	}
	
	private function updateGroup($package_id){
		// create a new cURL resource
		$ch = curl_init();

		// Data
		$dataset 				= 'http://data.rotterdamopendata.nl:9090/nl/api/rest/group/'.$this->group.'';
		$data 					= json_decode(file_get_contents($dataset));

		// Update
		$data->packages			= array_merge($data->packages, array(''.$package_id.''));

		// set URL and other appropriate options
		$options 	= array(
						CURLOPT_URL			=> $dataset,
						CURLOPT_HEADER		=> false,
						CURLOPT_POSTFIELDS	=> json_encode($data),
						CURLOPT_HTTPHEADER	=> array('Authorization: 27b900c8-cd20-4647-b8be-bdf3446b32b3'),
						);

		curl_setopt_array($ch, $options);
		
		// don't echo out curl
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		
		// grab URL and pass it to the browser
		curl_exec($ch);

		// close cURL resource, and free up system resources
		curl_close($ch);
	}
	
	private function getPackages(){
		$package_ids = array();
		
		$current_package_id = $this->getStatus();
		
		$group_data = get_object_vars($this->ckan->get_group(''.$this->group.''));
		
		foreach($group_data['packages'] as $package_id){
			array_push($package_ids, $package_id);
		}
		
		if(!in_array($current_package_id, $package_ids)){
			array_push($package_ids, $current_package_id);
		}
		
		return $package_ids;		
	}

	public function publishSensorData(){
		$urls = array();
		$group_data = get_object_vars($this->ckan->get_group(''.$this->group.''));
		$group_id = $group_data['id'];
		
		$tags = array();
		
		if(isset($this->data['sensors'])){
			if(is_array($this->data['sensors'])){
				foreach($this->data['sensors'] as $sensor){
					array_push($urls, array(
						'url' => 'https://api.sense-os.nl/sensors/'.$sensor['id'].'/data.json?API_KEY='.$this->data['uuid'],
						'format' => 'json',
						'hash' => ''.$sensor['id'].'',
						'type' => 'API',
						'name' => ''.$sensor['name'].' ('.$sensor['description'].')'.'',
						'description' => ''.$sensor['name'].' ('.$sensor['description'].')'.''
					));
					
					array_push($tags, $sensor['name']);
				}
			}
		}
		
		$data = json_encode(array(
			'name' => ''.$this->name.'',
			'title' => 'Dataset of '.$this->title.'',
			'author' => $this->title,
			'author_email' => $this->title,
			'resources' => $urls,
			'maintainer'=> 'Sense',
			'maintainer_email' => 'info@sense-os.nl',
			'tags' => $tags,
			'url' => 'http://common.sense-os.nl'
		));
		
		$array = array();
		
		$package_id = $this->getStatus();
		
		if($package_id == false){ /* Dataset does not exist (yet) */
			if($this->ckan->post_package_register($data) == '201'){ /* Succesfull */
				$data2 = json_encode(array('packages' => $this->getPackages()));
				$this->updateGroup($this->getStatus());				
				$array = array('status' => '201', 'message' => 'The chosen sensors has been shared succesfully');
			}
			else{
				$array = array('status' => '400', 'message' => 'An error has occurred while creating your dataset, the dataset has not been created');
			}
		}
		
		else{ /* Update the Dataset */		
			if(is_object($this->ckan->put_package_entity($package_id, $data, $this->name))){
				$this->updateGroup($package_id);
				$array = array('status' => '201', 'message' => 'Succesfully updated your dataset'); 
			}
			elseif($this->ckan->put_package_entity($package_id, $data, $this->name) == '403'){/* No persmission to update the dataset */
				$array = array('status' => '403', 'message' => 'No permission');
			}
			else{
				$array = array('status' => '400', 'message' => 'An error has occurred while updating your dataset, the dataset has not been updated');
			}
		}
		
		//$this->returnJson($array);
	}

	
	public function getDatasetURL(){
		if($this->getStatus()){
			$url_data = array();
			$temp = get_object_vars($this->ckan->get_package($this->name));
			$url_data['url'] = $temp['id'];
			$temp2 =  get_object_vars($this->ckan->get_package($this->name));
			$url_data['title'] = $temp2['title'];
			return $url_data;
		}
	}
	
	public function getMyDataset(){		
		if(is_object($this->ckan->get_package($this->name))){
			$data = get_object_vars($this->ckan->get_package($this->name));
		}
		else{
			$data = '';
		}
		
		$array = array();
		
		if(is_array($data)){
			$resource_names = array();
			
			if(isset($data['resources'])){
				foreach($data['resources'] as $resource){
					array_push($resource_names, $resource->hash);
				}
			}
			
			$array = array('status' => '200', 'url' => ''.$data['id'].'', 'title' => ''.$data['title'].'', 'sensors' => $resource_names, 'name' => ''.$data['name'].'');
		}
		else{
			$array = array('status' => '400', 'message' => 'Error retrieving your dataset data, probably because you haven\'t  share any sensor data in the past. You need to share sensordata in order to be able to retrieve information about your dataset');
		}
		$this->returnJson($array);
	}
	
	private function returnJson($array)
	{
		if (is_array($array)) 
		{
			header('Content-type: application/json');
			echo json_encode($array);	
		}
	}

	
	
	
	
	
	
	
	
	
	
}
?>
