fn main() {
    tonic_build::configure()
        //.disable_package_emission() // this somehow breaks at client runtime
        .out_dir("../grpc/src") // you can change the generated code's location
        .compile(&["proto/sensor.proto"], &["proto"])
        .unwrap();
}
