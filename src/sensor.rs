mod grpc;

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

struct UnixTimestamp {
    nanos: u128,
    seconds: u64,
    subsec_nanos: u32,
}

impl UnixTimestamp {
    pub fn now() -> Self {
        Self::new(&SystemTime::now())
    }

    pub fn new(now: &SystemTime) -> Self {
        match now.duration_since(SystemTime::UNIX_EPOCH) {
            Ok(n) => Self {
                nanos: n.as_nanos(),
                seconds: n.as_secs(),
                subsec_nanos: n.subsec_nanos(),
            },
            Err(_) => panic!("Not a valid unix time")
        }
    }

    pub fn timestamp(&self, context: &Context) -> Timestamp {
        Timestamp::from_unix(context, self.seconds, self.subsec_nanos)
    }
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

    pub async fn run(&mut self) -> Result<(), Box<dyn std::error::Error>> {
        let now = UnixTimestamp::now();

        let uuid_bytes = self.uuid.as_bytes();
        let len = uuid_bytes.len();

        let uuid_datapoint =  Uuid::new_v1(now.timestamp(&self.context),
                                           <&[u8; 6]>::try_from(&uuid_bytes[len-6..]).unwrap());
        let request = tonic::Request::new(Event {
            volcano_name: self.volcano_name.clone(),
            uuid_sensor: self.uuid.as_bytes().to_vec(),
            uuid_datapoint: uuid_datapoint.as_bytes().to_vec(),
            x: 0,
            y: 0,
            z: 0,
            data_timestamp: now.seconds,
        });

        let response = self.client.put_event(request).await?;

        println!("RESPONSE={:?}", response);

        Ok(())
    }

}
