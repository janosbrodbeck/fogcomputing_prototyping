fn main() {
    tonic_build::configure()
        .disable_package_emission()
        .out_dir("../grpc/src")  // you can change the generated code's location
        .compile(&["proto/sensor.proto"], &["proto"])
        .unwrap();
}
