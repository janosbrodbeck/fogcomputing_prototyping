extern crate grpc;


use futures_util::FutureExt;
use prototyping::sensor::Sensor;
use prototyping::server::SensorServer;
use tokio::sync::oneshot;
use tonic::transport::Server;
use grpc::sensor_client::SensorClient as GrpcSensorClient;
use grpc::sensor_server::SensorServer as GrpcSensorServer;

#[tokio::test]
async fn test_emit_event() {
    let addr = "[::1]:50052".parse().unwrap();
    let svc = SensorServer::default();

    let (tx, rx) = oneshot::channel::<()>();

    let s = tokio::spawn(async move {
        Server::builder()
            .add_service(GrpcSensorServer::new(svc))
            .serve_with_shutdown(addr, rx.map(drop))
            .await
            .unwrap()
    });

    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let client = GrpcSensorClient::connect("http://[::1]:50052").await.unwrap();
    let volcano_name = String::from("Test-Tonic");


    let mut sensor = Sensor::new(client, volcano_name);
    sensor.run().await;

    tx.send(()).unwrap();
    s.await.unwrap();
}
