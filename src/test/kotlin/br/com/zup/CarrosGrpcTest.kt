package br.com.zup

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
class CarrosGrpcTest(
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub,
    val repository: CarrosRepository
) {


    @Test
    fun `deve cadastrar carro`() {

        repository.deleteAll()

        val response = grpcClient.adicionar(
            CarrosRequest.newBuilder()
                .setModelo("Gol")
                .setPlaca("HPX-1234")
                .build()
        )

        with(response) {
            assertNotNull(this.id)
            assertTrue(repository.existsById(this.id))
        }

    }

    @Test
    fun `nao deve cadastrar carro quando carro com placa já existente`() {

        //cenario
        repository.deleteAll()

        val carroExistente = repository.save(
            Carro(modelo = "Gol", placa = "HPX-1234")
        )

        //acao

        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarrosRequest.newBuilder()
                    .setModelo("Gol")
                    .setPlaca(carroExistente.placa)
                    .build()
            )
        }


        //validcao

        with(erro) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("Carro com placa existente", this.status.description)
        }

    }

    @Test
    fun `nao deve cadastrar carro pois entrada com dados invalidos`() {

        //cenario
        repository.deleteAll()

        //acao

        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarrosRequest.newBuilder()
                    .setModelo("")
                    .setPlaca("")
                    .build()
            )
        }


        //validcao

        with(erro) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Dados invalidos", this.status.description)
        }

    }


    @Factory
    class Clients {
        @Singleton
        fun blockStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub {
            return CarrosGrpcServiceGrpc.newBlockingStub((channel))

        }
    }
}

