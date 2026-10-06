package grpcclientapp;

import io.grpc.stub.StreamObserver;
import servicestubs.IntNumber;

public class PrimeNumbersStream implements StreamObserver<IntNumber> {
    boolean completed = false;
    int start, end;

    PrimeNumbersStream(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void onNext(IntNumber reply) {
        System.out.printf("More one prime number in [%d, %d]: %d\n", start, end, reply.getIntNumber());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.printf("Completed with error: %s\n", throwable.getMessage());
        completed = true;
    }

    @Override
    public void onCompleted() {
        System.out.printf("Prime numbers inside [%d, %d] completed\n", start, end);
        completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }
}
