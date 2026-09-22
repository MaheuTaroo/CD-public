package com.example;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import com.example.grpc.HelloResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("34.51.149.246", 50051)
                .usePlaintext()
                .build();

        try {
            GreetingServiceGrpc.GreetingServiceBlockingStub stub =
                    GreetingServiceGrpc.newBlockingStub(channel);

            HelloRequest request = HelloRequest.newBuilder()
                    .setName("John")
                    .setRepetitions(3)
                    .build();

            HelloResponse response = stub.sayHello(request);

            System.out.println("Server response: " + response.getMessage());
        } finally {
            channel.shutdown();
        }
    }
}
