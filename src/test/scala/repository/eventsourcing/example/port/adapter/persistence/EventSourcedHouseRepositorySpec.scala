package repository.eventsourcing.example.port.adapter.persistence

import eventstore.impl.InMemoryEventStore
import org.scalatest._
import repository.eventsourcing.example.domain.{House, HouseRepository}

class EventSourcedHouseRepositorySpec extends FlatSpec with Matchers {
  val awesomeHouse = new House("100500 Awesome str., Chicago, USA", 100500, "Alexey Balchunas")

  "get" should "return previously put object" in {
    successHouseScenario(new EventSourcedHouseRepository(new InMemoryEventStore))
  }
  
  "conflict" should "be handled properly" in {
    failedHouseScenario(new EventSourcedHouseRepository(new InMemoryEventStore))
  }

  private def successHouseScenario(houseRepository: HouseRepository): Unit = {
    var house = awesomeHouse
    houseRepository.save(house)
    houseRepository.size should be(1)
    houseRepository.get(house.address).get should be(house)

    house = house.buy("Stepan Stepanov")
    houseRepository.save(house)
    houseRepository.get(house.address).get.owner should be("Stepan Stepanov")

    house = house.destroy()
    houseRepository.save(house)
    houseRepository.get(house.address).get.destroyed should be(right = true)
    houseRepository.get(house.address).get.owner should be("Stepan Stepanov")
  }
  
  private def failedHouseScenario(houseRepository: HouseRepository): Unit = {
    houseRepository.save(awesomeHouse.destroy())
    a [IllegalStateException] shouldBe thrownBy {
      houseRepository.save(awesomeHouse.buy("Stepan Stepanov"))
    }
  }


}
