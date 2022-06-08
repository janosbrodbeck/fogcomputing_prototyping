extern crate grpc;

use std::time::SystemTime;
use grpc::sensor_client::SensorClient as GrpcSensorClient;
use grpc::Event;
use tonic::transport::Channel;
use uuid::Uuid;
use uuid::v1::{Context, Timestamp};

struct Sensor {
    client: GrpcSensorClient<Channel>,
    volcano_name: String,
    uuid: Uuid,
    context: Context,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let client = GrpcSensorClient::connect("http://[::1]:50051").await?;

    let volcano_name = String::from("Tonic");

    let mut sensor = Sensor::new(client, volcano_name);

    sensor.run().await?;


    Ok(())
}

impl Sensor {

    pub fn new(client: GrpcSensorClient<Channel>, volcano_name: String) -> Self {
        let uuid =  Uuid::new_v5(&Uuid::NAMESPACE_DNS, volcano_name.as_bytes());

        Self {
            client,
            volcano_name,
            uuid,
            context: Context::new(uuid.to_fields_le().1)
        }
    }

    pub fn emit_event(&self, x: i64, y: i64, z: i64) -> Event {
        let now =  SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap();

        let uuid_bytes = self.uuid.as_bytes();
        let len = uuid_bytes.len();

        let uuid_datapoint =  Uuid::new_v1(Timestamp::from_unix(&self.context, now.as_secs(), now.subsec_nanos()),
                                           <&[u8; 6]>::try_from(&uuid_bytes[len-6..]).unwrap());

        Event {
            volcano_name: self.volcano_name.clone(),
            uuid_sensor: self.uuid.as_bytes().to_vec(),
            uuid_datapoint: uuid_datapoint.as_bytes().to_vec(),
            x,
            y,
            z,
            data_timestamp: now.as_secs(),
        }
    }

    pub async fn run(&mut self) -> Result<(), Box<dyn std::error::Error>> {

        let request = tonic::Request::new(self.emit_event(0, 0, 0));

        let response = self.client.put_event(request).await?;

        println!("RESPONSE={:?}", response);

        Ok(())
    }

}
