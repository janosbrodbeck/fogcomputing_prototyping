extern crate grpc;

use tonic::{transport::Server, Request, Response, Status};

use grpc::sensor_server::{Sensor, SensorServer as GrpcSensorServer};
use grpc::{Event, EventResponse};


#[derive(Debug, Default)]
pub struct SensorServer {}

#[tonic::async_trait]
impl Sensor for SensorServer {
    async fn put_event(&self, request: Request<Event>) -> Result<Response<EventResponse>, Status> {
        println!("received an event: {:?}", request.get_ref().uuid_datapoint);
        let reply = EventResponse {
            status: String::from("OK"),
        };

        Ok(Response::new(reply))
    }
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let addr = "[::1]:50051".parse()?;
    let server = SensorServer::default();

    Server::builder()
        .add_service(GrpcSensorServer::new(server))
        .serve(addr)
        .await?;

    Ok(())
}
