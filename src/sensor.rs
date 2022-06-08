extern crate grpc;

use grpc::sensor_client::SensorClient as GrpcSensorClient;
use grpc::Event;


#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let client = GrpcSensorClient::connect("http://[::1]:50051").await?;
    run(client).await?;
    Ok(())
}


async fn run(mut client:  GrpcSensorClient<tonic::transport::Channel>) -> Result<(), Box<dyn std::error::Error>> {
    let request = tonic::Request::new(Event {
        volcano_name: "Tonic".into(),
        uuid_sensor: "asdf".into(),
        uuid_datapoint: "fdsa".into(),
        x: 0,
        y: 0,
        z: 0,
        data_timestamp: 0,
    });

    let response = client.put_event(request).await?;

    println!("RESPONSE={:?}", response);

    Ok(())
}
