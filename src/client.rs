use std::error::Error;
use std::env;
use grpc::sensor_client::{SensorClient as GrpcSensorClient};
use grpc::{Event};

pub mod grpc {
    tonic::include_proto!("prototyping");
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let args: Vec<String> = env::args().collect();
    let csv_path: String;
    match args.len() {
        2 => {
            csv_path = args[1].parse().unwrap();
            println!("Given path: {:?}", csv_path);
        }
        _ => {
            help();
            panic!("No argument given")
        }
    }
    run(csv_path).await.expect("TODO: panic message");
    Ok(())
}

async fn run(csv_path: String) -> Result<(), Box<dyn Error>> {
    let mut csv_reader = csv::ReaderBuilder::new()
        .comment(Some(b'#'))
        .from_path(csv_path).unwrap();
    let mut client = GrpcSensorClient::connect("http://[::1]:50051").await?;

    for record in csv_reader.records() {
        let row = record?;
        println!("{:?}", row);
    }

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

fn help() -> () {
    println!("Please provide a path to a CSV as client data");
}
