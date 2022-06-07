fn main() {
    tonic_build::configure()
        //.build_server(false)
        //.out_dir("src/generated")  // you can change the generated code's location
        .compile(&["proto/sensor.proto"], &["proto"])
        .unwrap();
}
