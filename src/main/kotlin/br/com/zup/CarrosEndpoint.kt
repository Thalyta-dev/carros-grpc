package br.com.zup

import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarrosRepository): CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase() {
    override fun adicionar(request: CarrosRequest?, responseObserver: StreamObserver<CarrosResponse>) {

        if(repository.existsByPlaca(request?.placa)) return responseObserver.onError(Status.ALREADY_EXISTS
            .withDescription("Carro com placa existente")
            .asRuntimeException())


        val carro = request?.toModel()
        try{
            val carro = repository.save(carro)
        }catch (e: ConstraintViolationException){

          return  responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Dados invalidos")
                .asRuntimeException())
        }

        responseObserver.onNext(carro!!.id?.let { CarrosResponse.newBuilder().setId(it).build() })
        responseObserver.onCompleted()

    }

}

fun CarrosRequest.toModel(): Carro {
    return Carro(
        placa = this.placa,
        modelo = this.modelo
    )
}

