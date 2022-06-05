use grpc::sensor_client::{SensorClient as GrpcSensorClient};
use grpc::{Event};

pub mod grpc {
    tonic::include_proto!("prototyping");
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let mut client = GrpcSensorClient::connect("http://[::1]:50051").await?;

    let request = tonic::Request::new(Event {
        volcano_name: String::from("Test"),
        sensor_id: 3,
        datapoint_id: 42,
        x:  100,
        y: 100,
        z: 100,
        data_timestamp: 123123123
    });

    let response = client.put_event(request).await?;
    println!("RESPONSE={:?}", response);

    Ok(())
}
