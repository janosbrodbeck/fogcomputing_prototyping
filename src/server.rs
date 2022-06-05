use tonic::{transport::Server, Request, Response, Status};

use grpc::sensor_server::{Sensor, SensorServer as GrpcSensorServer};
use grpc::{Event, EventResponse};

pub mod grpc {
    tonic::include_proto!("prototyping");
}

#[derive(Debug, Default)]
pub struct SensorServer {}

#[tonic::async_trait]
impl Sensor for SensorServer {
    async fn put_event(&self, request: Request<Event>) -> Result<Response<EventResponse>, Status> {
        println!("Got an event: {:?}", request);

        let response = EventResponse {
            status: String::from("OK")
        };

        Ok(Response::new(response))
    }
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let addr = "[::1]:50051".parse()?;
    let sensor_server = SensorServer::default();

    Server::builder()
        .add_service(GrpcSensorServer::new(sensor_server))
        .serve(addr)
        .await?;
    Ok(())
}
