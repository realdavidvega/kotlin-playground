package functional

import arrow.core.raise.nullable

/** (3) Arrow's nullable */
object Nullable {

  data class UserDTO(val name: String?, val age: Int?, val email: String?)

  data class User(val name: String, val age: Int, val email: String)

  // painful
  private fun UserDTO.toUser(): User? =
    name?.let { age?.let { email?.let { User(name, age, email) } } }

  // nullable to the rescue
  private fun UserDTO.toUserNullable(): User? = nullable {
    User(name.bind(), age.bind(), email.bind())
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val dto2 = UserDTO("shadow", 42, "shadow@mail.com")
    val user2 = dto2.toUser()
    println(user2)

    val nullDTO2 = UserDTO("shadow", null, "shadow@mail.com")
    val nullUser2 = nullDTO2.toUser()
    println(nullUser2)

    val dto = UserDTO("shadow", 42, "shadow@mail.com")
    val user = dto.toUserNullable()
    println(user)

    val nullDTO = UserDTO("shadow", null, "shadow@mail.com")
    val nullUser = nullDTO.toUserNullable()
    println(nullUser)
  }
}
