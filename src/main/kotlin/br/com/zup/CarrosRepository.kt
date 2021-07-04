package br.com.zup

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.repository.CrudRepository

@Repository
interface CarrosRepository: JpaRepository<Carro, Long> {

     fun existsByPlaca(placa: String?): Boolean
}